package it.iorfino.s3.storage;

import java.util.Arrays;

public record StoredObject(String bucket, String key, byte[] content, ObjectMetadata metadata) {

  public StoredObject {
    content = Arrays.copyOf(content, content.length);
  }

  @Override
  public byte[] content() {
    return Arrays.copyOf(content, content.length);
  }
}
