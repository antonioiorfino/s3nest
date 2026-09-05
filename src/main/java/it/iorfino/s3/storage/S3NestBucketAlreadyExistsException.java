package it.iorfino.s3.storage;

public class S3NestBucketAlreadyExistsException extends S3NestStorageException {

  public S3NestBucketAlreadyExistsException(String bucket) {
    super("Bucket already exists: " + bucket);
  }
}
