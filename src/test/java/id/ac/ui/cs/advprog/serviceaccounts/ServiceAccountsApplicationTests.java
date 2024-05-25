package id.ac.ui.cs.advprog.serviceaccounts;

import id.ac.ui.cs.advprog.serviceaccounts.controller.AuthController;
import id.ac.ui.cs.advprog.serviceaccounts.controller.EditProfileController;
import id.ac.ui.cs.advprog.serviceaccounts.service.profile.EditProfileServiceImpl;
import id.ac.ui.cs.advprog.serviceaccounts.service.auth.AuthServiceImpl;
import id.ac.ui.cs.advprog.serviceaccounts.service.auth.EmailTokenServiceImpl;
import id.ac.ui.cs.advprog.serviceaccounts.service.auth.MailSenderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ServiceAccountsApplicationTests {
    @Mock
    private AuthController authController;

    @Mock
    private EditProfileController editProfileController;

    @Mock
    private AuthServiceImpl authServiceImpl;

    @Mock
    private EmailTokenServiceImpl emailTokenServiceImpl;

    @Mock
    private MailSenderServiceImpl mailSenderServiceImpl;

    @Mock
    private EditProfileServiceImpl editProfileServiceImpl;

    @Test
    void contextLoads() {
        assertThat(authController).isNotNull();
        assertThat(editProfileController).isNotNull();
        assertThat(authServiceImpl).isNotNull();
        assertThat(emailTokenServiceImpl).isNotNull();
        assertThat(mailSenderServiceImpl).isNotNull();
        assertThat(editProfileServiceImpl).isNotNull();
    }

}
