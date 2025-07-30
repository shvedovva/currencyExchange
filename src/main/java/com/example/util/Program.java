package com.example.util;

import com.example.dao.CurrencyDao;
import com.example.dao.ExchangeRateDao;
import com.example.model.Currency;
import com.example.model.ExchangeRate;
import com.example.service.CurrencyService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class Program {
    public static void main(String[] args) throws SQLException {
        //DatabaseManager dbm = new DatabaseManager();

        //DatabaseManager.getConnection();
        //dbm.initDatabase();

        //CurrencyDao dao = new CurrencyDao();

        /*System.out.println(dao.getAllCurrencies());

        Optional<Currency> RUB = dao.getCurrencyByCode("RUB");

        System.out.println(RUB);*/


        //Currency currency = new Currency("TST", "Test Currency", "T");
        //dao.addCurrency(currency);
        //CurrencyService service = new CurrencyService();
        //System.out.println(service.getAllCurrencies());

        //ExchangeRateDao exchangeRateDao = new ExchangeRateDao();
        //System.out.println(exchangeRateDao.getAllExchangeRates());
        Connection conn = DatabaseManager.getConnection();
        Statement stmt = conn.createStatement();


        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ExchangeRates");
        if(rs.next()) {
            System.out.println(rs.getInt(1));
        }
        rs.close();

    }

}
