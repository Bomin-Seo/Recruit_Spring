package com.sparta.icy.exception;

public class DuplicateUsernameException extends RuntimeException {
    public DuplicateUsernameException(String message){
        super(message);
    }
}
