package edu.iua.nexus.util;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            log.warn("AccessDenied: User = {}; Authorities = {}; Method = {}; URI = {}; Exception = {}",
                    auth.getName(),
                    auth.getAuthorities(),
                    request.getMethod(),
                    request.getRequestURI(),
                    accessDeniedException.getMessage()
            );
        } else {
            log.warn("AccessDenied: Usuario anónimo; Method = {}; URI = {}; Exception = {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    accessDeniedException.getMessage()
            );
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"Access Denied\", \"message\": \""
                + accessDeniedException.getMessage() + "\"}");
    }
}
