package org.userservice.user_service.service.blacklist;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.*;
import org.userservice.user_service.entity.UserEntity;
import org.userservice.user_service.repository.UserRepository;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BlacklistedUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private WebClient.Builder webClientBuilder;

    private BlacklistedUserService blacklistedUserService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock builder chain
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        blacklistedUserService = new BlacklistedUserService(userRepository, webClientBuilder);
    }

    // 1️⃣ Happy Path — Blacklist user successfully
    @Test
    void blacklistUser_ShouldDeactivateUserAndCallWalletService() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);
        mockWebClientPostFlow("");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer testToken");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        blacklistedUserService.blacklistUser(1L);

        assertFalse(user.getActive());
        verify(userRepository).save(user);
        verify(webClient).post();
    }

    // 2️⃣ User not found
    @Test
    void blacklistUser_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> blacklistedUserService.blacklistUser(999L));
    }

    // 3️⃣ Already inactive user — should skip wallet call
    @Test
    void blacklistUser_ShouldSkip_WhenAlreadyInactive() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setActive(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        blacklistedUserService.blacklistUser(1L);

        verify(userRepository, never()).save(any());
        verify(webClient, never()).post();
    }

    // 4️⃣ Unblock user — happy path
    @Test
    void unblockUser_ShouldActivateAndCallWalletService() {
        UserEntity user = new UserEntity();
        user.setId(2L);
        user.setActive(false);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        mockWebClientPostFlow("/unblock");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer unblockToken");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        blacklistedUserService.unblockUser(2L);

        assertTrue(user.getActive());
        verify(userRepository).save(user);
        verify(webClient).post();
    }

    // 5️⃣ Unblock user — not found
    @Test
    void unblockUser_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> blacklistedUserService.unblockUser(10L));
    }

    // 6️⃣ Unblock already active — should skip
    @Test
    void unblockUser_ShouldSkip_WhenAlreadyActive() {
        UserEntity user = new UserEntity();
        user.setId(2L);
        user.setActive(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        blacklistedUserService.unblockUser(2L);

        verify(userRepository, never()).save(any());
        verify(webClient, never()).post();
    }

    // 7️⃣ extractCurrentToken() — valid token
    @Test
    void extractToken_ShouldReturn_WhenValidHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer validToken");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String token = invokeExtractToken();
        assertEquals("validToken", token);
    }

    // 8️⃣ extractCurrentToken() — missing header
    @Test
    void extractToken_ShouldReturnNull_WhenNoHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String token = invokeExtractToken();
        assertNull(token);
    }

    // 9️⃣ extractCurrentToken() — invalid header format
    @Test
    void extractToken_ShouldReturnNull_WhenInvalidFormat() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("InvalidToken");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String token = invokeExtractToken();
        assertNull(token);
    }

    // 🔟 extractCurrentToken() — no request context
    @Test
    void extractToken_ShouldReturnNull_WhenNoContext() {
        RequestContextHolder.resetRequestAttributes();
        String token = invokeExtractToken();
        assertNull(token);
    }

    // Helper to mock webclient chain
    private void mockWebClientPostFlow(String uri) {
        doReturn(requestBodyUriSpec).when(webClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(uri);
        doReturn(requestBodySpec).when(requestBodySpec).header(anyString(), anyString());
        doReturn(requestHeadersSpec).when(requestBodySpec).bodyValue(any(Map.class));
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        doReturn(Mono.empty()).when(responseSpec).bodyToMono(Void.class);
    }

    // Helper to invoke private token method using reflection
    private String invokeExtractToken() {
        try {
            var method = BlacklistedUserService.class.getDeclaredMethod("extractCurrentToken");
            method.setAccessible(true);
            return (String) method.invoke(blacklistedUserService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }
}
