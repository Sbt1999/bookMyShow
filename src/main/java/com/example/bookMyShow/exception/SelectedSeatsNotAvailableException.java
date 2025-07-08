package com.example.bookMyShow.exception;

public class SelectedSeatsNotAvailableException extends Throwable {
    public SelectedSeatsNotAvailableException() {
    }

    public SelectedSeatsNotAvailableException(String message) {
        super(message);
    }
}
