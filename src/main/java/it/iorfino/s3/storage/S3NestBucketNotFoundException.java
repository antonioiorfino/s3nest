package it.iorfino.s3.storage;

public class S3NestBucketNotFoundException extends S3NestStorageException {

  public S3NestBucketNotFoundException(String bucket) {
    super("Bucket not found: " + bucket);
  }
}
