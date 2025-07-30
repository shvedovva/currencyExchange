package com.example.controller;

import com.example.dto.ExchangeRateDto;
import com.example.service.ExchangeRateService;
import com.example.util.JsonHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/exchangeRates", "/exchangeRate/*"})
public class ExchangeRateServlet extends HttpServlet {
    private final ExchangeRateService exchangeRateService = new ExchangeRateService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        // Обработка GET /exchangeRates
        if (pathInfo == null || pathInfo.equals("/")){
            List<ExchangeRateDto> exchangeRates = exchangeRateService.getAllExchangeRates();
            resp.getWriter().write(JsonHelper.toJson(exchangeRates));
            return;
        }

        String currencyPair = pathInfo.substring(1);
        System.out.println(currencyPair);
        ExchangeRateDto exchangeRate = exchangeRateService.getExchangeRate(currencyPair);
        resp.getWriter().write(JsonHelper.toJson(exchangeRate));
    }
}
