package com.example.service;

import com.example.dao.CurrencyDao;
import com.example.dao.ExchangeRateDao;
import com.example.dto.CurrencyDto;
import com.example.dto.ExchangeDto;
import com.example.exception.BadRequestException;
import com.example.exception.CurrencyNotFoundException;
import com.example.exception.ExchangeRateNotFoundException;
import com.example.model.Currency;
import com.example.model.ExchangeRate;
import com.example.util.CurrencyValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class ExchangeService {
    private final CurrencyDao currencyDao;
    private final ExchangeRateDao exchangeRateDao;

    public ExchangeService() {
        this.currencyDao = new CurrencyDao();
        this.exchangeRateDao = new ExchangeRateDao();
    }

    public ExchangeDto calculateExchange(String fromCurrencyCode, String toCurrencyCode, BigDecimal amount){
        //Валидация входных данных
        if (fromCurrencyCode == null || fromCurrencyCode.trim().isEmpty()){
            throw new BadRequestException("Код исходной валюты не может быть пустым!");
        }
        if (toCurrencyCode == null || toCurrencyCode.trim().isEmpty()){
            throw new BadRequestException("Код целевой валюты не может быть пустым!");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new BadRequestException("Сумма должна быть положительным числом!");
        }
        if (!CurrencyValidator.isValidCurrencyCode(fromCurrencyCode)){
            throw new BadRequestException("Недопустимый код исходной валюты: " + fromCurrencyCode);
        }
        if (!CurrencyValidator.isValidCurrencyCode(toCurrencyCode)){
            throw new BadRequestException("Недопустимый код целевой валюты: " + toCurrencyCode);
        }

        //Получаем валюты по кодам
        Currency fromCurrency = currencyDao.getCurrencyByCode(fromCurrencyCode)
                .orElseThrow(()->new CurrencyNotFoundException("Валюта с кодом" + fromCurrencyCode + " не найдена"));
        Currency toCurrency = currencyDao.getCurrencyByCode(toCurrencyCode)
                .orElseThrow(()-> new CurrencyNotFoundException("Валюта с кодом" + toCurrencyCode + " не найдена"));

        BigDecimal rate = getExchangeRate(fromCurrency, toCurrency);
        BigDecimal convertedAmount = amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);

        ExchangeDto exchangeDto = new ExchangeDto();

        CurrencyDto fromCurrencyDto = new CurrencyDto();
        fromCurrencyDto.setId(fromCurrency.getId());
        fromCurrencyDto.setName(fromCurrency.getFullName());
        fromCurrencyDto.setCode(fromCurrency.getCode());
        fromCurrencyDto.setSign(fromCurrency.getSign());

        CurrencyDto toCurrencyDto = new CurrencyDto();
        toCurrencyDto.setId(toCurrency.getId());
        toCurrencyDto.setName(toCurrency.getFullName());
        toCurrencyDto.setCode(toCurrency.getCode());
        toCurrencyDto.setSign(toCurrency.getSign());

        exchangeDto.setBaseCurrency(fromCurrencyDto);
        exchangeDto.setTargetCurrency(toCurrencyDto);
        exchangeDto.setRate(rate);
        exchangeDto.setAmount(amount.setScale(2, RoundingMode.HALF_UP));
        exchangeDto.setConvertedAmount(convertedAmount);

        return exchangeDto;
    }

    private BigDecimal getExchangeRate(Currency fromCurrency, Currency toCurrency){
        //1. Прямой курс
        Optional<ExchangeRate> directRate = exchangeRateDao.getExchangeRateByCurrencyIds(fromCurrency.getId(), toCurrency.getId());
        if (directRate.isPresent()){
            return directRate.get().getRate();
        }
        //2. Обратный курс
        Optional<ExchangeRate> reverseRate = exchangeRateDao.getExchangeRateByCurrencyIds(toCurrency.getId(), fromCurrency.getId());
        if (reverseRate.isPresent()){
            // Обратный курс (1/курс)
            return BigDecimal.ONE.divide(reverseRate.get().getRate(), 6, RoundingMode.HALF_UP);
        }
        //3. Кросс курс через USD
        String usdCode = "USD";
        Currency usd = currencyDao.getCurrencyByCode(usdCode)
                .orElseThrow(()-> new CurrencyNotFoundException("Валюта USD не найдена для кросс курса!"));
        //Находим курсы USD->fromCurrency и USD->toCurrency
        Optional<ExchangeRate> usdToFrom = exchangeRateDao.getExchangeRateByCurrencyIds(usd.getId(), fromCurrency.getId());
        Optional<ExchangeRate> usdToTo = exchangeRateDao.getExchangeRateByCurrencyIds(usd.getId(), toCurrency.getId());

        // Если оба курса найдены, вычисляем кросс-курс
        if (usdToFrom.isPresent() && usdToTo.isPresent()){
            BigDecimal rateUsdToFrom = usdToFrom.get().getRate();
            BigDecimal rateUsdToTo = usdToTo.get().getRate();
            return rateUsdToTo.divide(rateUsdToFrom, 6, RoundingMode.HALF_UP);
        }

        // Попробуем обратные курсы от валют к USD
        Optional<ExchangeRate> fromToUsd = exchangeRateDao.getExchangeRateByCurrencyIds(fromCurrency.getId(), usd.getId());
        Optional<ExchangeRate> toToUsd = exchangeRateDao.getExchangeRateByCurrencyIds(toCurrency.getId(), usd.getId());
        if (fromToUsd.isPresent() && usdToTo.isPresent()){
            // fromCurrency -> USD -> toCurrency
            BigDecimal rateFromToUsd = fromToUsd.get().getRate();
            BigDecimal rateUsdToTo = usdToTo.get().getRate();
            return rateFromToUsd.multiply(rateUsdToTo);
        }

        if (usdToFrom.isPresent() && toToUsd.isPresent()){
            // USD -> fromCurrency -> USD -> toCurrency
            BigDecimal rateUsdToFrom = usdToFrom.get().getRate();
            BigDecimal rateToToUsd = toToUsd.get().getRate();
            return BigDecimal.ONE.divide(rateUsdToFrom, 6, RoundingMode.HALF_UP).multiply(rateToToUsd);
        }

        if (fromToUsd.isPresent() && toToUsd.isPresent()){
            // fromCurrency -> USD, toCurrency -> USD
            BigDecimal rateFromToUsd = fromToUsd.get().getRate();
            BigDecimal rateToToUsd = toToUsd.get().getRate();
            return rateFromToUsd.divide(rateToToUsd, 6, RoundingMode.HALF_UP);
        }
        // Если не нашли ни один из вариантов курса
        throw new ExchangeRateNotFoundException("Не удалось найти или рассчитать обменный курс между валютами " +
                fromCurrency.getCode() + " и " + toCurrency.getCode());
    }
}
