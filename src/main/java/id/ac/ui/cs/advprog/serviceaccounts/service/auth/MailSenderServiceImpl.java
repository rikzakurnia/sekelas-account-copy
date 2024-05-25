package id.ac.ui.cs.advprog.serviceaccounts.service.auth;

import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.resource.Emailv31;
import id.ac.ui.cs.advprog.serviceaccounts.exceptions.SendEmailFailedException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailSenderServiceImpl implements MailSenderService {

    @Autowired
    MailjetClient mailjetClient;

    @Override
    public void sendEmail(String recipient, String subject, String body) {
         MailjetRequest request = new MailjetRequest(Emailv31.resource)
                    .property(Emailv31.MESSAGES, new JSONArray()
                            .put(new JSONObject()
                                    .put(Emailv31.Message.FROM, new JSONObject()
                                            .put("Email", System.getenv("SEKELAS_MAIL_ACCOUNT"))
                                            .put("Name", "SeKelas"))
                                    .put(Emailv31.Message.TO, new JSONArray()
                                            .put(new JSONObject()
                                                    .put("Email", recipient)))
                                    .put(Emailv31.Message.SUBJECT, subject)
                                    .put(Emailv31.Message.TEXTPART, body)));
        try {
            mailjetClient.post(request);
        } catch (MailjetException e) {
            throw new SendEmailFailedException();
        }
    }
}
