package com.digitalwallet.platform.service;

import com.digitalwallet.platform.dto.*;
import com.digitalwallet.platform.model.*;
import com.digitalwallet.platform.repository.TransactionRepository;
import com.digitalwallet.platform.repository.UserRepository;
import com.digitalwallet.platform.repository.WalletRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

  private final WalletRepository walletRepository;
  private final UserRepository userRepository;
  private final TransactionRepository transactionRepository;

  @Transactional(readOnly = true)
  @Cacheable(value = "wallets", key = "#userId", cacheManager = "redisCacheManager")
  public WalletResponse getWalletInfo(Long userId) {
    log.info("Getting wallet info for user ID: {}", userId);

    Wallet wallet =
        walletRepository
            .findByUserId(userId)
            .orElseThrow(
                () -> {
                  log.error("Wallet not found for user ID: {}", userId);
                  return new RuntimeException("Wallet not found for user ID: " + userId);
                });

    log.info("Found wallet: {} with balance: {}", wallet.getWalletNumber(), wallet.getBalance());

    return WalletResponse.builder()
        .walletId(wallet.getId())
        .walletNumber(wallet.getWalletNumber())
        .balance(wallet.getBalance())
        .currency(wallet.getCurrency())
        .status(wallet.getStatus())
        .dailyLimit(wallet.getDailyLimit())
        .transactionLimit(wallet.getTransactionLimit())
        .totalDailySpent(
            wallet.getTotalDailySpent() != null ? wallet.getTotalDailySpent() : BigDecimal.ZERO)
        .totalMonthlySpent(
            wallet.getTotalMonthlySpent() != null ? wallet.getTotalMonthlySpent() : BigDecimal.ZERO)
        .createdAt(wallet.getCreatedAt())
        .updatedAt(wallet.getUpdatedAt())
        .build();
  }

  @Transactional(readOnly = true)
  public BigDecimal getBalance(String email) {
    log.info("Getting balance for user: {}", email);

    var user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found: " + email));

    Wallet wallet =
        walletRepository
            .findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("Wallet not found for user: " + email));

    log.info("Balance for user {}: {}", email, wallet.getBalance());
    return wallet.getBalance();
  }

  @Transactional(readOnly = true)
  public BalanceResponse getBalance(Long userId) {
    log.info("Getting balance for user ID: {}", userId);

    Wallet wallet =
        walletRepository
            .findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Wallet not found for user ID: " + userId));

    log.info("Balance for user ID {}: {}", userId, wallet.getBalance());

    return BalanceResponse.builder()
        .balance(wallet.getBalance())
        .currency(wallet.getCurrency())
        .walletNumber(wallet.getWalletNumber())
        .walletStatus(wallet.getStatus().name())
        .lastUpdated(LocalDateTime.now())
        .build();
  }

  public Map<String, Object> getWalletDetails(Long userId) {
    log.info("Getting wallet details for user ID: {}", userId);

    Wallet wallet =
        walletRepository
            .findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Wallet not found for user ID: " + userId));

    BigDecimal remainingDailyLimit = BigDecimal.ZERO;
    if (wallet.getDailyLimit() != null && wallet.getTotalDailySpent() != null) {
      remainingDailyLimit = wallet.getDailyLimit().subtract(wallet.getTotalDailySpent());
      if (remainingDailyLimit.compareTo(BigDecimal.ZERO) < 0) {
        remainingDailyLimit = BigDecimal.ZERO;
      }
    }

    BigDecimal monthlyLimit = new BigDecimal("100000.00");
    BigDecimal monthlySpent =
        wallet.getTotalMonthlySpent() != null ? wallet.getTotalMonthlySpent() : BigDecimal.ZERO;
    BigDecimal remainingMonthlyLimit = monthlyLimit.subtract(monthlySpent);
    if (remainingMonthlyLimit.compareTo(BigDecimal.ZERO) < 0) {
      remainingMonthlyLimit = BigDecimal.ZERO;
    }

    return Map.ofEntries(
        Map.entry("walletNumber", wallet.getWalletNumber()),
        Map.entry("balance", wallet.getBalance()),
        Map.entry("currency", wallet.getCurrency()),
        Map.entry("status", wallet.getStatus()),
        Map.entry("dailyLimit", wallet.getDailyLimit()),
        Map.entry("transactionLimit", wallet.getTransactionLimit()),
        Map.entry(
            "dailySpent",
            wallet.getTotalDailySpent() != null ? wallet.getTotalDailySpent() : BigDecimal.ZERO),
        Map.entry("monthlySpent", monthlySpent),
        Map.entry("remainingDailyLimit", remainingDailyLimit),
        Map.entry("remainingMonthlyLimit", remainingMonthlyLimit),
        Map.entry("createdAt", wallet.getCreatedAt()),
        Map.entry("updatedAt", wallet.getUpdatedAt()));
  }

  @Transactional(readOnly = true)
  public PagedResponse<TransactionResponse> getTransactionHistory(
      Long userId,
      int page,
      int size,
      LocalDateTime startDate,
      LocalDateTime endDate,
      TransactionType type,
      TransactionStatus status) {

    log.info("Getting transaction history for user ID: {}, page: {}, size: {}", userId, page, size);

    org.springframework.data.domain.Pageable pageable =
        org.springframework.data.domain.PageRequest.of(
            page, size, org.springframework.data.domain.Sort.by("created_at").descending());

    // Convert parameters to Strings for native query
    String startDateStr = startDate != null ? startDate.toString() : null;
    String endDateStr = endDate != null ? endDate.toString() : null;
    String typeStr = type != null ? type.name() : null;
    String statusStr = status != null ? status.name() : null;

    org.springframework.data.domain.Page<Transaction> transactionPage =
        transactionRepository.findTransactionsWithFilters(
            userId, startDateStr, endDateStr, typeStr, statusStr, pageable);

    log.info(
        "Found {} transactions (total: {})",
        transactionPage.getNumberOfElements(),
        transactionPage.getTotalElements());

    List<TransactionResponse> content =
        transactionPage.getContent().stream()
            .map(this::mapToTransactionResponse)
            .collect(Collectors.toList());

    return PagedResponse.<TransactionResponse>builder()
        .content(content)
        .page(transactionPage.getNumber())
        .size(transactionPage.getSize())
        .totalElements(transactionPage.getTotalElements())
        .totalPages(transactionPage.getTotalPages())
        .last(transactionPage.isLast())
        .build();
  }

  public WalletStatementResponse getWalletStatement(
      Long userId, LocalDateTime startDate, LocalDateTime endDate) {
    log.info("Getting wallet statement for user ID: {}", userId);

    Wallet wallet =
        walletRepository
            .findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Wallet not found"));

    final LocalDateTime statementStartDate =
        startDate != null ? startDate : LocalDateTime.now().minusDays(30);
    final LocalDateTime statementEndDate = endDate != null ? endDate : LocalDateTime.now();

    log.info("Statement period: {} to {}", statementStartDate, statementEndDate);

    List<Transaction> allTransactions =
        transactionRepository.findByFromWalletUserIdOrToWalletUserId(userId, userId);

    List<Transaction> filteredTransactions =
        allTransactions.stream()
            .filter(
                tx ->
                    !tx.getCreatedAt().isBefore(statementStartDate)
                        && !tx.getCreatedAt().isAfter(statementEndDate))
            .collect(Collectors.toList());

    log.info("Filtered to {} transactions for statement", filteredTransactions.size());

    BigDecimal totalDeposits =
        filteredTransactions.stream()
            .filter(
                tx -> tx.getToWallet() != null && tx.getToWallet().getId().equals(wallet.getId()))
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalWithdrawals =
        filteredTransactions.stream()
            .filter(
                tx ->
                    tx.getFromWallet() != null && tx.getFromWallet().getId().equals(wallet.getId()))
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalTransfers =
        filteredTransactions.stream()
            .filter(tx -> tx.getType() != null && tx.getType() == TransactionType.TRANSFER)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal openingBalance = wallet.getBalance().add(totalWithdrawals).subtract(totalDeposits);

    log.info(
        "Statement summary - Opening: {}, Closing: {}, Deposits: {}, Withdrawals: {}",
        openingBalance,
        wallet.getBalance(),
        totalDeposits,
        totalWithdrawals);

    return WalletStatementResponse.builder()
        .walletNumber(wallet.getWalletNumber())
        .periodStart(statementStartDate)
        .periodEnd(statementEndDate)
        .openingBalance(openingBalance)
        .closingBalance(wallet.getBalance())
        .totalCredits(totalDeposits)
        .totalDebits(totalWithdrawals)
        .totalTransfers(totalTransfers)
        .transactions(
            filteredTransactions.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList()))
        .generatedAt(LocalDateTime.now())
        .build();
  }

  @Transactional
  @CacheEvict(value = "wallets", key = "#userId", cacheManager = "redisCacheManager")
  public WalletResponse updateWalletLimits(Long userId, UpdateLimitsRequest request) {
    log.info("Updating wallet limits for user ID: {}", userId);

    Wallet wallet =
        walletRepository
            .findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Wallet not found"));

    if (request.getDailyLimit() != null) {
      wallet.setDailyLimit(request.getDailyLimit());
      log.info("Updated daily limit to: {}", request.getDailyLimit());
    }
    if (request.getTransactionLimit() != null) {
      wallet.setTransactionLimit(request.getTransactionLimit());
      log.info("Updated transaction limit to: {}", request.getTransactionLimit());
    }

    Wallet updatedWallet = walletRepository.save(wallet);
    log.info("Wallet limits updated successfully");

    return WalletResponse.builder()
        .walletId(updatedWallet.getId())
        .walletNumber(updatedWallet.getWalletNumber())
        .balance(updatedWallet.getBalance())
        .currency(updatedWallet.getCurrency())
        .status(updatedWallet.getStatus())
        .dailyLimit(updatedWallet.getDailyLimit())
        .transactionLimit(updatedWallet.getTransactionLimit())
        .totalDailySpent(
            updatedWallet.getTotalDailySpent() != null
                ? updatedWallet.getTotalDailySpent()
                : BigDecimal.ZERO)
        .totalMonthlySpent(
            updatedWallet.getTotalMonthlySpent() != null
                ? updatedWallet.getTotalMonthlySpent()
                : BigDecimal.ZERO)
        .createdAt(updatedWallet.getCreatedAt())
        .updatedAt(updatedWallet.getUpdatedAt())
        .build();
  }

  @Transactional
  @CacheEvict(value = "wallets", key = "#userId", cacheManager = "redisCacheManager")
  public Map<String, Object> resetSpendingLimits(Long userId) {
    log.info("Resetting spending limits for user ID: {}", userId);

    Wallet wallet =
        walletRepository
            .findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Wallet not found"));

    wallet.setTotalDailySpent(BigDecimal.ZERO);
    boolean monthlyReset = false;

    if (LocalDateTime.now().getDayOfMonth() == 1) {
      wallet.setTotalMonthlySpent(BigDecimal.ZERO);
      monthlyReset = true;
      log.info("Monthly spending also reset (1st of month)");
    }

    walletRepository.save(wallet);
    log.info("Spending limits reset successfully");

    return Map.of(
        "message",
        "Spending limits reset successfully",
        "dailySpentReset",
        true,
        "monthlySpentReset",
        monthlyReset,
        "timestamp",
        LocalDateTime.now());
  }

  // === TRANSACTION METHODS ===

  @Transactional
  @CacheEvict(value = "wallets", key = "#userId", cacheManager = "redisCacheManager")
  public TransactionResponse deposit(Long userId, BigDecimal amount, String currency) {
    log.info(
        "Processing deposit for user ID: {}, Amount: {}, Currency: {}", userId, amount, currency);

    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new RuntimeException("Deposit amount must be greater than zero");
    }

    Wallet wallet =
        walletRepository
            .findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Wallet not found for user ID: " + userId));

    if (wallet.getStatus() != WalletStatus.ACTIVE) {
      throw new RuntimeException("Wallet is not active. Current status: " + wallet.getStatus());
    }

    Transaction transaction =
        Transaction.builder()
            .referenceId(generateReferenceId())
            .amount(amount)
            .currency(currency)
            .type(TransactionType.DEPOSIT)
            .status(TransactionStatus.PENDING)
            .description("Wallet deposit")
            .toWallet(wallet)
            .fromWallet(null)
            .createdAt(LocalDateTime.now())
            .build();

    BigDecimal newBalance = wallet.getBalance().add(amount);
    wallet.setBalance(newBalance);
    wallet.setUpdatedAt(LocalDateTime.now());

    transaction.setStatus(TransactionStatus.COMPLETED);
    transaction.setCompletedAt(LocalDateTime.now());

    walletRepository.save(wallet);
    transactionRepository.save(transaction);

    log.info("Deposit successful. New balance: {}", newBalance);

    return mapToTransactionResponse(transaction);
  }

  @Transactional
  @CacheEvict(value = "wallets", key = "#userId", cacheManager = "redisCacheManager")
  public TransactionResponse withdraw(Long userId, BigDecimal amount, String currency) {
    log.info(
        "Processing withdrawal for user ID: {}, Amount: {}, Currency: {}",
        userId,
        amount,
        currency);

    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new RuntimeException("Withdrawal amount must be greater than zero");
    }

    Wallet wallet =
        walletRepository
            .findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Wallet not found for user ID: " + userId));

    if (wallet.getStatus() != WalletStatus.ACTIVE) {
      throw new RuntimeException("Wallet is not active. Current status: " + wallet.getStatus());
    }

    if (wallet.getBalance().compareTo(amount) < 0) {
      throw new RuntimeException(
          "Insufficient balance. Available: " + wallet.getBalance() + ", Required: " + amount);
    }

    if (wallet.getTransactionLimit() != null
        && amount.compareTo(wallet.getTransactionLimit()) > 0) {
      throw new RuntimeException(
          "Amount exceeds transaction limit. Limit: " + wallet.getTransactionLimit());
    }

    BigDecimal dailySpent =
        wallet.getTotalDailySpent() != null ? wallet.getTotalDailySpent() : BigDecimal.ZERO;
    BigDecimal potentialDailyTotal = dailySpent.add(amount);
    if (wallet.getDailyLimit() != null
        && potentialDailyTotal.compareTo(wallet.getDailyLimit()) > 0) {
      throw new RuntimeException(
          "Amount exceeds daily spending limit. Daily limit: "
              + wallet.getDailyLimit()
              + ", Already spent: "
              + dailySpent);
    }

    Transaction transaction =
        Transaction.builder()
            .referenceId(generateReferenceId())
            .amount(amount)
            .currency(currency)
            .type(TransactionType.WITHDRAWAL)
            .status(TransactionStatus.PENDING)
            .description("Wallet withdrawal")
            .fromWallet(wallet)
            .toWallet(null)
            .createdAt(LocalDateTime.now())
            .build();

    BigDecimal newBalance = wallet.getBalance().subtract(amount);
    wallet.setBalance(newBalance);
    wallet.setTotalDailySpent(potentialDailyTotal);
    wallet.setUpdatedAt(LocalDateTime.now());

    transaction.setStatus(TransactionStatus.COMPLETED);
    transaction.setCompletedAt(LocalDateTime.now());

    walletRepository.save(wallet);
    transactionRepository.save(transaction);

    log.info("Withdrawal successful. New balance: {}", newBalance);

    return mapToTransactionResponse(transaction);
  }

  // Injected Producer (optional - do not fail if bean is absent)
  @Autowired(required = false)
  private com.digitalwallet.platform.service.messaging.TransactionProducer transactionProducer;

  @Transactional
  public TransferResponse initiateTransfer(
      Long userId,
      String receiverWalletNumber,
      BigDecimal amount,
      String currency,
      String description) {
    log.info(
        "Initiating async transfer from user ID: {} to wallet: {}, Amount: {}",
        userId,
        receiverWalletNumber,
        amount);

    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new RuntimeException("Transfer amount must be greater than zero");
    }

    if (receiverWalletNumber == null || receiverWalletNumber.trim().isEmpty()) {
      throw new RuntimeException("Receiver wallet number is required");
    }

    Wallet senderWallet =
        walletRepository
            .findByUserId(userId)
            .orElseThrow(
                () -> new RuntimeException("Sender wallet not found for user ID: " + userId));

    if (senderWallet.getStatus() != WalletStatus.ACTIVE) {
      throw new RuntimeException(
          "Sender wallet is not active. Current status: " + senderWallet.getStatus());
    }

    if (receiverWalletNumber.equals(senderWallet.getWalletNumber())) {
      throw new RuntimeException("Cannot transfer to your own wallet");
    }

    // Preliminary check (balance validation) happen here to fail fast
    // Note: The actual concurrent check happens in executeAsyncTransfer with
    // Optimistic Locking
    if (senderWallet.getBalance().compareTo(amount) < 0) {
      throw new RuntimeException("Insufficient balance. Available: " + senderWallet.getBalance());
    }

    // Reference ID generated here
    String referenceId = generateReferenceId();

    // Create PENDING Transaction (Outbox Pattern)
    Transaction transaction =
        Transaction.builder()
            .referenceId(referenceId)
            .amount(amount)
            .currency(currency)
            .type(TransactionType.TRANSFER)
            .status(TransactionStatus.PENDING)
            .description(description != null ? description : "Wallet transfer")
            .fromWallet(senderWallet)
            .toWallet(
                walletRepository
                    .findByWalletNumber(receiverWalletNumber)
                    .orElseThrow(() -> new RuntimeException("Receiver wallet not found")))
            .createdAt(LocalDateTime.now())
            .build();

    transactionRepository.save(transaction);

    // Send to SQS (Async) — only if producer bean exists
    TransactionEvent event =
        TransactionEvent.builder()
            .transactionReferenceId(referenceId)
            .senderId(userId)
            .transactionId(transaction.getId())
            .receiverWalletNumber(receiverWalletNumber)
            .amount(amount)
            .currency(currency)
            .description(description)
            .build();

    if (transactionProducer != null) {
      transactionProducer.sendTransactionEvent(event);
    } else {
      log.debug(
          "TransactionProducer bean not available — skipping async send for transaction {}",
          referenceId);
    }

    log.info("Transfer initiated. Reference: {}", referenceId);

    return TransferResponse.builder()
        .transactionId(transaction.getId())
        .referenceId(referenceId)
        .status(TransactionStatus.PENDING)
        .amount(amount)
        .currency(currency)
        .senderWallet(senderWallet.getWalletNumber())
        .receiverWallet(receiverWalletNumber)
        .timestamp(LocalDateTime.now())
        .build();
  }

  @Transactional
  @CacheEvict(value = "wallets", allEntries = true, cacheManager = "redisCacheManager")
  public Transaction executeAsyncTransfer(TransactionEvent event) {
    log.info("Executing async transfer for reference: {}", event.getTransactionReferenceId());

    Transaction transaction =
        transactionRepository
            .findById(event.getTransactionId())
            .orElseThrow(
                () -> new RuntimeException("Transaction not found: " + event.getTransactionId()));

    // Idempotency Check
    if (transaction.getStatus() == TransactionStatus.COMPLETED
        || transaction.getStatus() == TransactionStatus.FAILED) {
      log.warn("Transaction {} already processed. Skipping.", event.getTransactionReferenceId());
      return transaction;
    }

    try {
      Wallet senderWallet = transaction.getFromWallet();
      Wallet receiverWallet = transaction.getToWallet();

      // Re-validate Balance (Optimistic Locking will handle race conditions on save)
      if (senderWallet.getBalance().compareTo(event.getAmount()) < 0) {
        throw new RuntimeException("Insufficient balance during async execution");
      }

      // Spending Limit Check
      BigDecimal dailySpent =
          senderWallet.getTotalDailySpent() != null
              ? senderWallet.getTotalDailySpent()
              : BigDecimal.ZERO;
      BigDecimal potentialDailyTotal = dailySpent.add(event.getAmount());
      if (senderWallet.getDailyLimit() != null
          && potentialDailyTotal.compareTo(senderWallet.getDailyLimit()) > 0) {
        throw new RuntimeException("Daily limit exceeded during async execution");
      }

      // Update Sender
      BigDecimal senderNewBalance = senderWallet.getBalance().subtract(event.getAmount());
      senderWallet.setBalance(senderNewBalance);
      senderWallet.setTotalDailySpent(potentialDailyTotal);
      senderWallet.setUpdatedAt(LocalDateTime.now());

      // Update Receiver
      BigDecimal receiverNewBalance = receiverWallet.getBalance().add(event.getAmount());
      receiverWallet.setBalance(receiverNewBalance);
      receiverWallet.setUpdatedAt(LocalDateTime.now());

      // Save Wallets (Will throw ObjectOptimisticLockingFailureException if version
      // mismatch)
      walletRepository.save(senderWallet);
      walletRepository.save(receiverWallet);

      // Update Transaction
      transaction.setStatus(TransactionStatus.COMPLETED);
      transaction.setCompletedAt(LocalDateTime.now());
      transactionRepository.save(transaction);

      log.info("Async transfer completed successfully.");
      return transaction;

    } catch (Exception e) {
      log.error("Transfer failed for reference: {}", event.getTransactionReferenceId(), e);
      transaction.setStatus(TransactionStatus.FAILED);
      transaction.setFailureReason(e.getMessage());
      return transactionRepository.save(transaction);
    }
  }

  // === HELPER METHODS ===

  private TransactionResponse mapToTransactionResponse(Transaction transaction) {
    return TransactionResponse.builder()
        .transactionId(transaction.getId())
        .referenceId(transaction.getReferenceId())
        .amount(transaction.getAmount())
        .currency(transaction.getCurrency())
        .type(transaction.getType())
        .status(transaction.getStatus())
        .description(transaction.getDescription())
        .senderWallet(
            transaction.getFromWallet() != null
                ? transaction.getFromWallet().getWalletNumber()
                : "SYSTEM")
        .receiverWallet(
            transaction.getToWallet() != null
                ? transaction.getToWallet().getWalletNumber()
                : "SYSTEM")
        .createdAt(transaction.getCreatedAt())
        .completedAt(transaction.getCompletedAt())
        .build();
  }

  @Transactional(readOnly = true)
  public TransactionResponse getTransactionByReferenceId(Long userId, String referenceId) {
    Transaction transaction =
        transactionRepository
            .findByReferenceId(referenceId)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));

    // Security check
    boolean isSender =
        transaction.getFromWallet() != null
            && transaction.getFromWallet().getUser().getId().equals(userId);
    boolean isReceiver =
        transaction.getToWallet() != null
            && transaction.getToWallet().getUser().getId().equals(userId);

    if (!isSender && !isReceiver) {
      throw new RuntimeException("Unauthorized access to transaction");
    }

    return mapToTransactionResponse(transaction);
  }

  private String generateReferenceId() {
    return "TXN"
        + System.currentTimeMillis()
        + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
  }
}
