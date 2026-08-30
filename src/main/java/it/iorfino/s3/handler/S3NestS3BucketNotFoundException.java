package it.iorfino.s3.handler;

/**
 * Indicates that an S3 bucket could not be found.
 *
 * <p>The exception belongs to the S3 application layer and is independent of HTTP transport and
 * concrete storage implementations.
 */
public final class S3NestS3BucketNotFoundException extends RuntimeException {

  private final String bucket;

  /**
   * Creates an exception for a missing bucket.
   *
   * @param bucket the requested bucket name
   */
  public S3NestS3BucketNotFoundException(String bucket) {
    super("S3 bucket not found: " + bucket);
    this.bucket = bucket;
  }

  public String bucket() {
    return bucket;
  }
}
