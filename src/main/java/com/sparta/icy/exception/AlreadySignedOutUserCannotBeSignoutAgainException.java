package com.sparta.icy.exception;

public class AlreadySignedOutUserCannotBeSignoutAgainException extends RuntimeException{
    public AlreadySignedOutUserCannotBeSignoutAgainException(String message){
        super(message);
    }
}
