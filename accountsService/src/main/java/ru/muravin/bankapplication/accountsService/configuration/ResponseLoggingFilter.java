package ru.muravin.bankapplication.accountsService.configuration;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.filters.AddDefaultCharsetFilter;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

@Component
public class ResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ResponseLoggingFilter.class);  // Logger instance for logging.

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Log request details before processing the request.
        logger.info("Request: Method={}, URI={}",
                request.getMethod(),
                request.getRequestURI());

        long startTime = System.currentTimeMillis();  // Capture the start time to measure processing duration.

        if (!(response instanceof ContentCachingResponseWrapper)) {
            response = new ContentCachingResponseWrapper(response);
        }
        try {
            filterChain.doFilter(request, response);  // Continue with the next filter in the chain.
        } finally {
            long duration = System.currentTimeMillis() - startTime;  // Calculate how long the request took.

            // Log response details after request processing.
            logger.info("Response: Status={}, URI={}, Duration={}ms, Body={}",
                    response.getStatus(),
                    request.getRequestURI(),
                    duration,
                    getResponsePayload(response));

            // Log any exceptions that occurred during processing.
            if (request.getAttribute("javax.servlet.error.exception") != null) {
                logger.error("Exception during request processing",
                        (Exception) request.getAttribute("javax.servlet.error.exception"));
            }
            updateResponse(response);

        }
    }

    // Utility method to extract request headers for logging.
    private void getRequestHeaders(HttpServletRequest request) {
        request.getHeaderNames().asIterator()
                .forEachRemaining(header -> logger.info("Request Header: {}={}", header, request.getHeader(header)));
    }

    // Utility method to extract response headers for logging.
    private void getResponseHeaders(HttpServletResponse response) {
        response.getHeaderNames().stream()
                .forEach(header -> logger.info("Response Header: {}={}", header, response.getHeader(header)));
    }

    private String getResponsePayload(HttpServletResponse response) {
        ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (wrapper != null) {

            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                int length = Math.min(buf.length, 5120);
                try {
                    return new String(buf, 0, length, wrapper.getCharacterEncoding());
                }
                catch (UnsupportedEncodingException ex) {
                    // NOOP
                }
            }
        }
        return "[unknown]";
    }
    private void updateResponse(HttpServletResponse response) throws IOException {
        ContentCachingResponseWrapper responseWrapper =
                WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        responseWrapper.copyBodyToResponse();
    }
}
