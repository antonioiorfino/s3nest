package it.iorfino.http;

/**
 * Handles HTTP requests independently of the underlying HTTP server
 * implementation.
 *
 * <p>Implementations operate exclusively on S3Nest HTTP abstractions and
 * must not depend on JDK-specific HTTP transport classes.</p>
 */
public interface S3NestHttpHandler {

    /**
     * Handles an HTTP request and produces its response.
     *
     * @param request the incoming HTTP request
     * @return the HTTP response
     */
    S3NestHttpResponse handle(S3NestHttpRequest request);
}
