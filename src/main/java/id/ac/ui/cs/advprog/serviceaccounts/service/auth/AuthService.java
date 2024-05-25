package id.ac.ui.cs.advprog.serviceaccounts.service.auth;

import id.ac.ui.cs.advprog.serviceaccounts.dto.LoginRequest;
import id.ac.ui.cs.advprog.serviceaccounts.dto.LoginResponse;
import id.ac.ui.cs.advprog.serviceaccounts.dto.RegisterRequest;

import id.ac.ui.cs.advprog.serviceaccounts.model.Role;
import id.ac.ui.cs.advprog.serviceaccounts.model.User;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    void confirmEmail(String userId, String token);

    void verifyUser(String userId);
    LoginResponse register(RegisterRequest request);

    LoginResponse authenticate(LoginRequest request);

    Role checkRoles(String roles);

    boolean isEmail(String email);

    void delete(User currentUser);

    void addUserInOtherMicroservice(User newUser, String url);

    void verifyUserInOtherMicroservice(User updatedUser, String url);

    void deleteUserInOtherMicroservice(String url);
}
