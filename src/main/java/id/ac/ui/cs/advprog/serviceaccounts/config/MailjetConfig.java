package id.ac.ui.cs.advprog.serviceaccounts.config;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailjetConfig {
    @Bean
    public MailjetClient mailjetClient() {
        ClientOptions options = ClientOptions.builder()
                .apiKey(System.getenv("MAILJET_API_KEY"))
                .apiSecretKey(System.getenv("MAILJET_SECRET_KEY")).build();
        return new MailjetClient(options);
    }
}