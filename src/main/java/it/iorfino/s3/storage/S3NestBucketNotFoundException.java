package it.iorfino.s3.storage;

/**
 * Thrown when an operation references a bucket that does not exist.
 */
public class S3NestBucketNotFoundException extends S3NestStorageException {

  public S3NestBucketNotFoundException(String bucket) {
    super("Bucket not found: " + bucket);
  }
}
