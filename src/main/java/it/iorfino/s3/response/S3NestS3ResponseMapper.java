package it.iorfino.s3.response;

import it.iorfino.http.S3NestHttpResponse;
import it.iorfino.s3.model.S3NestS3OperationResult;
import it.iorfino.s3.result.S3NestS3EmptyResult;
import it.iorfino.s3.result.S3NestS3ObjectResult;
import java.util.Map;

/**
 * Maps S3 operation results to transport-independent HTTP responses.
 *
 * <p>Operation handlers produce S3 operation results and do not construct HTTP responses directly.
 * This mapper is responsible for translating those results into {@link S3NestHttpResponse}
 * instances.
 */
public final class S3NestS3ResponseMapper {

  /**
   * Maps an S3 operation result to an HTTP response.
   *
   * @param result the result produced by an S3 operation handler
   * @return the HTTP response corresponding to the operation result
   */
  public S3NestHttpResponse map(S3NestS3OperationResult result) {

    if (result instanceof S3NestS3EmptyResult) {
      return new S3NestHttpResponse(200, Map.of(), output -> {});
    }

    if (result instanceof S3NestS3ObjectResult objectResult) {
      return new S3NestHttpResponse(
          200, objectResult.metadata(), output -> objectResult.body().transferTo(output));
    }

    throw new IllegalArgumentException(
        "Unsupported S3 operation result: " + result.getClass().getName());
  }
}
