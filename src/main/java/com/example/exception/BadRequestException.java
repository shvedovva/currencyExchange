package com.example.exception;

public class BadRequestException extends CurrencyExchangeException{
    public BadRequestException(String message) {
        super(message, 400);
    }
}
