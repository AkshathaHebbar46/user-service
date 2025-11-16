package org.userservice.user_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.userservice.user_service.dto.request.user.UserPatchRequestDTO;
import org.userservice.user_service.dto.request.user.UserUpdateRequestDTO;
import org.userservice.user_service.properties.WalletServiceProperties;
import org.userservice.user_service.dto.request.user.UserRequestDTO;
import org.userservice.user_service.dto.response.user.UserResponseDTO;
import org.userservice.user_service.entity.UserEntity;
import org.userservice.user_service.mapper.UserMapper;
import org.userservice.user_service.repository.UserRepository;

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
        logger.info("UserService initialized");
    }

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO request) {
        logger.info("Creating user with username={}", request.username());
        UserEntity entity = userMapper.toEntity(request);
        entity.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(entity);
        logger.info("User created successfully with id={}", entity.getId());
        return userMapper.toDTO(entity);
    }

    public List<UserResponseDTO> getAllUsers() {
        logger.info("Fetching all users");
        return userRepository.findAll().stream()
                .map(userMapper::toDTO)
                .toList();
    }

    public UserResponseDTO getUserById(Long userId) {
        logger.info("Fetching user by id={}", userId);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with id={}", userId);
                    return new IllegalArgumentException("User not found");
                });
        return userMapper.toDTO(user);
    }

    @Transactional
    public UserResponseDTO patchUpdateUser(Long userId, UserPatchRequestDTO dto) {
        logger.info("Patching user with id={}", userId);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with id={}", userId);
                    return new IllegalArgumentException("User not found");
                });
        if (dto.username() != null && !dto.username().isBlank()) {
            logger.debug("Updating username to {}", dto.username());
            user.setUsername(dto.username());
        }
        if (dto.password() != null && !dto.password().isBlank()) {
            logger.debug("Updating password for user id={}", userId);
            user.setPassword(passwordEncoder.encode(dto.password()));
        }
        if (dto.age() != null) {
            logger.debug("Updating age to {}", dto.age());
            user.setAge(dto.age());
        }
        userRepository.save(user);
        logger.info("User patched successfully with id={}", userId);
        return userMapper.toDTO(user);
    }

    public UserResponseDTO updateUserByAdmin(Long userId, UserUpdateRequestDTO dto) {
        logger.info("Admin updating user with id={}", userId);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with id={}", userId);
                    return new IllegalArgumentException("User not found");
                });

        if (dto.getName() != null) {
            logger.debug("Updating username to {}", dto.getName());
            user.setUsername(dto.getName());
        }
        if (dto.getEmail() != null) {
            logger.debug("Updating email to {}", dto.getEmail());
            user.setEmail(dto.getEmail());
        }
        if (dto.getAge() != null) {
            logger.debug("Updating age to {}", dto.getAge());
            user.setAge(dto.getAge());
        }

        UserEntity updated = userRepository.save(user);
        logger.info("Admin updated user successfully with id={}", userId);

        return new UserResponseDTO(
                updated.getId(),
                updated.getUsername(),
                updated.getEmail(),
                updated.getAge(),
                updated.getCreatedAt()
        );
    }

    @Transactional
    public void deleteUserByAdmin(Long userId, String token) {

        logger.warn("Admin deleting user with id={}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with id={}", userId);
                    return new IllegalArgumentException("User not found");
                });

        userRepository.delete(user);
        logger.warn("Admin deleted user successfully with id={}", userId);

        try {
            webClient.method(HttpMethod.DELETE)
                    .uri(walletProperties.getAdminUrl())
                    .header("Authorization", "Bearer " + token)
                    .bodyValue(Map.of("userId", userId))  // now valid
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            logger.info("Requested wallet-service to delete wallets for userId={}", userId);
        }
        catch (Exception ex) {
            logger.error("Wallet cascade delete failed: {}", ex.getMessage());
        }
    }


}
