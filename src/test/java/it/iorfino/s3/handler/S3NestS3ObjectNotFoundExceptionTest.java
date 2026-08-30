package it.iorfino.s3.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests the information exposed by {@link S3NestS3ObjectNotFoundException}.
 *
 * <p>The exception must preserve the bucket and object key so that the S3 response layer can use
 * them when generating an S3-compatible error response.
 */
class S3NestS3ObjectNotFoundExceptionTest {

  /** Verifies that the exception preserves the bucket and object key of the missing object. */
  @Test
  void shouldExposeBucketAndObjectKey() {
    S3NestS3ObjectNotFoundException exception =
        new S3NestS3ObjectNotFoundException("my-bucket", "folder/file.txt");

    assertEquals("my-bucket", exception.bucket());
    assertEquals("folder/file.txt", exception.objectKey());
  }
}
