package it.iorfino.s3.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import it.iorfino.http.S3NestHttpResponse;
import it.iorfino.s3.handler.S3NestS3BucketNotFoundException;
import it.iorfino.s3.model.S3Operation;
import it.iorfino.s3.result.S3NestS3EmptyResult;
import it.iorfino.s3.result.S3NestS3ErrorResult;
import it.iorfino.s3.result.S3NestS3ObjectResult;
import it.iorfino.s3.result.S3NestS3XmlResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
    assertEquals(
        List.of(String.valueOf(bodyMessage.length())), response.headers().get("Content-Length"));
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

  /**
   * Verifies that the content length of an object result is propagated to the HTTP response as the
   * {@code Content-Length} header.
   */
  @Test
  void shouldMapObjectContentLengthToResponseHeader() {
    S3NestS3ResponseMapper mapper = new S3NestS3ResponseMapper();

    S3NestS3ObjectResult result =
        new S3NestS3ObjectResult(
            new ByteArrayInputStream("hello S3".getBytes(StandardCharsets.UTF_8)), 8, Map.of());

    S3NestHttpResponse response = mapper.map(result);

    assertEquals(List.of("8"), response.headers().get("Content-Length"));
  }

  /**
   * Verifies that empty S3 operation results are mapped to the HTTP status code defined by the
   * corresponding S3 operation semantics.
   */
  @ParameterizedTest
  @CsvSource({
    "CREATE_BUCKET, 200",
    "DELETE_BUCKET, 204",
    "DELETE_OBJECT, 204",
    "ABORT_MULTIPART_UPLOAD, 204",
    "HEAD_BUCKET, 200",
    "HEAD_OBJECT, 200",
    "PUT_OBJECT, 200",
    "COPY_OBJECT, 200"
  })
  void shouldMapEmptyResultToOperationSpecificStatusCode(
      S3Operation operation, int expectedStatusCode) {

    S3NestS3ResponseMapper mapper = new S3NestS3ResponseMapper();

    S3NestHttpResponse response = mapper.map(new S3NestS3EmptyResult(operation));

    assertEquals(expectedStatusCode, response.statusCode());
  }

  /**
   * Verifies that an XML operation result is mapped to a successful HTTP response with the XML
   * payload preserved in the response body and the appropriate content type.
   */
  @Test
  void shouldMapXmlResultToResponseBody() throws IOException {
    S3NestS3ResponseMapper mapper = new S3NestS3ResponseMapper();

    String xml = "<ListBucketResult><Name>test-bucket</Name></ListBucketResult>";
    S3NestS3XmlResult result = new S3NestS3XmlResult(S3Operation.LIST_OBJECTS, xml);

    S3NestHttpResponse response = mapper.map(result);

    assertEquals(200, response.statusCode());
    assertEquals(List.of("application/xml"), response.headers().get("Content-Type"));
    assertEquals(xml, responseBody(response));
  }

  /**
   * Verifies that an S3 error result is mapped to a {@code 404 Not Found} response containing the
   * corresponding S3 error code and message in the XML response body.
   */
  @Test
  void shouldMapNoSuchBucketErrorToNotFoundResponse() throws IOException {
    S3NestS3ResponseMapper mapper = new S3NestS3ResponseMapper();

    S3NestS3ErrorResult result =
        new S3NestS3ErrorResult(
            "NoSuchBucket", "The specified bucket does not exist.", null, null, null);

    S3NestHttpResponse response = mapper.map(result);

    assertEquals(404, response.statusCode());
    assertEquals(List.of("application/xml"), response.headers().get("Content-Type"));

    String body = responseBody(response);

    assertEquals(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<Error>\n"
            + "  <Code>NoSuchBucket</Code>\n"
            + "  <Message>The specified bucket does not exist.</Message>\n"
            + "</Error>\n",
        body);
  }

  /** Verifies that S3 error codes are mapped to their corresponding HTTP status codes. */
  @ParameterizedTest
  @CsvSource({
    "NoSuchBucket, 404",
    "NoSuchKey, 404",
    "NoSuchUpload, 404",
    "AccessDenied, 403",
    "InvalidAccessKeyId, 403",
    "SignatureDoesNotMatch, 403",
    "InvalidRequest, 400",
    "InvalidArgument, 400"
  })
  void shouldMapS3ErrorCodeToHttpStatusCode(String errorCode, int expectedStatusCode)
      throws IOException {

    S3NestS3ResponseMapper mapper = new S3NestS3ResponseMapper();

    S3NestS3ErrorResult result = new S3NestS3ErrorResult(errorCode, "Test error", null, null, null);

    S3NestHttpResponse response = mapper.map(result);

    assertEquals(expectedStatusCode, response.statusCode());
  }

  /**
   * Verifies that XML-sensitive characters in an S3 error message are escaped so that the generated
   * response remains a well-formed XML document.
   */
  @Test
  void shouldEscapeXmlCharactersInErrorMessage() throws IOException {
    S3NestS3ResponseMapper mapper = new S3NestS3ResponseMapper();

    S3NestS3ErrorResult result =
        new S3NestS3ErrorResult(
            "InvalidRequest", "Invalid value: <test> & \"example\"", null, null, null);

    S3NestHttpResponse response = mapper.map(result);

    assertEquals(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<Error>\n"
            + "  <Code>InvalidRequest</Code>\n"
            + "  <Message>Invalid value: &lt;test&gt; &amp; &quot;example&quot;</Message>\n"
            + "</Error>\n",
        responseBody(response));
  }

  /**
   * Verifies that an unrecognized S3 error code is translated into an internal server error without
   * exposing implementation details through the HTTP response.
   */
  @Test
  void shouldMapUnknownErrorCodeToInternalServerError() throws IOException {
    S3NestS3ResponseMapper mapper = new S3NestS3ResponseMapper();

    S3NestS3ErrorResult result =
        new S3NestS3ErrorResult("UnknownError", "An unexpected error occurred.", null, null, null);

    S3NestHttpResponse response = mapper.map(result);

    assertEquals(500, response.statusCode());
    assertEquals(List.of("application/xml"), response.headers().get("Content-Type"));
    assertEquals(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<Error>\n"
            + "  <Code>UnknownError</Code>\n"
            + "  <Message>An unexpected error occurred.</Message>\n"
            + "</Error>\n",
        responseBody(response));
  }

  /**
   * Verifies that a missing bucket exception is translated into the corresponding S3 error result.
   */
  @Test
  void shouldMapBucketNotFoundExceptionToNoSuchBucketError() {
    S3NestS3ErrorMapper mapper = new S3NestS3ErrorMapper();

    S3NestS3BucketNotFoundException exception = new S3NestS3BucketNotFoundException("test-bucket");

    S3NestS3ErrorResult result = mapper.map(exception);

    assertEquals("NoSuchBucket", result.code());
    assertEquals("The specified bucket does not exist.", result.message());
    assertEquals("test-bucket", result.bucket());
    assertNull(result.objectKey());
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
