package com.example.exception;

public class CurrencyExchangeException extends RuntimeException{
    private final int statusCode;

    public CurrencyExchangeException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode(){
        return statusCode;
    }
}
