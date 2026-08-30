package it.iorfino.s3.result;

import it.iorfino.s3.model.S3NestS3OperationResult;
import it.iorfino.s3.model.S3Operation;

/**
 * Represents an S3 operation result that does not contain a response payload.
 *
 * <p>The result identifies the S3 operation that produced it so that the response layer can apply
 * the operation-specific HTTP response semantics, including the appropriate status code.
 */
public record S3NestS3EmptyResult(S3Operation operation) implements S3NestS3OperationResult {}
