package it.iorfino.s3.storage;

public class MultipartPartNotFoundException extends S3NestStorageException {

  public MultipartPartNotFoundException(String uploadId, int partNumber) {
    super("Multipart part not found: " + uploadId + "/" + partNumber);
  }
}
