package id.ac.ui.cs.advprog.serviceaccounts.service;

import id.ac.ui.cs.advprog.serviceaccounts.model.User;

public interface ResetPasswordService {
    void sendResetPassword(User user);
    void confirmResetPasswordRequest(String userId, String token);
    void saveNewPassword(String userId, String token, String newPassword);

}
