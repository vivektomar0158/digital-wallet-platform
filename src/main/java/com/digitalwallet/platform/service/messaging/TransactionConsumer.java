package com.digitalwallet.platform.service.messaging;

import com.digitalwallet.platform.dto.TransactionEvent;
import com.digitalwallet.platform.model.Transaction;
import com.digitalwallet.platform.model.TransactionStatus;
import com.digitalwallet.platform.service.EmailService;
import com.digitalwallet.platform.service.PdfReceiptService;
import com.digitalwallet.platform.service.WalletService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionConsumer {

  private final WalletService walletService;
  private final PdfReceiptService pdfReceiptService;
  private final EmailService emailService;

  @Retryable(
      retryFor = {OptimisticLockingFailureException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 100))
  @SqsListener("transaction-queue")
  @Transactional
  public void receiveTransactionEvent(TransactionEvent event) {
    log.info("Received transaction event. Ref: {}", event.getTransactionReferenceId());
    try {
      Transaction transaction = walletService.executeAsyncTransfer(event);

      if (transaction.getStatus() == TransactionStatus.COMPLETED) {
        log.info("Transaction {} completed. Generating receipt...", transaction.getReferenceId());
        byte[] pdf = pdfReceiptService.generateTransactionReceipt(transaction);

        String userEmail = transaction.getFromWallet().getUser().getEmail();
        emailService.sendTransactionEmail(
            userEmail,
            "Transaction Receipt - " + transaction.getReferenceId(),
            "Hello, your transaction has been processed successfully. Attached is your receipt.",
            pdf,
            "Receipt-" + transaction.getReferenceId() + ".pdf");
      }

      log.info(
          "Successfully processed transaction event. Ref: {}", event.getTransactionReferenceId());
    } catch (Exception e) {
      log.error(
          "Error processing transaction event. Ref: {}. Error: {}",
          event.getTransactionReferenceId(),
          e.getMessage());
      // Depending on error type, we might want to throw to DLQ or just log if it's a
      // "business" failure (which is handled inside executeAsyncTransfer to mark TX
      // as FAILED)
      // If executeAsyncTransfer swallowed the exception and marked TX as failed, we
      // shouldn't throw here,
      // unless we want SQS to retry (e.g. DB connection issues).
      // For now, WalletService catches generic exceptions and marks FAILED, so we
      // don't re-throw.
    }
  }
}
