package com.digitalwallet.platform.dto;

import com.digitalwallet.platform.model.WalletStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse implements java.io.Serializable {
  private static final long serialVersionUID = 1L;
  private Long walletId; // Changed from 'id' to 'walletId'
  private String walletNumber;
  private BigDecimal balance;
  private String currency;
  private WalletStatus status;
  private BigDecimal dailyLimit;
  private BigDecimal transactionLimit;
  private BigDecimal totalDailySpent;
  private BigDecimal totalMonthlySpent;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
