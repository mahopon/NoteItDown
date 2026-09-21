package com.tcyao.nid.common.middleware;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TrailingSlashNormalizationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        if (uri.length() > 1 && uri.endsWith("/")) {
            String normalized = uri.replaceAll("/+$", "");
            HttpServletRequestWrapper wrapped = new HttpServletRequestWrapper(request) {
                @Override
                public String getRequestURI() {
                    return normalized;
                }

                @Override
                public StringBuffer getRequestURL() {
                    StringBuffer url = new StringBuffer();
                    url.append(getScheme()).append("://")
                            .append(getServerName());
                    if (getServerPort() != 80 && getServerPort() != 443)
                        url.append(":").append(getServerPort());
                    url.append(normalized);
                    return url;
                }
            };
            filterChain.doFilter(wrapped, response);
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
