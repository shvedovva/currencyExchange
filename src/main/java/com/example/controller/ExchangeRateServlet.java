package com.example.controller;

import com.example.dto.ErrorDto;
import com.example.dto.ExchangeRateDto;
import com.example.service.ExchangeRateService;
import com.example.util.JsonHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet(urlPatterns = {"/exchangeRates", "/exchangeRate/*"})
public class ExchangeRateServlet extends HttpServlet {
    private final ExchangeRateService exchangeRateService = new ExchangeRateService();

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getMethod();

        if ("PATCH".equals(method)){
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    private void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.length() <= 1){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonHelper.toJson(new ErrorDto("Отсутствует валютная пара в URL")));
            return;
        }
        String currencyPair = pathInfo.substring(1);
        String rateStr = req.getParameter("rate");

        BigDecimal rate = new BigDecimal(rateStr);

        ExchangeRateDto updatedExchangeRate = exchangeRateService.updateExchangeRate(currencyPair, rate);
        resp.getWriter().write(JsonHelper.toJson(updatedExchangeRate));
    }

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
        ExchangeRateDto exchangeRate = exchangeRateService.getExchangeRate(currencyPair);
        resp.getWriter().write(JsonHelper.toJson(exchangeRate));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String baseCurrencyCode = req.getParameter("baseCurrencyCode");
        String targetCurrencyCode = req.getParameter("targetCurrencyCode");
        String rateStr = req.getParameter("rate");

        BigDecimal rate = new BigDecimal(rateStr);

        ExchangeRateDto addedExchangeRate = exchangeRateService.addExchangeRate(baseCurrencyCode, targetCurrencyCode, rate);

        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.getWriter().write(JsonHelper.toJson(addedExchangeRate));
    }


}
