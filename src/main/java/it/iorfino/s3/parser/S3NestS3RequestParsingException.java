package it.iorfino.s3.parser;

/**
 * Indicates that an HTTP request cannot be parsed as a valid S3 request.
 *
 * <p>This exception represents a request-layer error, such as a malformed
 * path, invalid query parameters or an unsupported request structure.</p>
 *
 * <p>The exception does not expose transport-specific or storage-specific
 * implementation details.</p>
 */
public class S3NestS3RequestParsingException extends RuntimeException {

    /**
     * Creates a request parsing exception with the specified message.
     *
     * @param message description of the parsing error
     */
    public S3NestS3RequestParsingException(String message) {
        super(message);
    }

    /**
     * Creates a request parsing exception with the specified message and cause.
     *
     * @param message description of the parsing error
     * @param cause   the underlying cause of the parsing error
     */
    public S3NestS3RequestParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
