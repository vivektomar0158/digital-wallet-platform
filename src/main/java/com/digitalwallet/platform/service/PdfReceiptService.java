package com.digitalwallet.platform.service;

import com.digitalwallet.platform.model.Transaction;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfReceiptService {

  private final TemplateEngine templateEngine;
  private static final DateTimeFormatter formatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public byte[] generateTransactionReceipt(Transaction transaction) {
    log.info("Generating PDF receipt for transaction: {}", transaction.getReferenceId());

    try {
      Context context = new Context();
      context.setVariable("referenceId", transaction.getReferenceId());
      context.setVariable("timestamp", transaction.getCreatedAt().format(formatter));
      context.setVariable("type", transaction.getType().name());
      context.setVariable(
          "sender",
          transaction.getFromWallet().getUser().getFirstName()
              + " "
              + transaction.getFromWallet().getUser().getLastName());
      context.setVariable("receiverWallet", transaction.getToWallet().getWalletNumber());
      context.setVariable("status", transaction.getStatus().name());
      context.setVariable("amount", transaction.getAmount());
      context.setVariable("currency", transaction.getCurrency());

      String html = templateEngine.process("receipt", context);

      try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(html, null);
        builder.toStream(os);
        builder.run();
        return os.toByteArray();
      }
    } catch (Exception e) {
      log.error("Failed to generate PDF receipt", e);
      throw new RuntimeException("Error generating transaction receipt", e);
    }
  }
}
