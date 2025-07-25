package com.example.dao;

import com.example.model.Currency;
import com.example.model.ExchangeRate;
import com.example.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExchangeRateDao {
    private final CurrencyDao currencyDao;

    public ExchangeRateDao() {
        this.currencyDao = new CurrencyDao();
    }

    public List<ExchangeRate> getAllExchangeRates(){
        List<ExchangeRate> exchangeRates = new ArrayList<>();
        Connection conn = null;

        try {
            conn = DatabaseManager.getConnection();
            String sql = "SELECT er.ID, er.BaseCurrencyId, er.TargetCurrencyId, er.Rate, " +
                    "bc.ID as BaseID, bc.Code as BaseCode, bc.FullName as BaseFullName, bc.Sign as BaseSign, " +
                    "tc.ID as TargetID, tc.Code as TargetCode, tc.FullName as TargetFullName, tc.Sign as TargetSign " +
                    "FROM ExchangeRates er " +
                    "JOIN Currencies bc ON er.BaseCurrencyId = bc.ID " +
                    "JOIN Currencies tc ON er.TargetCurrencyId = tc.ID";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()){

                while (rs.next()){
                    ExchangeRate exchangeRate = mapResultSetToExchangeRate(rs);
                    exchangeRates.add(exchangeRate);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DatabaseManager.closeConnection(conn);
        }
        return exchangeRates;
    }

    private ExchangeRate mapResultSetToExchangeRate(ResultSet rs) throws SQLException {
        Currency baseCurrency=  new Currency(
                rs.getInt("BaseID"),
                rs.getString("BaseCode"),
                rs.getString("BaseFullName"),
                rs.getString("BaseSign")
        );
        Currency targetCurrency=  new Currency(
                rs.getInt("TargetID"),
                rs.getString("TargetCode"),
                rs.getString("TargetFullName"),
                rs.getString("TargetSign")
        );
        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setId(rs.getInt("ID"));
        exchangeRate.setBaseCurrency(baseCurrency);
        exchangeRate.setBaseCurrency(targetCurrency);
        exchangeRate.setRate(rs.getBigDecimal("Rate"));

        return exchangeRate;
    }
}
