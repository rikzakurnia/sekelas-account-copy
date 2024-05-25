package id.ac.ui.cs.advprog.serviceaccounts.exceptions;

public class UsernameAlreadyExistException extends RuntimeException{
    public UsernameAlreadyExistException(String username){
        super("Username " + username + " already exists");
    }
}
