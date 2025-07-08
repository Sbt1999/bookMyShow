package com.example.bookMyShow.exception;

public class TheatreNotFoundException extends RuntimeException {
    public TheatreNotFoundException() {
    }

    public TheatreNotFoundException(String message){
        super(message);
    }
}
