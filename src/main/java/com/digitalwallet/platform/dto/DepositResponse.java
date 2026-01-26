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
public class DepositResponse {
  private Long transactionId;
  private String referenceId;
  private BigDecimal amount;
  private String currency;
  private BigDecimal newBalance;
  private LocalDateTime timestamp;
}
