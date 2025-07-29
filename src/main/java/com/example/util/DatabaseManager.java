package com.example.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:currency_exchange.db";
    private static final HikariDataSource dataSource;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Ошибка загрузки драйвера", e);
        }
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DB_URL);
        config.setMaximumPoolSize(4);
        dataSource = new HikariDataSource(config);

        initDatabase();
    }

    private static void initDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()){

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS Currencies (" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "Code VARCHAR(3) UNIQUE, " +
                    "FullName VARCHAR(255), " +
                    "Sign VARCHAR(5)" +
                    ")");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ExchangeRates (" +
                            "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "BaseCurrencyId INTEGER, " +
                            "TargetCurrencyId INTEGER, " +
                            "Rate DECIMAL(10,6), " +
                            "FOREIGN KEY (BaseCurrencyId) REFERENCES Currencies(ID), " +
                            "FOREIGN KEY (TargetCurrencyId) REFERENCES Currencies(ID), " +
                            "UNIQUE(BaseCurrencyId, TargetCurrencyId)" +
                            ")"
            );

            int count = 0;
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Currencies");
            if(rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();

            if (count == 0) {
                stmt.executeUpdate("INSERT INTO Currencies (Code, FullName, Sign) VALUES ('USD', 'United States dollar', '$')");
                stmt.executeUpdate("INSERT INTO Currencies (Code, FullName, Sign) VALUES ('EUR', 'Euro', '€')");
                stmt.executeUpdate("INSERT INTO Currencies (Code, FullName, Sign) VALUES ('RUB', 'Russian Ruble', '₽')");

                stmt.executeUpdate("INSERT INTO ExchangeRates (BaseCurrencyId, TargetCurrencyId, Rate) VALUES (1, 2, 0.93)");
                stmt.executeUpdate("INSERT INTO ExchangeRates (BaseCurrencyId, TargetCurrencyId, Rate) VALUES (1, 3, 90.50)");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка инициализации базы данных: " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() throws SQLException{
        return dataSource.getConnection();
    }
    public static void closeConnection(Connection connection){
        if(connection != null){
            try{
                connection.close();
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
    }


    /*private static final String DB_URL = "jdbc:sqlite:currency_exchange.db";
    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DB_URL);
        config.setMaximumPoolSize(10);
        dataSource = new HikariDataSource(config);

        initDatabase();
    }

    private static void initDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Создание таблицы Currencies
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS Currencies (" +
                            "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "Code VARCHAR(3) UNIQUE, " +
                            "FullName VARCHAR(255), " +
                            "Sign VARCHAR(5)" +
                            ")"
            );

            // Создание таблицы ExchangeRates
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ExchangeRates (" +
                            "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "BaseCurrencyId INTEGER, " +
                            "TargetCurrencyId INTEGER, " +
                            "Rate DECIMAL(10,6), " +
                            "FOREIGN KEY (BaseCurrencyId) REFERENCES Currencies(ID), " +
                            "FOREIGN KEY (TargetCurrencyId) REFERENCES Currencies(ID), " +
                            "UNIQUE(BaseCurrencyId, TargetCurrencyId)" +
                            ")"
            );

            // Заполнение таблицы начальными данными, если она пуста
            int count = 0;
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM Currencies");
            if (rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();

            if (count == 0) {
                // Вставляем начальные валюты
                stmt.executeUpdate("INSERT INTO Currencies (Code, FullName, Sign) VALUES ('USD', 'United States dollar', '$')");
                stmt.executeUpdate("INSERT INTO Currencies (Code, FullName, Sign) VALUES ('EUR', 'Euro', '€')");
                stmt.executeUpdate("INSERT INTO Currencies (Code, FullName, Sign) VALUES ('RUB', 'Russian Ruble', '₽')");

                // Вставляем начальные курсы обмена
                stmt.executeUpdate("INSERT INTO ExchangeRates (BaseCurrencyId, TargetCurrencyId, Rate) VALUES (1, 2, 0.93)");
                stmt.executeUpdate("INSERT INTO ExchangeRates (BaseCurrencyId, TargetCurrencyId, Rate) VALUES (1, 3, 90.50)");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка инициализации базы данных: " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // Логирование ошибки
            }
        }
    }*/
}
