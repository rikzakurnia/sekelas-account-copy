package id.ac.ui.cs.advprog.serviceaccounts.service.auth;

import id.ac.ui.cs.advprog.serviceaccounts.dto.LoginRequest;
import id.ac.ui.cs.advprog.serviceaccounts.dto.LoginResponse;
import id.ac.ui.cs.advprog.serviceaccounts.dto.RegisterRequest;
import id.ac.ui.cs.advprog.serviceaccounts.exceptions.*;
import id.ac.ui.cs.advprog.serviceaccounts.model.EmailTokenType;
import id.ac.ui.cs.advprog.serviceaccounts.model.Role;
import id.ac.ui.cs.advprog.serviceaccounts.model.User;
import id.ac.ui.cs.advprog.serviceaccounts.repository.UserRepository;
import id.ac.ui.cs.advprog.serviceaccounts.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;



import java.util.Optional;
import java.util.UUID;

import static java.time.Duration.ofSeconds;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class
AuthServiceImplTest {
    @InjectMocks
    private AuthService service = new AuthServiceImpl();

    @Mock
    private MailSenderService mailSenderService = new MailSenderServiceImpl();

    @Mock
    private EmailTokenService emailTokenService = new EmailTokenServiceImpl();


    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;



    @Mock
    private WebClient webClientMock;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpecMock;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpecMock;

    @Mock
    private WebClient.RequestBodySpec requestBodySpecMock;

    @SuppressWarnings("rawtypes")
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpecMock;

    @Mock
    private WebClient.ResponseSpec responseSpecMock;


    User user;
    User userTest;
    User userSaved;
    LoginRequest loginRequestWithEmail;
    LoginRequest loginRequestWithUsername;
    RegisterRequest registerRequestStudent;
    RegisterRequest registerRequestTeacher;
    RegisterRequest registerRequestAdmin;
    @BeforeEach
    void setUp() {
        user = User.builder().
                id(UUID.fromString("a0e0b757-d3ec-4324-a8f8-2771b0522ddf")).
                name("Gib").
                email("gib@gmail.com").
                username("gib").
                password(passwordEncoder.encode("password")).
                isVerified(false).
                build();

        userTest = User.builder()
                .active(true)
                .id(UUID.fromString("f96867b3-86ff-4c79-b3d5-093f4f8d690e"))
                .email("rikzak@gmail.com")
                .username("Rikku")
                .password(passwordEncoder.encode("password"))
                .role(Role.STUDENT)
                .isVerified(true)
                .build();

        userSaved = User.builder()
                .id(UUID.fromString("f96867b3-86ff-4c79-b3d5-093f4f8d690e"))
                .email("rikza18@gmail.com")
                .username("Rikku18")
                .password(passwordEncoder.encode("password"))
                .role(Role.STUDENT)
                .isVerified(false)
                .build();

        registerRequestTeacher = RegisterRequest.builder()
                .username("joseph")
                .name("joseph joestar")
                .email("joseph@gmail.com")
                .role("TEACHER")
                .password("password")
                .build();

        registerRequestStudent = RegisterRequest.builder()
                .username("jotaro")
                .name("kujo jotaro")
                .email("jotaro@gmail.com")
                .role("STUDENT")
                .password("password")
                .build();

        registerRequestAdmin = RegisterRequest.builder()
                .username("dio")
                .name("dio brando")
                .email("dio@gmail.com")
                .role("ADMIN")
                .password("password")
                .build();

        loginRequestWithEmail = LoginRequest.builder()
                .emailOrUsername("rikzak@gmail.com")
                .password("12345")
                .build();

        loginRequestWithUsername = LoginRequest.builder().
                emailOrUsername("Rikku").
                password("12345").
                build();
    }



    @Test
    void whenLoginWithValidUsernameShouldReturnToken(){
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(new UsernamePasswordAuthenticationToken("username", "password"));
        when(jwtService.generateToken(any(User.class))).thenReturn("test.jwt.token");

        LoginResponse loginResponse = service.authenticate(loginRequestWithUsername);

        Assertions.assertNotNull(loginResponse);
        Assertions.assertNotNull(loginResponse.getToken());
    }

    @Test
    void whenLoginWithValidEmailShouldReturnToken(){
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(new UsernamePasswordAuthenticationToken("username", "password"));
        when(jwtService.generateToken(any(User.class))).thenReturn("test.jwt.token");

        LoginResponse loginResponse = service.authenticate(loginRequestWithEmail);

        Assertions.assertNotNull(loginResponse);
        Assertions.assertNotNull(loginResponse.getToken());
    }

    @Test
    void whenLoginWithInvalidCredentialsShouldThrow(){
        doThrow(new RuntimeException()).when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        Assertions.assertThrows(RuntimeException.class, () -> {
            service.authenticate(loginRequestWithEmail);
        });
    }


    @Test
    void whenRegisterStudentSuccessShouldReturnToken(){
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        when(jwtService.generateToken(any(User.class))).thenReturn("test.jwt.token");
        when(userRepository.save(any(User.class))).thenReturn(userTest);

        when(emailTokenService.generateTokenUrl(any(UUID.class), any(EmailTokenType.class))).thenReturn("emailtoken");

        LoginResponse loginResponse = service.register(registerRequestStudent);

        verify(userRepository).findByEmail(registerRequestStudent.getEmail());
        verify(userRepository).findByUsername(registerRequestStudent.getUsername());
        verify(userRepository).save(any(User.class));

        verify(jwtService).generateToken(any(User.class));

        await().atMost(ofSeconds(5)).untilAsserted(() -> {
            verify(mailSenderService).sendEmail(anyString(),anyString(),anyString());
        });
        Assertions.assertNotNull(loginResponse);
        Assertions.assertEquals("test.jwt.token", loginResponse.getToken());
    }

    @Test
    void whenRegisterTeacherSuccessShouldReturnToken(){
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        when(jwtService.generateToken(any(User.class))).thenReturn("test.jwt.token");
        when(userRepository.save(any(User.class))).thenReturn(userTest);

        when(emailTokenService.generateTokenUrl(any(UUID.class), any(EmailTokenType.class))).thenReturn("emailtoken");

        LoginResponse loginResponse = service.register(registerRequestTeacher);

        verify(userRepository).findByEmail(registerRequestTeacher.getEmail());
        verify(userRepository).findByUsername(registerRequestTeacher.getUsername());
        verify(userRepository).save(any(User.class));

        verify(jwtService).generateToken(any(User.class));

        await().atMost(ofSeconds(5)).untilAsserted(() -> {
            verify(mailSenderService).sendEmail(anyString(),anyString(),anyString());
        });
        Assertions.assertNotNull(loginResponse);
        Assertions.assertEquals("test.jwt.token", loginResponse.getToken());
    }

    @Test
    void whenRegisterAdminSuccessShouldReturnToken(){
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        when(jwtService.generateToken(any(User.class))).thenReturn("test.jwt.token");
        when(userRepository.save(any(User.class))).thenReturn(userTest);

        when(emailTokenService.generateTokenUrl(any(UUID.class), any(EmailTokenType.class))).thenReturn("emailtoken");

        LoginResponse loginResponse = service.register(registerRequestAdmin);

        verify(userRepository).findByEmail(registerRequestAdmin.getEmail());
        verify(userRepository).findByUsername(registerRequestAdmin.getUsername());
        verify(userRepository).save(any(User.class));

        verify(jwtService).generateToken(any(User.class));

        await().atMost(ofSeconds(5)).untilAsserted(() -> {
            verify(mailSenderService).sendEmail(anyString(),anyString(),anyString());
        });
        Assertions.assertNotNull(loginResponse);
        Assertions.assertEquals("test.jwt.token", loginResponse.getToken());
    }



    @Test
    void whenRegisterWithExistingEmailShouldThrowException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        Assertions.assertThrows(EmailAlreadyExistException.class, () -> service.register(registerRequestStudent));

        verify(userRepository).findByEmail(registerRequestStudent.getEmail());
        verify(userRepository).findByUsername(registerRequestStudent.getUsername());
    }

    @Test
    void whenRegisterWithInvalidEmailFormatShouldThrowException() {
        RegisterRequest invalidEmailRequest = RegisterRequest.builder()
                .email("wrong format")
                .build();

        Assertions.assertThrows(InvalidEmailException.class, () -> service.register(invalidEmailRequest));
    }

    @Test
    void whenRegisterWithExistingUsernameShouldThrowException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(userTest));

        Assertions.assertThrows(UsernameAlreadyExistException.class, () -> service.register(registerRequestStudent));

        verify(userRepository).findByEmail(registerRequestStudent.getEmail());
        verify(userRepository).findByUsername(registerRequestStudent.getUsername());
    }

    @Test
    void whenRegisterWithNullAttributeShouldThrowException() {
        RegisterRequest nullEmailRequest = RegisterRequest.builder()
                .build();
        RegisterRequest nullNameRequest = RegisterRequest.builder()
                .email("valid@email.com")
                .build();
        RegisterRequest nullUsernameRequest = RegisterRequest.builder()
                .email("valid@email.com")
                .name("valid")
                .build();
        RegisterRequest nullPasswordRequest = RegisterRequest.builder()
                .email("valid@email.com")
                .name("valid")
                .username("validusername")
                .build();
        RegisterRequest nullRoleRequest = RegisterRequest.builder()
                .email("valid@email.com")
                .name("valid")
                .username("validusername")
                .password("password")
                .build();
        Assertions.assertThrows(InvalidEmailException.class, () -> service.register(nullEmailRequest));
        Assertions.assertThrows(InvalidRegisterRequestException.class, () -> service.register(nullNameRequest));
        Assertions.assertThrows(InvalidRegisterRequestException.class, () -> service.register(nullUsernameRequest));
        Assertions.assertThrows(InvalidPasswordRequestException.class, () -> service.register(nullPasswordRequest));
        Assertions.assertThrows(InvalidRoleRequestException.class, () -> service.register(nullRoleRequest));
    }

    @Test
    void whenRegisterWithEmptyAttributeShouldThrowException() {
        RegisterRequest emptyNameRequest = RegisterRequest.builder()
                .email("valid@email.com")
                .name("")
                .build();
        RegisterRequest emptyUsernameRequest = RegisterRequest.builder()
                .email("valid@email.com")
                .name("valid")
                .username("")
                .build();
        RegisterRequest emptyPasswordRequest = RegisterRequest.builder()
                .email("valid@email.com")
                .name("valid")
                .username("validusername")
                .password("")
                .build();
        RegisterRequest emptyRoleRequest = RegisterRequest.builder()
                .email("valid@email.com")
                .name("valid")
                .username("validusername")
                .password("password")
                .role("")
                .build();

        Assertions.assertThrows(InvalidRegisterRequestException.class, () -> service.register(emptyNameRequest));
        Assertions.assertThrows(InvalidRegisterRequestException.class, () -> service.register(emptyUsernameRequest));
        Assertions.assertThrows(InvalidPasswordRequestException.class, () -> service.register(emptyPasswordRequest));
        Assertions.assertThrows(InvalidRoleRequestException.class, () -> service.register(emptyRoleRequest));
    }

    @Test
    void whenRegisterWithBlankAttributeShouldThrowException() {
        RegisterRequest blankNameRequest = RegisterRequest.builder()
                .email("valid@email.com")
                .name("   ")
                .build();
        RegisterRequest blankUsernameRequest = RegisterRequest.builder()
                .email("valid@email.com")
                .name("valid")
                .username("   ")
                .build();
        RegisterRequest blankPasswordRequest = RegisterRequest.builder()
                .email("valid@email.com")
                .name("valid")
                .username("validusername")
                .password("  ")
                .build();
        RegisterRequest blankRoleRequest = RegisterRequest.builder()
                .email("valid@email.com")
                .name("valid")
                .username("validusername")
                .password("password")
                .role("  ")
                .build();

        Assertions.assertThrows(InvalidRegisterRequestException.class, () -> service.register(blankNameRequest));
        Assertions.assertThrows(InvalidRegisterRequestException.class, () -> service.register(blankUsernameRequest));
        Assertions.assertThrows(InvalidPasswordRequestException.class, () -> service.register(blankPasswordRequest));
        Assertions.assertThrows(InvalidRoleRequestException.class, () -> service.register(blankRoleRequest));
    }

    @Test
    void whenRegisterWithPasswordTooShortShouldThrowException(){
        RegisterRequest shortPasswordRequest = RegisterRequest.builder()
                .email("valid@email.com")
                .name("valid")
                .username("validusername")
                .password("pass")
                .role("STUDENT")
                .build();

        Assertions.assertThrows(InvalidPasswordRequestException.class, () -> service.register(shortPasswordRequest));
    }

    @Test
    void whenRegisterNotMatchingRoleShouldThrowException(){
        RegisterRequest wrongRoleRequest = RegisterRequest.builder()
                .email("valid@email.com")
                .name("valid")
                .username("validusername")
                .password("passworddd")
                .role("PRESIDENT")
                .build();

        Assertions.assertThrows(InvalidRoleRequestException.class, () -> service.register(wrongRoleRequest));
    }

    @Test
    void whenEmailSizeBiggerThanMaxLengthShouldReturnFalse(){
        String emailTooLong = "kdjenfjaksjdkljksldjfklaksdaadadasdadadadsadadasdadasjdkeijwoqjjkljdklasdjkladjskaldjksaljdskaljdkaljssdasdsadsadasdadsadadadddadsadadsdadadsadsdadadsadadsadadadadsadsdsadadsdaddadsadsadsaddaasddadsadadaddadadasdaddaddadadadsadadsadsadsadadadaddsaddsakljjklsajdkajdkaljdkajdkaljdskaljdkajdsalkdjakldj@gmail.com";
        boolean hasil = service.isEmail(emailTooLong);
        Assertions.assertEquals(false, hasil);
    }

    @Test
    void whenEmailIsBlankOrEmptyShouldReturnFalse(){
        String emailBlank = "   ";
        String emailEmpty = "";
        boolean hasilBlank = service.isEmail(emailBlank);
        boolean hasilEmpty = service.isEmail(emailEmpty);
        Assertions.assertEquals(false, hasilBlank);
        Assertions.assertEquals(false, hasilEmpty);
    }

    @Test
    void whenConfirmEmailIfTokenIsNotValidShouldThrowError() {
        String userId = user.getId().toString();
        String token = "b918d391-46ce-4c45-a58d-dbb9ff9fbb5a";
        lenient().when(emailTokenService
                        .isValidToken(any(String.class), any(String.class), any(EmailTokenType.class)))
                        .thenReturn(false);
        Assertions.assertThrows(EmailTokenIsInvalidException.class, () -> {
            service.confirmEmail(userId, token);
        });
    }

    @Test
    void whenConfirmEmailSuccessShouldSaveUser() {
        String userId = userSaved.getId().toString();
        String token = "b918d391-46ce-4c45-a58d-dbb9ff9fbb5a";
        when(emailTokenService
                .isValidToken(any(String.class), any(String.class), any(EmailTokenType.class)))
                .thenReturn(true);
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(userSaved));

        service.confirmEmail(userId, token);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void whenVerifyUserIfUserDoesNotExistShouldThrowError() {
        String userId = user.getId().toString();

        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        Assertions.assertThrows(UserDoesNotExistException.class, () -> {
            service.verifyUser(userId);
        });
    }

    @Test
    void whenDeleteUserIfUserDoesNotExistShouldThrowError() {
        User deleteUser = User.builder().
                username("").
                build();
        when(userRepository.usernameExists(anyString())).thenReturn(false);
        Assertions.assertThrows(UserDoesNotExistException.class, () -> {
            service.delete(deleteUser);
        });
    }

    @Test
    void whenDeleteUserSuccessShouldDelete() {
        User deleteUser = User.builder()
                .username("")
                .build();
        when(userRepository.usernameExists(anyString())).thenReturn(true);
        service.delete(deleteUser);
        verify(userRepository).delete(deleteUser);
    }

    @Test
    void whenAddUserToAnotherServiceSuccessShouldPost(){
        User userWeb = User.builder()
                .username("ruzain")
                .build();

        String testUrl = "http://localhost:8081/tes";

        when(webClientMock.post()).thenReturn(requestBodyUriSpecMock);
        when(requestBodyUriSpecMock.uri(anyString())).thenReturn(requestBodySpecMock);
        when(requestBodySpecMock.bodyValue(userWeb)).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(String.class)).thenReturn(Mono.just("response"));

        service.addUserInOtherMicroservice(userWeb, testUrl);

        await().atMost(ofSeconds(5)).untilAsserted(() -> {
            verify(webClientMock).post();
            verify(requestBodyUriSpecMock).uri(testUrl);
            verify(requestBodySpecMock).bodyValue(userWeb);
            verify(requestHeadersSpecMock).retrieve();
            verify(responseSpecMock).bodyToMono(String.class);
        });

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

        service.verifyUserInOtherMicroservice(userWeb, testUrl);

        await().atMost(ofSeconds(5)).untilAsserted(() -> {
            verify(webClientMock).put();
            verify(requestBodyUriSpecMock).uri(testUrl);
            verify(requestBodySpecMock).bodyValue(userWeb);
            verify(requestHeadersSpecMock).retrieve();
            verify(responseSpecMock).bodyToMono(String.class);
        });

    }

    @Test
    void whenDeleteUserInAnotherServiceSuccessShouldDelete(){
        String testUrl = "http://localhost:8081/tes";

        when(webClientMock.delete()).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.uri(anyString())).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(String.class)).thenReturn(Mono.just("response"));

        service.deleteUserInOtherMicroservice(testUrl);

        await().atMost(ofSeconds(5)).untilAsserted(() -> {
            verify(webClientMock).delete();
            verify(requestHeadersUriSpecMock).uri(testUrl);
            verify(requestHeadersSpecMock).retrieve();
            verify(responseSpecMock).bodyToMono(String.class);
        });
    }

}
