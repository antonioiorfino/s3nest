package it.iorfino.s3.result;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests the information contained in an S3-compatible error.
 *
 * <p>The error model must preserve the information required by the S3 response layer without
 * depending on HTTP transport details.
 */
class S3NestS3ErrorTest {

  /** Verifies that an S3 error preserves its protocol information and object context. */
  @Test
  void shouldExposeErrorInformation() {
    S3NestS3ErrorResult error =
        new S3NestS3ErrorResult(
            "NoSuchKey", "The specified key does not exist.", "my-bucket", "folder/file.txt", null);

    assertEquals("NoSuchKey", error.code());
    assertEquals("The specified key does not exist.", error.message());
    assertEquals("my-bucket", error.bucket());
    assertEquals("folder/file.txt", error.objectKey());
  }
}
