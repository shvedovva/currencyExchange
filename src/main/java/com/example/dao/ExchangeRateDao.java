package com.example.dao;

import com.example.exception.DuplicateResourceException;
import com.example.exception.ExchangeRateNotFoundException;
import com.example.model.Currency;
import com.example.model.ExchangeRate;
import com.example.util.DatabaseManager;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateDao {
    private final CurrencyDao currencyDao;

    public ExchangeRateDao() {
        this.currencyDao = new CurrencyDao();
    }

    public List<ExchangeRate> getAllExchangeRates() {
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
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
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

    public Optional<ExchangeRate> getExchangeRateByCurrencyCode(String baseCode, String targetBase) {
        Connection conn = null;

        try {
            conn = DatabaseManager.getConnection();
            String sql = "SELECT er.ID, er.BaseCurrencyId, er.TargetCurrencyId, er.Rate, " +
                    "bc.ID as BaseID, bc.Code as BaseCode, bc.FullName as BaseFullName, bc.Sign as BaseSign, " +
                    "tc.ID as TargetID, tc.Code as TargetCode, tc.FullName as TargetFullName, tc.Sign as TargetSign " +
                    "FROM ExchangeRates er " +
                    "JOIN Currencies bc ON er.BaseCurrencyId = bc.ID " +
                    "JOIN Currencies tc ON er.TargetCurrencyId = tc.ID " +
                    "WHERE bc.Code = ? AND tc.Code = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, baseCode);
                stmt.setString(2, targetBase);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
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

    public Optional<ExchangeRate> getExchangeRateByCurrencyIds(int baseId, int targetId) {
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
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, baseId);
                stmt.setInt(2, targetId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
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

    public ExchangeRate addExchangeRate(String baseCode, String targetCode, BigDecimal rate) {
        Currency baseCurrency = currencyDao.getCurrencyByCode(baseCode)
                .orElseThrow(() -> new ExchangeRateNotFoundException("Базовая валюта не найдена: " + baseCode));
        Currency targetCurrency = currencyDao.getCurrencyByCode(targetCode)
                .orElseThrow(() -> new ExchangeRateNotFoundException("Целевая валюта не найдена: " + targetCode));
        return addExchangeRate(baseCurrency.getId(), targetCurrency.getId(), rate);
    }

    public ExchangeRate addExchangeRate(int baseId, int targetId, BigDecimal rate) {
        Currency baseCurrency = currencyDao.getCurrencyById(baseId)
                .orElseThrow(() -> new ExchangeRateNotFoundException("Базовая валюта с ID " + baseId + " не найдена"));
        Currency targetCurrency = currencyDao.getCurrencyById(targetId)
                .orElseThrow(() -> new ExchangeRateNotFoundException("Целевая валюта с ID " + targetId + " не найдена"));

        Connection conn = null;

        try {
            conn = DatabaseManager.getConnection();
            String sql = "INSERT INTO ExchangeRates (BaseCurrencyId, TargetCurrencyId, Rate) VALUES (?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, baseId);
                stmt.setInt(2, targetId);
                stmt.setBigDecimal(3, rate);

                int affectedRows = stmt.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("Создание обменного курса не удалось, ни одна строка не была добавлена");
                }
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        return new ExchangeRate(id, baseCurrency, targetCurrency, rate);
                    } else {
                        throw new SQLException("Создание обменного курса не удалось, ID не был получен");
                    }
                }
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                throw new DuplicateResourceException("Обменный курс для пары: " + baseCurrency.getCode() + "/" + targetCurrency.getCode() + " уже существует");
            }
            throw new RuntimeException("Ошибка при добавлении обменного курса", e);
        } finally {
            DatabaseManager.closeConnection(conn);
        }
    }

    public ExchangeRate updateExchangeRate(String baseCode, String targetCode, BigDecimal rate) {
        Optional<ExchangeRate> exchangeRate = getExchangeRateByCurrencyCode(baseCode, targetCode);

        if (exchangeRate.isEmpty()) {
            throw new ExchangeRateNotFoundException("Обменный курс для пары " + baseCode + "/" + targetCode + " не найден");
        }

        ExchangeRate existingRate = exchangeRate.get();
        Connection conn = null;

        try {
            conn = DatabaseManager.getConnection();
            String sql = "UPDATE ExchangeRates SET Rate = ? WHERE ID = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBigDecimal(1, rate);
                stmt.setInt(2, existingRate.getId());

                int affectedRows = stmt.executeUpdate();

                if (affectedRows == 0) {
                    throw new ExchangeRateNotFoundException("Обновление обменного курса не удалось для ID: " + existingRate.getId());
                }

                existingRate.setRate(rate);
                return existingRate;
            }


        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении обменного курса", e);
        } finally {
            DatabaseManager.closeConnection(conn);
        }
    }


    private ExchangeRate mapResultSetToExchangeRate(ResultSet rs) throws SQLException {
        Currency baseCurrency = new Currency(
                rs.getInt("BaseID"),
                rs.getString("BaseCode"),
                rs.getString("BaseFullName"),
                rs.getString("BaseSign")
        );
        Currency targetCurrency = new Currency(
                rs.getInt("TargetID"),
                rs.getString("TargetCode"),
                rs.getString("TargetFullName"),
                rs.getString("TargetSign")
        );
        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setId(rs.getInt("ID"));
        exchangeRate.setBaseCurrency(baseCurrency);
        exchangeRate.setTargetCurrency(targetCurrency);
        exchangeRate.setRate(rs.getBigDecimal("Rate"));

        return exchangeRate;
    }
}
