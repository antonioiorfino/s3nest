package it.iorfino.s3.parser;

import it.iorfino.http.S3NestHttpRequest;
import it.iorfino.s3.model.S3NestS3Request;
import it.iorfino.s3.model.S3Operation;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses an HTTP request into an internal S3 request representation.
 *
 * <p>The parser is responsible for interpreting HTTP method, path, query
 * parameters, headers and request body in the context of the S3 protocol.</p>
 *
 * <p>The parser does not access storage, perform authentication or generate
 * HTTP responses.</p>
 */
public final class S3NestS3RequestParser {

    /**
     * Parses an incoming HTTP request into an internal S3 request.
     *
     * <p>The HTTP method and resource path determine the S3 operation, while
     * query parameters, headers and the request body are preserved as part of
     * the resulting request.</p>
     *
     * @param request the incoming HTTP request
     * @return the parsed S3 request
     * @throws S3NestS3RequestParsingException if the request path is malformed
     *                                         or the HTTP method is not supported
     */
    public S3NestS3Request parse(S3NestHttpRequest request) {
        String method = request.method();
        String path = request.path();

        if (!path.startsWith("/")) {
            throw new S3NestS3RequestParsingException(
                    "Unsupported S3 request: " + method + " " + path);
        }

        String resource = path.substring(1);

        return switch (method) {
            case "GET" -> parseGetRequest(request, resource);
            case "HEAD" -> parseHeadRequest(request, resource);
            case "PUT" -> parsePutRequest(request, resource);
            case "DELETE" -> parseDeleteRequest(request, resource);
            default -> throw new S3NestS3RequestParsingException(
                    "Unsupported S3 request: " + method + " " + path
            );
        };
    }

    /**
     * Parses the raw HTTP query string into named query parameters.
     *
     * <p>Parameter names and values are URL-decoded using UTF-8. Multiple
     * occurrences of the same parameter are preserved in insertion order.</p>
     *
     * <p>An empty or missing query string results in an empty parameter map.
     * Parameters without an explicit value are represented with an empty
     * string value.</p>
     *
     * @param query the raw query string, without the leading {@code ?}
     * @return a map containing the parsed query parameters
     */
    private Map<String, List<String>> parseQuery(String query) {
        if (query == null || query.isBlank()) {
            return Map.of();
        }

        Map<String, List<String>> parameters = new HashMap<>();

        for (String parameter : query.split("&")) {
            String[] parts = parameter.split("=", 2);

            String name = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length > 1
                    ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                    : "";

            parameters
                    .computeIfAbsent(name, ignored -> new ArrayList<>())
                    .add(value);
        }

        return parameters;
    }


    /**
     * Parses a GET request according to S3 resource semantics.
     *
     * <p>A GET request can target the S3 service, a bucket or an object:</p>
     *
     * <ul>
     *     <li>the root resource represents a request to list buckets;</li>
     *     <li>a bucket resource represents a request to list objects;</li>
     *     <li>an object resource represents a request to retrieve an object;</li>
     *     <li>the {@code list-type=2} query parameter selects ListObjectsV2.</li>
     * </ul>
     *
     * @param request  the incoming HTTP request
     * @param resource the request resource without the leading slash
     * @return the parsed S3 request
     */
    private S3NestS3Request parseGetRequest(
            S3NestHttpRequest request,
            String resource) {

        if (resource.isEmpty()) {
            return createRequest(
                    S3Operation.LIST_BUCKETS,
                    null,
                    null,
                    request
            );
        }

        if (resource.contains("/")) {
            ResourcePath resourcePath = parseResourcePath(resource);

            return createRequest(
                    S3Operation.GET_OBJECT,
                    resourcePath.bucket(),
                    resourcePath.objectKey(),
                    request
            );
        }

        Map<String, List<String>> queryParameters = parseQuery(request.query());

        if (isListObjectsV2(queryParameters)) {
            return createRequest(
                    S3Operation.LIST_OBJECTS_V2,
                    resource,
                    null,
                    request
            );
        }

        return createRequest(
                S3Operation.LIST_OBJECTS,
                resource,
                null,
                request
        );
    }

    /**
     * Determines whether the parsed query parameters identify an S3
     * ListObjectsV2 request.
     *
     * <p>The S3 ListObjectsV2 operation is selected when the {@code list-type}
     * query parameter is present and has the value {@code 2}.</p>
     *
     * @param queryParameters the parsed request query parameters
     * @return {@code true} if the request targets ListObjectsV2,
     *         otherwise {@code false}
     */
    private boolean isListObjectsV2(
            Map<String, List<String>> queryParameters) {
        return "2".equals(
                queryParameters
                        .getOrDefault("list-type", List.of())
                        .stream()
                        .findFirst()
                        .orElse(null)
        );
    }

