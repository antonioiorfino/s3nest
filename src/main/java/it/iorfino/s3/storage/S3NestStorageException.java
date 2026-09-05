package it.iorfino.s3.storage;

/**
 * Base runtime exception for storage-level failures in S3Nest.
 *
 * <p>Implementations use domain-specific subclasses to report storage conditions without exposing
 * persistence or HTTP-specific details.
 */
public class S3NestStorageException extends RuntimeException {

  /**
   * Creates a storage exception with the specified message.
   *
   * @param message the detail message
   */
  public S3NestStorageException(String message) {
    super(message);
  }

  /**
   * Creates a storage exception with the specified message and cause.
   *
   * @param message the detail message
   * @param cause the underlying cause
   */
  public S3NestStorageException(String message, Throwable cause) {
    super(message, cause);
  }
}
