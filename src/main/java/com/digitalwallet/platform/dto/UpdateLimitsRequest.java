package com.digitalwallet.platform.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class UpdateLimitsRequest {
  @NotNull(message = "Daily limit is required")
  @DecimalMin(value = "0.00", message = "Daily limit must be positive")
  private BigDecimal dailyLimit;

  @NotNull(message = "Transaction limit is required")
  @DecimalMin(value = "0.00", message = "Transaction limit must be positive")
  private BigDecimal transactionLimit;
}
