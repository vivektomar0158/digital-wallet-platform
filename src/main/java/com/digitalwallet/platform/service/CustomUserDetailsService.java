package com.digitalwallet.platform.service;

import com.digitalwallet.platform.model.User;
import com.digitalwallet.platform.repository.UserRepository;
import com.digitalwallet.platform.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    log.info("Loading user by email: {}", email);

    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () -> {
                  log.error("User not found with email: {}", email);
                  return new UsernameNotFoundException("User not found with email: " + email);
                });

    log.info("User found: {} (ID: {})", user.getEmail(), user.getId());
    return new CustomUserDetails(user);
  }
}
