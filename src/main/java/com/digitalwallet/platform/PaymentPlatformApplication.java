package com.digitalwallet.platform;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableRetry
@OpenAPIDefinition(
    info =
        @Info(
            title = "Digital Wallet Platform API",
            version = "1.0.0",
            description = "Secure P2P Payment System for Digital Wallets"))
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT")
public class PaymentPlatformApplication {
  public static void main(String[] args) {
    SpringApplication.run(PaymentPlatformApplication.class, args);
  }
}
