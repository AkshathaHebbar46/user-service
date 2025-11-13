package org.userservice.user_service.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.userservice.user_service.entity.UserEntity;
import org.userservice.user_service.entity.Role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    private UserEntity user1;
    private UserEntity user2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user1 = new UserEntity();
        user1.setId(1L);
        user1.setUsername("Alice");
        user1.setEmail("alice@example.com");
        user1.setPassword("password1");
        user1.setRole(Role.USER);
        user1.setActive(true);
        user1.setCreatedAt(LocalDateTime.now().minusDays(2));

        user2 = new UserEntity();
        user2.setId(2L);
        user2.setUsername("Bob");
        user2.setEmail("bob@example.com");
        user2.setPassword("password2");
        user2.setRole(Role.ADMIN);
        user2.setActive(false);
        user2.setCreatedAt(LocalDateTime.now().minusDays(1));
    }

    @Test
    void testFindByEmail_ReturnsUser() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user1));
        Optional<UserEntity> result = userRepository.findByEmail("alice@example.com");
        assertTrue(result.isPresent());
        assertEquals("Alice", result.get().getUsername());
    }

    @Test
    void testFindByEmail_ReturnsEmpty() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        Optional<UserEntity> result = userRepository.findByEmail("unknown@example.com");
        assertTrue(result.isEmpty());
    }

    @Test
    void testExistsByEmail_ReturnsTrue() {
        when(userRepository.existsByEmail("bob@example.com")).thenReturn(true);
        assertTrue(userRepository.existsByEmail("bob@example.com"));
    }

    @Test
    void testExistsByEmail_ReturnsFalse() {
        when(userRepository.existsByEmail("unknown@example.com")).thenReturn(false);
        assertFalse(userRepository.existsByEmail("unknown@example.com"));
    }

    @Test
    void testFindByUsernameContainingIgnoreCase_ReturnsMatchingUsers() {
        when(userRepository.findByUsernameContainingIgnoreCase("ali")).thenReturn(List.of(user1));
        List<UserEntity> results = userRepository.findByUsernameContainingIgnoreCase("ali");
        assertEquals(1, results.size());
        assertEquals("Alice", results.get(0).getUsername());
    }

    @Test
    void testFindByUsernameContainingIgnoreCase_ReturnsEmpty() {
        when(userRepository.findByUsernameContainingIgnoreCase("xyz")).thenReturn(List.of());
        List<UserEntity> results = userRepository.findByUsernameContainingIgnoreCase("xyz");
        assertTrue(results.isEmpty());
    }

    @Test
    void testFindByCreatedAtAfter_ReturnsUsers() {
        LocalDateTime date = LocalDateTime.now().minusDays(2).minusHours(1);
        when(userRepository.findByCreatedAtAfter(date)).thenReturn(List.of(user1, user2));
        List<UserEntity> results = userRepository.findByCreatedAtAfter(date);
        assertEquals(2, results.size());
    }

    @Test
    void testFindByCreatedAtAfter_ReturnsEmpty() {
        LocalDateTime date = LocalDateTime.now().plusDays(1);
        when(userRepository.findByCreatedAtAfter(date)).thenReturn(List.of());
        List<UserEntity> results = userRepository.findByCreatedAtAfter(date);
        assertTrue(results.isEmpty());
    }

    @Test
    void testSaveUser_ReturnsSavedUser() {
        UserEntity newUser = new UserEntity();
        newUser.setUsername("Charlie");
        newUser.setEmail("charlie@example.com");
        when(userRepository.save(newUser)).thenReturn(newUser);
        UserEntity saved = userRepository.save(newUser);
        assertNotNull(saved);
        assertEquals("Charlie", saved.getUsername());
    }

    @Test
    void testDeleteById_VerifyCalled() {
        doNothing().when(userRepository).deleteById(1L);
        userRepository.deleteById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void testFindAll_ReturnsAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        List<UserEntity> allUsers = userRepository.findAll();
        assertEquals(2, allUsers.size());
    }
}
