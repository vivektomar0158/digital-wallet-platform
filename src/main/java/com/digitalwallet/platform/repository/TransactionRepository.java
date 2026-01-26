package com.digitalwallet.platform.repository;

import com.digitalwallet.platform.model.Transaction;
import com.digitalwallet.platform.model.TransactionStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

  Optional<Transaction> findByReferenceId(String referenceId);

  List<Transaction> findByFromWalletUserId(Long userId);

  List<Transaction> findByToWalletUserId(Long userId);

  List<Transaction> findByFromWalletUserIdOrToWalletUserId(Long fromUserId, Long toUserId);

  @Query(
      value =
          "SELECT t.* FROM transactions t "
              + "LEFT JOIN wallets fw ON t.from_wallet_id = fw.id "
              + "LEFT JOIN wallets tw ON t.to_wallet_id = tw.id "
              + "WHERE (fw.user_id = :userId OR tw.user_id = :userId) "
              + "AND (CAST(:startDate AS TIMESTAMP) IS NULL OR t.created_at >= CAST(:startDate AS TIMESTAMP)) "
              + "AND (CAST(:endDate AS TIMESTAMP) IS NULL OR t.created_at <= CAST(:endDate AS TIMESTAMP)) "
              + "AND (CAST(:type AS VARCHAR) IS NULL OR t.type = CAST(:type AS VARCHAR)) "
              + "AND (CAST(:status AS VARCHAR) IS NULL OR t.status = CAST(:status AS VARCHAR))",
      nativeQuery = true,
      countQuery =
          "SELECT COUNT(*) FROM transactions t "
              + "LEFT JOIN wallets fw ON t.from_wallet_id = fw.id "
              + "LEFT JOIN wallets tw ON t.to_wallet_id = tw.id "
              + "WHERE (fw.user_id = :userId OR tw.user_id = :userId) "
              + "AND (CAST(:startDate AS TIMESTAMP) IS NULL OR t.created_at >= CAST(:startDate AS TIMESTAMP)) "
              + "AND (CAST(:endDate AS TIMESTAMP) IS NULL OR t.created_at <= CAST(:endDate AS TIMESTAMP)) "
              + "AND (CAST(:type AS VARCHAR) IS NULL OR t.type = CAST(:type AS VARCHAR)) "
              + "AND (CAST(:status AS VARCHAR) IS NULL OR t.status = CAST(:status AS VARCHAR))")
  Page<Transaction> findTransactionsWithFilters(
      @Param("userId") Long userId,
      @Param("startDate") String startDate,
      @Param("endDate") String endDate,
      @Param("type") String type,
      @Param("status") String status,
      Pageable pageable);

  @Query("SELECT t FROM Transaction t WHERE t.status = :status AND t.createdAt < :timestamp")
  List<Transaction> findByStatusAndCreatedAtBefore(
      @Param("status") TransactionStatus status, @Param("timestamp") LocalDateTime timestamp);
}
