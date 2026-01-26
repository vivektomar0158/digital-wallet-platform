package com.digitalwallet.platform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.digitalwallet.platform.dto.TransferRequest;
import com.digitalwallet.platform.dto.TransferResponse;
import com.digitalwallet.platform.model.*;
import com.digitalwallet.platform.repository.TransactionRepository;
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
@DisplayName("TransactionService Unit Tests")
class TransactionServiceTest {

  @Mock private TransactionRepository transactionRepository;

  @Mock private WalletRepository walletRepository;

  @InjectMocks private TransactionService transactionService;

  private Wallet senderWallet;
  private Wallet receiverWallet;
  private TransferRequest transferRequest;

  @BeforeEach
  void setUp() {
    senderWallet = new Wallet();
    senderWallet.setId(1L);
    senderWallet.setWalletNumber("WAL111");
    senderWallet.setBalance(BigDecimal.valueOf(1000));
    senderWallet.setDailyLimit(BigDecimal.valueOf(5000));
    senderWallet.setTransactionLimit(BigDecimal.valueOf(1000));
    senderWallet.setTotalDailySpent(BigDecimal.ZERO);
    senderWallet.setStatus(WalletStatus.ACTIVE);

    receiverWallet = new Wallet();
    receiverWallet.setId(2L);
    receiverWallet.setWalletNumber("WAL222");
    receiverWallet.setBalance(BigDecimal.valueOf(500));
    receiverWallet.setStatus(WalletStatus.ACTIVE);

    transferRequest = new TransferRequest();
    transferRequest.setReceiverWalletNumber("WAL222");
    transferRequest.setAmount(BigDecimal.valueOf(200));
    transferRequest.setCurrency("USD");
    transferRequest.setDescription("Test transfer");
  }

  @Test
  @DisplayName("Should transfer money successfully")
  void shouldTransferMoneySuccessfully() {
    // Given
    when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(senderWallet));
    when(walletRepository.findByWalletNumber("WAL222")).thenReturn(Optional.of(receiverWallet));
    when(transactionRepository.save(any(Transaction.class)))
        .thenAnswer(
            invocation -> {
              Transaction tx = invocation.getArgument(0);
              tx.setId(100L);
              return tx;
            });

    // When
    TransferResponse response = transactionService.transfer(transferRequest, 1L);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
    assertThat(senderWallet.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(800));
    assertThat(receiverWallet.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(700));
    verify(walletRepository).save(senderWallet);
    verify(walletRepository).save(receiverWallet);
    verify(transactionRepository).save(any(Transaction.class));
  }

  @Test
  @DisplayName("Should throw exception when sender wallet not found")
  void shouldThrowExceptionWhenSenderWalletNotFound() {
    // Given
    when(walletRepository.findByUserId(1L)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> transactionService.transfer(transferRequest, 1L))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Sender wallet not found");
  }

  @Test
  @DisplayName("Should throw exception when receiver wallet not found")
  void shouldThrowExceptionWhenReceiverWalletNotFound() {
    // Given
    when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(senderWallet));
    when(walletRepository.findByWalletNumber("WAL222")).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> transactionService.transfer(transferRequest, 1L))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Receiver wallet not found");
  }

  @Test
  @DisplayName("Should throw exception when insufficient balance")
  void shouldThrowExceptionWhenInsufficientBalance() {
    // Given
    transferRequest.setAmount(BigDecimal.valueOf(2000));
    when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(senderWallet));

    // When & Then
    assertThatThrownBy(() -> transactionService.transfer(transferRequest, 1L))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Insufficient balance");
  }

  @Test
  @DisplayName("Should throw exception when transaction limit exceeded")
  void shouldThrowExceptionWhenTransactionLimitExceeded() {
    // Given
    senderWallet.setBalance(BigDecimal.valueOf(5000));
    transferRequest.setAmount(BigDecimal.valueOf(1500));
    when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(senderWallet));
    when(walletRepository.findByWalletNumber("WAL222")).thenReturn(Optional.of(receiverWallet));

    // When & Then
    assertThatThrownBy(() -> transactionService.transfer(transferRequest, 1L))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Exceeds per-transaction limit");
  }
}
