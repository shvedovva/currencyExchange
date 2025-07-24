package com.example.dao;

import com.example.exception.DuplicateResourceException;
import com.example.model.Currency;
import com.example.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CurrencyDao {

    public List<Currency> getAllCurrencies() {
        List<Currency> currencies = new ArrayList<>();
        Connection conn = null;

        try {
            conn = DatabaseManager.getConnection();
            String sql = "SELECT ID, Code, FullName, Sign FROM Currencies";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    Currency currency = mapResultSetToCurrency(rs);
                    currencies.add(currency);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении списка валют", e);
        } finally {
            DatabaseManager.closeConnection(conn);
        }
        return currencies;
    }

    public Optional<Currency> getCurrencyByCode(String code) {
        Connection conn = null;

        try {
            conn = DatabaseManager.getConnection();
            String sql = "SELECT ID, Code, FullName, Sign FROM Currencies WHERE Code = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, code);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Currency currency = mapResultSetToCurrency(rs);
                        return Optional.of(currency);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении валюты по коду: " + code, e);
        } finally {
            DatabaseManager.closeConnection(conn);
        }
        return Optional.empty();
    }

    public Optional<Currency> getCurrencyById(int id) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            String sql = "SELECT ID, Code, FullName, Sign FROM Currencies WHERE ID = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Currency currency = mapResultSetToCurrency(rs);
                        return Optional.of(currency);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении валюты по ID: " + id, e);
        } finally {
            DatabaseManager.closeConnection(conn);
        }
        return Optional.empty();
    }

    public Currency addCurrency(Currency currency) {
        Connection conn = null;

        try {
            conn = DatabaseManager.getConnection();
            String sql = "INSERT INTO Currencies (Code, FullName, Sign) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, currency.getCode());
                stmt.setString(2, currency.getFullName());
                stmt.setString(3, currency.getSign());

                int affectedRows = stmt.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("Создание валюты не удалось, ни одна строка не была добавлена");
                }

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        currency.setId(generatedKeys.getInt(1));
                        return currency;
                    } else {
                        throw new SQLException("Создание валюты не удалось, ID не был получен");
                    }
                }
            }
        } catch (SQLException e) {
            if(e.getMessage().contains("UNIQUE constraint failed: Currencies.Code")) {
                throw new DuplicateResourceException("Валюта с кодом " + currency.getCode() + " уже существует");
            }
            throw new RuntimeException("Ошибка при добавлении валюты", e);
        } finally {
            DatabaseManager.closeConnection(conn);
        }
    }


    private Currency mapResultSetToCurrency(ResultSet rs) throws SQLException {
        Currency currency = new Currency();
        currency.setId(rs.getInt("ID"));
        currency.setCode(rs.getString("Code"));
        currency.setFullName(rs.getString("FullName"));
        currency.setSign(rs.getString("Sign"));
        return currency;
    }
}
