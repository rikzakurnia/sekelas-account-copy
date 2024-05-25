package id.ac.ui.cs.advprog.serviceaccounts.exceptions;

public class InvalidEmailException extends RuntimeException{
    public InvalidEmailException(String email) {
        super(email + " is not a valid email format");
    }
}
