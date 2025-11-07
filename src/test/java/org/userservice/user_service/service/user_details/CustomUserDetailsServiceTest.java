package org.userservice.user_service.service.user_details;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.userservice.user_service.entity.Role;
import org.userservice.user_service.entity.UserEntity;
import org.userservice.user_service.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new CustomUserDetailsService(userRepository);
    }

    @Test
    void testLoadUserByUsername_Success() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setEmail("user@test.com");
        user.setPassword("encoded");
        user.setRole(Role.USER);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("user@test.com");

        assertNotNull(details);
        assertEquals("user@test.com", details.getUsername());
        assertEquals("encoded", details.getPassword());
        assertEquals(1, details.getAuthorities().size());
        assertTrue(details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testLoadUserByUsername_NotFound() {
        when(userRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("notfound@test.com"));
    }

    @Test
    void testEncodePassword() {
        String raw = "mysecret";
        String encoded = service.encodePassword(raw);
        assertNotNull(encoded);
        assertNotEquals(raw, encoded); // BCrypt hashes are different from raw
        assertTrue(encoded.startsWith("$2a$") || encoded.startsWith("$2b$"));
    }
}
