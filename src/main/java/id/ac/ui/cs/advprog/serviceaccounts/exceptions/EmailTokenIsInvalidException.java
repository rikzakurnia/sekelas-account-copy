package id.ac.ui.cs.advprog.serviceaccounts.exceptions;

public class EmailTokenIsInvalidException extends RuntimeException {
    public EmailTokenIsInvalidException() {
        super("Email token is invalid");
    }
}
