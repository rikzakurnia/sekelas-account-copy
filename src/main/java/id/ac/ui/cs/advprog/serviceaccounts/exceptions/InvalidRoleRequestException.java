package id.ac.ui.cs.advprog.serviceaccounts.exceptions;

public class InvalidRoleRequestException extends RuntimeException{

    public InvalidRoleRequestException(){super("Role is not valid");}
}
