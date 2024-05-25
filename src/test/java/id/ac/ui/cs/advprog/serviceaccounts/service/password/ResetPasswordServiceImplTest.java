package id.ac.ui.cs.advprog.serviceaccounts.service.password;

import id.ac.ui.cs.advprog.serviceaccounts.dto.ResetPasswordRequest;
import id.ac.ui.cs.advprog.serviceaccounts.exceptions.EmailTokenIsInvalidException;
import id.ac.ui.cs.advprog.serviceaccounts.exceptions.InvalidPasswordRequestException;
import id.ac.ui.cs.advprog.serviceaccounts.exceptions.UserDoesNotExistException;
import id.ac.ui.cs.advprog.serviceaccounts.model.EmailTokenType;
import id.ac.ui.cs.advprog.serviceaccounts.model.User;
import id.ac.ui.cs.advprog.serviceaccounts.repository.UserRepository;
import id.ac.ui.cs.advprog.serviceaccounts.service.ResetPasswordServiceImpl;
import id.ac.ui.cs.advprog.serviceaccounts.service.auth.EmailTokenServiceImpl;
import id.ac.ui.cs.advprog.serviceaccounts.service.auth.MailSenderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ResetPasswordServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailTokenServiceImpl emailTokenService;

    @Mock
    private MailSenderServiceImpl mailSenderService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ResetPasswordServiceImpl resetPasswordService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void sendResetPassword_ShouldSendEmailWithCorrectContent() {
        User user = User.builder()
                .id(UUID.fromString("45a81670-bee5-4e1d-8780-616c44e95786"))
                .email("tarreq.maulana@gmail.com")
                .password("12345678")
                .build();

        String expectedSubject = "SeKelas | Reset Password";
        String expectedBody = "Tap the following link to reset your account password http://example.com/reset?token=12345" +
                " \nIf you didn't request a new password, you can safely delete this email";

        when(emailTokenService.generateTokenUrl(any(UUID.class), eq(EmailTokenType.RESET_PASSWORD)))
                .thenReturn("http://example.com/reset?token=12345");

        resetPasswordService.sendResetPassword(user);

        verify(mailSenderService).sendEmail(user.getEmail(), expectedSubject, expectedBody);
    }

    @Test
    void confirmResetPasswordRequest_ValidToken_ShouldNotThrowException() {
        String userId = UUID.randomUUID().toString();
        String token = "ca7f4242-4146-4043-a977-5dc56fc4f737";

        when(emailTokenService.isValidToken(userId, token, EmailTokenType.RESET_PASSWORD))
                .thenReturn(true);

        assertDoesNotThrow(() -> resetPasswordService.confirmResetPasswordRequest(userId, token));
    }

    @Test
    void confirmResetPasswordRequest_InvalidToken_ShouldThrowEmailTokenIsInvalidException() {
        String userId = UUID.randomUUID().toString();
        String token = "ca7f4242-4146-4043-a977-5dc56fc4f737";

        when(emailTokenService.isValidToken(userId, token, EmailTokenType.RESET_PASSWORD))
                .thenReturn(false);

        assertThrows(EmailTokenIsInvalidException.class,
                () -> resetPasswordService.confirmResetPasswordRequest(userId, token));
    }

    @Test
    void saveNewPassword_ValidRequest_ShouldSaveNewPassword() {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .userId("45a81670-bee5-4e1d-8780-616c44e95786")
                .token("ca7f4242-4146-4043-a977-5dc56fc4f737")
                .newPassword("suksesBanget")
                .build();

        User user = User.builder()
                .id(UUID.fromString("45a81670-bee5-4e1d-8780-616c44e95786"))
                .password("12345678")
                .build();


        when(userRepository.findById(UUID.fromString(user.getId().toString())))
                .thenReturn(Optional.of(user));
        when(emailTokenService.isValidToken(request.getUserId(), request.getToken(), EmailTokenType.RESET_PASSWORD))
                .thenReturn(true);
        when(passwordEncoder.encode(request.getNewPassword()))
                .thenReturn("encodedPassword");
        when(userRepository.save(user))
                .thenReturn(user);

        resetPasswordService.saveNewPassword(request);

        assertEquals("encodedPassword", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void saveNewPassword_UserDoesNotExist_ShouldThrowUserDoesNotExistException() {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .userId("45a81670-bee5-4e1d-8780-616c44e95786")
                .token("ca7f4242-4146-4043-a977-5dc56fc4f737")
                .newPassword("suksess")
                .build();

        when(userRepository.findById(UUID.fromString(request.getUserId())))
                .thenReturn(Optional.empty());

        assertThrows(UserDoesNotExistException.class, () -> resetPasswordService.saveNewPassword(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shortOrNullOrBlankNewPasswordShouldThrowInvalidPasswordException() {
        String userId = "45a81670-bee5-4e1d-8780-616c44e95786";
        String token = "ca7f4242-4146-4043-a977-5dc56fc4f737";
        ResetPasswordRequest nullRequest = ResetPasswordRequest.builder()
                .userId(userId)
                .token(token)
                .newPassword(null)
                .build();

        ResetPasswordRequest blankRequest = ResetPasswordRequest.builder()
                .userId(userId)
                .token(token)
                .newPassword(" ")
                .build();

        User user = User.builder()
                .id(UUID.fromString(userId))
                .password("12345678")
                .build();

        when(userRepository.findById(UUID.fromString(user.getId().toString())))
                .thenReturn(Optional.of(user));
        when(emailTokenService.isValidToken(nullRequest.getUserId(), nullRequest.getToken(), EmailTokenType.RESET_PASSWORD))
                .thenReturn(true);

        when(userRepository.findById(UUID.fromString(user.getId().toString())))
                .thenReturn(Optional.of(user));
        when(emailTokenService.isValidToken(blankRequest.getUserId(), blankRequest.getToken(), EmailTokenType.RESET_PASSWORD))
                .thenReturn(true);

        assertThrows(InvalidPasswordRequestException.class,
                () -> resetPasswordService.saveNewPassword(nullRequest));
        assertThrows(InvalidPasswordRequestException.class,
                () -> resetPasswordService.saveNewPassword(blankRequest));
    }

    @Test
    void shortNewPasswordShouldThrowInvalidPasswordException(){
        String userId = "45a81670-bee5-4e1d-8780-616c44e95786";
        String token = "ca7f4242-4146-4043-a977-5dc56fc4f737";
        ResetPasswordRequest shortRequest = ResetPasswordRequest.builder()
                .userId(userId)
                .token(token)
                .newPassword("haha")
                .build();

        User user = User.builder()
                .id(UUID.fromString(userId))
                .password("12345678")
                .build();

        when(userRepository.findById(UUID.fromString(user.getId().toString())))
                .thenReturn(Optional.of(user));
        when(emailTokenService.isValidToken(shortRequest.getUserId(), shortRequest.getToken(), EmailTokenType.RESET_PASSWORD))
                .thenReturn(true);

        assertThrows(InvalidPasswordRequestException.class,
                () -> resetPasswordService.saveNewPassword(shortRequest));
    }
}
