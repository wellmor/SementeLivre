package com.sementelivre.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter{
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        
        long startTime = System.currentTimeMillis();
        
        try{
            filterChain.doFilter(request, response);
        } finally{
            long duration = System.currentTimeMillis() - startTime;
            
            String username = "ANONIMO";
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")){
                username = auth.getName();
            }

            MDC.put("http_method", request.getMethod());
            MDC.put("http_route", request.getRequestURI());
            MDC.put("http_status", String.valueOf(response.getStatus()));
            MDC.put("http_duration_ms", String.valueOf(duration));
            MDC.put("usuario", username);

            logger.info("HTTP {} {} - Status: {} - Tempo: {}ms - Usuário: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration,
                    username);

            MDC.clear();
        }
    }
}
