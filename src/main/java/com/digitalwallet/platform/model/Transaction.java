package com.digitalwallet.platform.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "from_wallet_id")
  private Wallet fromWallet;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "to_wallet_id")
  private Wallet toWallet;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Builder.Default
  @Column(nullable = false, length = 3)
  private String currency = "USD";

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private TransactionType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private TransactionStatus status = TransactionStatus.PENDING;

  @Column(nullable = false, unique = true, name = "reference_id", length = 50)
  private String referenceId;

  @Column(length = 500)
  private String description;

  @Column(length = 500)
  private String metadata;

  @Column(name = "failure_reason", length = 255)
  private String failureReason;

  @CreationTimestamp
  @Column(nullable = false, updatable = false, name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;
}
