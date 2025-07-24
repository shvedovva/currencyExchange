package com.example.controller;

import com.example.dto.CurrencyDto;
import com.example.service.CurrencyService;
import com.example.util.JsonHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

//@WebServlet(urlPatterns = {"/currencies", "/currency/*"})
public class CurrencyServlet extends HttpServlet {
    private final CurrencyService currencyService = new CurrencyService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        // Обработка GET /currencies
        if(pathInfo == null || pathInfo.equals("/")){
            List<CurrencyDto> currencies = currencyService.getAllCurrencies();
            resp.getWriter().write(JsonHelper.toJson(currencies));
            return;
        }

        // Обработка GET /currency/{code}
        String code = pathInfo.substring(1);
        CurrencyDto currency = currencyService.getCurrencyByCode(code);
        resp.getWriter().write(JsonHelper.toJson(currency));
    }
}
