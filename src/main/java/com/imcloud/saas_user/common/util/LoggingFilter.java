package com.imcloud.saas_user.common.util;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        String xForwardedPrefix = request.getHeader("X-Forwarded-Prefix");

        // 로그 출력
        System.out.println("X-Forwarded-For: " + xForwardedFor);
        System.out.println("X-Forwarded-Prefix: " + xForwardedPrefix);

        filterChain.doFilter(request, response);
    }
}

