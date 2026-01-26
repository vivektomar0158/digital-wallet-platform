package com.digitalwallet.platform.controller;

import com.digitalwallet.platform.dto.AuthResponse;
import com.digitalwallet.platform.dto.LoginRequest;
import com.digitalwallet.platform.dto.RegisterRequest;
import com.digitalwallet.platform.dto.UserResponse;
import com.digitalwallet.platform.model.User;
import com.digitalwallet.platform.service.AuthService;
import com.digitalwallet.platform.service.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    logger.info("Register request for email: {}", request.getEmail());
    AuthResponse response = authService.register(request);
    logger.info("Registration successful for: {}", request.getEmail());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    logger.info("Login request for email: {}", request.getEmail());
    AuthResponse response = authService.login(request);
    logger.info("Login successful for: {}, Token generated", request.getEmail());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/me")
  public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
    logger.info("=== /api/auth/me called ===");

    if (userDetails == null) {
      logger.error("UserDetails is null in @AuthenticationPrincipal");

      var auth = SecurityContextHolder.getContext().getAuthentication();
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("error", "Not authenticated");
      errorResponse.put("message", "User not found in authentication context");
      errorResponse.put("authentication", auth != null ? auth.getName() : "null");

      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    User user = userDetails.getUser();
    logger.info("User authenticated: {}", user.getEmail());
    UserResponse response = UserResponse.fromEntity(user);
    return ResponseEntity.ok(response);
  }

  // Add debug endpoint
  @GetMapping("/debug")
  public ResponseEntity<Map<String, Object>> debugAuth(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Map<String, Object> response = new HashMap<>();

    var auth = SecurityContextHolder.getContext().getAuthentication();
    response.put("authentication", auth != null ? auth.getName() : "null");
    response.put("isAuthenticated", auth != null && auth.isAuthenticated());
    response.put(
        "userFromPrincipal", userDetails != null ? userDetails.getUser().getEmail() : "null");
    response.put("authorities", auth != null ? auth.getAuthorities() : "[]");

    return ResponseEntity.ok(response);
  }

  // Add health check
  @GetMapping("/health")
  public ResponseEntity<Map<String, String>> health() {
    Map<String, String> response = new HashMap<>();
    response.put("status", "UP");
    response.put("service", "auth-service");
    return ResponseEntity.ok(response);
  }
}
