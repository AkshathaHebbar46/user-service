package org.userservice.user_service.service.blacklist;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;
import org.userservice.user_service.entity.UserEntity;
import org.userservice.user_service.properties.WalletServiceProperties;
import org.userservice.user_service.repository.UserRepository;

import java.util.Map;

@Service
public class BlacklistedUserService {

    private static final Logger log = LoggerFactory.getLogger(BlacklistedUserService.class);

    private final UserRepository userRepository;
    private final WebClient walletWebClient;

    public BlacklistedUserService(UserRepository userRepository,
                                  WebClient.Builder webClientBuilder,
                                  WalletServiceProperties walletServiceProperties) {
        this.userRepository = userRepository;
        this.walletWebClient = webClientBuilder
                .baseUrl(walletServiceProperties.getAdminUrl() + "/blacklist")
                .build();
    }

    @Transactional
    public void blacklistUser(Long userId) {
        log.info("Attempting to blacklist user with id={}", userId);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.getActive()) {
            log.info("User id={} is already inactive. Skipping blacklist.", userId);
            return;
        }

        user.setActive(false);
        userRepository.save(user);

        String token = extractCurrentToken();

        walletWebClient.post()
                .uri("")
                .header("Authorization", "Bearer " + token)
                .bodyValue(Map.of("userId", userId))
                .retrieve()
                .bodyToMono(Void.class)
                .block();

        log.info("Wallets of user id={} blacklisted successfully via wallet service.", userId);
    }

    @Transactional
    public void unblockUser(Long userId) {
        log.info("Attempting to unblock user with id={}", userId);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getActive()) {
            log.info("User id={} is already active. Skipping unblock.", userId);
            return;
        }

        user.setActive(true);
        userRepository.save(user);

        String token = extractCurrentToken();

        walletWebClient.post()
                .uri("/unblock")
                .header("Authorization", "Bearer " + token)
                .bodyValue(Map.of("userId", userId))
                .retrieve()
                .bodyToMono(Void.class)
                .block();

        log.info("Wallets of user id={} unblocked successfully via wallet service.", userId);
    }

    private String extractCurrentToken() {
        var attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return null;

        HttpServletRequest request = attributes.getRequest();
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        return null;
    }
}
