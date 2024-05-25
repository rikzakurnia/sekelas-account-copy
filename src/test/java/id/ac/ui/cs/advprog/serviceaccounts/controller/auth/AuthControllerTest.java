package id.ac.ui.cs.advprog.serviceaccounts.controller.auth;

import id.ac.ui.cs.advprog.serviceaccounts.controller.AuthController;
import id.ac.ui.cs.advprog.serviceaccounts.dto.*;
import id.ac.ui.cs.advprog.serviceaccounts.exceptions.UserDoesNotExistException;
import id.ac.ui.cs.advprog.serviceaccounts.model.EmailTokenType;
import id.ac.ui.cs.advprog.serviceaccounts.model.Role;
import id.ac.ui.cs.advprog.serviceaccounts.model.User;
import id.ac.ui.cs.advprog.serviceaccounts.repository.UserRepository;
import id.ac.ui.cs.advprog.serviceaccounts.service.ResetPasswordService;
import id.ac.ui.cs.advprog.serviceaccounts.service.ResetPasswordServiceImpl;
import id.ac.ui.cs.advprog.serviceaccounts.service.auth.AuthServiceImpl;
import id.ac.ui.cs.advprog.serviceaccounts.service.auth.EmailTokenServiceImpl;
import id.ac.ui.cs.advprog.serviceaccounts.utils.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthServiceImpl service;

    @Mock
    private EmailTokenServiceImpl emailTokenService;

    @Mock
    private ResetPasswordServiceImpl resetPasswordService;

    @Mock @Autowired
    private UserRepository userRepository;

    @Test
    void whenLoginRequestShouldAuthenticate() {
        LoginRequest loginRequest = LoginRequest.builder()
                .emailOrUsername("rikza@email.com")
                .password("12345")
                .build();
        ResponseEntity<LoginResponse> loginResponse = authController.login(loginRequest);

        verify(service).authenticate(loginRequest);
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
    }
    @Test
    void whenLoginRequestInvalidShouldThrow() {
        LoginRequest loginRequest = LoginRequest.builder()
                .emailOrUsername("rikza@email.com")
                .build();

        doThrow(new RuntimeException()).when(service).authenticate(any(LoginRequest.class));

        Assertions.assertThrows(RuntimeException.class, () -> {
            authController.login(loginRequest);
        });
    }

    @Test
    void whenRegisterRequestShouldRegister() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("Rikku")
                .email("rikza@email.com")
                .name("rikza")
                .password("12345")
                .role("STUDENT")
                .build();
        ResponseEntity<LoginResponse> registerResponse = authController.register(registerRequest);

        verify(service).register(registerRequest);
        assertEquals(HttpStatus.OK, registerResponse.getStatusCode());
    }
    @Test
    void whenRegisterRequestInvalidShouldThrow() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("Rikku")
                .role("STUDENT")
                .build();

        doThrow(new RuntimeException()).when(service).register(any(RegisterRequest.class));

        Assertions.assertThrows(RuntimeException.class, () -> {
            authController.register(registerRequest);
        });
    }

    @Test
    void whenProcessResetPasswordUserDoesNotExistShouldThrowException() {
        String nonExistentEmail = "nonexistent@email.com";

        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        Assertions.assertThrows(UserDoesNotExistException.class, () -> {
            authController.processResetPassword(nonExistentEmail);
        });
    }

    @Test
    void whenGetCurrentUserShouldReturnCurrentUser() {
        User testUser = User.builder()
                .id(UUID.fromString("a0e0b757-d3ec-4324-a8f8-2771b0522ddf"))
                .name("Carlene")
                .email("carlene@gmail.com")
                .username("Carlene")
                .password("test123")
                .isVerified(true)
                .build();

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(Utils.getCurrentUser()).thenReturn(testUser);

        ResponseEntity<User> response = authController.getCurrentUser();

        assertEquals(testUser, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }


    @Test
    void whenConfirmEmailShouldConfirmEmail() {
        String email = "test@test.com";
        String token = "72e7d7ea-d03e-47f3-ad24-03f611727eb9";
        ResponseEntity<String> response = authController.confirmEmail(email, token);

        verify(service).confirmEmail(email, token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void whenSendEmailForResetPasswordShouldSendEmail(){
        String email = "tarreq.maulana@gmail.com";

        User user = User.builder()
                .active(true)
                .id(UUID.fromString("66787751-047b-4d4f-8a35-911f6e5c6d51"))
                .email("tarreq.maulana@gmail.com")
                .username("req")
                .password("123")
                .role(Role.STUDENT)
                .isVerified(true)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        ResponseEntity<ResetPasswordResponse> resetPasswordResponse = authController.processResetPassword(email);
        assertEquals(HttpStatus.OK, resetPasswordResponse.getStatusCode());
    }

    @Test
    void whenConfirmResetPasswordShouldConfirmResetPassword(){
            String userId = "tarreq.maulana@gmail.com";
            String token = "66787751-047b-4d4f-8a35-911f6e5c6d51";
            ResponseEntity<ResetPasswordResponse> resetPasswordResponse = authController.validateResetPassword(userId, token);

            verify(resetPasswordService).confirmResetPasswordRequest(userId, token);
            assertEquals(HttpStatus.OK, resetPasswordResponse.getStatusCode());
        }

    @Test
    void whenPerformResetPasswordShouldResetPassword(){
            ResetPasswordRequest resetPasswordRequest = ResetPasswordRequest.builder()
            .userId("bec2e684-c318-4068-b916-480f0831c487")
            .token("66787751-047b-4d4f-8a35-911f6e5c6d51")
            .newPassword("suksess")
            .build();

            ResponseEntity<ResetPasswordResponse> resetPasswordResponse = authController.resetPassword(resetPasswordRequest);

            verify(resetPasswordService).saveNewPassword(resetPasswordRequest);
            assertEquals(HttpStatus.OK, resetPasswordResponse.getStatusCode());
        }

    @Test
    void whenDeleteAccountShouldDelete() {
        User testUser = User.builder()
                .id(UUID.fromString("a0e0b757-d3ec-4324-a8f8-2771b0522ddf"))
                .name("Carlene")
                .email("carlene@gmail.com")
                .username("Carlene")
                .password("test123")
                .isVerified(true)
                .build();

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(Utils.getCurrentUser()).thenReturn(testUser);

        ResponseEntity<String> response = authController.delete();

        verify(service).delete(testUser);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
