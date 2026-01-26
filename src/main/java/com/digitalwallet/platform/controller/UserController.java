package com.digitalwallet.platform.controller;

import com.digitalwallet.platform.model.User;
import com.digitalwallet.platform.repository.UserRepository;
import com.digitalwallet.platform.service.StorageService;
import com.digitalwallet.platform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User", description = "User management and profile operations")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

  private final StorageService storageService;
  private final UserService userService;
  private final UserRepository userRepository;

  private User getCurrentUser(Authentication authentication) {
    String email = authentication.getName();
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found"));
  }

  @Operation(
      summary = "Upload profile picture",
      description = "Uploads a profile picture to S3 and updates user record")
  @PostMapping("/profile-pic")
  public ResponseEntity<Map<String, String>> uploadProfilePic(
      @RequestParam("file") MultipartFile file, Authentication authentication) {

    User user = getCurrentUser(authentication);
    String key = storageService.uploadFile(file, "profile-pics/" + user.getId());
    userService.updateUserProfilePic(user.getId(), key);

    String url = storageService.getFileUrl(key);
    return ResponseEntity.ok(
        Map.of(
            "message", "Profile picture uploaded successfully",
            "key", key,
            "url", url));
  }

  @Operation(
      summary = "Upload KYC document",
      description = "Uploads a KYC document to S3 and updates user record")
  @PostMapping("/kyc")
  public ResponseEntity<Map<String, String>> uploadKycDocument(
      @RequestParam("file") MultipartFile file, Authentication authentication) {

    User user = getCurrentUser(authentication);
    String key = storageService.uploadFile(file, "kyc-docs/" + user.getId());
    userService.updateUserKycDocument(user.getId(), key);

    String url = storageService.getFileUrl(key);
    return ResponseEntity.ok(
        Map.of(
            "message", "KYC document uploaded successfully",
            "key", key,
            "url", url));
  }

  @Operation(
      summary = "Get user profile",
      description = "Returns user profile details including image URLs")
  @GetMapping("/profile")
  public ResponseEntity<Map<String, Object>> getProfile(Authentication authentication) {
    User user = getCurrentUser(authentication);

    return ResponseEntity.ok(
        Map.of(
            "firstName",
            user.getFirstName(),
            "lastName",
            user.getLastName(),
            "email",
            user.getEmail(),
            "phone",
            user.getPhone() != null ? user.getPhone() : "",
            "profilePicUrl",
            user.getProfilePicKey() != null
                ? storageService.getFileUrl(user.getProfilePicKey())
                : "",
            "kycStatus",
            user.getKycVerified() ? "VERIFIED" : "PENDING",
            "kycDocumentUrl",
            user.getKycDocumentKey() != null
                ? storageService.getFileUrl(user.getKycDocumentKey())
                : ""));
  }
}
