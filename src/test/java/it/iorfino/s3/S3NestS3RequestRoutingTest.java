package it.iorfino.s3;

import static org.junit.jupiter.api.Assertions.*;

import it.iorfino.http.S3NestHttpRequest;
import it.iorfino.s3.handler.S3NestGetObjectHandler;
import it.iorfino.s3.model.S3NestS3Object;
import it.iorfino.s3.model.S3NestS3Request;
import it.iorfino.s3.model.S3Operation;
import it.iorfino.s3.parser.S3NestS3RequestParser;
import it.iorfino.s3.result.S3NestS3EmptyResult;
import it.iorfino.s3.routing.S3NestS3OperationHandler;
import it.iorfino.s3.routing.S3NestS3RequestRouter;
import it.iorfino.s3.storage.S3NestS3ObjectStorage;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

public class S3NestS3RequestRoutingTest {

  /**
   * Verifies that a parsed HTTP request can be routed to the handler associated with the resulting
   * S3 operation.
   */
  @Test
  void shouldParseAndRouteRequest() {
    AtomicReference<S3NestS3Request> handledRequest = new AtomicReference<>();

    S3NestS3OperationHandler handler =
        request -> {
          handledRequest.set(request);
          return new S3NestS3EmptyResult(S3Operation.GET_OBJECT);
        };

    S3NestS3RequestRouter router =
        new S3NestS3RequestRouter(Map.of(S3Operation.GET_OBJECT, handler));

    S3NestS3RequestParser parser = new S3NestS3RequestParser();

    S3NestHttpRequest httpRequest =
        new S3NestHttpRequest(
            "GET", "/my-bucket/my-object", "", Map.of(), new ByteArrayInputStream(new byte[0]));

    S3NestS3Request request = parser.parse(httpRequest);
    router.route(request);

    assertSame(request, handledRequest.get());
    assertEquals(S3Operation.GET_OBJECT, handledRequest.get().operation());
  }

  /** Verifies that a GET_OBJECT request is routed to the concrete GET object handler. */
  @Test
  void shouldRouteGetObjectToGetObjectHandler() {
    AtomicReference<S3NestS3Object> retrievedObject = new AtomicReference<>();
    final String bodyMessage = "hello";

    S3NestS3ObjectStorage storage =
        (bucket, objectKey) -> {
          S3NestS3Object object =
              new S3NestS3Object(
                  new ByteArrayInputStream(bodyMessage.getBytes(StandardCharsets.UTF_8)),
                  bodyMessage.length(),
                  Map.of());

          retrievedObject.set(object);
          return object;
        };

    S3NestGetObjectHandler handler = new S3NestGetObjectHandler(storage);

    S3NestS3RequestRouter router =
        new S3NestS3RequestRouter(Map.of(S3Operation.GET_OBJECT, handler));

    S3NestS3Request request =
        new S3NestS3Request(
            S3Operation.GET_OBJECT,
            "my-bucket",
            "my-object",
            Map.of(),
            Map.of(),
            new ByteArrayInputStream(new byte[0]));

    router.route(request);

    assertNotNull(retrievedObject.get());
  }
}
