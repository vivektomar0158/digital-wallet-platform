package com.digitalwallet.platform.service.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.storage.s3.enabled", havingValue = "true")
public class S3StorageService implements ObjectStorageService {

  private final S3Client s3Client;

  // TODO: externalize to configuration if you want multiple environments/buckets.
  private final String bucketName = "wallet-bucket";

  @Override
  public String upload(byte[] data, String key, String contentType) {
    log.info("Uploading file to S3: {}/{}", bucketName, key);

    try {
      PutObjectRequest.Builder requestBuilder =
          PutObjectRequest.builder().bucket(bucketName).key(key);

      if (contentType != null && !contentType.isBlank()) {
        requestBuilder = requestBuilder.contentType(contentType);
      }

      PutObjectRequest putObjectRequest = requestBuilder.build();

      s3Client.putObject(putObjectRequest, RequestBody.fromBytes(data));

      log.info("Successfully uploaded file to S3: {}", key);
      return key;
    } catch (Exception e) {
      log.error("Failed to upload file to S3", e);
      throw new RuntimeException("Could not upload file to cloud storage", e);
    }
  }

  @Override
  public String getFileUrl(String key) {
    // In a real AWS environment, you'd generate a presigned URL or return a CloudFront URL.
    // For LocalStack, we'll return the local URL format (kept from the previous implementation).
    String endpoint = "http://localhost:4566";
    return String.format("%s/%s/%s", endpoint, bucketName, key);
  }
}
