package it.iorfino.s3.storage;

import java.time.Instant;

/**
 * Summary of an object stored by S3Nest.
 *
 * <p>This type contains the information required for object listing without exposing the complete
 * object content.
 *
 * @param key the opaque object key
 * @param size the object content length in bytes
 * @param eTag the entity tag associated with the object
 * @param lastModified the last modification timestamp
 */
public record S3NestObjectSummary(String key, long size, String eTag, Instant lastModified) {}
