package id.ac.ui.cs.advprog.serviceaccounts.exceptions;

public class SendEmailFailedException extends RuntimeException {
    public SendEmailFailedException() {
        super("Email failed to be sent");
    }
}
