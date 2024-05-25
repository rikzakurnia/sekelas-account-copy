package id.ac.ui.cs.advprog.serviceaccounts.service.mail_sender;

import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.resource.Emailv31;
import id.ac.ui.cs.advprog.serviceaccounts.exceptions.SendEmailFailedException;
import id.ac.ui.cs.advprog.serviceaccounts.service.auth.MailSenderService;
import id.ac.ui.cs.advprog.serviceaccounts.service.auth.MailSenderServiceImpl;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailSenderServiceImplTest {
    @InjectMocks
    private MailSenderService service = new MailSenderServiceImpl();

    @Mock
    private MailjetClient mailjetClient;

    @Test
    void whenInvokedShouldSendEmail() throws MailjetException {
        String recipient = "test@test.com";
        String subject = "subject";
        String body = "body";

        MailjetResponse response = new MailjetResponse(200, "{}");
        when(mailjetClient.post(any(MailjetRequest.class))).thenReturn(response);
        service.sendEmail(recipient, subject, body);
        verify(mailjetClient).post(any(MailjetRequest.class));
    }

    @Test
    void whenSendEmailErrorShouldThrowException() throws MailjetException {
        String recipient = "test@test.com";
        String subject = "subject";
        String body = "body";
        when(mailjetClient.post(any(MailjetRequest.class))).thenThrow(MailjetException.class);
        Assertions.assertThrows(SendEmailFailedException.class, () -> {
            service.sendEmail(recipient, subject, body);
        });
    }
}
