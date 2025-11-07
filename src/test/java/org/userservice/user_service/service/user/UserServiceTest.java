package org.userservice.user_service.service.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClient;
import org.userservice.user_service.dto.request.user.UserRequestDTO;
import org.userservice.user_service.dto.response.user.UserResponseDTO;
import org.userservice.user_service.entity.UserEntity;
import org.userservice.user_service.mapper.UserMapper;
import org.userservice.user_service.properties.WalletServiceProperties;
import org.userservice.user_service.repository.UserRepository;
import org.userservice.user_service.service.UserService;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient restClient;
    @Mock
    private WalletServiceProperties walletProperties;

    @InjectMocks
    private UserService userService;

    private UserEntity userEntity;
    private UserRequestDTO userRequest;
    private UserResponseDTO userResponse;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("Akshatha");
        userEntity.setEmail("akshatha@example.com");
        userEntity.setPassword("encoded");
        userEntity.setAge(25);
        userEntity.setCreatedAt(LocalDateTime.now());

        userRequest = new UserRequestDTO("Akshatha", "akshatha@example.com", "password123", 25);
        userResponse = new UserResponseDTO(1L, "Akshatha", "akshatha@example.com", 25, LocalDateTime.now());
    }

    // ✅ CREATE USER - SUCCESS
    @Test
    void testCreateUser_Success() {
        when(userMapper.toEntity(userRequest)).thenReturn(userEntity);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(walletProperties.getBaseUrl()).thenReturn("http://wallet-service/wallets");
        when(userMapper.toDTO(userEntity)).thenReturn(userResponse);
        when(userRepository.save(any())).thenReturn(userEntity);

        RestClient.RequestBodyUriSpec mockRequest = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec mockBody = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec mockResponse = mock(RestClient.ResponseSpec.class);

        when(restClient.post()).thenReturn(mockRequest);
        when(mockRequest.uri(anyString())).thenReturn(mockBody);
        when(mockBody.body(any(Map.class))).thenReturn(mockBody);
        when(mockBody.retrieve()).thenReturn(mockResponse);
        when(mockResponse.toBodilessEntity()).thenReturn(null);

        UserResponseDTO result = userService.createUser(userRequest);

        assertNotNull(result);
        assertEquals("Akshatha", result.username());
        verify(userRepository).save(userEntity);
        verify(restClient).post();
    }

    // ✅ CREATE USER - WALLET FAILURE (should still return user)
    @Test
    void testCreateUser_WalletFailure() {
        when(userMapper.toEntity(userRequest)).thenReturn(userEntity);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(walletProperties.getBaseUrl()).thenReturn("http://wallet-service/wallets");
        when(userMapper.toDTO(userEntity)).thenReturn(userResponse);
        when(userRepository.save(any())).thenReturn(userEntity);

        when(restClient.post()).thenThrow(new RuntimeException("Wallet service down"));

        UserResponseDTO result = userService.createUser(userRequest);

        assertNotNull(result);
        verify(userRepository).save(userEntity);
    }

    // ✅ GET ALL USERS
    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(userEntity));
        when(userMapper.toDTO(userEntity)).thenReturn(userResponse);

        List<UserResponseDTO> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("Akshatha", result.get(0).username());
    }

    // ✅ GET USER BY ID - SUCCESS
    @Test
    void testGetUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userMapper.toDTO(userEntity)).thenReturn(userResponse);

        UserResponseDTO result = userService.getUserById(1L);

        assertEquals("Akshatha", result.username());
    }

    // ❌ GET USER BY ID - NOT FOUND
    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.getUserById(1L));
    }

    // ✅ UPDATE USER - SUCCESS
    @Test
    void testUpdateUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any())).thenReturn(userEntity);
        when(userMapper.toDTO(userEntity)).thenReturn(userResponse);

        UserResponseDTO result = userService.updateUser(1L, userRequest);

        assertEquals("Akshatha", result.username());
        verify(userRepository).save(userEntity);
    }

    // ❌ UPDATE USER - NOT FOUND
    @Test
    void testUpdateUser_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(1L, userRequest));
    }

    // ✅ DELETE USER
    @Test
    void testDeleteUser() {
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }
}