package it.iorfino.s3.result;

import it.iorfino.s3.model.S3NestS3OperationResult;

/**
 * Represents an S3 operation result that does not contain a response payload.
 *
 * <p>This result is used by operations whose successful completion does not produce an S3 response
 * body.
 */
public final class S3NestS3EmptyResult implements S3NestS3OperationResult {}
