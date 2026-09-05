package it.iorfino.s3.storage;

import java.time.Instant;

public record S3NestMultipartUpload(
    String uploadId,
    String bucket,
    String key,
    S3NestObjectMetadata metadata,
    Instant initiatedAt) {}
