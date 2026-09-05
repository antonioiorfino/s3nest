package it.iorfino.s3.storage;

import java.time.Instant;

public record ObjectSummary(String key, long size, String eTag, Instant lastModified) {}
