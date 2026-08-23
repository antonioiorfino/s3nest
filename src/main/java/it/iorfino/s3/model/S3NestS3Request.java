package it.iorfino.s3.model;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Represents an S3 request after it has been parsed from an HTTP request.
 *
 * <p>This class contains the semantic information required by S3 operation
 * handlers without exposing the underlying HTTP transport representation.</p>
 *
 * <p>The request contains the identified S3 operation, optional bucket and
 * object information, query parameters, request headers and the request
 * body.</p>
 *
 * <p>This class does not perform request parsing, routing, authentication or
 * storage access.</p>
 */
public final class S3NestS3Request {

    private final S3Operation operation;
    private final String bucket;
    private final String objectKey;
    private final Map<String, List<String>> queryParameters;
    private final Map<String, List<String>> headers;
    private final InputStream body;

    /**
     * Creates a parsed S3 request.
     *
     * @param operation       the S3 operation identified for the request
     * @param bucket          the requested bucket name, or {@code null} when the request
     *                        is not associated with a bucket
     * @param objectKey       the requested object key, or {@code null} when the request
     *                        is not associated with an object
     * @param queryParameters the parsed query parameters
     * @param headers         the relevant request headers
     * @param body            the request body stream
     */
    public S3NestS3Request(
            S3Operation operation,
            String bucket,
            String objectKey,
            Map<String, List<String>> queryParameters,
            Map<String, List<String>> headers,
            InputStream body) {
        this.operation = operation;
        this.bucket = bucket;
        this.objectKey = objectKey;
        this.queryParameters = queryParameters;
        this.headers = headers;
        this.body = body;
    }

    /**
     * @return the S3 operation identified for this request.
     */
    public S3Operation operation() {
        return operation;
    }

    /**
     * @return the requested bucket name, or {@code null}.
     */
    public String bucket() {
        return bucket;
    }

    /**
     * @return the requested object key, or {@code null}.
     */
    public String objectKey() {
        return objectKey;
    }

    public Map<String, List<String>> queryParameters() {
        return queryParameters;
    }

    public Map<String, List<String>> headers() {
        return headers;
    }

    public InputStream body() {
        return body;
    }
}
