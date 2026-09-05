package it.iorfino.s3.storage;

import java.time.Instant;
import java.util.Map;

/**
 * Metadata associated with a stored S3Nest object.
 *
 * @param contentType the content type of the object
 * @param contentLength the object content length in bytes
 * @param eTag the entity tag associated with the object
 * @param lastModified the last modification timestamp
 * @param userMetadata user-defined metadata associated with the object
 */
public record S3NestObjectMetadata(
    String contentType,
    long contentLength,
    String eTag,
    Instant lastModified,
    Map<String, String> userMetadata) {

  /**
   * Creates object metadata.
   *
   * <p>User metadata is defensively copied and cannot be modified through the original map after
   * construction.
   *
   * @throws IllegalArgumentException if {@code contentLength} is negative
   * @throws NullPointerException if {@code userMetadata} is {@code null}
   */
  public S3NestObjectMetadata {
    if (contentLength < 0) {
      throw new IllegalArgumentException("contentLength must not be negative");
    }

    userMetadata = Map.copyOf(userMetadata);
  }
}
