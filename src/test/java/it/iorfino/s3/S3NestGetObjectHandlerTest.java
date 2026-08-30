package it.iorfino.s3;

import static org.junit.jupiter.api.Assertions.*;

import it.iorfino.s3.handler.S3NestGetObjectHandler;
import it.iorfino.s3.handler.S3NestS3ObjectNotFoundException;
import it.iorfino.s3.model.S3NestS3Object;
import it.iorfino.s3.model.S3NestS3OperationResult;
import it.iorfino.s3.model.S3NestS3Request;
import it.iorfino.s3.model.S3Operation;
import it.iorfino.s3.result.S3NestS3ObjectResult;
import it.iorfino.s3.storage.S3NestS3ObjectStorage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

public class S3NestGetObjectHandlerTest {

  /** Verifies that the GET object handler returns the object retrieved from the storage port. */
  @Test
  void shouldReturnObjectFromStorage() {
    String bodyMessage = "hello";
    InputStream body = new ByteArrayInputStream(bodyMessage.getBytes(StandardCharsets.UTF_8));

    Map<String, List<String>> metadata = Map.of("content-type", List.of("text/plain"));

    S3NestS3Object object = new S3NestS3Object(body, bodyMessage.length(), metadata);

    S3NestS3ObjectStorage storage = (bucket, objectKey) -> object;

    S3NestGetObjectHandler handler = new S3NestGetObjectHandler(storage);

    S3NestS3Request request =
        new S3NestS3Request(
            S3Operation.GET_OBJECT,
            "my-bucket",
            "my-object",
            Map.of(),
            Map.of(),
            new ByteArrayInputStream(new byte[0]));

    S3NestS3OperationResult result = handler.handle(request);

    S3NestS3ObjectResult objectResult = (S3NestS3ObjectResult) result;

    assertSame(body, objectResult.body());
    assertEquals(metadata, objectResult.metadata());
  }

  /**
   * Verifies that the GET object handler passes the bucket and object key from the S3 request to
   * the storage port.
   */
  @Test
  void shouldRequestObjectUsingBucketAndObjectKey() {
    AtomicReference<String> requestedBucket = new AtomicReference<>();
    AtomicReference<String> requestedObjectKey = new AtomicReference<>();
    final String bodyMessage = "hello";

    InputStream body = new ByteArrayInputStream(bodyMessage.getBytes(StandardCharsets.UTF_8));

    S3NestS3ObjectStorage storage =
        (bucket, objectKey) -> {
          requestedBucket.set(bucket);
          requestedObjectKey.set(objectKey);

          return new S3NestS3Object(body, bodyMessage.length(), Map.of());
        };

    S3NestGetObjectHandler handler = new S3NestGetObjectHandler(storage);

    S3NestS3Request request =
        new S3NestS3Request(
            S3Operation.GET_OBJECT,
            "my-bucket",
            "path/to/my-object.txt",
            Map.of(),
            Map.of(),
            new ByteArrayInputStream(new byte[0]));

    handler.handle(request);

    assertEquals("my-bucket", requestedBucket.get());
    assertEquals("path/to/my-object.txt", requestedObjectKey.get());
  }

  /** Verifies that the GET object handler rejects a missing object returned by the storage port. */
  @Test
  void shouldRejectMissingObject() {
    S3NestS3ObjectStorage storage = (bucket, objectKey) -> null;

    S3NestGetObjectHandler handler = new S3NestGetObjectHandler(storage);

    S3NestS3Request request =
        new S3NestS3Request(
            S3Operation.GET_OBJECT,
            "my-bucket",
            "missing-object",
            Map.of(),
            Map.of(),
            new ByteArrayInputStream(new byte[0]));

    assertThrows(S3NestS3ObjectNotFoundException.class, () -> handler.handle(request));
  }

  /** Verifies that storage failures are propagated by the GET object handler. */
  @Test
  void shouldPropagateStorageFailure() {
    RuntimeException storageFailure = new RuntimeException("Storage unavailable");

    S3NestS3ObjectStorage storage =
        (bucket, objectKey) -> {
          throw storageFailure;
        };

    S3NestGetObjectHandler handler = new S3NestGetObjectHandler(storage);

    S3NestS3Request request =
        new S3NestS3Request(
            S3Operation.GET_OBJECT,
            "my-bucket",
            "my-object",
            Map.of(),
            Map.of(),
            new ByteArrayInputStream(new byte[0]));

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> handler.handle(request));

    assertSame(storageFailure, exception);
  }
}
