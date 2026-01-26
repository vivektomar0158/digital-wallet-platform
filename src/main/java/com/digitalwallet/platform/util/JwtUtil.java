package com.digitalwallet.platform.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

  // Use a proper 256-bit (32 character) secret key
  @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
  private String secretKey;

  @Value("${jwt.expiration:86400000}")
  private Long expiration;

  // FIXED: Proper key generation
  private Key getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  // Extract username (email) from token
  public String extractUsername(String token) {
    try {
      String username = extractClaim(token, Claims::getSubject);
      logger.debug("Extracted username from token: {}", username);
      return username;
    } catch (Exception e) {
      logger.error("Error extracting username from token: {}", e.getMessage());
      return null;
    }
  }

  // Extract expiration date from token
  public Date extractExpiration(String token) {
    try {
      return extractClaim(token, Claims::getExpiration);
    } catch (Exception e) {
      logger.error("Error extracting expiration from token: {}", e.getMessage());
      return new Date(0); // Return past date if error
    }
  }

  // Extract specific claim from token
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    try {
      final Claims claims = extractAllClaims(token);
      return claimsResolver.apply(claims);
    } catch (Exception e) {
      logger.error("Error extracting claim from token: {}", e.getMessage());
      return null;
    }
  }

  // Extract all claims from token - FIXED THIS METHOD
  private Claims extractAllClaims(String token) {
    try {
      logger.debug("Parsing token with key...");
      return Jwts.parserBuilder()
          .setSigningKey(getSignInKey())
          .build()
          .parseClaimsJws(token)
          .getBody();
    } catch (Exception e) {
      logger.error("Error parsing JWT token: {}", e.getMessage());
      throw new RuntimeException("Invalid JWT token: " + e.getMessage());
    }
  }

  // Check if token is expired
  private Boolean isTokenExpired(String token) {
    try {
      Date expiration = extractExpiration(token);
      Date now = new Date();
      boolean expired = expiration.before(now);
      logger.debug(
          "Token expiration: {}, Current time: {}, Is expired: {}", expiration, now, expired);
      return expired;
    } catch (Exception e) {
      logger.error("Error checking token expiration: {}", e.getMessage());
      return true;
    }
  }

  // Validate token
  public Boolean validateToken(String token, UserDetails userDetails) {
    try {
      final String username = extractUsername(token);
      logger.info("Validating token for user: {}", username);

      if (username == null) {
        logger.error("Cannot extract username from token");
        return false;
      }

      if (userDetails == null) {
        logger.error("UserDetails is null");
        return false;
      }

      boolean usernameMatches = username.equals(userDetails.getUsername());
      boolean notExpired = !isTokenExpired(token);

      logger.info(
          "Token validation - Username match: {}, Not expired: {}, Expected: {}, Actual: {}",
          usernameMatches,
          notExpired,
          userDetails.getUsername(),
          username);

      return usernameMatches && notExpired;
    } catch (Exception e) {
      logger.error("Error validating token: {}", e.getMessage(), e);
      return false;
    }
  }

  // Generate token for user
  public String generateToken(String username) {
    Map<String, Object> claims = new HashMap<>();
    String token = createToken(claims, username);
    logger.info("Generated token for user: {}, Token length: {}", username, token.length());
    return token;
  }

  // Create token with claims
  private String createToken(Map<String, Object> claims, String subject) {
    Date now = new Date();
    Date expirationDate = new Date(now.getTime() + expiration);

    logger.debug(
        "Creating token for subject: {}, Issued at: {}, Expires at: {}",
        subject,
        now,
        expirationDate);

    return Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(now)
        .setExpiration(expirationDate)
        .signWith(getSignInKey(), SignatureAlgorithm.HS256)
        .compact();
  }
}
