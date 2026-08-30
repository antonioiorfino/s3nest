package it.iorfino.s3.response;

import it.iorfino.s3.handler.S3NestS3ObjectNotFoundException;
import it.iorfino.s3.result.S3NestS3Error;

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
  public S3NestS3Error map(RuntimeException exception) {
    if (exception instanceof S3NestS3ObjectNotFoundException objectNotFound) {
      return new S3NestS3Error(
          "NoSuchKey",
          "The specified key does not exist.",
          objectNotFound.bucket(),
          objectNotFound.objectKey());
    }

    throw new IllegalArgumentException(
        "Unsupported S3 exception: " + exception.getClass().getName());
  }
}
