package id.ac.ui.cs.advprog.serviceaccounts.service;

import id.ac.ui.cs.advprog.serviceaccounts.dto.*;
import id.ac.ui.cs.advprog.serviceaccounts.exceptions.EmailTokenIsInvalidException;
import id.ac.ui.cs.advprog.serviceaccounts.exceptions.InvalidPasswordRequestException;
import id.ac.ui.cs.advprog.serviceaccounts.exceptions.UserDoesNotExistException;
import id.ac.ui.cs.advprog.serviceaccounts.model.EmailTokenType;
import id.ac.ui.cs.advprog.serviceaccounts.model.User;
import id.ac.ui.cs.advprog.serviceaccounts.repository.EmailTokenRepository;
import id.ac.ui.cs.advprog.serviceaccounts.repository.UserRepository;
import id.ac.ui.cs.advprog.serviceaccounts.service.auth.AuthServiceImpl;
import id.ac.ui.cs.advprog.serviceaccounts.service.auth.EmailTokenService;
import id.ac.ui.cs.advprog.serviceaccounts.service.auth.EmailTokenServiceImpl;
import id.ac.ui.cs.advprog.serviceaccounts.service.auth.MailSenderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ResetPasswordServiceImpl {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailTokenServiceImpl emailTokenService;

    @Autowired
    private MailSenderServiceImpl mailSenderService;

    @Autowired
    private EmailTokenRepository emailTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void sendResetPassword(User user) {
        String tokenUrl = emailTokenService.generateTokenUrl(user.getId(), EmailTokenType.RESET_PASSWORD);
        var subject = "SeKelas | Reset Password";
        var body = "Tap the following link to reset your account password " + tokenUrl + " \nIf you didn't request a new password, you can safely delete this email";
        mailSenderService.sendEmail(user.getEmail(), subject, body);
    }

    public void confirmResetPasswordRequest(String userId, String token) {
        if (!emailTokenService.isValidToken(userId, token, EmailTokenType.RESET_PASSWORD)) {
            throw new EmailTokenIsInvalidException();
        }
    }

    public void saveNewPassword(ResetPasswordRequest request) {
        Optional<User> user = userRepository.findById(UUID.fromString(request.getUserId()));
        if (user.isEmpty()) {
            throw new UserDoesNotExistException(request.getUserId());
        }
        confirmResetPasswordRequest(request.getUserId(), request.getToken());

        if (!validatePassword(request.getNewPassword())) {
            throw new InvalidPasswordRequestException();
        }
        user.get().setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user.get());
    }

    private boolean validatePassword(String password){
        if (password==null){
            return false;
        }
        return !(password.isEmpty()) && !(password.isBlank()) && (password.length()>=8);
    }
}
