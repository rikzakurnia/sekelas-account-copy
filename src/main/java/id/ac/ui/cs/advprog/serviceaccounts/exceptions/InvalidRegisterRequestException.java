package id.ac.ui.cs.advprog.serviceaccounts.exceptions;

public class InvalidRegisterRequestException extends RuntimeException{

    public InvalidRegisterRequestException(String field){super("field '"+ field + "' is required");}
}
