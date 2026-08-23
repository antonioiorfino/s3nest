package it.iorfino.s3.model;

/**
 * Represents the result produced by an S3 operation handler.
 *
 * <p>The result belongs to the S3 protocol layer and is independent of the
 * underlying HTTP transport and storage implementation.</p>
 *
 * <p>Concrete result types can represent successful operation results,
 * returned data or S3 errors.</p>
 */
public interface S3NestS3OperationResult {
}