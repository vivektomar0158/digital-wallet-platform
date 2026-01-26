package com.digitalwallet.platform.service;

import com.digitalwallet.platform.dto.*;
import com.digitalwallet.platform.model.*;
import com.digitalwallet.platform.repository.TransactionRepository;
import com.digitalwallet.platform.repository.WalletRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

  private final TransactionRepository transactionRepository;
  private final WalletRepository walletRepository;

  @Transactional
  public TransferResponse transfer(TransferRequest request, Long senderUserId) {
    // Convert amount to BigDecimal (handles both Integer and BigDecimal from DTO)
    BigDecimal amount = convertToBigDecimal(request.getAmount());

    // 1. Validate sender has sufficient balance
    Wallet senderWallet =
        walletRepository
            .findByUserId(senderUserId)
            .orElseThrow(() -> new RuntimeException("Sender wallet not found"));

    if (senderWallet.getBalance().compareTo(amount) < 0) {
      throw new RuntimeException("Insufficient balance");
    }

    // 2. Find receiver wallet
    Wallet receiverWallet =
        walletRepository
            .findByWalletNumber(request.getReceiverWalletNumber())
            .orElseThrow(() -> new RuntimeException("Receiver wallet not found"));

    // 3. Check transaction limits
    validateTransactionLimits(senderWallet, amount);

    // 4. Create transaction record
    Transaction transaction =
        Transaction.builder()
            .fromWallet(senderWallet)
            .toWallet(receiverWallet)
            .amount(amount)
            .currency(request.getCurrency())
            .type(TransactionType.TRANSFER)
            .status(TransactionStatus.PROCESSING)
            .referenceId(generateReferenceId())
            .description(request.getDescription())
            .build();

    // 5. Update balances
    senderWallet.setBalance(senderWallet.getBalance().subtract(amount));
    receiverWallet.setBalance(receiverWallet.getBalance().add(amount));

    // 6. Update daily/monthly limits
    updateSpendingLimits(senderWallet, amount);

    // 7. Save everything
    walletRepository.save(senderWallet);
    walletRepository.save(receiverWallet);
    transaction.setStatus(TransactionStatus.COMPLETED);
    transaction.setCompletedAt(LocalDateTime.now());
    Transaction savedTransaction = transactionRepository.save(transaction);

    log.info(
        "P2P Transfer completed: {} {} from {} to {}",
        amount,
        request.getCurrency(),
        senderWallet.getWalletNumber(),
        receiverWallet.getWalletNumber());

    return TransferResponse.builder()
        .transactionId(savedTransaction.getId())
        .referenceId(savedTransaction.getReferenceId())
        .status(savedTransaction.getStatus())
        .amount(savedTransaction.getAmount())
        .currency(savedTransaction.getCurrency())
        .senderWallet(senderWallet.getWalletNumber())
        .receiverWallet(receiverWallet.getWalletNumber())
        .timestamp(LocalDateTime.now())
        .build();
  }

  @Transactional
  public DepositResponse deposit(DepositRequest request, Long userId) {
    // Convert amount to BigDecimal
    BigDecimal amount = convertToBigDecimal(request.getAmount());

    Wallet wallet =
        walletRepository
            .findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Wallet not found"));

    Transaction transaction =
        Transaction.builder()
            .toWallet(wallet)
            .amount(amount)
            .currency(request.getCurrency())
            .type(TransactionType.DEPOSIT)
            .status(TransactionStatus.COMPLETED)
            .referenceId(generateReferenceId())
            .description(request.getDescription())
            .completedAt(LocalDateTime.now())
            .build();

    // Update wallet balance
    wallet.setBalance(wallet.getBalance().add(amount));

    walletRepository.save(wallet);
    Transaction savedTransaction = transactionRepository.save(transaction);

    return DepositResponse.builder()
        .transactionId(savedTransaction.getId())
        .referenceId(savedTransaction.getReferenceId())
        .amount(savedTransaction.getAmount())
        .currency(savedTransaction.getCurrency())
        .newBalance(wallet.getBalance())
        .timestamp(LocalDateTime.now())
        .build();
  }

  @Transactional
  public WithdrawResponse withdraw(WithdrawRequest request, Long userId) {
    // Convert amount to BigDecimal
    BigDecimal amount = convertToBigDecimal(request.getAmount());

    Wallet wallet =
        walletRepository
            .findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Wallet not found"));

    if (wallet.getBalance().compareTo(amount) < 0) {
      throw new RuntimeException("Insufficient balance");
    }

    validateTransactionLimits(wallet, amount);

    Transaction transaction =
        Transaction.builder()
            .fromWallet(wallet)
            .amount(amount)
            .currency(request.getCurrency())
            .type(TransactionType.WITHDRAWAL)
            .status(TransactionStatus.COMPLETED)
            .referenceId(generateReferenceId())
            .description(request.getDescription())
            .completedAt(LocalDateTime.now())
            .build();

    // Update wallet balance
    wallet.setBalance(wallet.getBalance().subtract(amount));
    updateSpendingLimits(wallet, amount);

    walletRepository.save(wallet);
    Transaction savedTransaction = transactionRepository.save(transaction);

    return WithdrawResponse.builder()
        .transactionId(savedTransaction.getId())
        .referenceId(savedTransaction.getReferenceId())
        .amount(savedTransaction.getAmount())
        .currency(savedTransaction.getCurrency())
        .newBalance(wallet.getBalance())
        .timestamp(LocalDateTime.now())
        .build();
  }

  // Helper method to convert any number to BigDecimal
  private BigDecimal convertToBigDecimal(Object amount) {
    if (amount == null) {
      throw new RuntimeException("Amount cannot be null");
    }

    if (amount instanceof BigDecimal) {
      return (BigDecimal) amount;
    } else if (amount instanceof Integer) {
      return new BigDecimal((Integer) amount);
    } else if (amount instanceof Double) {
      return BigDecimal.valueOf((Double) amount);
    } else if (amount instanceof Long) {
      return BigDecimal.valueOf((Long) amount);
    } else if (amount instanceof String) {
      return new BigDecimal((String) amount);
    } else {
      return new BigDecimal(amount.toString());
    }
  }

  private void validateTransactionLimits(Wallet wallet, BigDecimal amount) {
    // Check daily limit
    if (wallet.getDailyLimit() != null
        && wallet.getTotalDailySpent().add(amount).compareTo(wallet.getDailyLimit()) > 0) {
      throw new RuntimeException("Exceeds daily transaction limit");
    }

    // Check transaction limit
    if (wallet.getTransactionLimit() != null
        && amount.compareTo(wallet.getTransactionLimit()) > 0) {
      throw new RuntimeException("Exceeds per-transaction limit");
    }
  }

  private void updateSpendingLimits(Wallet wallet, BigDecimal amount) {
    // Update daily spent
    wallet.setTotalDailySpent(wallet.getTotalDailySpent().add(amount));
    // Update monthly spent
    wallet.setTotalMonthlySpent(wallet.getTotalMonthlySpent().add(amount));
  }

  private String generateReferenceId() {
    return "TX"
        + System.currentTimeMillis()
        + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
  }
}
