package id.ac.ui.cs.advprog.serviceaccounts.config;

import id.ac.ui.cs.advprog.serviceaccounts.model.Role;
import id.ac.ui.cs.advprog.serviceaccounts.model.User;
import id.ac.ui.cs.advprog.serviceaccounts.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;

import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String JWT_TOKEN = "valid-token";
    private static final String USER_EMAIL = "test@test.com";

    @Test
    void testDoFilterInternalWithValidToken() throws ServletException, IOException {
        when(request.getHeader(anyString())).thenReturn("Bearer" + " " + JWT_TOKEN);

        when(jwtService.extractUsername(JWT_TOKEN)).thenReturn(USER_EMAIL);

        UserDetails userDetails = User.builder()
                .email(USER_EMAIL)
                .role(Role.STUDENT)
                .build();

        when(userDetailsService.loadUserByUsername(USER_EMAIL)).thenReturn(userDetails);
        when(jwtService.isTokenValid(anyString(), any(UserDetails.class))).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService, times(1)).extractUsername(JWT_TOKEN);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalWithInvalidToken() throws ServletException, IOException {
        // Mocking HttpServletRequest and its getHeader method to return an invalid JWT token.
        when(request.getHeader(anyString())).thenReturn("invalid-token");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Verifying that the authentication token was not set.
        verify(jwtService, never()).isTokenValid(anyString(), any(UserDetails.class));
        verify(request, never()).setAttribute(eq("userEmail"), anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalWithNullToken() throws ServletException, IOException {
        when(request.getHeader(anyString())).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService, never()).isTokenValid(anyString(), any(UserDetails.class));
        verify(request, never()).setAttribute(eq("userEmail"), anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
