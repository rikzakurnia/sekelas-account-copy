package id.ac.ui.cs.advprog.serviceaccounts.service.jwt;

import id.ac.ui.cs.advprog.serviceaccounts.model.User;
import id.ac.ui.cs.advprog.serviceaccounts.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.Key;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
    @InjectMocks
    private JwtService jwtService = new JwtService();
    @Mock
    private PasswordEncoder passwordEncoder;

    private static final String USERNAME = "testuser";
    private static final String TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY4MzYxMDExNCwiZXhwIjoxNzE1MjU4OTA2fQ.wjvfVNLZSGjpJYnE68kDjXcIY9x_Cw71b-WYpl9KIug";
    private static final String INVALIDTOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJSaWtrdTEyMzQiLCJpYXQiOjE2ODM2MTAxMTQsImV4cCI6MTcxNTI1ODkwNn0.05gaQlBKpGyx3FUEreLQnFy88UcUK2KFEs92wHgXRCQ";

    private static final String EXPIREDTOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY4MzYxMDExNCwiZXhwIjoxNjgzNjEwMTc0fQ.bONIeQyn16FqN54Kw2NhZEY7-PH1GoEFn9SAB7JfMmk";

    @Test
    void extractUsername() {
        String username = jwtService.extractUsername(TOKEN);
        Assertions.assertEquals(USERNAME, username);
    }
    @Test
    void generateToken() {
        UserDetails userDetails = User.builder()
                .username(USERNAME)
                .password(passwordEncoder.encode("password"))
                .email("test@gmail.com")
                .active(true)
                .build();
        String token = jwtService.generateToken(userDetails);
        Assertions.assertNotNull(token);
    }

    @Test
    void whenTokenValidShouldReturnTrue() {
        UserDetails userDetails = User.builder()
                .username(USERNAME)
                .password(passwordEncoder.encode("password"))
                .email("test@gmail.com")
                .active(true)
                .build();
        boolean isValid = jwtService.isTokenValid(TOKEN, userDetails);
        Assertions.assertTrue(isValid);
    }

    @Test
    void whenTokenInvalidShouldReturnFalse() {
        UserDetails userDetails = User.builder()
                .username(USERNAME)
                .password(passwordEncoder.encode("password"))
                .email("test@gmail.com")
                .active(true)
                .build();
        boolean isValid = jwtService.isTokenValid(INVALIDTOKEN, userDetails);
        Assertions.assertFalse(isValid);
    }

    @Test
    void whenTokenExpiredShouldThrowExpiredException() {
        UserDetails userDetails = User.builder()
                .username(USERNAME)
                .password(passwordEncoder.encode("password"))
                .email("test@gmail.com")
                .active(true)
                .build();
        Assertions.assertThrows(ExpiredJwtException.class, () -> jwtService.isTokenValid(EXPIREDTOKEN, userDetails));
    }

    @Test
    void extractClaim() {
        String subject = jwtService.extractClaim(TOKEN, Claims::getSubject);
        Assertions.assertEquals(USERNAME, subject);
    }

    @Test
    void extractAllClaims() {
        Claims claims = jwtService.extractAllClaims(TOKEN);
        Assertions.assertNotNull(claims);
    }

    @Test
    void getSignInKey() {
        Key key = jwtService.getSignInKey();
        Assertions.assertNotNull(key);
    }
}
