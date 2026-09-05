package it.iorfino.s3.storage;

public record S3NestMultipartPart(int partNumber, byte[] content, String eTag) {

  public S3NestMultipartPart {
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
