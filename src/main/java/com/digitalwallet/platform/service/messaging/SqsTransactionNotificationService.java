package com.digitalwallet.platform.service.messaging;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.cloud.aws.sqs.enabled", havingValue = "true")
public class SqsTransactionNotificationService implements TransactionNotificationService {

  private final SqsTemplate sqsTemplate;

  @Override
  public void sendTransactionNotification(String message) {
    sqsTemplate.send("transaction-queue", message);
    log.info("Transaction sent to SQS: {}", message);
  }
}
