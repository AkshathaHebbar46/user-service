package org.userservice.user_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.userservice.user_service.dto.request.UserRequestDTO;
import org.userservice.user_service.dto.response.UserResponseDTO;
import org.userservice.user_service.entity.UserEntity;
import org.userservice.user_service.exception.WalletServiceException;
import org.userservice.user_service.mapper.UserMapper;
import org.userservice.user_service.repository.UserRepository;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RestClient restClient;

    @Value("${wallet.service.url}")
    private String walletServiceUrl;

    public UserService(UserRepository userRepository, RestClient.Builder restClientBuilder) {
        this.userRepository = userRepository;
        this.restClient = restClientBuilder.build();
    }

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO request) {
        UserEntity entity = UserMapper.INSTANCE.toEntity(request);
        userRepository.save(entity);

        restClient.post()
                .uri(walletServiceUrl + "/wallets")
                .body(Map.of("userId", entity.getId()))
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(),
                        (req, res) -> { throw new WalletServiceException("Failed to create wallet for userId=" + entity.getId()); })
                .toBodilessEntity();

        return UserMapper.INSTANCE.toDTO(entity);
    }


    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper.INSTANCE::toDTO)
                .toList();
    }

    public UserResponseDTO getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(UserMapper.INSTANCE::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
    }

    public UserResponseDTO updateUser(Long userId, UserRequestDTO request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setAge(request.age());
        userRepository.save(user);
        return UserMapper.INSTANCE.toDTO(user);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
