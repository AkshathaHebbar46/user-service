package org.userservice.user_service.service.user_details;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.userservice.user_service.entity.UserEntity;
import org.userservice.user_service.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
        log.info("CustomUserDetailsService initialized");
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Attempting to load user by email={}", email);
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email={}", email);
                    return new UsernameNotFoundException("User not found");
                });
        log.info("User found with email={}, userId={}", email, user.getId());
        return new CustomUserDetails(user);
    }

    public String encodePassword(String rawPassword) {
        log.debug("Encoding password for raw input");
        return passwordEncoder.encode(rawPassword);
    }
}
