package it.iorfino.s3.handler;

import it.iorfino.s3.model.S3NestS3OperationResult;
import it.iorfino.s3.model.S3NestS3Request;
import it.iorfino.s3.model.S3Operation;
import it.iorfino.s3.result.S3NestS3EmptyResult;
import it.iorfino.s3.routing.S3NestS3OperationHandler;
import it.iorfino.s3.storage.S3NestS3BucketStorage;

/**
 * Handles the S3 DELETE_BUCKET operation.
 *
 * <p>The handler delegates bucket deletion to the bucket storage port and returns an empty S3
 * operation result when the operation completes successfully.
 *
 * <p>The handler does not depend on HTTP transport details or on a concrete storage implementation.
 */
public final class S3NestDeleteBucketHandler implements S3NestS3OperationHandler {

  private final S3NestS3BucketStorage storage;

  /**
   * Creates a DELETE_BUCKET handler.
   *
   * @param storage the bucket storage port
   */
  public S3NestDeleteBucketHandler(S3NestS3BucketStorage storage) {
    this.storage = storage;
  }

  /**
   * Deletes the bucket identified by the supplied request.
   *
   * @param request the parsed S3 DELETE_BUCKET request
   * @return an empty result when the bucket is deleted successfully
   */
  @Override
  public S3NestS3OperationResult handle(S3NestS3Request request) {
    storage.deleteBucket(request.bucket());

    return new S3NestS3EmptyResult(S3Operation.DELETE_BUCKET);
  }
}
