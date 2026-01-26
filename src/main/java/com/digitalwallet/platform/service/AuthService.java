package com.digitalwallet.platform.service;

import com.digitalwallet.platform.dto.AuthResponse;
import com.digitalwallet.platform.dto.LoginRequest;
import com.digitalwallet.platform.dto.RegisterRequest;
import com.digitalwallet.platform.dto.UserResponse;
import com.digitalwallet.platform.model.User;
import com.digitalwallet.platform.model.UserStatus;
import com.digitalwallet.platform.model.Wallet;
import com.digitalwallet.platform.repository.UserRepository;
import com.digitalwallet.platform.repository.WalletRepository;
import com.digitalwallet.platform.util.JwtUtil;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
  private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

  private final UserRepository userRepository;
  private final WalletRepository walletRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

  @Transactional
  public AuthResponse register(RegisterRequest request) {

    try {
      // If the user already exists, fail fast with a clear message.
      if (userRepository.existsByEmail(request.getEmail())) {
        logger.warn("Registration attempt with already registered email: {}", request.getEmail());
        throw new RuntimeException("Email already registered: " + request.getEmail());
      }

      User user = new User();
      user.setEmail(request.getEmail());
      user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
      user.setFirstName(request.getFirstName());
      user.setLastName(request.getLastName());
      user.setPhone(request.getPhone());
      user.setStatus(UserStatus.ACTIVE);

      User savedUser = userRepository.save(user);
      logger.info("User created: userId={}, email={}", savedUser.getId(), savedUser.getEmail());

      Wallet wallet = new Wallet();
      wallet.setUser(savedUser);
      wallet.setWalletNumber(generateWalletNumber());
      wallet.setBalance(BigDecimal.ZERO);
      wallet.setStatus(com.digitalwallet.platform.model.WalletStatus.ACTIVE);

      Wallet savedWallet = walletRepository.save(wallet);
      logger.info(
          "Wallet created: walletId={}, walletNumber={}",
          savedWallet.getId(),
          savedWallet.getWalletNumber());

      String token = jwtUtil.generateToken(savedUser.getEmail());

      return AuthResponse.builder().token(token).user(mapToUserResponse(savedUser)).build();

    } catch (DataIntegrityViolationException ex) {
      // Handle race conditions where another registration for the same email just completed.
      logger.warn(
          "DataIntegrityViolation during registration for email {}. Assuming duplicate email.",
          request.getEmail(),
          ex);

      throw new RuntimeException("Email already registered: " + request.getEmail(), ex);
    }
  }

  @Transactional(readOnly = true)
  public AuthResponse login(LoginRequest request) {

    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(
                () -> {
                  logger.warn(
                      "Login failed: Invalid credentials for email - {}", request.getEmail());
                  return new RuntimeException("Invalid email or password");
                });

    if (user.getStatus() != UserStatus.ACTIVE) {
      logger.warn("Login failed: User account is {} - {}", user.getStatus(), request.getEmail());
      throw new RuntimeException("Account is " + user.getStatus().toString().toLowerCase());
    }

    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      logger.warn("Login failed: Invalid password for email - {}", request.getEmail());
      throw new RuntimeException("Invalid email or password");
    }

    String token = jwtUtil.generateToken(user.getEmail());
    logger.info("User logged in successfully: userId={}, email={}", user.getId(), user.getEmail());

    return AuthResponse.builder().token(token).user(mapToUserResponse(user)).build();
  }

  private String generateWalletNumber() {
    // Generate shorter wallet number (max 20 chars)
    String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

    String timestamp = String.valueOf(System.currentTimeMillis());
    String timePart = timestamp.substring(timestamp.length() - 6); // Last 6 digits

    return "WAL" + timePart + randomPart; // Total: 3 + 6 + 8 = 17 characters
  }

  private UserResponse mapToUserResponse(User user) {
    return UserResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .phone(user.getPhone())
        .status(user.getStatus())
        .createdAt(user.getCreatedAt())
        .build();
  }
}
