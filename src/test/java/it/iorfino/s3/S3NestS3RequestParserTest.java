package it.iorfino.s3;

import static org.junit.jupiter.api.Assertions.*;

import it.iorfino.http.S3NestHttpRequest;
import it.iorfino.s3.model.S3NestS3Request;
import it.iorfino.s3.model.S3Operation;
import it.iorfino.s3.parser.S3NestS3RequestParser;
import it.iorfino.s3.parser.S3NestS3RequestParsingException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class S3NestS3RequestParserTest {

  /**
   * Verifies that a GET request targeting a bucket is interpreted as an object listing operation.
   *
   * <p>The bucket name must be extracted from the request path and preserved in the parsed S3
   * request.
   */
  @Test
  void shouldParseBucketObjectListingRequest() {
    S3NestHttpRequest httpRequest =
        new S3NestHttpRequest(
            "GET", "/my-bucket", "", Map.of(), new ByteArrayInputStream(new byte[0]));

    S3NestS3RequestParser parser = new S3NestS3RequestParser();

    S3NestS3Request request = parser.parse(httpRequest);

    assertEquals(S3Operation.LIST_OBJECTS, request.operation());
    assertEquals("my-bucket", request.bucket());
  }

  /**
   * Verifies that a GET request targeting an object is interpreted as a {@link
   * S3Operation#GET_OBJECT} operation.
   *
   * <p>The parser must extract both the bucket name and the complete object key from the request
   * path.
   */
  @Test
  void shouldParseObjectRequest() {
    S3NestHttpRequest httpRequest =
        new S3NestHttpRequest(
            "GET",
            "/my-bucket/photos/image.jpg",
            "",
            Map.of(),
            new ByteArrayInputStream(new byte[0]));

    S3NestS3RequestParser parser = new S3NestS3RequestParser();

    S3NestS3Request request = parser.parse(httpRequest);

    assertEquals(S3Operation.GET_OBJECT, request.operation());
    assertEquals("my-bucket", request.bucket());
    assertEquals("photos/image.jpg", request.objectKey());
  }

  /**
   * Verifies that URL-encoded characters in an object key are decoded while preserving the object
   * key structure.
   */
  @Test
  void shouldDecodeUrlEncodedObjectKey() {
    S3NestHttpRequest httpRequest =
        new S3NestHttpRequest(
            "GET",
            "/my-bucket/photos/my%20image.jpg",
            "",
            Map.of(),
            new ByteArrayInputStream(new byte[0]));

    S3NestS3RequestParser parser = new S3NestS3RequestParser();

    S3NestS3Request request = parser.parse(httpRequest);

    assertEquals(S3Operation.GET_OBJECT, request.operation());
    assertEquals("my-bucket", request.bucket());
    assertEquals("photos/my image.jpg", request.objectKey());
  }

  /**
   * Verifies that a HEAD request targeting a bucket is interpreted as a {@link
   * S3Operation#HEAD_BUCKET} operation.
   */
  @Test
  void shouldParseHeadBucketRequest() {
    S3NestHttpRequest httpRequest =
        new S3NestHttpRequest(
            "HEAD", "/my-bucket", "", Map.of(), new ByteArrayInputStream(new byte[0]));

    S3NestS3RequestParser parser = new S3NestS3RequestParser();

    S3NestS3Request request = parser.parse(httpRequest);

    assertEquals(S3Operation.HEAD_BUCKET, request.operation());
    assertEquals("my-bucket", request.bucket());
    assertEquals(null, request.objectKey());
  }

  /**
   * Verifies that a PUT request targeting a bucket is interpreted as a {@link
   * S3Operation#CREATE_BUCKET} operation.
   */
  @Test
  void shouldParseCreateBucketRequest() {
    S3NestHttpRequest httpRequest =
        new S3NestHttpRequest(
            "PUT", "/my-bucket", "", Map.of(), new ByteArrayInputStream(new byte[0]));

    S3NestS3RequestParser parser = new S3NestS3RequestParser();

    S3NestS3Request request = parser.parse(httpRequest);

    assertEquals(S3Operation.CREATE_BUCKET, request.operation());
    assertEquals("my-bucket", request.bucket());
    assertEquals(null, request.objectKey());
  }

  /**
   * Verifies that a DELETE request targeting a bucket is interpreted as a {@link
   * S3Operation#DELETE_BUCKET} operation.
   */
  @Test
  void shouldParseDeleteBucketRequest() {
    S3NestHttpRequest httpRequest =
        new S3NestHttpRequest(
            "DELETE", "/my-bucket", "", Map.of(), new ByteArrayInputStream(new byte[0]));

    S3NestS3RequestParser parser = new S3NestS3RequestParser();

    S3NestS3Request request = parser.parse(httpRequest);

    assertEquals(S3Operation.DELETE_BUCKET, request.operation());
    assertEquals("my-bucket", request.bucket());
    assertEquals(null, request.objectKey());
  }

  /**
   * Verifies that a PUT request targeting an object is interpreted as a {@link
   * S3Operation#PUT_OBJECT} operation.
   *
   * <p>The parser must distinguish an object request from a bucket creation request based on the
   * presence of an object key in the request path.
   */
  @Test
  void shouldParsePutObjectRequest() {
    S3NestHttpRequest httpRequest =
        new S3NestHttpRequest(
            "PUT",
            "/my-bucket/photos/image.jpg",
            "",
            Map.of(),
            new ByteArrayInputStream(new byte[0]));

    S3NestS3RequestParser parser = new S3NestS3RequestParser();

    S3NestS3Request request = parser.parse(httpRequest);

    assertEquals(S3Operation.PUT_OBJECT, request.operation());
    assertEquals("my-bucket", request.bucket());
    assertEquals("photos/image.jpg", request.objectKey());
  }

  /**
   * Verifies that a DELETE request targeting an object is interpreted as a {@link
   * S3Operation#DELETE_OBJECT} operation.
   *
   * <p>The parser must extract both the bucket name and the complete object key from the request
   * path.
   */
  @Test
  void shouldParseDeleteObjectRequest() {
    S3NestHttpRequest httpRequest =
        new S3NestHttpRequest(
            "DELETE",
            "/my-bucket/photos/image.jpg",
            "",
            Map.of(),
            new ByteArrayInputStream(new byte[0]));

    S3NestS3RequestParser parser = new S3NestS3RequestParser();

    S3NestS3Request request = parser.parse(httpRequest);

    assertEquals(S3Operation.DELETE_OBJECT, request.operation());
    assertEquals("my-bucket", request.bucket());
    assertEquals("photos/image.jpg", request.objectKey());
  }

  /**
   * Verifies that a HEAD request targeting an object is interpreted as a {@link
   * S3Operation#HEAD_OBJECT} operation.
   *
   * <p>The parser must extract both the bucket name and the complete object key from the request
   * path.
   */
  @Test
  void shouldParseHeadObjectRequest() {
    S3NestHttpRequest httpRequest =
        new S3NestHttpRequest(
            "HEAD",
            "/my-bucket/photos/image.jpg",
            "",
            Map.of(),
            new ByteArrayInputStream(new byte[0]));

    S3NestS3RequestParser parser = new S3NestS3RequestParser();

    S3NestS3Request request = parser.parse(httpRequest);

    assertEquals(S3Operation.HEAD_OBJECT, request.operation());
    assertEquals("my-bucket", request.bucket());
    assertEquals("photos/image.jpg", request.objectKey());
  }

  /**
   * Verifies that a GET request targeting the root path is interpreted as a {@link
   * S3Operation#LIST_BUCKETS} operation.
   */
  @Test
  void shouldParseListBucketsRequest() {
    S3NestHttpRequest httpRequest =
        new S3NestHttpRequest("GET", "/", "", Map.of(), new ByteArrayInputStream(new byte[0]));

    S3NestS3RequestParser parser = new S3NestS3RequestParser();

    S3NestS3Request request = parser.parse(httpRequest);

    assertEquals(S3Operation.LIST_BUCKETS, request.operation());
    assertEquals(null, request.bucket());
    assertEquals(null, request.objectKey());
  }

  /**
   * Verifies that a GET request with the S3 ListObjectsV2 query parameter is interpreted as a
   * {@link S3Operation#LIST_OBJECTS_V2} operation.
   */
  @Test
  void shouldParseListObjectsV2Request() {
    S3NestHttpRequest httpRequest =
        new S3NestHttpRequest(
            "GET", "/my-bucket", "list-type=2", Map.of(), new ByteArrayInputStream(new byte[0]));

    S3NestS3RequestParser parser = new S3NestS3RequestParser();

    S3NestS3Request request = parser.parse(httpRequest);

    assertEquals(S3Operation.LIST_OBJECTS_V2, request.operation());
    assertEquals("my-bucket", request.bucket());
    assertEquals(null, request.objectKey());
  }

  /** Verifies that query parameters are parsed and exposed by the resulting S3 request. */
  @Test
  void shouldParseQueryParameters() {
    S3NestHttpRequest httpRequest =
        new S3NestHttpRequest(
            "GET",
            "/my-bucket",
            "prefix=photos&max-keys=100",
            Map.of(),
            new ByteArrayInputStream(new byte[0]));

    S3NestS3RequestParser parser = new S3NestS3RequestParser();

    S3NestS3Request request = parser.parse(httpRequest);

    assertEquals("photos", request.queryParameters().get("prefix").getFirst());
    assertEquals("100", request.queryParameters().get("max-keys").getFirst());
  }

  /**
   * Verifies that repeated query parameters are preserved instead of overwriting previous values.
   */
  @Test
  void shouldPreserveRepeatedQueryParameters() {
    S3NestHttpRequest httpRequest =
        new S3NestHttpRequest(
            "GET",
            "/my-bucket",
            "prefix=photos&prefix=documents",
            Map.of(),
            new ByteArrayInputStream(new byte[0]));

    S3NestS3RequestParser parser = new S3NestS3RequestParser();

    S3NestS3Request request = parser.parse(httpRequest);

    assertEquals(List.of("photos", "documents"), request.queryParameters().get("prefix"));
  }

  /** Verifies that URL-encoded query parameter names and values are decoded correctly. */
  @Test
  void shouldDecodeUrlEncodedQueryParameters() {
    S3NestHttpRequest httpRequest =
        new S3NestHttpRequest(
            "GET",
            "/my-bucket",
            "prefix=my%20photos",
            Map.of(),
            new ByteArrayInputStream(new byte[0]));

    S3NestS3RequestParser parser = new S3NestS3RequestParser();

    S3NestS3Request request = parser.parse(httpRequest);

    assertEquals("my photos", request.queryParameters().get("prefix").getFirst());
  }

  /** Verifies that relevant S3 request headers are preserved by the request parser. */
  @Test
  void shouldPreserveS3Headers() {
    Map<String, List<String>> headers = Map.of("x-amz-content-sha256", List.of("UNSIGNED-PAYLOAD"));

    S3NestHttpRequest httpRequest =
        new S3NestHttpRequest(
            "PUT", "/my-bucket/my-object", "", headers, new ByteArrayInputStream(new byte[0]));

    S3NestS3RequestParser parser = new S3NestS3RequestParser();

    S3NestS3Request request = parser.parse(httpRequest);

    assertEquals(List.of("UNSIGNED-PAYLOAD"), request.headers().get("x-amz-content-sha256"));
  }

  /** Verifies that the request body is preserved and remains available to the operation handler. */
  @Test
  void shouldPreserveRequestBody() throws IOException {
    byte[] body = "hello s3".getBytes(StandardCharsets.UTF_8);

    S3NestHttpRequest httpRequest =
        new S3NestHttpRequest(
            "PUT", "/my-bucket/my-object", "", Map.of(), new ByteArrayInputStream(body));

    S3NestS3RequestParser parser = new S3NestS3RequestParser();

    S3NestS3Request request = parser.parse(httpRequest);

    assertArrayEquals(body, request.body().readAllBytes());
  }

  /**
   * Verifies that a request with an empty body is accepted and exposes an empty request body to the
   * operation handler.
   */
  @Test
  void shouldHandleEmptyRequestBody() throws IOException {
    S3NestHttpRequest httpRequest =
        new S3NestHttpRequest(
            "PUT", "/my-bucket/my-object", "", Map.of(), new ByteArrayInputStream(new byte[0]));

    S3NestS3RequestParser parser = new S3NestS3RequestParser();

    S3NestS3Request request = parser.parse(httpRequest);

    assertEquals(0, request.body().readAllBytes().length);
  }

  /** Verifies that an unsupported HTTP method is rejected by the parser. */
  @Test
  void shouldRejectUnsupportedHttpMethod() {
    S3NestHttpRequest httpRequest =
        new S3NestHttpRequest(
            "PATCH", "/my-bucket/my-object", "", Map.of(), new ByteArrayInputStream(new byte[0]));

    S3NestS3RequestParser parser = new S3NestS3RequestParser();

    S3NestS3RequestParsingException exception =
        assertThrows(S3NestS3RequestParsingException.class, () -> parser.parse(httpRequest));

    assertEquals("Unsupported S3 request: PATCH /my-bucket/my-object", exception.getMessage());
  }

  /** Verifies that a request with a malformed resource path is rejected by the parser. */
  @Test
  void shouldRejectMalformedRequestPath() {
    S3NestHttpRequest httpRequest =
        new S3NestHttpRequest(
            "GET", "my-bucket", "", Map.of(), new ByteArrayInputStream(new byte[0]));

    S3NestS3RequestParser parser = new S3NestS3RequestParser();

    S3NestS3RequestParsingException exception =
        assertThrows(S3NestS3RequestParsingException.class, () -> parser.parse(httpRequest));

    assertEquals("Unsupported S3 request: GET my-bucket", exception.getMessage());
  }

  /**
   * Verifies that ListObjectsV2 is identified when the list-type query parameter is combined with
   * other query parameters.
   */
  @Test
  void shouldParseListObjectsV2WithAdditionalQueryParameters() {
    S3NestHttpRequest httpRequest =
        new S3NestHttpRequest(
            "GET",
            "/my-bucket",
            "prefix=photos&list-type=2",
            Map.of(),
            new ByteArrayInputStream(new byte[0]));

    S3NestS3RequestParser parser = new S3NestS3RequestParser();

    S3NestS3Request request = parser.parse(httpRequest);

    assertEquals(S3Operation.LIST_OBJECTS_V2, request.operation());
    assertEquals("my-bucket", request.bucket());
    assertEquals("photos", request.queryParameters().get("prefix").getFirst());
    assertEquals("2", request.queryParameters().get("list-type").getFirst());
  }
}
