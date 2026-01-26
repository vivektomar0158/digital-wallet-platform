package com.digitalwallet.platform.controller;

import com.digitalwallet.platform.util.JwtUtil;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

  private final JwtUtil jwtUtil;
  private final UserDetailsService userDetailsService;

  @GetMapping("/check-token")
  public ResponseEntity<?> checkToken(
      @RequestHeader(value = "Authorization", required = false) String authHeader) {
    Map<String, Object> response = new HashMap<>();

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      response.put("status", "NO_TOKEN");
      response.put("message", "No Bearer token found in Authorization header");
      return ResponseEntity.ok(response);
    }

    String token = authHeader.substring(7);

    try {
      // 1. Extract email from token
      String email = jwtUtil.extractUsername(token);
      response.put("email_from_token", email);

      if (email == null) {
        response.put("token_status", "INVALID");
        response.put("message", "Cannot extract email from token");
        return ResponseEntity.ok(response);
      }

      // 2. Load user details
      UserDetails userDetails = userDetailsService.loadUserByUsername(email);
      response.put("user_found", true);
      response.put("user_email", userDetails.getUsername());

      // 3. Validate token
      boolean isValid = jwtUtil.validateToken(token, userDetails);
      response.put("token_valid", isValid);

      // 4. Check SecurityContext
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      response.put("security_context_auth", auth != null ? "PRESENT" : "NULL");
      if (auth != null) {
        response.put("auth_name", auth.getName());
        response.put("auth_authenticated", auth.isAuthenticated());
      }

      response.put("status", "SUCCESS");

    } catch (Exception e) {
      response.put("status", "ERROR");
      response.put("error", e.getMessage());
      response.put("error_class", e.getClass().getName());
    }

    return ResponseEntity.ok(response);
  }
}
