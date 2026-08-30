package it.iorfino.s3.result;

/**
 * Represents an S3-compatible error independently of the HTTP transport.
 *
 * <p>An S3 error contains the information required by the S3 protocol layer to generate an error
 * response. It does not contain HTTP-specific details such as status codes or response headers.
 */
public final class S3NestS3Error {

  private final String code;
  private final String message;
  private final String bucket;
  private final String objectKey;

  /**
   * Creates an S3 error.
   *
   * @param code the S3 error code
   * @param message the human-readable error message
   * @param bucket the affected bucket, or {@code null} when not applicable
   * @param objectKey the affected object key, or {@code null} when not applicable
   */
  public S3NestS3Error(String code, String message, String bucket, String objectKey) {
    this.code = code;
    this.message = message;
    this.bucket = bucket;
    this.objectKey = objectKey;
  }

  /**
   * @return the S3 error code
   */
  public String code() {
    return code;
  }

  /**
   * @return the human-readable error message
   */
  public String message() {
    return message;
  }

  /**
   * @return the affected bucket, or {@code null} when not applicable
   */
  public String bucket() {
    return bucket;
  }

  /**
   * @return the affected object key, or {@code null} when not applicable
   */
  public String objectKey() {
    return objectKey;
  }
}
