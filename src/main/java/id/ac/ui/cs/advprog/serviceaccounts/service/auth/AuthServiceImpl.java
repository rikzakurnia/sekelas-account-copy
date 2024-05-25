package id.ac.ui.cs.advprog.serviceaccounts.service.auth;

import id.ac.ui.cs.advprog.serviceaccounts.dto.LoginRequest;
import id.ac.ui.cs.advprog.serviceaccounts.dto.RegisterRequest;
import id.ac.ui.cs.advprog.serviceaccounts.dto.LoginResponse;
import id.ac.ui.cs.advprog.serviceaccounts.exceptions.*;
import id.ac.ui.cs.advprog.serviceaccounts.model.EmailTokenType;
import id.ac.ui.cs.advprog.serviceaccounts.model.Role;
import id.ac.ui.cs.advprog.serviceaccounts.model.User;
import id.ac.ui.cs.advprog.serviceaccounts.repository.UserRepository;
import id.ac.ui.cs.advprog.serviceaccounts.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private EmailTokenService emailTokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailSenderService mailSenderService;

    @Autowired
    private WebClient webClient;

    private static final String MINIQUIZ_API_URL = System.getenv("MINIQUIZ_API_URL");
    private static final String KELAS_API_URL = System.getenv("KELAS_API_URL");

    public synchronized LoginResponse register(RegisterRequest request) {
        String email = request.getEmail();

        if(!isEmail(email)){
            throw new InvalidEmailException(email);
        }

        validateRegisterRequest(request);

        var checkUserByEmail = userRepository.findByEmail(request.getEmail()).orElse(null);
        var checkUserByUname = userRepository.findByUsername(request.getUsername()).orElse(null);

        if(checkUserByEmail != null ) {
            throw new EmailAlreadyExistException(request.getEmail());
        }
        if(checkUserByUname != null){
            throw new UsernameAlreadyExistException(request.getUsername());
        }
        String roles = request.getRole().toUpperCase();

        var user = User.builder()
                .name(request.getName())
                .username(request.getUsername())
                .active(true)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(checkRoles(roles))
                .build();

        var finalUser = userRepository.save(user);
        var jwtToken = jwtService.generateToken(finalUser);
        sendConfirmationEmail(finalUser);
        addUserInOtherMicroservice(finalUser, MINIQUIZ_API_URL + "/user/add");
        addUserInOtherMicroservice(finalUser, KELAS_API_URL + "/user/add");

        return LoginResponse.builder().token(jwtToken).message("Email verifikasi sedang dikirimkan kepadamu. Harap memeriksa emailmu.").build();
    }

    private void sendConfirmationEmail(User user) {
        CompletableFuture.supplyAsync(() -> {
            String tokenUrl = emailTokenService.generateTokenUrl(user.getId(), EmailTokenType.CONFIRM_EMAIL);
            var subject = "SeKelas | Confirmation Email";
            var body = "Thanks for signing up to SeKelas. To confirm your email, kindly click on this link: " + tokenUrl;
            mailSenderService.sendEmail(user.getEmail(), subject, body);
            return null;
        });
    }

    public LoginResponse authenticate(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmailOrUsername(),
                        request.getPassword()
                )
        );

        var user = getUserForLogin(request.getEmailOrUsername());
        var jwtToken = jwtService.generateToken(user);
        return LoginResponse.builder().
                token(jwtToken).
                user(user).
                build();
    }

    public void confirmEmail(String userId, String token) {
        if (!emailTokenService.isValidToken(userId, token, EmailTokenType.CONFIRM_EMAIL)) {
            throw new EmailTokenIsInvalidException();
        }
        emailTokenService.expireToken(userId, token, EmailTokenType.CONFIRM_EMAIL);
        verifyUser(userId);
    }

    public void verifyUser(String userId) {
        var uuid = UUID.fromString(userId);
        Optional<User> user = userRepository.findById(uuid);
        if (user.isEmpty()) {
            throw new UserDoesNotExistException(userId);
        }
        user.get().setVerified(true);
        userRepository.save(user.get());

        verifyUserInOtherMicroservice(user.get(), MINIQUIZ_API_URL + "/user/verify");
        verifyUserInOtherMicroservice(user.get(), KELAS_API_URL + "/user/verify");

    }

    public boolean isEmail(String email){
        int maxEmailLength = 254;
        if(email == null){
            return false;
        }
        if(email.length()>maxEmailLength){
            return false;
        }
        if(email.isEmpty() || email.isBlank()){
            return false;
        }
        var regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        var pattern = Pattern.compile(regex);
        var matcher = pattern.matcher(email);
        return matcher.matches();
    }
    private boolean isNameValid(String nama) {
        if(nama == null){
            return false;
        }
        return !nama.isEmpty() && !nama.isBlank();
    }

    private boolean isUsernameValid(String username) {
        if(username == null){
            return false;
        }
        return !username.isEmpty() && !username.isBlank();
    }

    private boolean isPasswordValid(String password) {
        if(password == null){
            return false;
        }
        return !password.isEmpty()&& !password.isBlank() && password.length() >= 8;
    }

    private boolean isRoleValid(String role) {
        if(role == null){
            return false;
        }
        String checkedRole = checkRoles(role).getRoleName();
        return !role.isEmpty() && !role.isBlank() && role.equals(checkedRole);
    }


    private void validateRegisterRequest(RegisterRequest request) {
        if (!isNameValid(request.getName())) {
            throw new InvalidRegisterRequestException("Name");
        }
        if (!isUsernameValid(request.getUsername())) {
            throw new InvalidRegisterRequestException("Username");
        }
        if (!isPasswordValid(request.getPassword())) {
            throw new InvalidPasswordRequestException();
        }
        if (!isRoleValid(request.getRole())) {
            throw new InvalidRoleRequestException();
        }
    }

    public User getUserForLogin(String emailOrUname){
        if(isEmail(emailOrUname)){
            return userRepository.findByEmail(emailOrUname).orElseThrow();
        }
        return userRepository.findByUsername(emailOrUname).orElseThrow();
    }

    public Role checkRoles(String roles){
        if(roles.equals("ADMIN")){
            return Role.ADMIN;
        }
        if(roles.equals("STUDENT")){
            return Role.STUDENT;
        }
        return Role.TEACHER;
    }

    public void delete(User currentUser) {
        String username = currentUser.getUsername();
        if(!userRepository.usernameExists(username)){
            throw new UserDoesNotExistException(username);
        }
        userRepository.delete(currentUser);

        deleteUserInOtherMicroservice(KELAS_API_URL + "/delete/" + currentUser.getId());
        deleteUserInOtherMicroservice(MINIQUIZ_API_URL + "/delete/" + currentUser.getId());
    }

    public void addUserInOtherMicroservice(User newUser, String url) {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() ->
            webClient.post()
                    .uri(url)
                    .bodyValue(newUser)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block()
        );

        future.thenAccept(response -> {});
    }

    public void verifyUserInOtherMicroservice(User updatedUser, String url) {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() ->
            webClient.put()
                    .uri(url)
                    .bodyValue(updatedUser)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block()
        );

        future.thenAccept(response -> {});
    }

    public void deleteUserInOtherMicroservice(String url) {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() ->
                webClient.delete()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block()
        );

        future.thenAccept(response -> {});
    }
}
