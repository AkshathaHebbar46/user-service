package org.userservice.user_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.userservice.user_service.properties.WalletServiceProperties;
import org.userservice.user_service.dto.request.user.UserRequestDTO;
import org.userservice.user_service.dto.response.user.UserResponseDTO;
import org.userservice.user_service.dto.response.wallet.WalletResponseDTO;
import org.userservice.user_service.entity.UserEntity;
import org.userservice.user_service.mapper.UserMapper;
import org.userservice.user_service.repository.UserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final WebClient webClient;
    private final WalletServiceProperties walletProperties;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       UserMapper userMapper,
                       WebClient webClient,
                       WalletServiceProperties walletProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.webClient = webClient;
        this.walletProperties = walletProperties;
    }

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO request) {
        // 1️⃣ Save user
        UserEntity entity = userMapper.toEntity(request);
        entity.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(entity);
        logger.info("User created successfully with id={}", entity.getId());
        // 3️⃣ Return user DTO
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
        String url = walletProperties.getBaseUrl() + "/user/" + userId;

        WalletResponseDTO[] wallets = webClient.get()
                .uri(url)
                .header("Authorization", authHeader)
                .retrieve()
                .bodyToMono(WalletResponseDTO[].class)
                .block(); // synchronous call

        return Arrays.asList(wallets);
    }
}
