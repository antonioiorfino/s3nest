package it.iorfino.s3.storage;

import java.util.Arrays;

/**
 * Represents an object stored by S3Nest.
 *
 * @param bucket the bucket containing the object
 * @param key the opaque object key
 * @param content the object content
 * @param metadata the metadata associated with the object
 */
public record S3NestStoredObject(
    String bucket, String key, byte[] content, S3NestObjectMetadata metadata) {

  /**
   * Creates a stored object.
   *
   * <p>The content is defensively copied to prevent external modification of the stored byte array.
   */
  public S3NestStoredObject {
    content = Arrays.copyOf(content, content.length);
  }

  /**
   * Returns a defensive copy of the object content.
   *
   * @return a copy of the stored content
   */
  @Override
  public byte[] content() {
    return Arrays.copyOf(content, content.length);
  }
}
