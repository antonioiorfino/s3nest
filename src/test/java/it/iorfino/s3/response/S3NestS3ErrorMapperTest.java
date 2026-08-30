package it.iorfino.s3.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import it.iorfino.s3.handler.S3NestS3ObjectNotFoundException;
import it.iorfino.s3.result.S3NestS3Error;
import org.junit.jupiter.api.Test;

/**
 * Tests the mapping of internal S3 exceptions to protocol-level S3 errors.
 *
 * <p>The mapper translates application exceptions into errors understood by S3 clients without
 * exposing the internal Java exception type.
 */
class S3NestS3ErrorMapperTest {

  /** Verifies that a missing object is mapped to the S3 {@code NoSuchKey} error. */
  @Test
  void shouldMapObjectNotFoundToNoSuchKey() {
    S3NestS3ErrorMapper mapper = new S3NestS3ErrorMapper();

    S3NestS3ObjectNotFoundException exception =
        new S3NestS3ObjectNotFoundException("my-bucket", "folder/file.txt");

    S3NestS3Error error = mapper.map(exception);

    assertEquals("NoSuchKey", error.code());
    assertEquals("The specified key does not exist.", error.message());
    assertEquals("my-bucket", error.bucket());
    assertEquals("folder/file.txt", error.objectKey());
  }
}
