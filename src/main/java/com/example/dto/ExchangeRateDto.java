package com.example.dto;

import java.math.BigDecimal;

public class ExchangeRateDto {
    private int id;
    private CurrencyDto baseCurrency;
    private CurrencyDto targetCurrency;
    private BigDecimal rate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CurrencyDto getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(CurrencyDto baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public CurrencyDto getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(CurrencyDto targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }
}
