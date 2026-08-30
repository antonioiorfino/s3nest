package it.iorfino.s3.routing;

import it.iorfino.s3.model.S3NestS3OperationResult;
import it.iorfino.s3.model.S3NestS3Request;

/**
 * Handles a parsed S3 operation.
 *
 * <p>The handler operates exclusively on the internal S3 request model and produces an S3 operation
 * result. It does not depend on HTTP transport details or storage implementation details.
 */
@FunctionalInterface
public interface S3NestS3OperationHandler {

  /**
   * Executes the operation represented by the supplied request.
   *
   * @param request the parsed S3 request
   * @return the result of the S3 operation
   */
  S3NestS3OperationResult handle(S3NestS3Request request);
}
