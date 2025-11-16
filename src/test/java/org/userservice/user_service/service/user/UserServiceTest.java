package org.userservice.user_service.service.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;
import org.userservice.user_service.dto.request.user.UserPatchRequestDTO;
import org.userservice.user_service.dto.request.user.UserRequestDTO;
import org.userservice.user_service.dto.request.user.UserUpdateRequestDTO;
import org.userservice.user_service.dto.response.user.UserResponseDTO;
import org.userservice.user_service.entity.UserEntity;
import org.userservice.user_service.mapper.UserMapper;
import org.userservice.user_service.properties.WalletServiceProperties;
import org.userservice.user_service.repository.UserRepository;
import org.userservice.user_service.service.UserService;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

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

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ------------------- CREATE USER -------------------
    @Test
    void testCreateUser() {
        UserRequestDTO dto = new UserRequestDTO("john", "john@mail.com", "pass", 20);
        UserEntity entity = new UserEntity();
        entity.setId(1L);

        when(userMapper.toEntity(dto)).thenReturn(entity);
        when(passwordEncoder.encode("pass")).thenReturn("ENC_PASS");
        when(userRepository.save(entity)).thenReturn(entity);

        UserResponseDTO response = new UserResponseDTO(1L, "john", "john@mail.com", 20, LocalDateTime.now());
        when(userMapper.toDTO(entity)).thenReturn(response);

        UserResponseDTO result = userService.createUser(dto);

        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(userRepository).save(entity);
    }

    // ------------------- GET ALL USERS -------------------
    @Test
    void testGetAllUsers() {
        UserEntity e = new UserEntity();
        e.setId(1L);
        when(userRepository.findAll()).thenReturn(List.of(e));

        UserResponseDTO dto = new UserResponseDTO(1L, "A", "mail@mail.com", 30, LocalDateTime.now());
        when(userMapper.toDTO(e)).thenReturn(dto);

        List<UserResponseDTO> result = userService.getAllUsers();
        assertEquals(1, result.size());
        assertEquals("A", result.get(0).username());
    }

    // ------------------- GET USER BY ID -------------------
    @Test
    void testGetUserById() {
        UserEntity e = new UserEntity();
        e.setId(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(e));

        UserResponseDTO mapped = new UserResponseDTO(10L, "X", "x@mail.com", 22, LocalDateTime.now());
        when(userMapper.toDTO(e)).thenReturn(mapped);

        UserResponseDTO result = userService.getUserById(10L);
        assertEquals(10L, result.id());
        assertEquals("X", result.username());
    }

    // ------------------- PATCH UPDATE USER -------------------
    @Test
    void testPatchUpdateUser() {
        Long id = 5L;
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setUsername("oldName");
        user.setPassword("oldPass");
        user.setAge(25);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPass")).thenReturn("ENC_PASS");

        UserPatchRequestDTO dto = new UserPatchRequestDTO("newName", "newPass", 30);
        when(userRepository.save(user)).thenReturn(user);

        UserResponseDTO mapped = new UserResponseDTO(id, "newName", "mail@mail.com", 30, LocalDateTime.now());
        when(userMapper.toDTO(user)).thenReturn(mapped);

        UserResponseDTO res = userService.patchUpdateUser(id, dto);

        assertEquals("newName", res.username());
        assertEquals(30, res.age());
    }

    // ------------------- UPDATE USER BY ADMIN -------------------
    @Test
    void testUpdateUserByAdmin() {
        Long id = 3L;
        UserEntity user = new UserEntity();
        user.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        UserUpdateRequestDTO dto = new UserUpdateRequestDTO();
        dto.setName("ADMIN_UPDATE");
        dto.setEmail("admin@mail.com");
        dto.setAge(40);

        UserResponseDTO result = userService.updateUserByAdmin(id, dto);
        assertEquals("ADMIN_UPDATE", result.username());
        assertEquals("admin@mail.com", result.email());
        assertEquals(40, result.age());
    }

    // ------------------- CREATE USER NULL PASSWORD -------------------
    @Test
    void testCreateUserNullPassword() {
        UserRequestDTO dto = new UserRequestDTO("john", "john@mail.com", null, 20);
        UserEntity entity = new UserEntity();
        entity.setId(1L);

        when(userMapper.toEntity(dto)).thenReturn(entity);
        when(passwordEncoder.encode(null)).thenReturn(null);
        when(userRepository.save(entity)).thenReturn(entity);

        UserResponseDTO response = new UserResponseDTO(1L, "john", "john@mail.com", 20, LocalDateTime.now());
        when(userMapper.toDTO(entity)).thenReturn(response);

        UserResponseDTO result = userService.createUser(dto);
        assertNotNull(result);
    }

    // ------------------- PATCH UPDATE USER NULL FIELDS -------------------
    @Test
    void testPatchUpdateUserNullFields() {
        Long id = 5L;
        UserEntity user = new UserEntity();
        user.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserPatchRequestDTO dto = new UserPatchRequestDTO(null, null, null);
        UserResponseDTO mapped = new UserResponseDTO(id, "name", "mail@mail.com", 25, LocalDateTime.now());
        when(userMapper.toDTO(user)).thenReturn(mapped);

        UserResponseDTO res = userService.patchUpdateUser(id, dto);
        assertNotNull(res);
    }

    // ------------------- GET USER NOT FOUND -------------------
    @Test
    void testGetUserByIdNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.getUserById(99L));
    }

    // ------------------- DELETE USER NOT FOUND -------------------
    @Test
    void testDeleteUserByAdminNotFound() {
        Long id = 99L;
        String token = "JWT";
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.deleteUserByAdmin(id, token));
    }

}
