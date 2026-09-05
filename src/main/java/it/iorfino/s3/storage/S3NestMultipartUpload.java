package it.iorfino.s3.storage;

import java.time.Instant;

/**
 * Represents the state of an active multipart upload.
 *
 * @param uploadId the unique multipart upload identifier
 * @param bucket the bucket containing the resulting object
 * @param key the opaque object key
 * @param metadata the metadata associated with the resulting object
 * @param initiatedAt the time at which the multipart upload was created
 */
public record S3NestMultipartUpload(
    String uploadId,
    String bucket,
    String key,
    S3NestObjectMetadata metadata,
    Instant initiatedAt) {}
