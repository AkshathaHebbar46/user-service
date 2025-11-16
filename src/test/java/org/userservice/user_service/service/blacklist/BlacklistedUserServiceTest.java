package org.userservice.user_service.service.blacklist;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;
import org.userservice.user_service.entity.UserEntity;
import org.userservice.user_service.repository.UserRepository;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BlacklistedUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec uriSpec;

    @Mock
    private WebClient.RequestBodySpec bodySpec;

    // IMPORTANT FIX: use raw type to avoid capture errors
    @Mock
    @SuppressWarnings("rawtypes")
    private WebClient.RequestHeadersSpec headersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private BlacklistedUserService service;

    private UserEntity activeUser;
    private UserEntity inactiveUser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        service = new BlacklistedUserService(userRepository, webClientBuilder);

        activeUser = new UserEntity();
        activeUser.setId(1L);
        activeUser.setActive(true);

        inactiveUser = new UserEntity();
        inactiveUser.setId(2L);
        inactiveUser.setActive(false);
    }

    private void mockToken(String token) {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader("Authorization")).thenReturn(token);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));
    }

    private void mockWebClientPost(String expectedUri) {
        when(webClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(expectedUri)).thenReturn(bodySpec);

        when(bodySpec.header(eq("Authorization"), anyString())).thenReturn(bodySpec);

        // FIX: bodyValue() returns RequestHeadersSpec<?> but mocks need raw type
        when(bodySpec.bodyValue(any())).thenReturn(headersSpec);

        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());
    }

    @Test
    void testBlacklistUserSuccess() {
        mockToken("Bearer X");
        mockWebClientPost("");

        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));
        when(userRepository.save(activeUser)).thenReturn(activeUser);

        service.blacklistUser(1L);

        assertFalse(activeUser.getActive());
        verify(userRepository).save(activeUser);
        verify(webClient).post();
    }

    @Test
    void testBlacklistUser_UserNotFound() {
        when(userRepository.findById(123L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.blacklistUser(123L));
    }

    @Test
    void testBlacklistUser_AlreadyInactive() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(inactiveUser));

        service.blacklistUser(2L);

        verify(userRepository, never()).save(any());
        verify(webClient, never()).post();
    }

    @Test
    void testBlacklistUser_ExtractToken() {
        mockToken("Bearer TOKEN_ABC");

        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));
        when(userRepository.save(activeUser)).thenReturn(activeUser);

        when(webClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri("")).thenReturn(bodySpec);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        when(bodySpec.header(eq("Authorization"), captor.capture())).thenReturn(bodySpec);
        when(bodySpec.bodyValue(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        service.blacklistUser(1L);

        assertEquals("Bearer TOKEN_ABC", captor.getValue());
    }

    @Test
    void testUnblockUserSuccess() {
        mockToken("Bearer TOKEN");
        mockWebClientPost("/unblock");

        when(userRepository.findById(2L)).thenReturn(Optional.of(inactiveUser));
        when(userRepository.save(inactiveUser)).thenReturn(inactiveUser);

        service.unblockUser(2L);

        assertTrue(inactiveUser.getActive());
        verify(webClient).post();
    }

    @Test
    void testUnblockUser_UserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.unblockUser(999L));
    }

    @Test
    void testUnblockUser_AlreadyActive() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        service.unblockUser(1L);

        verify(userRepository, never()).save(any());
        verify(webClient, never()).post();
    }

    @Test
    void testBlacklist_NoTokenGraceful() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader("Authorization")).thenReturn(null);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));

        mockWebClientPost("");

        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));
        when(userRepository.save(activeUser)).thenReturn(activeUser);

        assertDoesNotThrow(() -> service.blacklistUser(1L));
    }
}
