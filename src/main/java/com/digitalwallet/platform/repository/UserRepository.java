package com.digitalwallet.platform.repository;

import com.digitalwallet.platform.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  // Find user by email
  Optional<User> findByEmail(String email);

  // Check if email exists
  boolean existsByEmail(String email);

  // Check if phone exists
  boolean existsByPhone(String phone);
}
