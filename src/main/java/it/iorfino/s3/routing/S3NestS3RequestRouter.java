package it.iorfino.s3.routing;

import it.iorfino.s3.model.S3NestS3OperationResult;
import it.iorfino.s3.model.S3NestS3Request;
import it.iorfino.s3.model.S3Operation;

import java.util.Map;
import java.util.Objects;

/**
 * Routes parsed S3 requests to the handler associated with their operation.
 *
 * <p>The router operates exclusively on {@link S3NestS3Request} and
 * {@link S3Operation}. It does not inspect HTTP methods or paths.</p>
 */
public final class S3NestS3RequestRouter {

    private final Map<S3Operation, S3NestS3OperationHandler> handlers;

    /**
     * Creates a router with the supplied operation handlers.
     *
     * @param handlers handlers indexed by S3 operation
     */
    public S3NestS3RequestRouter(
            Map<S3Operation, S3NestS3OperationHandler> handlers) {
        this.handlers = Map.copyOf(
                Objects.requireNonNull(handlers, "handlers")
        );
    }

    /**
     * Routes the supplied S3 request to the handler associated with its
     * operation.
     *
     * <p>The result produced by the handler is returned to the caller so that
     * the response layer can translate the operation result into an HTTP
     * response.</p>
     *
     * @param request the parsed S3 request
     * @return the result produced by the operation handler
     * @throws S3NestS3RequestRoutingException if no handler is registered
     *                                         for the requested operation
     */
    public S3NestS3OperationResult route(S3NestS3Request request) {
        S3NestS3OperationHandler handler = handlers.get(request.operation());

        if (handler == null) {
            throw new S3NestS3RequestRoutingException(
                    "Unsupported S3 operation: " + request.operation()
            );
        }

        return handler.handle(request);
    }
}