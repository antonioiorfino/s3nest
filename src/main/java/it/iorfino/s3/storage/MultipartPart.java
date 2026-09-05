package it.iorfino.s3.storage;

public record MultipartPart(int partNumber, byte[] content, String eTag) {

  public MultipartPart {
    if (partNumber <= 0) {
      throw new IllegalArgumentException("partNumber must be positive");
    }

    content = content.clone();
  }

  @Override
  public byte[] content() {
    return content.clone();
  }
}
