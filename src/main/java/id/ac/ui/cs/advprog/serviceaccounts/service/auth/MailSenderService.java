package id.ac.ui.cs.advprog.serviceaccounts.service.auth;

import org.springframework.stereotype.Service;

@Service
public interface MailSenderService {
    void sendEmail(String recipient, String subject, String body);
}
