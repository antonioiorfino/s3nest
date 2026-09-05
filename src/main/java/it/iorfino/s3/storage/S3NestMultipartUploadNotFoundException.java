package it.iorfino.s3.storage;

public class S3NestMultipartUploadNotFoundException extends S3NestStorageException {

  public S3NestMultipartUploadNotFoundException(String uploadId) {
    super("Multipart upload not found: " + uploadId);
  }
}
