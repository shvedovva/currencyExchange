package com.example.controller;

import com.example.dto.ExchangeDto;
import com.example.service.ExchangeService;
import com.example.util.JsonHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {
    private final ExchangeService exchangeService = new ExchangeService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fromCurrencyCode = req.getParameter("from");
        String toCurrencyCode = req.getParameter("to");
        String amountStr = req.getParameter("amount");

        BigDecimal amount = new BigDecimal(amountStr);

        ExchangeDto exchangeDto = exchangeService.calculateExchange(fromCurrencyCode, toCurrencyCode, amount);

        resp.getWriter().write(JsonHelper.toJson(exchangeDto));
    }
}
