package it.iorfino.s3.model;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Represents an object retrieved from S3 storage.
 *
 * <p>The object contains its content stream and associated metadata.
 * It is independent of both HTTP transport and the concrete storage
 * implementation.</p>
 */
public final class S3NestS3Object {

    private final InputStream body;
    private final Map<String, List<String>> metadata;

    /**
     * Creates a stored S3 object.
     *
     * @param body the object content stream
     * @param metadata the object metadata
     */
    public S3NestS3Object(
            InputStream body,
            Map<String, List<String>> metadata) {
        this.body = body;
        this.metadata = metadata;
    }

    /**
     * @return the object content stream
     */
    public InputStream body() {
        return body;
    }

    /**
     * @return the object metadata
     */
    public Map<String, List<String>> metadata() {
        return metadata;
    }
}