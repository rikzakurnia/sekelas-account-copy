package id.ac.ui.cs.advprog.serviceaccounts.exceptions;

public class EmailAlreadyExistException extends RuntimeException{
    public EmailAlreadyExistException(String email) {super("Email " + email + " is already taken");}
}
