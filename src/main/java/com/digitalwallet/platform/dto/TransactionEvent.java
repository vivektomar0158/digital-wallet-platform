package com.digitalwallet.platform.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent {
  private String transactionReferenceId;
  private Long senderId;
  private Long transactionId; // Database ID of the PENDING transaction
  private String receiverWalletNumber;
  private BigDecimal amount;
  private String currency;
  private String description;
}
