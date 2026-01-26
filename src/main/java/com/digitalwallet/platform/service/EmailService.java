package com.digitalwallet.platform.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

  private final JavaMailSender mailSender;

  public void sendTransactionEmail(
      String to, String subject, String body, byte[] attachment, String attachmentName) {
    log.info("Preparing to send transaction email to: {}", to);

    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);

      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(body);

      if (attachment != null) {
        helper.addAttachment(attachmentName, new ByteArrayResource(attachment));
      }

      // In a real environment with SMTP configured, we would call:
      // mailSender.send(message);

      log.info("Email sent successfully (Simulated). To: {}, Subject: {}", to, subject);
      log.info("Email Body: {}", body);
      if (attachment != null) {
        log.info("Attachment '{}' included ({} bytes)", attachmentName, attachment.length);
      }

    } catch (MessagingException e) {
      log.error("Failed to send transaction email", e);
      // We don't throw here to avoid failing the transaction just because email
      // failed
    }
  }
}
