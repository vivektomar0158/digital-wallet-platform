package com.digitalwallet.platform.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletStatementResponse {
  private String walletNumber;
  private LocalDateTime periodStart; // NOT 'statementPeriodStart'
  private LocalDateTime periodEnd; // NOT 'statementPeriodEnd'
  private BigDecimal openingBalance;
  private BigDecimal closingBalance;
  private BigDecimal totalCredits; // NOT 'totalDeposits'
  private BigDecimal totalDebits; // NOT 'totalWithdrawals'
  private BigDecimal totalTransfers;
  private List<TransactionResponse> transactions;
  private LocalDateTime generatedAt;
}
