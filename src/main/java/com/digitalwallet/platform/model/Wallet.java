package com.digitalwallet.platform.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "wallets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @Column(
      nullable = false,
      unique = true,
      name = "wallet_number",
      length = 30) // CHANGED FROM 20 TO 30
  private String walletNumber;

  @Column(nullable = false, precision = 19, scale = 2)
  @Builder.Default
  private BigDecimal balance = BigDecimal.ZERO;

  @Column(nullable = false, length = 3)
  @Builder.Default
  private String currency = "USD";

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private WalletStatus status = WalletStatus.ACTIVE;

  @Column(name = "daily_limit", precision = 19, scale = 2)
  @Builder.Default
  private BigDecimal dailyLimit = new BigDecimal("10000.00");

  @Column(name = "transaction_limit", precision = 19, scale = 2)
  @Builder.Default
  private BigDecimal transactionLimit = new BigDecimal("5000.00");

  @CreationTimestamp
  @Column(nullable = false, updatable = false, name = "created_at")
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Version private Long version;

  @Column(name = "total_daily_spent", precision = 19, scale = 2)
  @Builder.Default
  private BigDecimal totalDailySpent = BigDecimal.ZERO;

  @Column(name = "total_monthly_spent", precision = 19, scale = 2)
  @Builder.Default
  private BigDecimal totalMonthlySpent = BigDecimal.ZERO;

  // Reset method for daily limits
  public void resetDailySpent() {
    this.totalDailySpent = BigDecimal.ZERO;
  }

  // Reset method for monthly limits
  public void resetMonthlySpent() {
    this.totalMonthlySpent = BigDecimal.ZERO;
  }
}
