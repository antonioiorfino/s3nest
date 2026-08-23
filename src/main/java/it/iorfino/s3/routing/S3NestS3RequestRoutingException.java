package it.iorfino.s3.routing;

/**
 * Indicates that an S3 request cannot be routed because no handler is
 * available for its operation.
 */
public final class S3NestS3RequestRoutingException extends RuntimeException {

    public S3NestS3RequestRoutingException(String message) {
        super(message);
    }
}