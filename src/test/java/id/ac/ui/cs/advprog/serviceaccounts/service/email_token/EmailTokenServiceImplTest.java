package id.ac.ui.cs.advprog.serviceaccounts.service.email_token;

import id.ac.ui.cs.advprog.serviceaccounts.exceptions.EmailTokenIsInvalidException;
import id.ac.ui.cs.advprog.serviceaccounts.exceptions.UserDoesNotExistException;
import id.ac.ui.cs.advprog.serviceaccounts.model.EmailToken;
import id.ac.ui.cs.advprog.serviceaccounts.model.EmailTokenType;
import id.ac.ui.cs.advprog.serviceaccounts.repository.EmailTokenRepository;
import id.ac.ui.cs.advprog.serviceaccounts.repository.UserRepository;
import id.ac.ui.cs.advprog.serviceaccounts.service.auth.EmailTokenService;
import id.ac.ui.cs.advprog.serviceaccounts.service.auth.EmailTokenServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import id.ac.ui.cs.advprog.serviceaccounts.model.User;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailTokenServiceImplTest {
    @InjectMocks
    private EmailTokenService service = new EmailTokenServiceImpl();

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailTokenRepository emailTokenRepository;

    User user;
    EmailToken notExpiredEmailToken;
    EmailToken expiredEmailToken;

    @BeforeEach
    void setUp() {
        LocalDateTime expiredIssuedAt = LocalDateTime.now().minus(16, ChronoUnit.MINUTES);
        user = User.builder().
                id(UUID.fromString("a0e0b757-d3ec-4324-a8f8-2771b0522ddf")).
                name("Gib").
                email("gib@gmail.com").
                username("gib").
                password("test123").
                isVerified(false).
                build();

        notExpiredEmailToken = EmailToken.builder().
                token(UUID.fromString("b918d391-46ce-4c45-a58d-dbb9ff9fbb5a")).
                user(user).
                type(EmailTokenType.CONFIRM_EMAIL).
                issuedAt(LocalDateTime.now()).
                isExpired(false).
                build();

        expiredEmailToken = EmailToken.builder().
                token(UUID.fromString("e4b638a1-1183-456b-9e59-5a971fad3a16")).
                user(user).
                type(EmailTokenType.CONFIRM_EMAIL).
                issuedAt(expiredIssuedAt).
                isExpired(true).
                build();
    }

    @Test
    void whenEmailExistsAndConfirmEmailShouldGenerateTokenUrl() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));

        String id = "a0e0b757-d3ec-4324-a8f8-2771b0522ddf";
        String url = service.generateTokenUrl(UUID.fromString(id), EmailTokenType.CONFIRM_EMAIL);
        String userId = url.split("\\?")[1].split("&")[0].split("=")[1];
        String token = url.split("\\?")[1].split("&")[1].split("=")[1];
        Assertions.assertEquals(userId, user.getId().toString());
        Assertions.assertTrue(isUUID(token));
    }

    @Test
    void whenEmailExistsAndChangeEmailShouldGenerateTokenUrl() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));

        String id = "a0e0b757-d3ec-4324-a8f8-2771b0522ddf";
        String url = service.generateTokenUrl(UUID.fromString(id), EmailTokenType.CHANGE_EMAIL);
        String userId = url.split("\\?")[1].split("&")[0].split("=")[1];
        String token = url.split("\\?")[1].split("&")[1].split("=")[1];
        Assertions.assertEquals(userId, user.getId().toString());
        Assertions.assertTrue(isUUID(token));
    }

    @Test
    void whenUserDoesNotExistShouldThrowError() {
        UUID uuidFromString = UUID.fromString("a0e0b757-d3ec-4324-a8f8-2771b0522ddf");
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(UserDoesNotExistException.class, () ->
            service.generateTokenUrl(uuidFromString, EmailTokenType.CONFIRM_EMAIL)
        );
    }

    @Test
    void whenTokenIsValidShouldReturnTrue() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));
        when(emailTokenRepository.findByUserAndTokenAndType(any(User.class), any(UUID.class), any(EmailTokenType.class))).
            thenReturn(Optional.of(notExpiredEmailToken));

        Assertions.assertTrue(
                service.isValidToken(
                        user.getId().toString(),
                        notExpiredEmailToken.getToken().toString(),
                        EmailTokenType.CONFIRM_EMAIL
                )
        );
    }

    @Test
    void whenCheckingIsValidTokenIfUserDoesNotExistShouldThrowError() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Assertions.assertFalse(
            service.isValidToken("6060f29d-c9ab-4a45-89c2-5f69ee107e49", "6060f29d-c9ab-4a45-89c2-5f69ee107e49", EmailTokenType.CONFIRM_EMAIL)
        );
    }

    @Test
    void whenCheckingIsValidTokenIfEmailTokenEmptyShouldThrowError() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));
        when(emailTokenRepository.findByUserAndTokenAndType(any(User.class), any(UUID.class), any(EmailTokenType.class))).
                thenReturn(Optional.empty());

        Assertions.assertFalse(
                service.isValidToken("6060f29d-c9ab-4a45-89c2-5f69ee107e49", "6060f29d-c9ab-4a45-89c2-5f69ee107e49", EmailTokenType.CONFIRM_EMAIL)
        );
    }

    @Test
    void whenTokenIsInvalidShouldReturnFalse() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));
        when(emailTokenRepository.findByUserAndTokenAndType(any(User.class), any(UUID.class), any(EmailTokenType.class))).
                thenReturn(Optional.of(expiredEmailToken));

        Assertions.assertFalse(
                service.isValidToken(
                        user.getId().toString(),
                        expiredEmailToken.getToken().toString(),
                        EmailTokenType.CONFIRM_EMAIL
                )
        );
    }

    @Test
    void whenExpiringTokenIfUserDoesNotExistShouldThrowError() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        Assertions.assertThrows(UserDoesNotExistException.class, () -> {
            service.expireToken("53efdaf3-f601-472f-9a9d-d0f3eaadaae9", "f41ab5df-6596-4e20-ac8d-30479a721115", EmailTokenType.CONFIRM_EMAIL);
        });
    }

    @Test
    void whenExpiringTokenIfUserExistsButTokenDoesNotShouldThrowError() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));
        when(emailTokenRepository.findByUserAndTokenAndType(any(User.class), any(UUID.class), any(EmailTokenType.class))).thenReturn(Optional.empty());
        Assertions.assertThrows(EmailTokenIsInvalidException.class, () -> {
            service.expireToken("53efdaf3-f601-472f-9a9d-d0f3eaadaae9", "f578d7ba-0e39-4715-8d6f-2e212acb2679", EmailTokenType.CONFIRM_EMAIL);
        });
    }

    @Test
    void whenExpiringTokenAndUserAndTokenIsValidShouldExpireToken() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));
        when(emailTokenRepository.findByUserAndTokenAndType(any(User.class), any(UUID.class), any(EmailTokenType.class))).thenReturn(Optional.of(notExpiredEmailToken));
        service.expireToken(user.getId().toString(), notExpiredEmailToken.getToken().toString(), EmailTokenType.CONFIRM_EMAIL);
        verify(emailTokenRepository).save(notExpiredEmailToken);
        Assertions.assertTrue(notExpiredEmailToken.isExpired());
    }

    private boolean isUUID(String uuidString) {
        try {
            UUID uuid = UUID.fromString(uuidString);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
