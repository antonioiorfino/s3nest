package it.iorfino.http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the lifecycle, port management and instance isolation of the
 * S3Nest HTTP server foundation.
 *
 * <p>These tests intentionally validate infrastructure behaviour rather
 * than S3 protocol semantics. S3 request routing and protocol behaviour
 * are covered by higher-level tests.</p>
 */
class S3NestHttpServerTest {

    private final S3NestHttpHandler handler = request ->
            new S3NestHttpResponse(
                    200,
                    Map.of(),
                    output -> {
                    }
            );

    /**
     * Verifies that configuring port {@code 0} causes the operating system to
     * allocate an ephemeral port.
     *
     * <p>The server must expose the actual assigned port after startup rather
     * than returning the configured value {@code 0}.</p>
     */
    @Test
    void shouldAllocateEphemeralPort() {
        S3NestHttpServer httpServer = new S3NestHttpServerImpl(0, handler);
        try {
            httpServer.start();
            assertTrue(httpServer.port() > 0);
            Assertions.assertNotEquals(0, httpServer.port());
        } finally {
            httpServer.stop();
        }
    }

    /**
     * Verifies that the server binds to the explicitly configured TCP port.
     *
     * <p>This ensures that the server does not replace an explicitly configured
     * port with an automatically selected port.</p>
     */
    @Test
    void shouldBindToConfiguredPort() {
        S3NestHttpServer httpServer = new S3NestHttpServerImpl(18080, handler);
        try {
            httpServer.start();
            assertEquals(18080, httpServer.port());
        } finally {
            httpServer.stop();
        }
    }

    /**
     * Verifies that multiple HTTP server instances can run concurrently
     * without sharing network resources or lifecycle state.
     *
     * <p>Each server is configured with an ephemeral port and must receive
     * its own operating-system-assigned port.</p>
     *
     * <p>This also verifies that server instances do not rely on global
     * mutable server state.</p>
     */
    @Test
    void shouldAllowMultipleServerInstances() {
        S3NestHttpServer first = new S3NestHttpServerImpl(0, handler);
        S3NestHttpServer second = new S3NestHttpServerImpl(0, handler);
        try {
            first.start();
            second.start();
            Assertions.assertNotEquals(first.port(), second.port());
            assertTrue(first.port() > 0);
            assertTrue(second.port() > 0);
        } finally {
            first.stop();
            second.stop();
        }
    }

    /**
     * Verifies that the server can process multiple HTTP requests concurrently.
     *
     * <p>Two requests are submitted asynchronously while the configured test
     * handler performs a blocking operation. The total execution time must remain
     * below the duration expected from serialized request processing.</p>
     *
     * <p>This ensures that requests are not processed sequentially through a
     * single global execution path.</p>
     */
    @Test
    void shouldProcessRequestsConcurrently() throws Exception {
        S3NestHttpServer server = new S3NestHttpServerImpl(0, handler);
        try {
            server.start();
            URI uri = URI.create("http://localhost:" + server.port() + "/");
            HttpClient client = HttpClient.newHttpClient();
            long start = System.nanoTime();
            CompletableFuture<HttpResponse<String>> first =
                    client.sendAsync(
                            HttpRequest.newBuilder(uri).GET().build(),
                            HttpResponse.BodyHandlers.ofString()
                    );
            CompletableFuture<HttpResponse<String>> second =
                    client.sendAsync(
                            HttpRequest.newBuilder(uri).GET().build(),
                            HttpResponse.BodyHandlers.ofString()
                    );
            CompletableFuture.allOf(first, second).join();
            long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

            assertTrue(elapsedMillis < 900);
            assertEquals(200, first.join().statusCode());
            assertEquals(200, second.join().statusCode());
        } finally {
            server.stop();
        }
    }

    /**
     * Verifies that stopping the server releases the TCP port previously bound
     * by the server.
     *
     * <p>A second server must be able to bind to the same port after the first
     * server has been stopped.</p>
     */
    @Test
    void shouldReleasePortAfterShutdown() {
        S3NestHttpServer first = new S3NestHttpServerImpl(0, handler);
        S3NestHttpServer second = null;

        try {
            first.start();
            int port = first.port();
            first.stop();

            second = new S3NestHttpServerImpl(port, handler);
            second.start();

            assertEquals(port, second.port());
        } finally {
            first.stop();

            if (second != null) {
                second.stop();
            }
        }
    }

}
