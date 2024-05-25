package id.ac.ui.cs.advprog.serviceaccounts.service.admin;

import id.ac.ui.cs.advprog.serviceaccounts.model.User;
import java.util.List;
import java.util.UUID;

public interface AdminService {
    List<User> getAllUsers();
    void deactivateUser(UUID id);
    void deactivateUserInOtherMicroservice(User updatedUser, String url);
}
