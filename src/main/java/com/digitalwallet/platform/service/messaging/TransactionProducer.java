package com.digitalwallet.platform.service.messaging;

import com.digitalwallet.platform.dto.TransactionEvent;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.cloud.aws.sqs.enabled", havingValue = "true")
public class TransactionProducer {

  private final SqsTemplate sqsTemplate;
  private static final String QUEUE_NAME = "transaction-queue";

  public void sendTransactionEvent(TransactionEvent event) {
    log.info("Sending transaction event to SQS. Ref: {}", event.getTransactionReferenceId());
    try {
      sqsTemplate.send(to -> to.queue(QUEUE_NAME).payload(event));
      log.info(
          "Successfully sent transaction event for ref: {}", event.getTransactionReferenceId());
    } catch (Exception e) {
      log.error(
          "Failed to send transaction event to SQS for ref: {}",
          event.getTransactionReferenceId(),
          e);
      // In a real outbox pattern, we might mark the DB record as 'FAILED_SEND' here,
      // but for now we rely on the scheduler to pick up 'PENDING' ones.
      throw new RuntimeException("Failed to send SQS message", e);
    }
  }
}
