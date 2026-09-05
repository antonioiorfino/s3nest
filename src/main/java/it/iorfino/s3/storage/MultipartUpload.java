package it.iorfino.s3.storage;

import java.time.Instant;

public record MultipartUpload(
    String uploadId, String bucket, String key, ObjectMetadata metadata, Instant initiatedAt) {}
