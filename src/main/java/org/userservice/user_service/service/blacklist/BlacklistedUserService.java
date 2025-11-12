package org.userservice.user_service.service.blacklist;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;
import org.userservice.user_service.entity.UserEntity;
import org.userservice.user_service.repository.UserRepository;

import java.util.Map;

@Service
public class BlacklistedUserService {

    private static final Logger log = LoggerFactory.getLogger(BlacklistedUserService.class);

    private final UserRepository userRepository;
    private final WebClient walletWebClient;

    public BlacklistedUserService(UserRepository userRepository, WebClient.Builder webClientBuilder) {
        this.userRepository = userRepository;
        this.walletWebClient = webClientBuilder
                .baseUrl("http://localhost:8082/admin/wallets/blacklist")
                .build();
    }

    @Transactional
    public void blacklistUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!user.getActive()) return;

        user.setActive(false);
        userRepository.save(user);

        String token = extractCurrentToken();

        // ✅ Proper wallet-service call
        walletWebClient.post()
                .uri("")
                .header("Authorization", "Bearer " + token)
                .bodyValue(Map.of("userId", userId)) // important
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }


    @Transactional
    public void unblockUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getActive()) return;

        user.setActive(true);
        userRepository.save(user);

        String token = extractCurrentToken();

        // Call wallet-service to unblock all wallets of this user
        walletWebClient.post()
                .uri("/unblock")
                .header("Authorization", "Bearer " + token)
                .bodyValue(Map.of("userId", userId))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }


    private String extractCurrentToken() {
        var attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.warn("⚠️ No current request context — cannot extract token.");
            return null;
        }

        HttpServletRequest request = attributes.getRequest();
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        log.warn("⚠️ Missing Authorization header in current request.");
        return null;
    }
}
