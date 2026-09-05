package it.iorfino.s3.storage;

/**
 * Thrown when a multipart operation references a part that does not exist.
 */
public class S3NestMultipartPartNotFoundException extends S3NestStorageException {

  public S3NestMultipartPartNotFoundException(String uploadId, int partNumber) {
    super("Multipart part not found: " + uploadId + "/" + partNumber);
  }
}
