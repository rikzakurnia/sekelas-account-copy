package id.ac.ui.cs.advprog.serviceaccounts.utils;

import id.ac.ui.cs.advprog.serviceaccounts.model.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class Utils {
    private Utils() {}

    public static User getCurrentUser() {
        return ((User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal());
    }
}
