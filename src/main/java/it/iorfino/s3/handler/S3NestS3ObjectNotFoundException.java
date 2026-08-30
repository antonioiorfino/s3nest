package it.iorfino.s3.handler;

/**
 * Indicates that an S3 object could not be found in storage.
 *
 * <p>The exception belongs to the S3 application layer and does not depend on HTTP transport or on
 * a concrete storage implementation.
 */
public final class S3NestS3ObjectNotFoundException extends RuntimeException {

  private final String bucket;
  private final String objectKey;

  /**
   * Creates an exception for a missing S3 object.
   *
   * @param bucket the bucket containing the requested object
   * @param objectKey the requested object key
   */
  public S3NestS3ObjectNotFoundException(String bucket, String objectKey) {
    super("S3 object not found: " + bucket + "/" + objectKey);
    this.bucket = bucket;
    this.objectKey = objectKey;
  }

  /**
   * @return the bucket containing the requested object
   */
  public String bucket() {
    return bucket;
  }

  /**
   * @return the requested object key
   */
  public String objectKey() {
    return objectKey;
  }
}
