package com.digitalwallet.platform.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.digitalwallet.platform.dto.AuthResponse;
import com.digitalwallet.platform.dto.LoginRequest;
import com.digitalwallet.platform.dto.RegisterRequest;
import com.digitalwallet.platform.model.User;
import com.digitalwallet.platform.model.UserStatus;
import com.digitalwallet.platform.model.Wallet;
import com.digitalwallet.platform.repository.UserRepository;
import com.digitalwallet.platform.repository.WalletRepository;
import com.digitalwallet.platform.util.JwtUtil;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private WalletRepository walletRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private JwtUtil jwtUtil;

  @InjectMocks private AuthService authService;

  private RegisterRequest registerRequest;
  private LoginRequest loginRequest;
  private User testUser;

  @BeforeEach
  void setUp() {
    registerRequest = new RegisterRequest();
    registerRequest.setEmail("test@example.com");
    registerRequest.setPassword("password123");
    registerRequest.setFirstName("Test");
    registerRequest.setLastName("User");
    registerRequest.setPhone("+1234567890");

    loginRequest = new LoginRequest();
    loginRequest.setEmail("test@example.com");
    loginRequest.setPassword("password123");

    testUser = new User();
    testUser.setId(1L);
    testUser.setEmail("test@example.com");
    testUser.setPasswordHash("encodedPassword");
    testUser.setFirstName("Test");
    testUser.setLastName("User");
    testUser.setRole("USER");
    testUser.setStatus(UserStatus.ACTIVE);
  }

  @Test
  @DisplayName("Should register user successfully")
  void shouldRegisterUserSuccessfully() {
    // Given
    when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
    when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class)))
        .thenAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              user.setId(1L);
              return user;
            });
    when(walletRepository.save(any(Wallet.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(jwtUtil.generateToken(anyString())).thenReturn("jwt-token");

    // When
    AuthResponse response = authService.register(registerRequest);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getToken()).isEqualTo("jwt-token");
    assertThat(response.getUser()).isNotNull();
    assertThat(response.getUser().getEmail()).isEqualTo(registerRequest.getEmail());
    verify(userRepository).existsByEmail(registerRequest.getEmail());
    verify(passwordEncoder).encode(registerRequest.getPassword());
    verify(userRepository).save(any(User.class));
    verify(walletRepository).save(any(Wallet.class));
    verify(jwtUtil).generateToken(registerRequest.getEmail());
  }

  @Test
  @DisplayName("Should throw exception when email already exists")
  void shouldThrowExceptionWhenEmailExists() {
    // Given
    when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

    // When & Then
    assertThatThrownBy(() -> authService.register(registerRequest))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Email already registered");

    verify(userRepository).existsByEmail(registerRequest.getEmail());
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("Should login successfully")
  void shouldLoginSuccessfully() {
    // Given
    when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash()))
        .thenReturn(true);
    when(jwtUtil.generateToken(loginRequest.getEmail())).thenReturn("jwt-token");

    // When
    AuthResponse response = authService.login(loginRequest);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getToken()).isEqualTo("jwt-token");
    assertThat(response.getUser()).isNotNull();
    assertThat(response.getUser().getEmail()).isEqualTo(testUser.getEmail());
    verify(userRepository).findByEmail(loginRequest.getEmail());
    verify(passwordEncoder).matches(loginRequest.getPassword(), testUser.getPasswordHash());
    verify(jwtUtil).generateToken(loginRequest.getEmail());
  }

  @Test
  @DisplayName("Should throw exception when user not found")
  void shouldThrowExceptionWhenUserNotFound() {
    // Given
    when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> authService.login(loginRequest))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Invalid email or password");

    verify(userRepository).findByEmail(loginRequest.getEmail());
    verify(jwtUtil, never()).generateToken(anyString());
  }

  @Test
  @DisplayName("Should throw exception when password is incorrect")
  void shouldThrowExceptionWhenPasswordIncorrect() {
    // Given
    when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash()))
        .thenReturn(false);

    // When & Then
    assertThatThrownBy(() -> authService.login(loginRequest))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Invalid email or password");

    verify(userRepository).findByEmail(loginRequest.getEmail());
    verify(passwordEncoder).matches(loginRequest.getPassword(), testUser.getPasswordHash());
    verify(jwtUtil, never()).generateToken(anyString());
  }
}
