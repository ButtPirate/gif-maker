package ru.bpirate.exceptions;

public class FileException extends Throwable {
    public FileException(String message, Throwable cause) {
        //super(message, cause);
        System.out.println(message);
    }

}
