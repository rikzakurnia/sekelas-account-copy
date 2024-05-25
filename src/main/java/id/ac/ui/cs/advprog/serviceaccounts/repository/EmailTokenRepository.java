package id.ac.ui.cs.advprog.serviceaccounts.repository;

import id.ac.ui.cs.advprog.serviceaccounts.model.EmailToken;
import id.ac.ui.cs.advprog.serviceaccounts.model.EmailTokenType;
import id.ac.ui.cs.advprog.serviceaccounts.model.User;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmailTokenRepository extends JpaRepository<EmailToken, UUID> {
    @NonNull
    Optional<EmailToken> findByUserAndTokenAndType(@NonNull User user, @NonNull UUID token, @NonNull EmailTokenType type);
}
