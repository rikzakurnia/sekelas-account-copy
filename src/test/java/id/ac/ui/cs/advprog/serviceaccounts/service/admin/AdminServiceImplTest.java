package id.ac.ui.cs.advprog.serviceaccounts.service.admin;

import id.ac.ui.cs.advprog.serviceaccounts.exceptions.UserAlreadyDeactivatedException;
import id.ac.ui.cs.advprog.serviceaccounts.exceptions.UserDoesNotExistException;
import id.ac.ui.cs.advprog.serviceaccounts.model.User;
import id.ac.ui.cs.advprog.serviceaccounts.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.time.Duration.ofSeconds;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {
    @InjectMocks
    private AdminService service = new AdminServiceImpl();

    @Mock
    private UserRepository userRepository;
    @Mock
    private WebClient webClientMock;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpecMock;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpecMock;

    @Mock
    private WebClient.RequestBodySpec requestBodySpecMock;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpecMock;

    @Mock
    private WebClient.ResponseSpec responseSpecMock;

    User user;

    @BeforeEach
    void setUp() {
        user = User.builder().
                id(UUID.fromString("a0e0b757-d3ec-4324-a8f8-2771b0522ddf")).
                name("Gib").
                email("gib@gmail.com").
                username("gib").
                password("test123").
                isVerified(false).
                build();
    }

    @Test
    void testGetAllUsers() {
        List<User> users = List.of(user);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = service.getAllUsers();

        verify(userRepository).findAll();
        assertEquals(users, result);
    }

    @Test
    void testDeactivateUser() {
        UUID userId = UUID.fromString("a0e0b757-d3ec-4324-a8f8-2771b0522ddf");

        User deactivatedUser = User.builder()
                .id(userId)
                .name("Gib")
                .email("gib@gmail.com")
                .username("gib")
                .password("test123")
                .isVerified(false)
                .active(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.ofNullable(deactivatedUser));
        when(userRepository.save(any(User.class))).thenReturn(deactivatedUser);

        service.deactivateUser(userId);

        verify(userRepository).findById(userId);
        verify(userRepository).save(deactivatedUser);
    }


    @Test
    void testDeactivateUserNotFound() {
        UUID userId = UUID.fromString("a0e0b757-d3ec-4324-a8f8-2771b0522ddf");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserDoesNotExistException.class, () -> {
            service.deactivateUser(userId);
        });

        verify(userRepository).findById(userId);
    }

    @Test
    void testDeactivateUserAlreadyDisabled() {
        UUID userId = UUID.fromString("a0e0b757-d3ec-4324-a8f8-2771b0522ddf");

        User disabledUser = User.builder()
                .id(userId)
                .name("Gib")
                .email("gib@gmail.com")
                .username("gib")
                .password("test123")
                .isVerified(false)
                .active(false) // user is already disabled
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(disabledUser));

        assertThrows(UserAlreadyDeactivatedException.class, () -> {
            service.deactivateUser(userId);
        });

        verify(userRepository).findById(userId);
    }
    @Test
    void whenDeactivateUserInAnotherServiceSuccessShouldPut(){
        User userWeb = User.builder()
                .username("ruzain")
                .build();

        String testUrl = "http://localhost:8081/tes";

        when(webClientMock.put()).thenReturn(requestBodyUriSpecMock);
        when(requestBodyUriSpecMock.uri(anyString())).thenReturn(requestBodySpecMock);
        when(requestBodySpecMock.bodyValue(userWeb)).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(String.class)).thenReturn(Mono.just("response"));

        service.deactivateUserInOtherMicroservice(userWeb, testUrl);

        await().atMost(ofSeconds(5)).untilAsserted(() -> {
            verify(webClientMock).put();
            verify(requestBodyUriSpecMock).uri(testUrl);
            verify(requestBodySpecMock).bodyValue(userWeb);
            verify(requestHeadersSpecMock).retrieve();
            verify(responseSpecMock).bodyToMono(String.class);
        });

    }
}
