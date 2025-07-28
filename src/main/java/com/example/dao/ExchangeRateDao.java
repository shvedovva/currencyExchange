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
import java.util.Optional;

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
            throw new RuntimeException("Ошибка при получении списка обменных курсов", e);
        } finally {
            DatabaseManager.closeConnection(conn);
        }
        return exchangeRates;
    }

    public Optional<ExchangeRate> getExchangeRateByCurrencyCode(String baseCode, String targetBase){
        Connection conn = null;

        try{
            conn = DatabaseManager.getConnection();
            String sql = "SELECT er.ID, er.BaseCurrencyId, er.TargetCurrencyId, er.Rate, " +
                    "bc.ID as BaseID, bc.Code as BaseCode, bc.FullName as BaseFullName, bc.Sign as BaseSign, " +
                    "tc.ID as TargetID, tc.Code as TargetCode, tc.FullName as TargetFullName, tc.Sign as TargetSign " +
                    "FROM ExchangeRates er " +
                    "JOIN Currencies bc ON er.BaseCurrencyId = bc.ID " +
                    "JOIN Currencies tc ON er.TargetCurrencyId = tc.ID " +
                    "WHERE bc.Code = ? AND tc.Code = ?";

            try(PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setString(1, baseCode);
                stmt.setString(2, targetBase);

                try (ResultSet rs = stmt.executeQuery()){
                    if(rs.next()){
                        ExchangeRate exchangeRate = mapResultSetToExchangeRate(rs);
                        return Optional.of(exchangeRate);
                    }

                }

            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении обменного курса для валютной пары: " + baseCode + "/" + targetBase, e);
        } finally {
            DatabaseManager.closeConnection(conn);
        }
        return Optional.empty();
    }

    public Optional<ExchangeRate> getExchangeRateByCurrencyIds(int baseId, int targetId){
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            String sql = "SELECT er.ID, er.BaseCurrencyId, er.TargetCurrencyId, er.Rate, " +
                    "bc.ID as BaseID, bc.Code as BaseCode, bc.FullName as BaseFullName, bc.Sign as BaseSign, " +
                    "tc.ID as TargetID, tc.Code as TargetCode, tc.FullName as TargetFullName, tc.Sign as TargetSign " +
                    "FROM ExchangeRates er " +
                    "JOIN Currencies bc ON er.BaseCurrencyId = bc.ID " +
                    "JOIN Currencies tc ON er.TargetCurrencyId = tc.ID " +
                    "WHERE er.BaseCurrencyId = ? AND er.TargetCurrencyId = ?";
            try(PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setInt(1, baseId);
                stmt.setInt(2, targetId);
                try(ResultSet rs = stmt.executeQuery()){
                    if(rs.next()){
                        ExchangeRate exchangeRate = mapResultSetToExchangeRate(rs);
                        return Optional.of(exchangeRate);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении обменного курса по ID валют", e);
        } finally {
            DatabaseManager.closeConnection(conn);
        }

        return Optional.empty();
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
