package com.example.bookMyShow.exception;

public class SeatNotFoundException extends RuntimeException {
    public SeatNotFoundException() {
    }

    public SeatNotFoundException(String message){
        super(message);
    }
}
