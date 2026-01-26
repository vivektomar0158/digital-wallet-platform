package com.digitalwallet.platform.security;

import com.digitalwallet.platform.util.JwtUtil;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j // Add this
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException, java.io.IOException {

    // 1. Get Authorization header
    final String authHeader = request.getHeader("Authorization");
    log.debug("Authorization header: {}", authHeader); // Add this

    // 2. Check if header is present and starts with "Bearer "
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      log.debug("No Bearer token found, continuing filter chain");
      filterChain.doFilter(request, response);
      return;
    }

    // 3. Extract token (remove "Bearer " prefix)
    final String jwt = authHeader.substring(7);
    log.debug("Extracted JWT token: {}", jwt.substring(0, Math.min(jwt.length(), 20)) + "...");

    final String userEmail;

    try {
      // 4. Extract email from token
      userEmail = jwtUtil.extractUsername(jwt);
      log.debug("Extracted email from token: {}", userEmail); // Add this

      // 5. If email is present and user is not already authenticated
      if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        log.debug("Loading user details for email: {}", userEmail);

        // 6. Load user details from database
        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
        log.debug("User details loaded successfully: {}", userDetails.getUsername());

        // 7. Validate token
        if (jwtUtil.validateToken(jwt, userDetails)) {
          log.debug("JWT token validated successfully");

          // 8. Create authentication object
          UsernamePasswordAuthenticationToken authToken =
              new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());

          // 9. Set additional details
          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

          // 10. Set authentication in security context
          SecurityContextHolder.getContext().setAuthentication(authToken);
          log.debug("Authentication set in SecurityContextHolder");
        } else {
          log.warn("JWT token validation failed");
        }
      }
    } catch (Exception e) {
      // Token is invalid, continue without authentication
      log.error("JWT validation error: {}", e.getMessage(), e);
    }

    // 11. Continue filter chain
    filterChain.doFilter(request, response);
  }
}
