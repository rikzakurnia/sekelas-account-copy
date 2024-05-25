package id.ac.ui.cs.advprog.serviceaccounts.service.profile;

import id.ac.ui.cs.advprog.serviceaccounts.exceptions.*;
import id.ac.ui.cs.advprog.serviceaccounts.model.EmailTokenType;
import id.ac.ui.cs.advprog.serviceaccounts.model.User;
import id.ac.ui.cs.advprog.serviceaccounts.repository.UserRepository;
import id.ac.ui.cs.advprog.serviceaccounts.service.auth.EmailTokenService;
import id.ac.ui.cs.advprog.serviceaccounts.service.auth.MailSenderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

import static java.time.Duration.ofSeconds;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class EditProfileServiceImplTest {

    @InjectMocks
    private EditProfileServiceImpl editProfileService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MailSenderServiceImpl mailSenderService;

    @Mock
    private EmailTokenService emailTokenService;
    @Mock
    private WebClient webClientMock;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpecMock;

    @Mock
    private WebClient.RequestBodySpec requestBodySpecMock;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpecMock;

    @Mock
    private WebClient.ResponseSpec responseSpecMock;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testConfirmEmailChange() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("oldEmail@example.com")
                .pendingEmail("newEmail@example.com")
                .build();

        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));
        when(emailTokenService.isValidToken(anyString(), anyString(), any(EmailTokenType.class))).thenReturn(true);

        editProfileService.confirmEmailChange(user.getId().toString(), "token");

        verify(userRepository, times(1)).save(any(User.class));
        verify(emailTokenService, times(1)).expireToken(anyString(), anyString(), any(EmailTokenType.class));
    }

    @Test
    void testConfirmEmailChange_InvalidToken() {
        String userId = UUID.randomUUID().toString();

        when(emailTokenService.isValidToken(anyString(), anyString(), any(EmailTokenType.class))).thenReturn(false);

        assertThrows(EmailTokenIsInvalidException.class, () ->
                editProfileService.confirmEmailChange(userId, "token")
        );
    }

    @Test
    void testConfirmEmailChange_UserDoesNotExist() {
        String userId = UUID.randomUUID().toString();

        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        when(emailTokenService.isValidToken(anyString(), anyString(), any(EmailTokenType.class))).thenReturn(true);

        assertThrows(UserDoesNotExistException.class, () ->
                editProfileService.confirmEmailChange(userId, "token")
        );
    }

    @Test
    void testEditUserProfile() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("oldUsername")
                .email("oldEmail@example.com")
                .name("oldName")
                .build();

        when(userRepository.usernameExists(anyString())).thenReturn(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(emailTokenService.generateTokenUrl(any(UUID.class), any(EmailTokenType.class))).thenReturn("tokenUrl");

        doNothing().when(mailSenderService).sendEmail(anyString(), anyString(), anyString());

        ResponseEntity<String> response = editProfileService.editUserProfile(user, "newEmail@example.com", "newUsername", "newName");

        verify(userRepository, times(1)).save(any(User.class));
        verify(mailSenderService, times(1)).sendEmail(anyString(), anyString(), anyString());
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(userRepository, times(1)).usernameExists(anyString());

        assertEquals("Profile updated", response.getBody());
    }

    @Test
    void testEditUserProfile_UsernameAlreadyExists() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("oldUsername")
                .email("oldEmail@example.com")
                .build();

        when(userRepository.usernameExists(anyString())).thenReturn(true);

        assertThrows(UsernameAlreadyExistException.class, () ->
                editProfileService.editUserProfile(user, "newEmail@example.com", "newUsername", "newName")
        );
    }

    @Test
    void testEditUserProfile_EmailAlreadyExists() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("oldUsername")
                .email("oldEmail@example.com")
                .build();

        User existingUser = User.builder()
                .id(UUID.randomUUID())
                .username("existingUsername")
                .email("newEmail@example.com")
                .build();

        when(userRepository.usernameExists(anyString())).thenReturn(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(existingUser));

        assertThrows(EmailAlreadyExistException.class, () ->
                editProfileService.editUserProfile(user, "newEmail@example.com", "newUsername", "newName")
        );
    }

    @Test
    void testEditUserProfile_EmptyInputs() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("oldUsername")
                .email("oldEmail@example.com")
                .name("oldName")
                .build();

        assertThrows(InvalidInputException.class, () ->
                editProfileService.editUserProfile(user, "", "newUsername", "newName")
        );

        assertThrows(InvalidInputException.class, () ->
                editProfileService.editUserProfile(user, "newEmail@example.com", "", "newName")
        );

        assertThrows(InvalidInputException.class, () ->
                editProfileService.editUserProfile(user, "newEmail@example.com", "newUsername", "")
        );
    }

    @Test
    void testEditUserProfile_SameUsername() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("oldUsername")
                .email("oldEmail@example.com")
                .name("oldName")
                .build();

        when(userRepository.usernameExists(anyString())).thenReturn(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(emailTokenService.generateTokenUrl(any(UUID.class), any(EmailTokenType.class))).thenReturn("tokenUrl");

        doNothing().when(mailSenderService).sendEmail(anyString(), anyString(), anyString());

        ResponseEntity<String> response = editProfileService.editUserProfile(user, "newEmail@example.com", "oldUsername", "newName");

        verify(userRepository, times(1)).save(any(User.class));
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(userRepository, times(0)).usernameExists(anyString());

        assertEquals("Profile updated", response.getBody());
    }


    @Test
    void testEditUserProfile_SameEmail() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("oldUsername")
                .email("oldEmail@example.com")
                .name("oldName")
                .build();

        when(userRepository.usernameExists(anyString())).thenReturn(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(emailTokenService.generateTokenUrl(any(UUID.class), any(EmailTokenType.class))).thenReturn("tokenUrl");

        doNothing().when(mailSenderService).sendEmail(anyString(), anyString(), anyString());

        ResponseEntity<String> response = editProfileService.editUserProfile(user, "oldEmail@example.com", "newUsername", "newName");

        verify(userRepository, times(1)).save(any(User.class));
        verify(mailSenderService, times(0)).sendEmail(anyString(), anyString(), anyString());
        verify(userRepository, times(0)).findByEmail(anyString());
        verify(userRepository, times(1)).usernameExists(anyString());

        assertEquals("Profile updated", response.getBody());
    }

    @Test
    void testEditUserProfile_SameUserEmailUpdate() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("oldUsername")
                .email("oldEmail@example.com")
                .build();

        when(userRepository.usernameExists(anyString())).thenReturn(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        ResponseEntity<String> response = editProfileService.editUserProfile(user, "oldEmail@example.com", "newUsername", "newName");

        verify(userRepository, times(1)).save(any(User.class));
        verify(mailSenderService, times(0)).sendEmail(anyString(), anyString(), anyString());
        verify(userRepository, times(0)).findByEmail(anyString());
        verify(userRepository, times(1)).usernameExists(anyString());

        assertEquals("Profile updated", response.getBody());
    }

    @Test
    void testEditUserProfile_DifferentUserEmailExists() {
        User currentUser = User.builder()
                .id(UUID.randomUUID())
                .username("oldUsername")
                .email("oldEmail@example.com")
                .build();

        User existingUser = User.builder()
                .id(UUID.randomUUID()) // Different ID to trigger the exception
                .username("existingUsername")
                .email("newEmail@example.com")
                .build();

        when(userRepository.usernameExists(anyString())).thenReturn(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(existingUser));

        assertThrows(EmailAlreadyExistException.class, () ->
                editProfileService.editUserProfile(currentUser, "newEmail@example.com", "newUsername", "newName")
        );
    }



    @Test
    void whenVerifyUserInAnotherServiceSuccessShouldPut(){
        User userWeb = User.builder()
                .username("ruzain")
                .build();

        String testUrl = "http://localhost:8081/tes";

        when(webClientMock.put()).thenReturn(requestBodyUriSpecMock);
        when(requestBodyUriSpecMock.uri(anyString())).thenReturn(requestBodySpecMock);
        when(requestBodySpecMock.bodyValue(userWeb)).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(String.class)).thenReturn(Mono.just("response"));

        editProfileService.updateUserInOtherMicroservice(userWeb, testUrl);

        await().atMost(ofSeconds(5)).untilAsserted(() -> {
            verify(webClientMock).put();
            verify(requestBodyUriSpecMock).uri(testUrl);
            verify(requestBodySpecMock).bodyValue(userWeb);
            verify(requestHeadersSpecMock).retrieve();
            verify(responseSpecMock).bodyToMono(String.class);
        });

    }
}
