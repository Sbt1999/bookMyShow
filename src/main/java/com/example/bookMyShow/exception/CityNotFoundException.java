package com.example.bookMyShow.exception;

public class CityNotFoundException extends RuntimeException {
    public CityNotFoundException() {
    }

    public CityNotFoundException(String message){
        super(message);
    }
}
