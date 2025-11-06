package org.userservice.user_service.controller.auth;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.userservice.user_service.dto.request.login.AuthRequestDTO;
import org.userservice.user_service.dto.request.register.RegisterRequestDTO;
import org.userservice.user_service.dto.response.auth.AuthResponseDTO;
import org.userservice.user_service.entity.*;
import org.userservice.user_service.repository.UserRepository;
import org.userservice.user_service.service.jwt.JwtService;
import org.userservice.user_service.service.user_details.CustomUserDetailsService;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserRepository userRepository,
                          CustomUserDetailsService userDetailsService,
                          JwtService jwtService,
                          AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already registered");
        }

        UserEntity user = new UserEntity();
        user.setUsername(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(userDetailsService.encodePassword(request.getPassword()));
        user.setAge(request.getAge());
        user.setRole(Role.USER);

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }



    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserEntity user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        String token = jwtService.generateToken(user.getEmail(), user.getId(), user.getRole().name());
        return ResponseEntity.ok(new AuthResponseDTO(token, user.getRole().name()));
    }
}
