package it.iorfino.s3.result;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import it.iorfino.s3.model.S3NestS3OperationResult;
import it.iorfino.s3.model.S3Operation;
import org.junit.jupiter.api.Test;

/**
 * Tests the empty result produced by S3 operation handlers.
 *
 * <p>An empty result represents a successful operation that does not produce a response payload.
 */
class S3NestS3EmptyResultTest {

  /**
   * Verifies that an empty result is an S3 operation result.
   *
   * <p>This is important because the response layer receives operation results through the common
   * {@link S3NestS3OperationResult} abstraction.
   */
  @Test
  void shouldBeAnS3OperationResult() {
    S3NestS3EmptyResult result = new S3NestS3EmptyResult(S3Operation.GET_OBJECT);

    assertInstanceOf(S3NestS3OperationResult.class, result);
  }
}
