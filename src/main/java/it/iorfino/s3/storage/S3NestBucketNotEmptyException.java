package it.iorfino.s3.storage;

/** Thrown when an attempt is made to delete a bucket that still contains objects. */
public class S3NestBucketNotEmptyException extends S3NestStorageException {

  public S3NestBucketNotEmptyException(String bucket) {
    super("Bucket is not empty: " + bucket);
  }
}
