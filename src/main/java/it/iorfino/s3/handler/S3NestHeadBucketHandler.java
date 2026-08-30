package it.iorfino.s3.handler;

import it.iorfino.s3.model.S3NestS3OperationResult;
import it.iorfino.s3.model.S3NestS3Request;
import it.iorfino.s3.model.S3Operation;
import it.iorfino.s3.result.S3NestS3EmptyResult;
import it.iorfino.s3.routing.S3NestS3OperationHandler;
import it.iorfino.s3.storage.S3NestS3BucketStorage;

/**
 * Handles the S3 HEAD_BUCKET operation.
 *
 * <p>The handler checks whether the requested bucket exists through the bucket storage port.
 *
 * <p>A successful existence check produces an empty S3 operation result. The handler does not
 * depend on HTTP transport details or on a concrete storage implementation.
 */
public final class S3NestHeadBucketHandler implements S3NestS3OperationHandler {

  private final S3NestS3BucketStorage storage;

  /**
   * Creates a HEAD_BUCKET handler.
   *
   * @param storage the bucket storage port
   */
  public S3NestHeadBucketHandler(S3NestS3BucketStorage storage) {
    this.storage = storage;
  }

  /**
   * Checks whether the requested bucket exists.
   *
   * @param request the parsed S3 HEAD_BUCKET request
   * @return an empty result when the bucket exists
   */
  @Override
  public S3NestS3OperationResult handle(S3NestS3Request request) {
    boolean bucketExists = storage.bucketExists(request.bucket());

    if (!bucketExists) {
      throw new S3NestS3BucketNotFoundException(request.bucket());
    }

    return new S3NestS3EmptyResult(S3Operation.HEAD_BUCKET);
  }
}
