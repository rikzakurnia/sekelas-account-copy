package id.ac.ui.cs.advprog.serviceaccounts.exceptions;

public class UserDoesNotExistException extends RuntimeException {
    public UserDoesNotExistException(String uuid) {
        super("User with id " + uuid + " does not exist");
    }
}
