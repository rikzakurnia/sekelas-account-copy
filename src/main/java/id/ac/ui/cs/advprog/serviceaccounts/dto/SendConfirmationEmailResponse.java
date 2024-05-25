package id.ac.ui.cs.advprog.serviceaccounts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendConfirmationEmailResponse {
    private String message;
}
