package id.ac.ui.cs.advprog.serviceaccounts.service.admin;


import id.ac.ui.cs.advprog.serviceaccounts.exceptions.UserAlreadyDeactivatedException;
import id.ac.ui.cs.advprog.serviceaccounts.exceptions.UserDoesNotExistException;
import id.ac.ui.cs.advprog.serviceaccounts.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import id.ac.ui.cs.advprog.serviceaccounts.model.User;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebClient webClient;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void deactivateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserDoesNotExistException("User with id " + id + " does not exist"));
        if (!user.isEnabled()) {
            throw new UserAlreadyDeactivatedException("User with id " + id + " is already deactivated");
        }
        user.disableUser();
        userRepository.save(user);

        deactivateUserInOtherMicroservice(user, System.getenv("MINIQUIZ_API_URL") + "/user/deactivate");
    }

    public void deactivateUserInOtherMicroservice(User updatedUser, String url) {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() ->
             webClient.put()
                    .uri(url)
                    .bodyValue(updatedUser)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block()
        );

        future.thenAccept(response -> {
        });
    }

}
