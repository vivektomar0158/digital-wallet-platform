package com.digitalwallet.platform.dto;

import com.digitalwallet.platform.model.TransactionStatus;
import com.digitalwallet.platform.model.TransactionType;
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
public class TransactionResponse {
  private Long transactionId; // Changed from 'id' to 'transactionId'
  private String referenceId;
  private BigDecimal amount;
  private String currency;
  private TransactionType type;
  private TransactionStatus status;
  private String description;
  private String senderWallet;
  private String receiverWallet;
  private LocalDateTime createdAt;
  private LocalDateTime completedAt;
}
