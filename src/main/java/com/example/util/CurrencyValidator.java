package com.example.util;

import com.example.exception.BadRequestException;

import java.util.HashSet;
import java.util.Set;

public class CurrencyValidator {
    private static final Set<String> VALID_CURRENCY_CODES;

    static {
        VALID_CURRENCY_CODES = new HashSet<>();
        VALID_CURRENCY_CODES.add("USD");  // Доллар США
        VALID_CURRENCY_CODES.add("EUR");  // Евро
        VALID_CURRENCY_CODES.add("RUB");  // Российский рубль
        VALID_CURRENCY_CODES.add("GBP");  // Фунт стерлингов
        VALID_CURRENCY_CODES.add("JPY");  // Японская иена
        VALID_CURRENCY_CODES.add("CNY");  // Китайский юань
        VALID_CURRENCY_CODES.add("AUD");  // Австралийский доллар
        VALID_CURRENCY_CODES.add("CAD");  // Канадский доллар
        VALID_CURRENCY_CODES.add("CHF");  // Швейцарский франк
    }

    public static boolean isValidCurrencyCode(String code){
        if(code == null){
            return false;
        }
        return VALID_CURRENCY_CODES.contains(code.toUpperCase());
    }
}
