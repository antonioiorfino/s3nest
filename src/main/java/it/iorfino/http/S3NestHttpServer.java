package it.iorfino.http;

/**
 * Provides the lifecycle and network binding of the S3Nest HTTP server.
 *
 * <p>The server owns its listening socket and request processing resources.
 * Implementations must not rely on global mutable server state.</p>
 *
 * <p>A configured port of {@code 0} requests automatic ephemeral port
 * allocation. The actual assigned port can be retrieved through
 * {@link #port()} after the server has started.</p>
 */
public interface S3NestHttpServer {

    /**
     * Starts the server and binds the configured TCP port.
     *
     * @throws IllegalStateException if the server cannot be started
     */
    void start();

    /**
     * Stops the server and releases all resources owned by it.
     *
     * <p>Calling this method repeatedly must have deterministic behaviour.</p>
     */
    void stop();

    /**
     * Returns the TCP port currently bound by the server.
     *
     * @return the bound port
     * @throws IllegalStateException if the server has not been started
     */
    int port();
}
