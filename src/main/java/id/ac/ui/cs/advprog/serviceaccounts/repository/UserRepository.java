package id.ac.ui.cs.advprog.serviceaccounts.repository;


import id.ac.ui.cs.advprog.serviceaccounts.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,UUID> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findById(UUID id);

    default boolean usernameExists(String username) {
        return findByUsername(username).isPresent();
    }

}
