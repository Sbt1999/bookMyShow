package com.example.bookMyShow.exception;

public class PaymentProcessingException extends Exception{
    public PaymentProcessingException(){

    }
    public PaymentProcessingException(String message){
        super(message);
    }
}
