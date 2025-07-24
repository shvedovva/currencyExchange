package com.example.exception;

public class DuplicateResourceException extends CurrencyExchangeException{

    public DuplicateResourceException(String message) {
        super(message, 409);
    }
}
