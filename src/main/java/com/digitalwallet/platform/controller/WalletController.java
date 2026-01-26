package com.digitalwallet.platform.controller;

import com.digitalwallet.platform.dto.*;
import com.digitalwallet.platform.model.User;
import com.digitalwallet.platform.repository.UserRepository;
import com.digitalwallet.platform.security.CustomUserDetails;
import com.digitalwallet.platform.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Wallet", description = "Wallet management and operations")
@SecurityRequirement(name = "bearerAuth")
public class WalletController {

  private final WalletService walletService;
  private final UserRepository userRepository;

  // Helper method to get current user
  private User getCurrentUser(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new RuntimeException("User not authenticated");
    }

    Object principal = authentication.getPrincipal();

    // Prefer using the User already loaded by Spring Security to avoid repository mismatches
    if (principal instanceof CustomUserDetails customUserDetails) {
      User user = customUserDetails.getUser();
      log.debug(
          "Resolved current user from CustomUserDetails: {} (ID: {})",
          user.getEmail(),
          user.getId());
      return user;
    }

    String email = authentication.getName();
    log.debug("Getting user by email from repository: {}", email);

    return userRepository
        .findByEmail(email)
        .orElseThrow(
            () -> {
              log.error("User not found in repository with email: {}", email);
              return new RuntimeException("User not found: " + email);
            });
  }

  // === VIEW ENDPOINTS ===

  @Operation(
      summary = "Get wallet information",
      description = "Returns complete wallet details including balance, limits, and status")
  @GetMapping
  public ResponseEntity<?> getWallet(Authentication authentication) {
    try {
      User user = getCurrentUser(authentication);
      log.info("GET /api/wallet - User ID: {}", user.getId());
      WalletResponse wallet = walletService.getWalletInfo(user.getId());
      return ResponseEntity.ok(wallet);
    } catch (RuntimeException e) {
      log.error("Service error getting wallet: {}", e.getMessage());
      return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
      log.error("Unexpected error getting wallet: {}", e.getMessage(), e);
      return ResponseEntity.status(500).build();
    }
  }

  @Operation(summary = "Get wallet balance", description = "Returns current wallet balance")
  @GetMapping("/balance")
  public ResponseEntity<?> getBalance(Authentication authentication) {
    try {
      User user = getCurrentUser(authentication);
      log.info("GET /api/wallet/balance - User ID: {}", user.getId());
      BalanceResponse balance = walletService.getBalance(user.getId());
      return ResponseEntity.ok(balance);
    } catch (RuntimeException e) {
      log.error("Service error getting balance: {}", e.getMessage());
      return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
      log.error("Unexpected error getting balance: {}", e.getMessage(), e);
      return ResponseEntity.status(500).build();
    }
  }

  @Operation(
      summary = "Get wallet details",
      description = "Returns detailed wallet information including limits and spending")
  @GetMapping("/details")
  public ResponseEntity<?> getWalletDetails(Authentication authentication) {
    try {
      User user = getCurrentUser(authentication);
      log.info("GET /api/wallet/details - User ID: {}", user.getId());
      Map<String, Object> details = walletService.getWalletDetails(user.getId());
      return ResponseEntity.ok(details);
    } catch (RuntimeException e) {
      log.error("Service error getting wallet details: {}", e.getMessage());
      return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
      log.error("Unexpected error getting wallet details: {}", e.getMessage(), e);
      return ResponseEntity.status(500).build();
    }
  }

  @Operation(
      summary = "Get transaction history",
      description = "Returns list of transactions with pagination and filtering")
  @GetMapping("/transactions")
  public ResponseEntity<?> getTransactionHistory(
      Authentication authentication,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) LocalDateTime startDate,
      @RequestParam(required = false) LocalDateTime endDate,
      @RequestParam(required = false) com.digitalwallet.platform.model.TransactionType type,
      @RequestParam(required = false) com.digitalwallet.platform.model.TransactionStatus status) {
    try {
      User user = getCurrentUser(authentication);
      log.info(
          "GET /api/wallet/transactions - User ID: {}, Page: {}, Size: {}",
          user.getId(),
          page,
          size);
      PagedResponse<TransactionResponse> transactions =
          walletService.getTransactionHistory(
              user.getId(), page, size, startDate, endDate, type, status);
      return ResponseEntity.ok(transactions);
    } catch (Exception e) {
      log.error("Error getting transaction history: {}", e.getMessage(), e);
      // Return actual error for debugging
      return ResponseEntity.status(500)
          .body(
              PagedResponse.<TransactionResponse>builder()
                  .content(List.of())
                  .page(0)
                  .size(0)
                  .totalElements(0)
                  .totalPages(0)
                  .last(true)
                  .build());
    }
  }

  @Operation(
      summary = "Get wallet statement",
      description = "Returns wallet statement with transactions for a specific period")
  @GetMapping("/statement")
  public ResponseEntity<?> getWalletStatement(
      Authentication authentication,
      @RequestParam(required = false) LocalDateTime startDate,
      @RequestParam(required = false) LocalDateTime endDate) {
    try {
      User user = getCurrentUser(authentication);
      log.info("GET /api/wallet/statement - User ID: {}", user.getId());
      WalletStatementResponse statement =
          walletService.getWalletStatement(user.getId(), startDate, endDate);
      return ResponseEntity.ok(statement);
    } catch (RuntimeException e) {
      log.error("Service error getting wallet statement: {}", e.getMessage());
      return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
      log.error("Unexpected error getting wallet statement: {}", e.getMessage(), e);
      return ResponseEntity.status(500).build();
    }
  }

  // === TRANSACTION ENDPOINTS ===

  @Operation(summary = "Deposit money to wallet", description = "Add money to the user's wallet")
  @PostMapping("/deposit")
  public ResponseEntity<?> deposit(
      Authentication authentication, @Valid @RequestBody TransactionRequest request) {
    try {
      User user = getCurrentUser(authentication);
      log.info(
          "POST /api/wallet/deposit - User ID: {}, Amount: {}", user.getId(), request.getAmount());

      TransactionResponse response =
          walletService.deposit(user.getId(), request.getAmount(), request.getCurrency());
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error depositing: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @Operation(
      summary = "Withdraw money from wallet",
      description = "Withdraw money from the user's wallet")
  @PostMapping("/withdraw")
  public ResponseEntity<?> withdraw(
      Authentication authentication, @Valid @RequestBody TransactionRequest request) {
    try {
      User user = getCurrentUser(authentication);
      log.info(
          "POST /api/wallet/withdraw - User ID: {}, Amount: {}", user.getId(), request.getAmount());

      TransactionResponse response =
          walletService.withdraw(user.getId(), request.getAmount(), request.getCurrency());
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error withdrawing: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @Operation(
      summary = "Transfer money to another wallet",
      description = "Transfer money from your wallet to another user's wallet")
  @PostMapping("/transfer")
  public ResponseEntity<?> transfer(
      @RequestBody TransferRequest request, Authentication authentication) {
    try {
      User user = getCurrentUser(authentication);
      log.info(
          "POST /api/wallet/transfer - User ID: {}, To: {}, Amount: {}",
          user.getId(),
          request.getReceiverWalletNumber(),
          request.getAmount());

      TransferResponse response =
          walletService.initiateTransfer(
              user.getId(),
              request.getReceiverWalletNumber(),
              request.getAmount(),
              request.getCurrency(),
              request.getDescription());
      return ResponseEntity.accepted().body(response);
    } catch (RuntimeException e) {
      log.error("Error transferring: {}", e.getMessage(), e);
      return ResponseEntity.badRequest()
          .body(
              Map.of(
                  "error", e.getMessage(),
                  "timestamp", LocalDateTime.now()));
    } catch (Exception e) {
      log.error("Unexpected error during transfer: {}", e.getMessage(), e);
      return ResponseEntity.status(500)
          .body(Map.of("error", "Internal server error", "timestamp", LocalDateTime.now()));
    }
  }

  @Operation(
      summary = "Get transaction status",
      description = "Get the status of a transaction by its reference ID")
  @GetMapping("/transactions/{referenceId}")
  public ResponseEntity<?> getTransactionStatus(
      Authentication authentication, @PathVariable String referenceId) {
    try {
      User user = getCurrentUser(authentication);
      log.info("GET /api/wallet/transactions/{} - User ID: {}", referenceId, user.getId());

      TransactionResponse response =
          walletService.getTransactionByReferenceId(user.getId(), referenceId);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      log.error("Error getting transaction status: {}", e.getMessage());
      return ResponseEntity.notFound().build();
    }
  }

  // === LIMIT MANAGEMENT ENDPOINTS ===

  @Operation(
      summary = "Update wallet limits",
      description = "Update daily and transaction limits for the wallet")
  @PutMapping("/limits")
  public ResponseEntity<?> updateWalletLimits(
      Authentication authentication, @Valid @RequestBody UpdateLimitsRequest request) {
    try {
      User user = getCurrentUser(authentication);
      log.info("PUT /api/wallet/limits - User ID: {}", user.getId());
      WalletResponse wallet = walletService.updateWalletLimits(user.getId(), request);
      return ResponseEntity.ok(wallet);
    } catch (Exception e) {
      log.error("Error updating wallet limits: {}", e.getMessage(), e);
      return ResponseEntity.status(401).build();
    }
  }

  @Operation(
      summary = "Reset spending limits",
      description = "Reset daily and monthly spending counters (Admin/Scheduled use)")
  @PostMapping("/limits/reset")
  public ResponseEntity<Map<String, Object>> resetSpendingLimits(Authentication authentication) {
    try {
      User user = getCurrentUser(authentication);
      log.info("POST /api/wallet/limits/reset - User ID: {}", user.getId());
      Map<String, Object> result = walletService.resetSpendingLimits(user.getId());
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.error("Error resetting spending limits: {}", e.getMessage(), e);
      return ResponseEntity.status(401).build();
    }
  } // ← THIS BRACE WAS MISSING!

  @PostMapping("/test-parse")
  public ResponseEntity<Map<String, Object>> testParse(
      @RequestBody Map<String, Object> request, Authentication authentication) {

    Map<String, Object> response = new HashMap<>();

    try {
      log.info("=== RAW REQUEST RECEIVED ===");
      log.info("Full request map: {}", request);

      // Check each field
      for (Map.Entry<String, Object> entry : request.entrySet()) {
        log.info(
            "Field: {} | Type: {} | Value: {}",
            entry.getKey(),
            entry.getValue().getClass().getName(),
            entry.getValue());
      }

      // Try to create TransferRequest from map
      TransferRequest transferRequest = new TransferRequest();

      if (request.containsKey("receiverWalletNumber")) {
        transferRequest.setReceiverWalletNumber(request.get("receiverWalletNumber").toString());
        log.info("✓ receiverWalletNumber parsed: {}", transferRequest.getReceiverWalletNumber());
      }

      if (request.containsKey("amount")) {
        Object amountObj = request.get("amount");
        log.info("Amount object type: {}", amountObj.getClass().getName());
        log.info("Amount object value: {}", amountObj);

        try {
          if (amountObj instanceof Integer) {
            transferRequest.setAmount(new BigDecimal((Integer) amountObj));
          } else if (amountObj instanceof Double) {
            transferRequest.setAmount(BigDecimal.valueOf((Double) amountObj));
          } else if (amountObj instanceof Float) {
            transferRequest.setAmount(BigDecimal.valueOf((Float) amountObj));
          } else if (amountObj instanceof String) {
            transferRequest.setAmount(new BigDecimal((String) amountObj));
          } else if (amountObj instanceof BigDecimal) {
            transferRequest.setAmount((BigDecimal) amountObj);
          } else {
            transferRequest.setAmount(new BigDecimal(amountObj.toString()));
          }
          log.info("✓ Amount parsed as BigDecimal: {}", transferRequest.getAmount());
        } catch (Exception e) {
          log.error("Failed to parse amount: {}", e.getMessage());
        }
      }

      if (request.containsKey("currency")) {
        transferRequest.setCurrency(request.get("currency").toString());
        log.info("✓ Currency parsed: {}", transferRequest.getCurrency());
      }

      if (request.containsKey("description")) {
        transferRequest.setDescription(request.get("description").toString());
        log.info("✓ Description parsed: {}", transferRequest.getDescription());
      }

      // Build response
      response.put("status", "success");
      response.put("message", "Request parsed successfully");
      response.put(
          "parsedRequest",
          Map.of(
              "receiverWalletNumber", transferRequest.getReceiverWalletNumber(),
              "amount", transferRequest.getAmount(),
              "currency", transferRequest.getCurrency(),
              "description", transferRequest.getDescription()));
      response.put("rawRequest", request);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Error in test-parse: {}", e.getMessage(), e);
      response.put("status", "error");
      response.put("message", e.getMessage());
      response.put("error", e.getClass().getName());
      return ResponseEntity.badRequest().body(response);
    }
  }
}
