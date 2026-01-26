package com.digitalwallet.platform.controller;

import com.digitalwallet.platform.dto.*;
import com.digitalwallet.platform.security.CustomUserDetails;
import com.digitalwallet.platform.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Money transfer and transaction endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

  private final TransactionService transactionService;

  @Operation(
      summary = "Send money to another wallet",
      description = "Transfer money from your wallet to another user's wallet")
  @PostMapping("/transfer")
  public ResponseEntity<TransferResponse> transfer(
      @Valid @RequestBody TransferRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    TransferResponse response = transactionService.transfer(request, userDetails.getUser().getId());
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Deposit money to wallet",
      description = "Add money to your wallet (simulated deposit)")
  @PostMapping("/deposit")
  public ResponseEntity<DepositResponse> deposit(
      @Valid @RequestBody DepositRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    DepositResponse response = transactionService.deposit(request, userDetails.getUser().getId());
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Withdraw money from wallet",
      description = "Withdraw money from your wallet")
  @PostMapping("/withdraw")
  public ResponseEntity<WithdrawResponse> withdraw(
      @Valid @RequestBody WithdrawRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    WithdrawResponse response = transactionService.withdraw(request, userDetails.getUser().getId());
    return ResponseEntity.ok(response);
  }
}
