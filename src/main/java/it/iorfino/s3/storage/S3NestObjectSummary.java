package it.iorfino.s3.storage;

import java.time.Instant;

public record S3NestObjectSummary(String key, long size, String eTag, Instant lastModified) {}