    /**
     * Parses a HEAD request according to S3 resource semantics.
     *
     * <p>The presence of an object key determines whether the request targets
     * a bucket or an object.</p>
     *
     * <ul>
     *     <li>a bucket resource represents a HeadBucket operation;</li>
     *     <li>an object resource represents a HeadObject operation.</li>
     * </ul>
     *
     * @param request  the incoming HTTP request
     * @param resource the request resource without the leading slash
     * @return the parsed S3 request
     */
    private S3NestS3Request parseHeadRequest(
            S3NestHttpRequest request,
            String resource) {

        if (!resource.contains("/")) {
            return createRequest(
                    S3Operation.HEAD_BUCKET,
                    resource,
                    null,
                    request
            );
        }

        ResourcePath resourcePath = parseResourcePath(resource);

        return createRequest(
                S3Operation.HEAD_OBJECT,
                resourcePath.bucket(),
                resourcePath.objectKey(),
                request
        );
    }

    /**
     * Parses a PUT request according to S3 resource semantics.
     *
     * <p>The request target determines whether the operation creates a bucket
     * or stores an object.</p>
     *
     * <ul>
     *     <li>a bucket resource represents a CreateBucket operation;</li>
     *     <li>an object resource represents a PutObject operation.</li>
     * </ul>
     *
     * @param request  the incoming HTTP request
     * @param resource the request resource without the leading slash
     * @return the parsed S3 request
     */
    private S3NestS3Request parsePutRequest(
            S3NestHttpRequest request,
            String resource) {

        if (!resource.contains("/")) {
            return createRequest(
                    S3Operation.CREATE_BUCKET,
                    resource,
                    null,
                    request
            );
        }

        ResourcePath resourcePath = parseResourcePath(resource);

        return createRequest(
                S3Operation.PUT_OBJECT,
                resourcePath.bucket(),
                resourcePath.objectKey(),
                request
        );
    }

    /**
     * Parses a DELETE request according to S3 resource semantics.
     *
     * <p>The request target determines whether the operation deletes a bucket
     * or an object.</p>
     *
     * <ul>
     *     <li>a bucket resource represents a DeleteBucket operation;</li>
     *     <li>an object resource represents a DeleteObject operation.</li>
     * </ul>
     *
     * @param request  the incoming HTTP request
     * @param resource the request resource without the leading slash
     * @return the parsed S3 request
     */
    private S3NestS3Request parseDeleteRequest(
            S3NestHttpRequest request,
            String resource) {

        if (!resource.contains("/")) {
            return createRequest(
                    S3Operation.DELETE_BUCKET,
                    resource,
                    null,
                    request
            );
        }

        ResourcePath resourcePath = parseResourcePath(resource);

        return createRequest(
                S3Operation.DELETE_OBJECT,
                resourcePath.bucket(),
                resourcePath.objectKey(),
                request
        );
    }


    /**
     * Creates an internal S3 request from the identified operation and the
     * original HTTP request.
     *
     * <p>HTTP-specific data such as query parameters, headers and the request
     * body are transferred to the internal S3 request without applying
     * storage-specific logic.</p>
     *
     * @param operation the S3 operation identified by the parser
     * @param bucket    the target bucket, or {@code null} when not applicable
     * @param objectKey the target object key, or {@code null} when not applicable
     * @param request   the original HTTP request
     * @return the internal S3 request
     */
    private S3NestS3Request createRequest(
            S3Operation operation,
            String bucket,
            String objectKey,
            S3NestHttpRequest request) {

        return new S3NestS3Request(
                operation,
                bucket,
                objectKey,
                parseQuery(request.query()),
                request.headers(),
                request.body()
        );
    }


    /**
     * Parses an S3 resource into its bucket and object components.
     *
     * <p>The first path separator separates the bucket name from the object key.
     * Everything after that separator belongs to the object key, including
     * additional path separators. The object key is URL-decoded using UTF-8.</p>
     *
     * <p>When no separator is present, the resource represents a bucket and the
     * object key is {@code null}.</p>
     *
     * @param resource the request resource without the leading slash
     * @return the parsed bucket and object components
     */
    private ResourcePath parseResourcePath(String resource) {
        int separator = resource.indexOf('/');

        if (separator < 0) {
            return new ResourcePath(resource, null);
        }

        String bucket = resource.substring(0, separator);
        String objectKey = URLDecoder.decode(
                resource.substring(separator + 1),
                StandardCharsets.UTF_8
        );

        return new ResourcePath(bucket, objectKey);
    }


    /**
     * Represents the resource components extracted from an S3 request path.
     *
     * <p>A resource can identify either a bucket or an object. When the request
     * targets a bucket, {@code objectKey} is {@code null}.</p>
     *
     * @param bucket    the bucket name
     * @param objectKey the decoded object key, or {@code null} for bucket requests
     */
    private record ResourcePath(
            String bucket,
            String objectKey
    ) {

        /**
         * Indicates whether this resource identifies an object.
         *
         * @return {@code true} when an object key is present
         */
        boolean isObject() {
            return objectKey != null;
        }
    }
}
