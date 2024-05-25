package id.ac.ui.cs.advprog.serviceaccounts.dto;

import id.ac.ui.cs.advprog.serviceaccounts.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String message;
    private String token;
    private User user;
}
