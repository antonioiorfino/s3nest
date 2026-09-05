package it.iorfino.s3.storage;

/**
 * Thrown when an operation references a multipart upload that does not exist.
 */
public class S3NestMultipartUploadNotFoundException extends S3NestStorageException {

  public S3NestMultipartUploadNotFoundException(String uploadId) {
    super("Multipart upload not found: " + uploadId);
  }
}
