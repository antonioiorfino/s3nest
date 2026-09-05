package it.iorfino.s3.storage;

/**
 * Thrown when an attempt is made to create a bucket that already exists.
 */
public class S3NestBucketAlreadyExistsException extends S3NestStorageException {

  public S3NestBucketAlreadyExistsException(String bucket) {
    super("Bucket already exists: " + bucket);
  }
}
