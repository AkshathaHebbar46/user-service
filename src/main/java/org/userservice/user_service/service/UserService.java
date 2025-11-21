package org.userservice.user_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.userservice.user_service.entity.UserEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.userservice.user_service.dto.request.login.AuthRequestDTO;
import org.userservice.user_service.dto.request.register.RegisterRequestDTO;
import org.userservice.user_service.dto.request.user.UserPatchRequestDTO;
import org.userservice.user_service.dto.request.user.UserRequestDTO;
import org.userservice.user_service.dto.request.user.UserUpdateRequestDTO;
import org.userservice.user_service.dto.response.auth.AuthResponseDTO;
import org.userservice.user_service.dto.response.user.UserResponseDTO;
import org.userservice.user_service.entity.Role;
import org.userservice.user_service.mapper.UserMapper;
import org.userservice.user_service.properties.WalletServiceProperties;
import org.userservice.user_service.repository.UserRepository;
import org.userservice.user_service.service.jwt.JwtService;
import org.userservice.user_service.spec.UserSpecifications;

import java.util.ArrayList;
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
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       UserMapper userMapper,
                       WebClient webClient,
                       WalletServiceProperties walletProperties,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.webClient = webClient;
        this.walletProperties = walletProperties;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;

        logger.info("UserService initialized");
    }

    // ---------------------------------------------------------------------------
    // CREATE USER (admin use or system)
    // ---------------------------------------------------------------------------
    @Transactional
    public UserResponseDTO createUser(UserRequestDTO request) {
        logger.info("Creating user with username={}", request.username());

        UserEntity entity = userMapper.toEntity(request);
        entity.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(entity);

        logger.info("User created successfully with id={}", entity.getId());
        return userMapper.toDTO(entity);
    }

    // ---------------------------------------------------------------------------
    // GET ALL USERS
    // ---------------------------------------------------------------------------
    public List<UserResponseDTO> getAllUsers() {
        logger.info("Fetching all users");
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDTO)
                .toList();
    }

    // ---------------------------------------------------------------------------
    // GET USER BY ID
    // ---------------------------------------------------------------------------
    public UserResponseDTO getUserById(Long userId) {
        logger.info("Fetching user by id={}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with id={}", userId);
                    return new IllegalArgumentException("User not found");
                });

        return userMapper.toDTO(user);
    }

    // ---------------------------------------------------------------------------
    // PATCH UPDATE USER
    // ---------------------------------------------------------------------------
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

    // ---------------------------------------------------------------------------
    // ADMIN UPDATE USER
    // ---------------------------------------------------------------------------
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

    // ---------------------------------------------------------------------------
    // DELETE USER BY ADMIN + CASCADE DELETE WALLETS
    // ---------------------------------------------------------------------------
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
                    .bodyValue(Map.of("userId", userId))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            logger.info("Requested wallet-service to delete wallets for userId={}", userId);
        } catch (Exception ex) {
            logger.error("Wallet cascade delete failed: {}", ex.getMessage());
        }
    }

    // ---------------------------------------------------------------------------
    // REGISTER USER (PUBLIC)
    // ---------------------------------------------------------------------------
    public void registerUser(RegisterRequestDTO request) {

        logger.info("Register attempt for email={}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Registration failed: email {} already exists", request.getEmail());
            throw new IllegalArgumentException("Email already registered");
        }

        UserEntity user = new UserEntity();
        user.setUsername(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setAge(request.getAge());
        user.setRole(Role.USER);

        userRepository.save(user);

        logger.info("User registered successfully: id={}, email={}", user.getId(), user.getEmail());
    }

    // ---------------------------------------------------------------------------
    // LOGIN
    // ---------------------------------------------------------------------------

    public AuthResponseDTO login(AuthRequestDTO request) {

        // 1. Find user first
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 2. Check if active BEFORE authentication
        if (Boolean.FALSE.equals(user.getActive())) {
            throw new IllegalStateException("User account is inactive or blacklisted.");
        }

        // 3. Authenticate password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 4. Generate JWT
        String token = jwtService.generateToken(
                user.getEmail(),
                user.getId(),
                user.getRole().name()
        );

        return new AuthResponseDTO(token, user.getRole().name(), user.getId());
    }

    // Get all users with pagination and filtering
    public Page<UserResponseDTO> getUsers(
            String username,
            String email,
            Boolean active,
            String role,
            int page,
            int size
    ) {

        List<Specification<UserEntity>> specs = new ArrayList<>();

        if (username != null && !username.isBlank()) specs.add(UserSpecifications.usernameContains(username));
        if (email != null && !email.isBlank()) specs.add(UserSpecifications.emailContains(email));
        if (active != null) specs.add(UserSpecifications.statusEquals(active));
        if (role != null && !role.isBlank()) specs.add(UserSpecifications.roleEquals(role));

        Specification<UserEntity> finalSpec = specs.isEmpty() ? null : Specification.allOf(specs);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<UserEntity> pageResult = userRepository.findAll(finalSpec, pageable);

        return pageResult.map(user -> new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAge(),
                user.getCreatedAt()
        ));
    }
}
