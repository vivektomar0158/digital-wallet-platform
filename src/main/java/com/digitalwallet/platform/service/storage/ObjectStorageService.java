package com.digitalwallet.platform.service.storage;

public interface ObjectStorageService {

  /**
   * Uploads raw bytes to the underlying storage with the given key and content type.
   *
   * @param data        file contents
   * @param key         fully qualified key/path inside the storage backend
   * @param contentType optional MIME type; may be {@code null}
   * @return a storage-specific identifier for the uploaded object
   */
  String upload(byte[] data, String key, String contentType);

  /**
   * Returns a URL or URL-like identifier for accessing the stored object.
   */
  String getFileUrl(String key);
}
