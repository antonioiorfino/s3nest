package it.iorfino.s3.result;

import it.iorfino.s3.model.S3NestS3OperationResult;

/**
 * Represents an S3 operation failure independently of the HTTP transport layer.
 *
 * <p>The result contains the S3 error code and human-readable message that the response layer uses
 * to construct a protocol-compatible error response.
 *
 * @param code the S3 error code
 * @param message the human-readable error message
 */
public record S3NestS3ErrorResult(
    String code, String message, String bucket, String objectKey, String requestId)
    implements S3NestS3OperationResult {}
