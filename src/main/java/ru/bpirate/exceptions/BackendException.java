package ru.bpirate.exceptions;

public class BackendException extends Throwable {
    public BackendException(String message, Throwable cause) {
        //super(message, cause);
        System.out.println(message);
    }
}
