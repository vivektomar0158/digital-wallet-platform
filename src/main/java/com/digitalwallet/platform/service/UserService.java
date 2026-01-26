package com.digitalwallet.platform.service;

import com.digitalwallet.platform.model.User;
import com.digitalwallet.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

  private final UserRepository userRepository;

  @Transactional
  public void updateUserProfilePic(Long userId, String key) {
    log.info("Updating profile picture key for user ID: {}", userId);
    User user =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
    user.setProfilePicKey(key);
    userRepository.save(user);
  }

  @Transactional
  public void updateUserKycDocument(Long userId, String key) {
    log.info("Updating KYC document key for user ID: {}", userId);
    User user =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
    user.setKycDocumentKey(key);
    userRepository.save(user);
  }

  public User getUserById(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));
  }
}
