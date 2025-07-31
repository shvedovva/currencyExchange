package com.example.filter;

import com.example.dto.ErrorDto;
import com.example.exception.CurrencyExchangeException;
import com.example.util.JsonHelper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ExceptionHandlerFilter implements Filter {


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (CurrencyExchangeException e) {
            handlerException(response, e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            handlerException(response, "Внутренняя ошибка сервера: " + e.getMessage(), 500);
        }

    }

    private void handlerException(ServletResponse response, String message, int statusCode) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setStatus(statusCode);
        httpResponse.setContentType("application/json");
        httpResponse.setCharacterEncoding("UTF-8");

        ErrorDto errorDto = new ErrorDto(message);
        httpResponse.getWriter().write(JsonHelper.toJson(errorDto));
    }

}
