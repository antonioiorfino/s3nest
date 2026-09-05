package it.iorfino.s3.storage;

import java.time.Instant;
import java.util.Map;

public record S3NestObjectMetadata(
    String contentType,
    long contentLength,
    String eTag,
    Instant lastModified,
    Map<String, String> userMetadata) {

  public S3NestObjectMetadata {
    if (contentLength < 0) {
      throw new IllegalArgumentException("contentLength must not be negative");
    }

    userMetadata = Map.copyOf(userMetadata);
  }
}
