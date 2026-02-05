package com.digitalwallet.platform;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.s3.S3Client;

@SpringBootTest
@ActiveProfiles("test")
class PaymentPlatformApplicationTests {

  @MockBean private S3Client s3Client;
  @MockBean private SqsTemplate sqsTemplate;

  @Test
  void contextLoads() {}
}
