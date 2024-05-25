package id.ac.ui.cs.advprog.serviceaccounts.service.auth;

import id.ac.ui.cs.advprog.serviceaccounts.exceptions.EmailTokenIsInvalidException;
import id.ac.ui.cs.advprog.serviceaccounts.exceptions.UserDoesNotExistException;
import id.ac.ui.cs.advprog.serviceaccounts.model.EmailToken;
import id.ac.ui.cs.advprog.serviceaccounts.model.EmailTokenType;
import id.ac.ui.cs.advprog.serviceaccounts.model.User;
import id.ac.ui.cs.advprog.serviceaccounts.repository.EmailTokenRepository;
import id.ac.ui.cs.advprog.serviceaccounts.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static java.time.temporal.ChronoUnit.MINUTES;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailTokenServiceImpl implements EmailTokenService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailTokenRepository emailTokenRepository;

    public String generateTokenUrl(UUID userId, EmailTokenType emailTokenType) {
        String randomToken;
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            var user = optionalUser.get();
            randomToken = UUID.randomUUID().toString();
            var emailTokenObject = EmailToken
                    .builder()
                    .token(UUID.fromString(randomToken))
                    .user(user)
                    .type(emailTokenType)
                    .issuedAt(LocalDateTime.now())
                    .isExpired(false)
                    .build();

            emailTokenRepository.save(emailTokenObject);

            var emailTokenEndpoint = emailTokenType.name().toLowerCase().replace("_", "-");

            if(emailTokenType == EmailTokenType.CHANGE_EMAIL){
                emailTokenEndpoint = "confirm-email-change";
            }

            return String.format("%s/%s?userId=%s&token=%s",
                    System.getenv("SEKELAS_WEB_URL"),
                    emailTokenEndpoint,
                    userId,
                    randomToken
            );
        }
        throw new UserDoesNotExistException(userId.toString());
    }

    public boolean isValidToken(String userId, String token, EmailTokenType emailTokenType) {
        int expiryInMinutes = 15;
        Optional<User> user = userRepository.findById(UUID.fromString(userId));
        if (user.isPresent()) {
            Optional<EmailToken> emailToken = emailTokenRepository.findByUserAndTokenAndType(
                    user.get(),
                    UUID.fromString(token),
                    emailTokenType
            );

            if (emailToken.isPresent()) {
                LocalDateTime tokenIssueDate = emailToken.get().getIssuedAt();
                return Math.abs(MINUTES.between(LocalDateTime.now(), tokenIssueDate)) <= expiryInMinutes;
            }
        }
        return false;
    }

    public void expireToken(String userId, String token, EmailTokenType tokenType) {
        Optional<User> user = userRepository.findById(UUID.fromString(userId));
        if (user.isPresent()) {
            Optional<EmailToken> emailToken = emailTokenRepository.findByUserAndTokenAndType(
                    user.get(),
                    UUID.fromString(token),
                    tokenType
            );

            if (emailToken.isPresent()) {
                var emailTokenObject = emailToken.get();
                emailTokenObject.setExpired(true);
                emailTokenRepository.save(emailTokenObject);
            } else {
                throw new EmailTokenIsInvalidException();
            }
        } else {
            throw new UserDoesNotExistException(userId);
        }
    }
}
