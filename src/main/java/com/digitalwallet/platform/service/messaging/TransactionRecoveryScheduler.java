package com.digitalwallet.platform.service.messaging;

import com.digitalwallet.platform.dto.TransactionEvent;
import com.digitalwallet.platform.model.Transaction;
import com.digitalwallet.platform.model.TransactionStatus;
import com.digitalwallet.platform.repository.TransactionRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionRecoveryScheduler {

  private final TransactionRepository transactionRepository;
  private final TransactionProducer transactionProducer;

  @Scheduled(fixedDelay = 60000) // Run every minute
  @Transactional
  public void recoverStuckTransactions() {
    log.debug("Checking for stuck PENDING transactions...");

    LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(5);
    List<Transaction> stuckTransactions =
        transactionRepository.findByStatusAndCreatedAtBefore(TransactionStatus.PENDING, cutoffTime);

    if (stuckTransactions.isEmpty()) {
      return;
    }

    log.info(
        "Found {} stuck PENDING transactions. Attempting recovery...", stuckTransactions.size());

    for (Transaction transaction : stuckTransactions) {
      try {
        // Re-construct event
        // Note: We need to be careful. In a real system, we'd check if the message is
        // actually in DLQ or lost.
        // Here we just re-submit blindly, trusting the consumer's idempotency check.

        String receiverWallet =
            transaction.getToWallet() != null ? transaction.getToWallet().getWalletNumber() : null;
        if (receiverWallet == null) {
          log.error(
              "Cannot recover transaction {}: Receiver wallet missing",
              transaction.getReferenceId());
          continue;
        }

        TransactionEvent event =
            TransactionEvent.builder()
                .transactionReferenceId(transaction.getReferenceId())
                .senderId(
                    transaction
                        .getFromWallet()
                        .getUser()
                        .getId()) // Assuming fetch join or lazy load
                // works
                .transactionId(transaction.getId())
                .receiverWalletNumber(receiverWallet)
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .description(transaction.getDescription())
                .build();

        transactionProducer.sendTransactionEvent(event);
        log.info("Re-submitted transaction {} to SQS", transaction.getReferenceId());

      } catch (Exception e) {
        log.error("Failed to recover transaction {}", transaction.getReferenceId(), e);
      }
    }
  }
}
