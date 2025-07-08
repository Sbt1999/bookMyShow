package com.example.bookMyShow.exception;

public class ShowSeatNotFoundException extends RuntimeException {
    public ShowSeatNotFoundException() {
    }
    public ShowSeatNotFoundException(String message){
        super(message);
    }
}
