package it.iorfino.s3.storage;

import java.time.Instant;
import java.util.Map;

public record ObjectMetadata(
    String contentType,
    long contentLength,
    String eTag,
    Instant lastModified,
    Map<String, String> userMetadata) {

  public ObjectMetadata {
    if (contentLength < 0) {
      throw new IllegalArgumentException("contentLength must not be negative");
    }

    userMetadata = Map.copyOf(userMetadata);
  }
}
