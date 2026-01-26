package com.digitalwallet.platform.dto;

import com.digitalwallet.platform.model.TransactionStatus;
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
public class TransferResponse {
  private Long transactionId;
  private String referenceId;
  private TransactionStatus status;
  private BigDecimal amount;
  private String currency;
  private String senderWallet;
  private String receiverWallet;
  private LocalDateTime timestamp;
}
