package com.digitalwallet.platform.service.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(
    name = "spring.cloud.aws.sqs.enabled",
    havingValue = "false",
    matchIfMissing = true)
public class NoOpTransactionNotificationService implements TransactionNotificationService {

  @Override
  public void sendTransactionNotification(String message) {
    log.debug("SQS disabled — notification skipped: {}", message);
  }
}
