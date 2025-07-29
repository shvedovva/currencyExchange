package com.example.exception;

public class ExchangeRateNotFoundException extends CurrencyExchangeException{

    public ExchangeRateNotFoundException(String message) {
        super(message, 404);
    }
}
