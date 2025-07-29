package com.example.service;

import com.example.dao.ExchangeRateDao;
import com.example.dto.CurrencyDto;
import com.example.dto.ExchangeRateDto;
import com.example.exception.BadRequestException;
import com.example.model.ExchangeRate;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class ExchangeRateService {
    private final ExchangeRateDao exchangeRateDao;

    public ExchangeRateService() {
        this.exchangeRateDao = new ExchangeRateDao();
    }

    public List<ExchangeRateDto> getAllExchangeRates(){
        return exchangeRateDao.getAllExchangeRates().stream()
                .map(this::mapExchangeRateToDto)
                .collect(Collectors.toList());
    }
    public ExchangeRateDto getExchangeRate(String currencyPair) {
        if (currencyPair == null || currencyPair.length() != 6){
            throw new BadRequestException("Неверный формат валютной пары. Должно быть 6 символов! (USDRUB)");
        }

        String baseCode = currencyPair.substring(0, 3);
        String targetCode = currencyPair.substring(3, 6);

        return exchangeRateDao.getExchangeRateByCurrencyCode(baseCode, targetCode)
                .map(this::mapExchangeRateToDto)
                .orElseThrow(()-> new BadRequestException("Обменный курс не найден!"));
    }
    public ExchangeRateDto addExchangeRate(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate){
        if (baseCurrencyCode == null || baseCurrencyCode.isEmpty()){
            throw new BadRequestException("Код базовой валюты не может быть пустым!");
        }
        if (targetCurrencyCode == null || targetCurrencyCode.isEmpty()){
            throw new BadRequestException("Код целевой валюты не может быть пустым!");
        }
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0){
            throw new BadRequestException("Курс должен быть положительным числом!");
        }

        ExchangeRate exchangeRate = exchangeRateDao.addExchangeRate(baseCurrencyCode,targetCurrencyCode, rate);
        return mapExchangeRateToDto(exchangeRate);
    }

    private ExchangeRateDto mapExchangeRateToDto(ExchangeRate exchangeRate) {
        ExchangeRateDto dto = new ExchangeRateDto();
        dto.setId(exchangeRate.getId());

        CurrencyDto baseCurrencyDto = new CurrencyDto();
        baseCurrencyDto.setId(exchangeRate.getBaseCurrency().getId());
        baseCurrencyDto.setName(exchangeRate.getBaseCurrency().getFullName());
        baseCurrencyDto.setCode(exchangeRate.getBaseCurrency().getCode());
        baseCurrencyDto.setSign(exchangeRate.getBaseCurrency().getSign());

        CurrencyDto targetCurrencyDto = new CurrencyDto();
        targetCurrencyDto.setId(exchangeRate.getTargetCurrency().getId());
        targetCurrencyDto.setName(exchangeRate.getTargetCurrency().getFullName());
        targetCurrencyDto.setCode(exchangeRate.getTargetCurrency().getCode());
        targetCurrencyDto.setSign(exchangeRate.getTargetCurrency().getSign());

        dto.setBaseCurrency(baseCurrencyDto);
        dto.setTargetCurrency(targetCurrencyDto);
        dto.setRate(exchangeRate.getRate());

        return dto;
    }
}
