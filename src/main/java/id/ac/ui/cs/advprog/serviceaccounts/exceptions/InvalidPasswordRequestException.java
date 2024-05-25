package id.ac.ui.cs.advprog.serviceaccounts.exceptions;

public class InvalidPasswordRequestException extends RuntimeException{

    public InvalidPasswordRequestException(){super("Password can not be blank and must consist of at least 8 character");}
}