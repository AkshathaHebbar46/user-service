package org.userservice.user_service.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.userservice.user_service.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new UserEntity();
        user.setId(1L);
        user.setUsername("JohnDoe");
        user.setEmail("john@example.com");
        user.setCreatedAt(LocalDateTime.now().minusDays(1));
    }

    @Test
    void testFindByEmail() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        Optional<UserEntity> foundUser = userRepository.findByEmail("john@example.com");

        assertTrue(foundUser.isPresent());
        assertEquals("JohnDoe", foundUser.get().getUsername());
        verify(userRepository, times(1)).findByEmail("john@example.com");
    }

    @Test
    void testExistsByEmail() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        boolean exists = userRepository.existsByEmail("john@example.com");

        assertTrue(exists);
        verify(userRepository, times(1)).existsByEmail("john@example.com");
    }

    @Test
    void testFindByUsernameContainingIgnoreCase() {
        UserEntity user2 = new UserEntity();
        user2.setUsername("Johnny");
        List<UserEntity> users = Arrays.asList(user, user2);

        when(userRepository.findByUsernameContainingIgnoreCase("john")).thenReturn(users);

        List<UserEntity> result = userRepository.findByUsernameContainingIgnoreCase("john");

        assertEquals(2, result.size());
        verify(userRepository, times(1)).findByUsernameContainingIgnoreCase("john");
    }

    @Test
    void testFindByCreatedAtAfter() {
        LocalDateTime date = LocalDateTime.now().minusDays(2);
        List<UserEntity> users = Arrays.asList(user);

        when(userRepository.findByCreatedAtAfter(date)).thenReturn(users);

        List<UserEntity> result = userRepository.findByCreatedAtAfter(date);

        assertEquals(1, result.size());
        verify(userRepository, times(1)).findByCreatedAtAfter(date);
    }
}
