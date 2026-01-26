package com.digitalwallet.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 100)
  private String email;

  @Column(nullable = false, name = "password_hash", length = 255)
  private String passwordHash;

  @Column(nullable = false, name = "first_name", length = 50)
  private String firstName;

  @Column(nullable = false, name = "last_name", length = 50)
  private String lastName;

  @Column(unique = true, length = 15)
  private String phone;

  @Column(length = 20)
  @Builder.Default
  private String role = "USER";

  @Column(name = "email_verified")
  @Builder.Default
  private Boolean emailVerified = false;

  @Column(name = "phone_verified")
  @Builder.Default
  private Boolean phoneVerified = false;

  @Column(name = "kyc_verified")
  @Builder.Default
  private Boolean kycVerified = false;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private UserStatus status = UserStatus.ACTIVE;

  @Column(name = "profile_pic_key", length = 255)
  private String profilePicKey;

  @Column(name = "kyc_document_key", length = 255)
  private String kycDocumentKey;

  @CreationTimestamp
  @Column(nullable = false, updatable = false, name = "created_at")
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Wallet wallet;
}
