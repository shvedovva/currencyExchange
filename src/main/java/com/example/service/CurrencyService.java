package com.example.service;

import com.example.dao.CurrencyDao;
import com.example.dto.CurrencyDto;
import com.example.exception.BadRequestException;
import com.example.exception.CurrencyNotFoundException;
import com.example.model.Currency;
import com.example.util.CurrencyValidator;


import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CurrencyService {
    private final CurrencyDao currencyDao;

    public CurrencyService() {
        this.currencyDao = new CurrencyDao();
    }

    public List<CurrencyDto> getAllCurrencies(){
        return currencyDao.getAllCurrencies().stream()
                .map(this::mapCurrencyToDto)
                .collect(Collectors.toList());
    }

    public CurrencyDto getCurrencyByCode(String code){
        if (code==null|| code.isEmpty()){
            throw new BadRequestException("Код валюты не может быть пустым");
        }

        return currencyDao.getCurrencyByCode(code)
                .map(this::mapCurrencyToDto)
                .orElseThrow(() -> new CurrencyNotFoundException("Валюта с кодом " + code + " не найдена"));
    }

    public CurrencyDto addCurrency(String name, String code, String sign){
        if (name == null || name.trim().isEmpty()){
            throw new BadRequestException("Имя валюты не может быть пустым");
        }
        if (code == null || code.trim().isEmpty()){
            throw new BadRequestException("Код валюты не может быть пустым");
        }
        if (sign == null || sign.trim().isEmpty()){
            throw new BadRequestException("Знак валюты не может быть пустым");
        }
        if (!CurrencyValidator.isValidCurrencyCode(code)){
            throw new BadRequestException("Недопустимый код валюты: " + code);
        }
        Currency currency = new Currency();
        currency.setFullName(name);
        currency.setCode(code.toUpperCase());
        currency.setSign(sign);

        Currency addedCurrency = currencyDao.addCurrency(currency);
        return mapCurrencyToDto(addedCurrency);
    }

    private CurrencyDto mapCurrencyToDto(Currency currency) {
        CurrencyDto dto = new CurrencyDto();
        dto.setId(currency.getId());
        dto.setName(currency.getFullName());
        dto.setCode(currency.getCode());
        dto.setSign(currency.getSign());
        return dto;
    }
}
