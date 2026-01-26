package com.digitalwallet.platform.service;

import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

  private final S3Client s3Client;
  private final String bucketName = "wallet-bucket";

  public String uploadFile(MultipartFile file, String folder) {
    String key = folder + "/" + UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
    log.info("Uploading file to S3: {}/{}", bucketName, key);

    try {
      PutObjectRequest putObjectRequest =
          PutObjectRequest.builder()
              .bucket(bucketName)
              .key(key)
              .contentType(file.getContentType())
              .build();

      s3Client.putObject(
          putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
      log.info("Successfully uploaded file to S3: {}", key);
      return key;
    } catch (IOException e) {
      log.error("Failed to upload file to S3", e);
      throw new RuntimeException("Could not upload file to cloud storage", e);
    }
  }

  public String getFileUrl(String key) {
    // In a real AWS environment, you'd generate a presigned URL or return a
    // CloudFront URL.
    // For LocalStack, we'll return the local URL format.
    return String.format("http://localhost:4566/%s/%s", bucketName, key);
  }
}
