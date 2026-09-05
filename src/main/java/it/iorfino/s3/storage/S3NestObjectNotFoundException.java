package it.iorfino.s3.storage;

public class S3NestObjectNotFoundException extends S3NestStorageException {

  public S3NestObjectNotFoundException(String bucket, String key) {
    super("Object not found: " + bucket + "/" + key);
  }
}
