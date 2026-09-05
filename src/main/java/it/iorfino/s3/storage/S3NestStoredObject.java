package it.iorfino.s3.storage;

import java.util.Arrays;

public record S3NestStoredObject(
    String bucket, String key, byte[] content, S3NestObjectMetadata metadata) {

  public S3NestStoredObject {
    content = Arrays.copyOf(content, content.length);
  }

  @Override
  public byte[] content() {
    return Arrays.copyOf(content, content.length);
  }
}
