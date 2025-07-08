package com.example.bookMyShow.exception;

public class AuditoriumNotFoundException extends RuntimeException {
    public AuditoriumNotFoundException() {
    }

    public AuditoriumNotFoundException(String message){
        super(message);
    }
}
