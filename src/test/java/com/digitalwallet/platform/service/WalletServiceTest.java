package com.digitalwallet.platform.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.digitalwallet.platform.dto.*;
import com.digitalwallet.platform.model.*;
import com.digitalwallet.platform.repository.TransactionRepository;
import com.digitalwallet.platform.repository.UserRepository;
import com.digitalwallet.platform.repository.WalletRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("WalletService Unit Tests")
class WalletServiceTest {

  @Mock private WalletRepository walletRepository;

  @Mock private UserRepository userRepository;

  @Mock private TransactionRepository transactionRepository;

  @InjectMocks private WalletService walletService;

  private User testUser;
  private Wallet testWallet;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setEmail("test@example.com");
    testUser.setFirstName("Test");
    testUser.setLastName("User");

    testWallet = new Wallet();
    testWallet.setId(1L);
    testWallet.setUser(testUser);
    testWallet.setWalletNumber("WAL123456");
    testWallet.setBalance(BigDecimal.valueOf(1000));
    testWallet.setCurrency("USD");
    testWallet.setDailyLimit(BigDecimal.valueOf(5000));
    testWallet.setTransactionLimit(BigDecimal.valueOf(1000));
    testWallet.setTotalDailySpent(BigDecimal.ZERO);
    testWallet.setStatus(WalletStatus.ACTIVE);
  }

  @Test
  @DisplayName("Should get wallet info successfully")
  void shouldGetWalletInfo() {
    // Given
    when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(testWallet));

    // When
    WalletResponse response = walletService.getWalletInfo(1L);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getWalletNumber()).isEqualTo("WAL123456");
    assertThat(response.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1000));
    assertThat(response.getCurrency()).isEqualTo("USD");
    verify(walletRepository).findByUserId(1L);
  }

  @Test
  @DisplayName("Should throw exception when wallet not found")
  void shouldThrowExceptionWhenWalletNotFound() {
    // Given
    when(walletRepository.findByUserId(999L)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> walletService.getWalletInfo(999L))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("Should deposit successfully")
  void shouldDepositSuccessfully() {
    // Given
    BigDecimal depositAmount = BigDecimal.valueOf(500);
    when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(testWallet));
    when(transactionRepository.save(any(Transaction.class)))
        .thenAnswer(
            invocation -> {
              Transaction tx = invocation.getArgument(0);
              tx.setId(1L);
              return tx;
            });

    // When
    TransactionResponse response = walletService.deposit(1L, depositAmount, "USD");

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getAmount()).isEqualByComparingTo(depositAmount);
    assertThat(response.getType()).isEqualTo(TransactionType.DEPOSIT);
    verify(walletRepository).save(testWallet);
    verify(transactionRepository).save(any(Transaction.class));
  }

  @Test
  @DisplayName("Should withdraw successfully")
  void shouldWithdrawSuccessfully() {
    // Given
    BigDecimal withdrawAmount = BigDecimal.valueOf(300);
    when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(testWallet));
    when(transactionRepository.save(any(Transaction.class)))
        .thenAnswer(
            invocation -> {
              Transaction tx = invocation.getArgument(0);
              tx.setId(1L);
              return tx;
            });

    // When
    TransactionResponse response = walletService.withdraw(1L, withdrawAmount, "USD");

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getAmount()).isEqualByComparingTo(withdrawAmount);
    assertThat(response.getType()).isEqualTo(TransactionType.WITHDRAWAL);
    verify(walletRepository).save(testWallet);
  }

  @Test
  @DisplayName("Should throw exception when insufficient balance")
  void shouldThrowExceptionWhenInsufficientBalance() {
    // Given
    BigDecimal withdrawAmount = BigDecimal.valueOf(2000);
    when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(testWallet));

    // When & Then
    assertThatThrownBy(() -> walletService.withdraw(1L, withdrawAmount, "USD"))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("Should throw exception when transaction limit exceeded")
  void shouldThrowExceptionWhenTransactionLimitExceeded() {
    // Given
    BigDecimal withdrawAmount = BigDecimal.valueOf(1500);
    when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(testWallet));

    // When & Then
    assertThatThrownBy(() -> walletService.withdraw(1L, withdrawAmount, "USD"))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("Should update wallet limits successfully")
  void shouldUpdateWalletLimits() {
    // Given
    UpdateLimitsRequest request = new UpdateLimitsRequest();
    request.setDailyLimit(BigDecimal.valueOf(10000));
    request.setTransactionLimit(BigDecimal.valueOf(2000));

    when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(testWallet));
    when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);

    // When
    WalletResponse response = walletService.updateWalletLimits(1L, request);

    // Then
    assertThat(response).isNotNull();
    assertThat(testWallet.getDailyLimit()).isEqualByComparingTo(BigDecimal.valueOf(10000));
    assertThat(testWallet.getTransactionLimit()).isEqualByComparingTo(BigDecimal.valueOf(2000));
    verify(walletRepository).save(testWallet);
  }

  @Test
  @DisplayName("Should get balance by user ID")
  void shouldGetBalanceByUserId() {
    // Given
    when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(testWallet));

    // When
    BalanceResponse response = walletService.getBalance(1L);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1000));
    assertThat(response.getCurrency()).isEqualTo("USD");
  }
}
