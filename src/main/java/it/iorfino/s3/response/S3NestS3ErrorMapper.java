package it.iorfino.s3.response;

import it.iorfino.s3.handler.S3NestS3BucketNotFoundException;
import it.iorfino.s3.handler.S3NestS3ObjectNotFoundException;
import it.iorfino.s3.result.S3NestS3ErrorResult;

/**
 * Maps internal S3 application exceptions to protocol-level S3 errors.
 *
 * <p>This class keeps internal exception types separate from the S3 error representation that is
 * later used to build an HTTP response.
 */
public final class S3NestS3ErrorMapper {

  /**
   * Maps an internal S3 exception to an S3-compatible error.
   *
   * @param exception the internal S3 exception
   * @return the corresponding S3 error
   */
  public S3NestS3ErrorResult map(RuntimeException exception) {
    if (exception instanceof S3NestS3ObjectNotFoundException objectNotFound) {
      return new S3NestS3ErrorResult(
          "NoSuchKey",
          "The specified key does not exist.",
          objectNotFound.bucket(),
          objectNotFound.objectKey(),
          null);
    }

    if (exception instanceof S3NestS3BucketNotFoundException bucketNotFound) {
      return new S3NestS3ErrorResult(
          "NoSuchBucket",
          "The specified bucket does not exist.",
          bucketNotFound.bucket(),
          null,
          null);
    }

    return new S3NestS3ErrorResult(
        "InternalError", "We encountered an internal error. Please try again.", null, null, null);
  }
}
