package com.digitalwallet.platform.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

  // Your existing endpoints...
  @GetMapping("/health")
  public Map<String, String> health() {
    return Map.of(
        "status", "UP",
        "service", "Digital Wallet Platform",
        "version", "1.0.0");
  }

  @GetMapping("/public")
  public Map<String, String> publicEndpoint() {
    return Map.of(
        "message",
        "This is a public endpoint accessible without authentication",
        "timestamp",
        String.valueOf(System.currentTimeMillis()));
  }

  // ✅ ADD THESE NEW DEBUG ENDPOINTS
  @GetMapping("/debug/auth")
  public Map<String, Object> debugAuth(
      @RequestHeader(value = "Authorization", required = false) String authHeader) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    Map<String, Object> response = new HashMap<>();
    response.put("endpoint", "/api/test/debug/auth");
    response.put("authHeader", authHeader);
    response.put("authExists", auth != null);

    if (auth != null) {
      response.put("authenticated", auth.isAuthenticated());
      response.put("principalClass", auth.getPrincipal().getClass().getName());
      response.put("principal", auth.getPrincipal().toString());
      response.put("name", auth.getName());
      response.put("authorities", auth.getAuthorities().toString());
    }

    return response;
  }

  @GetMapping("/debug/headers")
  public Map<String, String> debugHeaders(HttpServletRequest request) {
    Map<String, String> headers = new HashMap<>();

    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      headers.put(headerName, request.getHeader(headerName));
    }

    return headers;
  }
}
