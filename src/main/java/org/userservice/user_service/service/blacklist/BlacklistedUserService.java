package org.userservice.user_service.service.blacklist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.userservice.user_service.entity.UserEntity;
import org.userservice.user_service.repository.UserRepository;

@Service
public class BlacklistedUserService {

    private final UserRepository userRepository;
    private final WebClient walletWebClient;

    public BlacklistedUserService(UserRepository userRepository, WebClient.Builder webClientBuilder) {
        this.userRepository = userRepository;
        this.walletWebClient = webClientBuilder.baseUrl("http://localhost:8082/wallets/blacklist").build();
    }

    @Transactional
    public void blacklistUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!user.getActive()) return;

        user.setActive(false);
        userRepository.save(user);

        walletWebClient.post()
                .uri("/user/{userId}", userId)
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

        walletWebClient.post()
                .uri("/user/{userId}/unblock", userId)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    @Transactional
    public void blacklistWallet(Long walletId) {
        walletWebClient.post()
                .uri("/wallet/{walletId}", walletId)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    @Transactional
    public void unblockWallet(Long walletId) {
        walletWebClient.post()
                .uri("/wallet/{walletId}/unblock", walletId)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    @Transactional(readOnly = true)
    public boolean isUserBlacklisted(Long userId) {
        return userRepository.findById(userId)
                .map(user -> !Boolean.TRUE.equals(user.getActive()))
                .orElse(false);
    }
}
