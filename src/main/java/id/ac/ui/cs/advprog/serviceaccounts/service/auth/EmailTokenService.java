package id.ac.ui.cs.advprog.serviceaccounts.service.auth;

import id.ac.ui.cs.advprog.serviceaccounts.model.EmailTokenType;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface EmailTokenService {
    String generateTokenUrl(UUID userId, EmailTokenType confirmEmail);
    boolean isValidToken(String userId, String token, EmailTokenType confirmEmail);
    void expireToken(String userId, String token, EmailTokenType tokenType);
}
