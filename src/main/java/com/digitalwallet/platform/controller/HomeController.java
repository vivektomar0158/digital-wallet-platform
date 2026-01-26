package com.digitalwallet.platform.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

  @GetMapping("/")
  public Map<String, Object> home() {
    return Map.of(
        "service", "Digital Wallet Platform API",
        "status", "running",
        "version", "1.0.0",
        "endpoints",
            Map.of(
                "health", "/api/test/health",
                "register", "/api/auth/register",
                "login", "/api/auth/login",
                "docs", "/swagger-ui/index.html"));
  }
}
