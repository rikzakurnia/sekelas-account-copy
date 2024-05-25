package id.ac.ui.cs.advprog.serviceaccounts.controller;

import id.ac.ui.cs.advprog.serviceaccounts.dto.*;
import id.ac.ui.cs.advprog.serviceaccounts.exceptions.UserDoesNotExistException;
import id.ac.ui.cs.advprog.serviceaccounts.model.EmailTokenType;
import id.ac.ui.cs.advprog.serviceaccounts.model.User;
import id.ac.ui.cs.advprog.serviceaccounts.repository.UserRepository;
import id.ac.ui.cs.advprog.serviceaccounts.service.ResetPasswordServiceImpl;
import id.ac.ui.cs.advprog.serviceaccounts.service.auth.AuthServiceImpl;
import id.ac.ui.cs.advprog.serviceaccounts.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private AuthServiceImpl authService;

    @Autowired
    private ResetPasswordServiceImpl resetPasswordService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login (
            @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register (
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authService.register(request));
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT','TEACHER')")
    public ResponseEntity<String> delete(){
        User currentUser = Utils.getCurrentUser();
        authService.delete(currentUser);
        return ResponseEntity.ok("Success");
    }

    @PutMapping("confirm-email")
    public ResponseEntity<String> confirmEmail(@RequestParam String userId, @RequestParam String token) {
        authService.confirmEmail(userId, token);
        return ResponseEntity.ok("Email has been confirmed!");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponse> processResetPassword(@RequestParam String userEmail) {
        Optional<User> user = userRepository.findByEmail(userEmail);
        if (user.isEmpty()){
            throw new UserDoesNotExistException(userEmail);
        }
        resetPasswordService.sendResetPassword(user.get());

        ResetPasswordResponse response = ResetPasswordResponse.builder().message("Email to reset your passsword has been sent!").build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-password")
    public ResponseEntity<ResetPasswordResponse> validateResetPassword(@RequestParam String userId, @RequestParam String token) {
        resetPasswordService.confirmResetPasswordRequest(userId, token);
        ResetPasswordResponse response = ResetPasswordResponse.builder().message("Now you can enter your new password!").build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/save-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        resetPasswordService.saveNewPassword(request);
        ResetPasswordResponse response = ResetPasswordResponse.builder().message("Please do remember your new password!").build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    public ResponseEntity<User> getCurrentUser() {
        User currentUser = Utils.getCurrentUser();
        return ResponseEntity.ok(currentUser);
    }
}


