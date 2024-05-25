package id.ac.ui.cs.advprog.serviceaccounts.controller;

import id.ac.ui.cs.advprog.serviceaccounts.dto.EditProfileRequest;
import id.ac.ui.cs.advprog.serviceaccounts.model.User;
import id.ac.ui.cs.advprog.serviceaccounts.service.profile.EditProfileService;
import id.ac.ui.cs.advprog.serviceaccounts.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(path = "api/v1/account/profile")

public class EditProfileController {

    @Autowired
    private EditProfileService editProfileService;

    @GetMapping("/")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT','TEACHER')")
    public ResponseEntity<User> getProfile() {
        User currentUser = Utils.getCurrentUser();
        return ResponseEntity.ok(currentUser);
    }


    @PostMapping("/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT','TEACHER')")
    public ResponseEntity<String> editProfile(@RequestBody EditProfileRequest request) {
        try {
            User currentUser = Utils.getCurrentUser();
            return editProfileService.editUserProfile(currentUser, request.getEmail(), request.getUsername(), request.getName());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("confirm-email-change")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT','TEACHER')")
    public ResponseEntity<String> confirmEmailChange(@RequestParam String userId, @RequestParam String token) {
        editProfileService.confirmEmailChange(userId, token);
        return ResponseEntity.ok("Email change confirmed");
    }

}
