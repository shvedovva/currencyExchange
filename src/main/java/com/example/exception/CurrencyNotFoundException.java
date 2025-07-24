package com.example.exception;

public class CurrencyNotFoundException extends CurrencyExchangeException{
    public CurrencyNotFoundException(String message){
        super(message, 404);
    }
}
