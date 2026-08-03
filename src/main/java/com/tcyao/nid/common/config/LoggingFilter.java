package com.tcyao.nid.common.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@Order(1)
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, 1);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long start = System.currentTimeMillis();

        try {
            chain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long duration = System.currentTimeMillis() - start;

            String method = wrappedRequest.getMethod();
            String uri = wrappedRequest.getRequestURI();
            String query = wrappedRequest.getQueryString();
            int status = wrappedResponse.getStatus();
            String queryString = query != null ? "?" + query : "";

            String headers = Collections.list(wrappedRequest.getHeaderNames()).stream()
                    .flatMap(name -> Collections.list(wrappedRequest.getHeaders(name)).stream()
                            .map(value -> name + ": " + value))
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");

            byte[] requestBody = wrappedRequest.getContentAsByteArray();
            String reqBody = requestBody.length > 0 ? new String(requestBody, wrappedRequest.getCharacterEncoding()) : "";

            byte[] responseBody = wrappedResponse.getContentAsByteArray();
            String resBody = responseBody.length > 0 ? new String(responseBody, wrappedResponse.getCharacterEncoding()) : "";

            log.info("\n--- Request ---\n{} {}{}\nHeaders: {}\nBody: {}\n--- Response ---\nStatus: {}\nBody: {}\nDuration: {}ms",
                    method, uri, queryString, headers, reqBody, status, resBody, duration);

            wrappedResponse.copyBodyToResponse();
        }
    }
}
