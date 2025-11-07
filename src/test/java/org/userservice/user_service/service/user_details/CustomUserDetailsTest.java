package org.userservice.user_service.service.user_details;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.userservice.user_service.entity.Role;
import org.userservice.user_service.entity.UserEntity;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class CustomUserDetailsTest {
    @Test
    void testCustomUserDetailsProperties() {
        UserEntity user = new UserEntity();
        user.setId(42L);
        user.setEmail("test@example.com");
        user.setPassword("secret");
        user.setRole(Role.USER);

        CustomUserDetails details = new CustomUserDetails(user);

        assertEquals(42L, details.getId());
        assertEquals("test@example.com", details.getUsername());
        assertEquals("secret", details.getPassword());
        assertEquals(Role.USER, details.getRole());

        Collection<? extends GrantedAuthority> authorities = details.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));

        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isCredentialsNonExpired());
        assertTrue(details.isEnabled());
    }
}
