package com.digitalwallet.platform.repository;

import com.digitalwallet.platform.model.Wallet;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

  // Find wallet by user ID
  Optional<Wallet> findByUserId(Long userId);

  // Find wallet by wallet number
  Optional<Wallet> findByWalletNumber(String walletNumber);

  // Check if wallet exists for user
  boolean existsByUserId(Long userId);
}
