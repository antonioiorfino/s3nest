package it.iorfino.s3.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import it.iorfino.http.S3NestHttpResponse;
import it.iorfino.s3.result.S3NestS3EmptyResult;
import it.iorfino.s3.result.S3NestS3ObjectResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests the mapping of S3 operation results to HTTP responses.
 *
 * <p>These tests verify the S3 response layer independently from the HTTP server implementation.
 */
class S3NestS3ResponseMapperTest {

  /**
   * Verifies that a successful operation without a response payload produces an HTTP 200 response
   * with an empty body.
   */
  @Test
  void shouldMapEmptyResultToSuccessfulEmptyResponse() throws IOException {
    S3NestS3ResponseMapper mapper = new S3NestS3ResponseMapper();

    S3NestHttpResponse response = mapper.map(new S3NestS3EmptyResult());

    assertEquals(200, response.statusCode());
    assertEquals("", responseBody(response));
  }

  /** Verifies that an object result streams its content to the HTTP response body. */
  @Test
  void shouldMapObjectResultToResponseBody() throws IOException {
    S3NestS3ResponseMapper mapper = new S3NestS3ResponseMapper();

    InputStream body = new ByteArrayInputStream("hello S3".getBytes(StandardCharsets.UTF_8));

    S3NestS3ObjectResult result = new S3NestS3ObjectResult(body, Map.of());

    S3NestHttpResponse response = mapper.map(result);

    assertEquals("hello S3", responseBody(response));
  }

  /** Verifies that object metadata is propagated to the HTTP response headers. */
  @Test
  void shouldMapObjectMetadataToResponseHeaders() {
    S3NestS3ResponseMapper mapper = new S3NestS3ResponseMapper();

    Map<String, List<String>> metadata =
        Map.of(
            "Content-Type", List.of("text/plain"),
            "ETag", List.of("\"abc123\""));

    S3NestS3ObjectResult result =
        new S3NestS3ObjectResult(new ByteArrayInputStream(new byte[0]), metadata);

    S3NestHttpResponse response = mapper.map(result);

    assertEquals(List.of("text/plain"), response.headers().get("Content-Type"));
    assertEquals(List.of("\"abc123\""), response.headers().get("ETag"));
  }

  private String responseBody(S3NestHttpResponse response) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    response.writeBody(output);

    return output.toString();
  }
}
