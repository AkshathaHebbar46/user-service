package org.userservice.user_service.service.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;
import org.userservice.user_service.dto.request.user.UserRequestDTO;
import org.userservice.user_service.dto.request.user.UserUpdateRequestDTO;
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

    @Mock
    private WebClient webClient;

    @Mock
    private WalletServiceProperties walletProperties;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ------------------ CREATE USER ------------------
    @Test
    void createUser_shouldReturnUserResponseDTO() {
        UserRequestDTO request = new UserRequestDTO("john", "john@example.com",  "pass123", 25);
        UserEntity entity = new UserEntity();
        entity.setId(1L);

        when(userMapper.toEntity(request)).thenReturn(entity);
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPass");
        when(userMapper.toDTO(entity)).thenReturn(
                new UserResponseDTO(1L, "john", "john@example.com", 25, LocalDateTime.now())
        );

        UserResponseDTO response = userService.createUser(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        verify(userRepository, times(1)).save(entity);
    }

    // ------------------ GET USER ------------------
    @Test
    void getUserById_shouldReturnUser_whenFound() {
        UserEntity entity = new UserEntity();
        entity.setId(2L);
        entity.setUsername("alice");

        when(userRepository.findById(2L)).thenReturn(Optional.of(entity));
        when(userMapper.toDTO(entity)).thenReturn(
                new UserResponseDTO(2L, "alice", "alice@mail.com", 30, LocalDateTime.now())
        );

        UserResponseDTO response = userService.getUserById(2L);

        assertEquals(2L, response.id());
        assertEquals("alice", response.username());
    }

    @Test
    void getUserById_shouldThrow_whenNotFound() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.getUserById(5L));
        assertEquals("User not found", ex.getMessage());
    }

    // ------------------ UPDATE USER ------------------
    @Test
    void updateUser_shouldUpdateAndReturnDTO() {
        UserEntity entity = new UserEntity();
        entity.setId(1L);

        UserRequestDTO request = new UserRequestDTO("bob", "bob@mail.com", "secret", 28);

        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(userMapper.toDTO(entity)).thenReturn(
                new UserResponseDTO(1L, "bob", "bob@mail.com", 28, LocalDateTime.now())
        );

        UserResponseDTO response = userService.updateUser(1L, request);

        assertEquals("bob", response.username());
        assertEquals(28, response.age());
        verify(userRepository).save(entity);
    }

    @Test
    void updateUser_shouldThrow_whenUserNotFound() {
        UserRequestDTO request = new UserRequestDTO("bob", "bob@mail.com",  "secret", 28);
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(10L, request));

        assertEquals("User not found", ex.getMessage());
    }

    // ------------------ DELETE USER ------------------
    @Test
    void deleteUser_shouldCallRepository() {
        userService.deleteUser(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    // ------------------ ADMIN UPDATE ------------------
    @Test
    void updateUserByAdmin_shouldUpdatePartialFields() {
        UserEntity entity = new UserEntity();
        entity.setId(1L);
        entity.setUsername("old");
        entity.setEmail("old@mail.com");
        entity.setAge(20);

        UserUpdateRequestDTO dto = new UserUpdateRequestDTO("newName", null, 25);

        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDTO result = userService.updateUserByAdmin(1L, dto);

        assertEquals("newName", result.username());
        assertEquals(25, result.age());
        assertEquals("old@mail.com", result.email());
        verify(userRepository).save(entity);
    }


    @Test
    void updateUserByAdmin_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(9L)).thenReturn(Optional.empty());
        UserUpdateRequestDTO dto = new UserUpdateRequestDTO("x", "y@mail.com", 30);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.updateUserByAdmin(9L, dto));
        assertEquals("User not found", ex.getMessage());
    }

    // ------------------ ADMIN DELETE ------------------
    @Test
    void deleteUserByAdmin_shouldDeleteUser() {
        UserEntity entity = new UserEntity();
        when(userRepository.findById(3L)).thenReturn(Optional.of(entity));

        userService.deleteUserByAdmin(3L);

        verify(userRepository).delete(entity);
    }

    @Test
    void deleteUserByAdmin_shouldThrow_whenNotFound() {
        when(userRepository.findById(7L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.deleteUserByAdmin(7L));
        assertEquals("User not found", ex.getMessage());
    }
}
