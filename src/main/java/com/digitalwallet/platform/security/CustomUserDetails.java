package com.digitalwallet.platform.security;

import com.digitalwallet.platform.model.User;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class CustomUserDetails implements UserDetails {

  private final User user;

  public CustomUserDetails(User user) {
    this.user = user;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    // Return default role or extract from user if you have roles field
    return List.of(new SimpleGrantedAuthority("ROLE_USER"));
  }

  @Override
  public String getPassword() {
    // Return password from your User entity
    // If your User entity has password field, use: user.getPassword()
    // If not, return empty string or throw exception
    return user.getPasswordHash() != null ? user.getPasswordHash() : "";
  }

  @Override
  public String getUsername() {
    return user.getEmail(); // Assuming email is the username
  }

  // These methods return true by default - you can customize later
  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    // If your User has enabled field: return user.isEnabled();
    // Otherwise default to true
    return true;
  }
}
