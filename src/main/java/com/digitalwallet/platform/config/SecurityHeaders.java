package com.digitalwallet.platform.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;

@Component
public class SecurityHeaders implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletResponse httpServletResponse = (HttpServletResponse) response;

    httpServletResponse.setHeader("X-Content-Type-Options", "nosniff");
    httpServletResponse.setHeader("X-Frame-Options", "DENY");
    httpServletResponse.setHeader("X-XSS-Protection", "1; mode=block");
    httpServletResponse.setHeader(
        "Strict-Transport-Security", "max-age=31536000; includeSubDomains");
    httpServletResponse.setHeader(
        "Content-Security-Policy", "default-src 'self'; frame-ancestors 'none';");

    chain.doFilter(request, response);
  }
}
