package com.example.EstoqueFacil.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;


public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Value("${spring.application.name:estoque-facil}")
    private String serviceName;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String incomingCorrelationId = request.getHeader(CORRELATION_ID_HEADER);
        String correlationId;

        if (incomingCorrelationId == null || incomingCorrelationId.isBlank()) {
            // Primeiro serviço da cadeia: gera novo ID
            correlationId = serviceName + "_" + UUID.randomUUID();
        } else {
            // Serviço downstream: encadeia o ID existente
            correlationId = serviceName + "_" + incomingCorrelationId;
        }

        //MDC para os logs
        MDC.put(CORRELATION_ID_HEADER, correlationId);

        // header de resposta
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        // request para outros componentes
        request.setAttribute(CORRELATION_ID_HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Erro no CorrelationIdFilter para requisição {}: {}",
                    request.getRequestURI(), e.getMessage(), e);
            throw e;
        } finally {
            MDC.remove(CORRELATION_ID_HEADER);
        }
    }
}