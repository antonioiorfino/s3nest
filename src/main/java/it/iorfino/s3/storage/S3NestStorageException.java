package it.iorfino.s3.storage;

public class S3NestStorageException extends RuntimeException {

  public S3NestStorageException(String message) {
    super(message);
  }

  public S3NestStorageException(String message, Throwable cause) {
    super(message, cause);
  }
}
