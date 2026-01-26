package com.digitalwallet.platform.service;

import com.digitalwallet.platform.service.storage.ObjectStorageService;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

  private final ObjectStorageService storageService;

  public String uploadFile(MultipartFile file, String folder) {
    String key = folder + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
    log.info("Uploading file using storage backend. key={} folder={}", key, folder);

    try {
      return storageService.upload(file.getBytes(), key, file.getContentType());
    } catch (IOException e) {
      log.error("Failed to read file contents for upload", e);
      throw new RuntimeException("Could not read file contents for upload", e);
    }
  }

  public String getFileUrl(String key) {
    return storageService.getFileUrl(key);
  }
}
