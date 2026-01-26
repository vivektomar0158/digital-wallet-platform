package com.digitalwallet.platform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.digitalwallet.platform.model.User;
import com.digitalwallet.platform.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private UserService userService;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setEmail("test@example.com");
    testUser.setFirstName("Test");
    testUser.setLastName("User");
  }

  @Test
  @DisplayName("Should update profile picture successfully")
  void shouldUpdateProfilePic() {
    // Given
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    // When
    userService.updateUserProfilePic(1L, "new-pic-key");

    // Then
    assertThat(testUser.getProfilePicKey()).isEqualTo("new-pic-key");
    verify(userRepository).save(testUser);
  }

  @Test
  @DisplayName("Should update KYC document successfully")
  void shouldUpdateKycDocument() {
    // Given
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    // When
    userService.updateUserKycDocument(1L, "kyc-key");

    // Then
    assertThat(testUser.getKycDocumentKey()).isEqualTo("kyc-key");
    verify(userRepository).save(testUser);
  }

  @Test
  @DisplayName("Should get user by ID")
  void shouldGetUserById() {
    // Given
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    // When
    User result = userService.getUserById(1L);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getEmail()).isEqualTo("test@example.com");
  }

  @Test
  @DisplayName("Should throw exception when user not found")
  void shouldThrowExceptionWhenUserNotFound() {
    // Given
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> userService.getUserById(1L))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("User not found");
  }
}
