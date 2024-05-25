package id.ac.ui.cs.advprog.serviceaccounts.service.profile;

import id.ac.ui.cs.advprog.serviceaccounts.model.User;
import org.springframework.http.ResponseEntity;

public interface EditProfileService {
    ResponseEntity<String> editUserProfile(User currentProfile, String email, String username, String name);
    void confirmEmailChange(String userId, String token);
    void updateUserInOtherMicroservice(User updatedUser, String url);
}
