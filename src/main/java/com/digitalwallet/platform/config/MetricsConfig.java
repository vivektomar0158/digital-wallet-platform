package com.digitalwallet.platform.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

  @Bean
  public Counter transactionCounter(MeterRegistry registry) {
    return Counter.builder("wallet.transactions.total")
        .description("Total number of wallet transactions")
        .tag("type", "all")
        .register(registry);
  }

  @Bean
  public Counter depositCounter(MeterRegistry registry) {
    return Counter.builder("wallet.deposits.total")
        .description("Total number of deposits")
        .register(registry);
  }

  @Bean
  public Counter withdrawalCounter(MeterRegistry registry) {
    return Counter.builder("wallet.withdrawals.total")
        .description("Total number of withdrawals")
        .register(registry);
  }

  @Bean
  public Counter transferCounter(MeterRegistry registry) {
    return Counter.builder("wallet.transfers.total")
        .description("Total number of P2P transfers")
        .register(registry);
  }

  @Bean
  public Timer transactionTimer(MeterRegistry registry) {
    return Timer.builder("wallet.operation.duration")
        .description("Duration of wallet operations")
        .register(registry);
  }
}
