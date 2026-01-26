package com.digitalwallet.platform.dto;

import com.digitalwallet.platform.model.User;
import com.digitalwallet.platform.model.UserStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

  private Long id;

  private String email;

  private String firstName;

  private String lastName;

  private String phone;

  private UserStatus status;

  private LocalDateTime createdAt;

  // Static factory method - converts User entity to UserResponse DTO
  public static UserResponse fromEntity(User user) {
    return UserResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .phone(user.getPhone())
        .status(user.getStatus())
        .createdAt(user.getCreatedAt())
        .build();
  }
}
