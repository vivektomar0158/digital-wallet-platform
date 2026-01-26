package com.digitalwallet.platform.integration;

import com.digitalwallet.platform.config.TestCacheConfig;
import com.digitalwallet.platform.service.messaging.TransactionConsumer;
import com.digitalwallet.platform.service.messaging.TransactionProducer;
import com.digitalwallet.platform.service.messaging.TransactionRecoveryScheduler;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestCacheConfig.class)
@ActiveProfiles("test")
@TestPropertySource(
    properties = {
      "management.health.redis.enabled=false",
      "spring.autoconfigure.exclude=org.springframework.boot.actuate.autoconfigure.data.redis.RedisReactiveHealthContributorAutoConfiguration,org.springframework.boot.actuate.autoconfigure.data.redis.RedisHealthContributorAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
    })
public abstract class AbstractIntegrationTest {

  @MockBean protected S3Client s3Client;
  @MockBean protected SqsAsyncClient sqsAsyncClient;
  @MockBean protected RedisConnectionFactory redisConnectionFactory;

  // Mock messaging components to prevent background processing and connection
  // errors
  @MockBean protected TransactionProducer transactionProducer;
  @MockBean protected TransactionConsumer transactionConsumer;
  @MockBean protected TransactionRecoveryScheduler transactionRecoveryScheduler;

  @LocalServerPort protected int port;

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    RestAssured.basePath = "/api";
  }
}
