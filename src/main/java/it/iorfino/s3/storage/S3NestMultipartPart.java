package it.iorfino.s3.storage;

/**
 * Represents a part belonging to a multipart upload.
 *
 * @param partNumber the multipart part number
 * @param content the part content
 * @param eTag the entity tag associated with the part
 */
public record S3NestMultipartPart(int partNumber, byte[] content, String eTag) {

  /**
   * Creates a multipart part.
   *
   * <p>The content is defensively copied to prevent external modification.
   *
   * @throws IllegalArgumentException if {@code partNumber} is not positive
   */
  public S3NestMultipartPart {
    if (partNumber <= 0) {
      throw new IllegalArgumentException("partNumber must be positive");
    }

    content = content.clone();
  }

  /**
   * Returns a defensive copy of the part content.
   *
   * @return a copy of the part content
   */
  @Override
  public byte[] content() {
    return content.clone();
  }
}
