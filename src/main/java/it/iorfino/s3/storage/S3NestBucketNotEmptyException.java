package it.iorfino.s3.storage;

public class S3NestBucketNotEmptyException extends S3NestStorageException {

  public S3NestBucketNotEmptyException(String bucket) {
    super("Bucket is not empty: " + bucket);
  }
}
