package com.digitalwallet.platform.service.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(
    name = "app.storage.s3.enabled",
    havingValue = "false",
    matchIfMissing = true)
public class NoOpStorageService implements ObjectStorageService {

  @Override
  public String upload(byte[] data, String key, String contentType) {
    log.warn(
        "S3 disabled — skipping upload for {} ({} bytes, type={})",
        key,
        data != null ? data.length : 0,
        contentType);
    // Pretend we stored it locally or just return a logical handle.
    return "local://" + key;
  }

  @Override
  public String getFileUrl(String key) {
    log.warn("S3 disabled — returning local URL for {}", key);
    return "local://" + key;
  }
}
