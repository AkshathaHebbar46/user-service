package org.userservice.user_service.service.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.userservice.user_service.dto.request.user.UserRequestDTO;
import org.userservice.user_service.dto.response.user.UserResponseDTO;
import org.userservice.user_service.dto.response.wallet.WalletResponseDTO;
import org.userservice.user_service.entity.UserEntity;
import org.userservice.user_service.mapper.UserMapper;
import org.userservice.user_service.repository.UserRepository;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final RestClient restClient; // Modern RestClient

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       UserMapper userMapper,
                       RestClient restClient) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.restClient = restClient;
    }

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO request) {
        // 1️⃣ Save user
        UserEntity entity = userMapper.toEntity(request);
        entity.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(entity);
        logger.info("User created successfully with id={}", entity.getId());

        // 2️⃣ Call Wallet Service
        try {
            String walletServiceUrl = "http://localhost:8082/wallets";
            Map<String, Object> walletRequest = Map.of(
                    "userId", entity.getId(),
                    "username", entity.getUsername()
            );

            restClient.post()
                    .uri(walletServiceUrl)
                    .body(walletRequest)
                    .retrieve()
                    .toBodilessEntity();

            logger.info("Wallet created for user id={}", entity.getId());
        } catch (Exception e) {
            logger.error("Failed to create wallet for user id={}: {}", entity.getId(), e.getMessage());
        }

        return userMapper.toDTO(entity);
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDTO)
                .toList();
    }

    public UserResponseDTO getUserById(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return userMapper.toDTO(user);
    }

    @Transactional
    public UserResponseDTO updateUser(Long userId, UserRequestDTO request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setAge(request.age());
        userRepository.save(user);
        logger.info("User updated successfully with id={}", userId);

        return userMapper.toDTO(user);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
        logger.info("User deleted successfully with id={}", userId);
    }

    public List<WalletResponseDTO> getUserWallets(String authHeader, Long userId) {
        String url = "http://localhost:8082/wallets/user/" + userId;

        // Synchronous call using RestClient
        WalletResponseDTO[] wallets = restClient.get()
                .uri(url)
                .header("Authorization", authHeader) // pass JWT token
                .retrieve()
                .body(WalletResponseDTO[].class);  // <-- correct method

        return Arrays.asList(wallets);
    }


}
