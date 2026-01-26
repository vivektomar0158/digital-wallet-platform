package com.digitalwallet.platform.dto;

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
public class BalanceResponse {
  private BigDecimal balance;
  private String currency;
  private String walletNumber;
  private String walletStatus; // NOT 'status'
  private LocalDateTime lastUpdated;
}
