package it.iorfino.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * JDK-based implementation of the S3Nest HTTP server.
 *
 * <p>This implementation uses the Java 21 HTTP server and virtual threads
 * to provide the HTTP infrastructure required by the S3 protocol layer.</p>
 *
 * <p>The implementation is responsible for TCP port binding, HTTP request
 * adaptation, request execution and resource lifecycle management. S3
 * protocol semantics are delegated to the configured
 * {@link S3NestHttpHandler}.</p>
 *
 * <p>Each instance owns its own listening socket and request processing
 * executor. No global server state is used.</p>
 */
public final class S3NestHttpServerImpl implements S3NestHttpServer {

    private final int configuredPort;
    private final S3NestHttpHandler handler;
    private com.sun.net.httpserver.HttpServer server;
    private ExecutorService executorService;

    /**
     * Creates an HTTP server.
     *
     * <p>A port of {@code 0} requests automatic ephemeral port allocation from
     * the operating system.</p>
     *
     * @param port    the configured TCP port, or {@code 0} for automatic allocation
     * @param handler the handler responsible for processing HTTP requests
     */
    public S3NestHttpServerImpl(int port, S3NestHttpHandler handler) {
        this.configuredPort = port;
        this.handler = handler;
    }

    /**
     * Starts the HTTP server and binds the configured TCP port.
     *
     * <p>The server uses a virtual thread per request, allowing multiple
     * requests to be processed concurrently without serializing request
     * handling through a single worker thread.</p>
     *
     * @throws IllegalStateException if the server cannot be started
     */
    @Override
    public void start() {
        try {
            InetSocketAddress address = new InetSocketAddress(configuredPort);
            this.server = com.sun.net.httpserver.HttpServer.create(address, 0);
            this.server.createContext("/", exchange -> {
                S3NestHttpRequest request = new S3NestHttpRequest(
                        exchange.getRequestMethod(),
                        exchange.getRequestURI().getPath(),
                        exchange.getRequestHeaders(),
                        exchange.getRequestBody()
                );

                S3NestHttpResponse response = this.handler.handle(request);
                response.headers().forEach((name, values) ->
                        exchange.getResponseHeaders().put(name, values));

                exchange.sendResponseHeaders(
                        response.statusCode(),
                        0
                );
                try (OutputStream outputStream = exchange.getResponseBody()) {
                    response.writeBody(outputStream);
                }
            });

            this.executorService = Executors.newVirtualThreadPerTaskExecutor();
            this.server.setExecutor(this.executorService);
            this.server.start();
        } catch (IOException e) {
            if (this.server != null) {
                this.server.stop(0);
                this.server = null;
            }
            if (this.executorService != null) {
                this.executorService.close();
                this.executorService = null;
            }
            throw new IllegalStateException("Failed to start HTTP server", e);
        }
    }

    /**
     * Stops the HTTP server and releases all resources owned by the instance.
     *
     * <p>The listening socket is closed and the request processing executor is
     * shut down. Calling this method when the server is already stopped has no
     * effect.</p>
     */
    @Override
    public void stop() {
        if (server == null) {
            return;
        }
        server.stop(0);
        server = null;

        if (executorService != null) {
            this.executorService.close();
            this.executorService = null;
        }
    }

    /**
     * Returns the TCP port currently bound by this server.
     *
     * <p>This method also returns the actual operating-system-assigned port
     * when the server was configured with port {@code 0}.</p>
     *
     * @return the currently bound TCP port
     * @throws IllegalStateException if the server has not been started
     */
    @Override
    public int port() {
        if (server == null) {
            throw new IllegalStateException("HTTP server has not been started");
        }
        return server.getAddress().getPort();
    }
}
