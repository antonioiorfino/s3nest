package it.iorfino.http;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Represents an HTTP request independently of the underlying HTTP transport.
 *
 * @param method  HTTP request method
 * @param path    request path
 * @param headers request headers
 * @param body    request body stream
 */
public record S3NestHttpRequest(
        String method,
        String path,
        String query,
        Map<String, List<String>> headers,
        InputStream body) {
}
