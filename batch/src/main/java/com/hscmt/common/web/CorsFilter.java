package com.hscmt.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class CorsFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        /* 내부 요청이라면 무조건 승인 */
        if (request.getHeader("X-Internal-Request") == "true") {
            filterChain.doFilter(request, response);
            return;
        }

        String uri = request.getRequestURI();
        // Swagger 관련 요청은 필터 무시
        if (isSwaggerUri(uri)) {
            filterChain.doFilter(request, response);
            return;
        }
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT, PATCH");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, Authorization");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition, Content-Type");

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isSwaggerUri(String uri) {
        return uri.contains("/swagger-ui")
                || uri.contains("/v3/batch-docs")
                || uri.contains("/simulation-batch")
                || uri.contains("/swagger-resources")
                || uri.contains("/webjars/");
    }
}
