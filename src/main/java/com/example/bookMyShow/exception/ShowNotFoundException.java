package com.example.bookMyShow.exception;

public class ShowNotFoundException extends RuntimeException {
    public ShowNotFoundException() {
    }

    public ShowNotFoundException(String message){
        super(message);
    }
}
