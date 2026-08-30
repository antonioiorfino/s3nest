package it.iorfino.s3.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import it.iorfino.http.S3NestHttpResponse;
import it.iorfino.s3.model.S3Operation;
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
   * Verifies that deleting an object produces an HTTP 204 No Content response with an empty body.
   */
  @Test
  void shouldMapDeleteObjectToNoContentResponse() throws IOException {
    S3NestS3ResponseMapper mapper = new S3NestS3ResponseMapper();

    S3NestHttpResponse response = mapper.map(new S3NestS3EmptyResult(S3Operation.DELETE_OBJECT));

    assertEquals(204, response.statusCode());
    assertEquals("", responseBody(response));
  }

  /** Verifies that an object result streams its content to the HTTP response body. */
  @Test
  void shouldMapObjectResultToResponseBody() throws IOException {
    String bodyMessage = "hello S3";
    S3NestS3ResponseMapper mapper = new S3NestS3ResponseMapper();

    InputStream body = new ByteArrayInputStream(bodyMessage.getBytes(StandardCharsets.UTF_8));

    S3NestS3ObjectResult result = new S3NestS3ObjectResult(body, bodyMessage.length(), Map.of());

    S3NestHttpResponse response = mapper.map(result);

    assertEquals("hello S3", responseBody(response));
  }

  /**
   * Verifies that metadata associated with an object result is propagated to the HTTP response
   * headers.
   */
  @Test
  void shouldMapObjectMetadataToResponseHeaders() {
    S3NestS3ResponseMapper mapper = new S3NestS3ResponseMapper();

    Map<String, List<String>> metadata =
        Map.of(
            "Content-Type", List.of("text/plain"),
            "ETag", List.of("\"abc123\""));

    S3NestS3ObjectResult result =
        new S3NestS3ObjectResult(new ByteArrayInputStream(new byte[0]), 0, metadata);

    S3NestHttpResponse response = mapper.map(result);

    assertEquals(List.of("text/plain"), response.headers().get("Content-Type"));
    assertEquals(List.of("\"abc123\""), response.headers().get("ETag"));
  }

    @Test
    void shouldMapObjectContentLengthToResponseHeader() {
        S3NestS3ResponseMapper mapper = new S3NestS3ResponseMapper();

        S3NestS3ObjectResult result =
            new S3NestS3ObjectResult(
                new ByteArrayInputStream("hello S3".getBytes(StandardCharsets.UTF_8)),
                8,
                Map.of());

        S3NestHttpResponse response = mapper.map(result);

        assertEquals(List.of("8"), response.headers().get("Content-Length"));
    }

  /**
   * Writes the response body to an in-memory output stream and returns it as a UTF-8 string.
   *
   * @param response the HTTP response whose body should be written
   * @return the response body decoded as UTF-8
   * @throws IOException if writing the response body fails
   */
  private String responseBody(S3NestHttpResponse response) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    response.writeBody(output);

    return output.toString(StandardCharsets.UTF_8);
  }
}
