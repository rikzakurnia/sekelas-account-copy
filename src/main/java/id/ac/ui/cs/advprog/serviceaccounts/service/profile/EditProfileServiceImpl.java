package id.ac.ui.cs.advprog.serviceaccounts.service.profile;

import id.ac.ui.cs.advprog.serviceaccounts.exceptions.*;
import id.ac.ui.cs.advprog.serviceaccounts.model.EmailTokenType;
import id.ac.ui.cs.advprog.serviceaccounts.model.User;
import id.ac.ui.cs.advprog.serviceaccounts.repository.UserRepository;
import id.ac.ui.cs.advprog.serviceaccounts.service.auth.EmailTokenService;
import id.ac.ui.cs.advprog.serviceaccounts.service.auth.MailSenderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class EditProfileServiceImpl implements EditProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailSenderServiceImpl mailSenderService;

    @Autowired
    private EmailTokenService emailTokenService;

    @Autowired
    private WebClient webClient;

    private static final String USER_EDIT_ENDPOINT = "/user/edit";

    @Override
    public ResponseEntity<String> editUserProfile(User currentProfile, String email, String username, String name) {

        if(email.equals("")|| username.equals("")|| name.equals("")){
            throw new InvalidInputException();
        }

        currentProfile.setName(name);

        if(!currentProfile.getUsername().equals(username)){
            if(userRepository.usernameExists(username)){
                throw new UsernameAlreadyExistException(username);
            } else {
                currentProfile.setUsername(username);
            }
        }

        if(!email.equals(currentProfile.getEmail())){
            var checkUser = userRepository.findByEmail(email).orElse(null);
            if(checkUser != null && !checkUser.getId().equals(currentProfile.getId())) {
                throw new EmailAlreadyExistException(email);
            }

            String tokenUrl = emailTokenService.generateTokenUrl(currentProfile.getId(), EmailTokenType.CHANGE_EMAIL);

            var subject = "Confirm Your Email Change";
            var body = "Click the following link to confirm your email change: " + tokenUrl;

            // Send confirmation email
            CompletableFuture.runAsync(() -> mailSenderService.sendEmail(email, subject, body));

            currentProfile.setPendingEmail(email);
        }

        var updatedUser = userRepository.save(currentProfile);
        updateUserInOtherMicroservice(updatedUser, System.getenv("MINIQUIZ_API_URL") + USER_EDIT_ENDPOINT);
        updateUserInOtherMicroservice(updatedUser, System.getenv("KELAS_API_URL") + USER_EDIT_ENDPOINT);

        return ResponseEntity.ok("Profile updated");
    }


    public void confirmEmailChange(String userId, String token) {
        if (!emailTokenService.isValidToken(userId, token, EmailTokenType.CHANGE_EMAIL)) {
            throw new EmailTokenIsInvalidException();
        }

        Optional<User> optionalUser = userRepository.findById(UUID.fromString(userId));

        if (!optionalUser.isPresent()) {
            throw new UserDoesNotExistException(userId);
        }

        User user = optionalUser.get();

        user.setEmail(user.getPendingEmail());
        user.setPendingEmail(null);
        emailTokenService.expireToken(userId, token, EmailTokenType.CHANGE_EMAIL);
        userRepository.save(user);

        updateUserInOtherMicroservice(user, System.getenv("MINIQUIZ_API_URL") + USER_EDIT_ENDPOINT);
        updateUserInOtherMicroservice(user, System.getenv("KELAS_API_URL") + USER_EDIT_ENDPOINT);

    }

    public void updateUserInOtherMicroservice(User updatedUser, String url) {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            return webClient.put()
                    .uri(url)
                    .bodyValue(updatedUser)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        });

        future.thenAccept(response -> {});
    }
}
