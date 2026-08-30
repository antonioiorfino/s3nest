package it.iorfino.s3;

import static org.junit.jupiter.api.Assertions.*;

import it.iorfino.s3.model.S3NestS3OperationResult;
import it.iorfino.s3.model.S3NestS3Request;
import it.iorfino.s3.model.S3Operation;
import it.iorfino.s3.result.S3NestS3EmptyResult;
import it.iorfino.s3.routing.S3NestS3OperationHandler;
import it.iorfino.s3.routing.S3NestS3RequestRouter;
import it.iorfino.s3.routing.S3NestS3RequestRoutingException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

public class S3NestS3RequestRouterTest {

  /** Verifies that a parsed S3 request is routed to the handler associated with its operation. */
  @Test
  void shouldRouteRequestToMatchingHandler() {
    AtomicReference<S3NestS3Request> handledRequest = new AtomicReference<>();

    S3NestS3OperationHandler handler =
        request -> {
          handledRequest.set(request);
          return new S3NestS3EmptyResult();
        };

    S3NestS3Request request =
        new S3NestS3Request(
            S3Operation.GET_OBJECT,
            "my-bucket",
            "my-object",
            Map.of(),
            Map.of(),
            new ByteArrayInputStream(new byte[0]));

    S3NestS3RequestRouter router =
        new S3NestS3RequestRouter(Map.of(S3Operation.GET_OBJECT, handler));

    router.route(request);

    assertSame(request, handledRequest.get());
  }

  /**
   * Verifies that routing fails deterministically when no handler is registered for the requested
   * S3 operation.
   */
  @Test
  void shouldRejectRequestWithoutMatchingHandler() {
    S3NestS3Request request =
        new S3NestS3Request(
            S3Operation.GET_OBJECT,
            "my-bucket",
            "my-object",
            Map.of(),
            Map.of(),
            new ByteArrayInputStream(new byte[0]));

    S3NestS3RequestRouter router = new S3NestS3RequestRouter(Map.of());

    S3NestS3RequestRoutingException exception =
        assertThrows(S3NestS3RequestRoutingException.class, () -> router.route(request));

    assertEquals("Unsupported S3 operation: GET_OBJECT", exception.getMessage());
  }

  /**
   * Verifies that the router passes the original request instance to the operation handler without
   * modifying it.
   */
  @Test
  void shouldPassOriginalRequestToHandler() {
    AtomicReference<S3NestS3Request> handledRequest = new AtomicReference<>();

    S3NestS3OperationHandler handler =
        request -> {
          handledRequest.set(request);
          return new S3NestS3EmptyResult();
        };

    S3NestS3Request request =
        new S3NestS3Request(
            S3Operation.PUT_OBJECT,
            "my-bucket",
            "my-object",
            Map.of(),
            Map.of("content-type", List.of("text/plain")),
            new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8)));

    S3NestS3RequestRouter router =
        new S3NestS3RequestRouter(Map.of(S3Operation.PUT_OBJECT, handler));

    router.route(request);

    assertSame(request, handledRequest.get());
  }

  /** Verifies that different S3 operations are routed to their respective handlers. */
  @Test
  void shouldRouteDifferentOperationsToTheirHandlers() {
    AtomicReference<S3Operation> handledOperation = new AtomicReference<>();

    S3NestS3OperationHandler getHandler =
        request -> {
          handledOperation.set(S3Operation.GET_OBJECT);
          return new S3NestS3EmptyResult();
        };

    S3NestS3OperationHandler deleteHandler =
        request -> {
          handledOperation.set(S3Operation.DELETE_OBJECT);
          return new S3NestS3EmptyResult();
        };

    S3NestS3RequestRouter router =
        new S3NestS3RequestRouter(
            Map.of(
                S3Operation.GET_OBJECT, getHandler,
                S3Operation.DELETE_OBJECT, deleteHandler));

    S3NestS3Request getRequest =
        new S3NestS3Request(
            S3Operation.GET_OBJECT,
            "my-bucket",
            "my-object",
            Map.of(),
            Map.of(),
            new ByteArrayInputStream(new byte[0]));

    router.route(getRequest);

    assertEquals(S3Operation.GET_OBJECT, handledOperation.get());

    S3NestS3Request deleteRequest =
        new S3NestS3Request(
            S3Operation.DELETE_OBJECT,
            "my-bucket",
            "my-object",
            Map.of(),
            Map.of(),
            new ByteArrayInputStream(new byte[0]));

    router.route(deleteRequest);

    assertEquals(S3Operation.DELETE_OBJECT, handledOperation.get());
  }

  /** Verifies that the router returns the result produced by the operation handler. */
  @Test
  void shouldReturnHandlerResult() {
    S3NestS3OperationResult expectedResult = new S3NestS3EmptyResult();

    S3NestS3OperationHandler handler = request -> expectedResult;

    S3NestS3Request request =
        new S3NestS3Request(
            S3Operation.GET_OBJECT,
            "my-bucket",
            "my-object",
            Map.of(),
            Map.of(),
            new ByteArrayInputStream(new byte[0]));

    S3NestS3RequestRouter router =
        new S3NestS3RequestRouter(Map.of(S3Operation.GET_OBJECT, handler));

    S3NestS3OperationResult result = router.route(request);

    assertSame(expectedResult, result);
  }
}
