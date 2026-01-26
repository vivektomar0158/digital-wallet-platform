package com.digitalwallet.platform.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TransactionIntegrationTest extends AbstractIntegrationTest {

  @Test
  void testAsyncTransfer() throws InterruptedException {
    // This test is a placeholder for async transfer verification.
    // The actual logic is covered in TransactionControllerIntegrationTest.
  }
}
