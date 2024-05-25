package id.ac.ui.cs.advprog.serviceaccounts.controller.profile;

import id.ac.ui.cs.advprog.serviceaccounts.controller.EditProfileController;
import id.ac.ui.cs.advprog.serviceaccounts.dto.EditProfileRequest;
import id.ac.ui.cs.advprog.serviceaccounts.model.User;
import id.ac.ui.cs.advprog.serviceaccounts.service.profile.EditProfileService;
import id.ac.ui.cs.advprog.serviceaccounts.utils.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EditProfileControllerTest {

    @InjectMocks
    private EditProfileController editProfileController;

    @Mock
    private EditProfileService editProfileService;

    private User testUser;

    @BeforeEach
    public void setup() {
        // Arrange
         testUser = User.builder()
                .id(UUID.fromString("a0e0b757-d3ec-4324-a8f8-2771b0522ddf"))
                .name("Carlene")
                .email("carlene@gmail.com")
                .username("Carlene")
                .password("test123")
                .isVerified(true)
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testGetProfile() {
        ResponseEntity<User> responseEntity = editProfileController.getProfile();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(testUser, responseEntity.getBody());
    }

    @Test
    void testGetCurrentUser() {
        User testUser = User.builder()
                .id(UUID.fromString("a0e0b757-d3ec-4324-a8f8-2771b0522ddf"))
                .name("Carlene")
                .email("carlene@gmail.com")
                .username("Carlene")
                .password("test123")
                .isVerified(true)
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        User currentUser = Utils.getCurrentUser();

        assertEquals(testUser, currentUser);
    }

    @Test
    void testEditProfile() {
        // Arrange
        EditProfileRequest request = new EditProfileRequest();
        request.setEmail("new_email@test.com");
        request.setName("New Name");
        request.setUsername("New Username");

        when(editProfileService.editUserProfile(any(User.class), any(String.class), any(String.class), any(String.class)))
                .thenThrow(new RuntimeException("Some exception"));

        // Act
        ResponseEntity<String> responseEntity = editProfileController.editProfile(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        verify(editProfileService).editUserProfile(any(User.class), any(String.class), any(String.class), any(String.class));
    }

    @Test
    void testEditProfile_Success() {
        // Arrange
        EditProfileRequest request = new EditProfileRequest();
        request.setEmail("new_email@test.com");
        request.setName("New Name");
        request.setUsername("New Username");

        ResponseEntity<String> expectedResponse = ResponseEntity.ok("Profile updated");

        when(editProfileService.editUserProfile(any(User.class), any(String.class), any(String.class), any(String.class)))
                .thenReturn(expectedResponse);

        // Act
        ResponseEntity<String> responseEntity = editProfileController.editProfile(request);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Profile updated", responseEntity.getBody());
        verify(editProfileService).editUserProfile(any(User.class), any(String.class), any(String.class), any(String.class));
    }


    @Test
    void testConfirmEmailChange() {
        String userId = "user123";
        String token = "email_change_token";
        ResponseEntity<String> response = editProfileController.confirmEmailChange(userId, token);
        assertEquals("Email change confirmed", response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(editProfileService).confirmEmailChange(userId, token);
    }

    @Test
    void testConfirmEmailChangeWithException() {
        doThrow(new RuntimeException("Some exception")).when(editProfileService).confirmEmailChange(any(String.class), any(String.class));
        assertThrows(RuntimeException.class, () -> editProfileController.confirmEmailChange("user123", "email_change_token"));
    }


}
