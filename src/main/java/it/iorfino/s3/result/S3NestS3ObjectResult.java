package it.iorfino.s3.result;

import it.iorfino.s3.model.S3NestS3OperationResult;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Represents the result of an S3 object retrieval operation.
 *
 * <p>The object content is exposed as a stream so that the result does not require the entire
 * object to be loaded into memory.
 *
 * <p>Object metadata is represented separately from the object content and remains independent of
 * the HTTP transport layer.
 */
public final class S3NestS3ObjectResult implements S3NestS3OperationResult {

  private final InputStream body;
  private final Map<String, List<String>> metadata;

  /**
   * Creates an object operation result.
   *
   * @param body the object content stream
   * @param metadata the object metadata
   */
  public S3NestS3ObjectResult(InputStream body, Map<String, List<String>> metadata) {
    this.body = body;
    this.metadata = metadata;
  }

  /**
   * @return the object content stream.
   */
  public InputStream body() {
    return body;
  }

  /**
   * @return the object metadata.
   */
  public Map<String, List<String>> metadata() {
    return metadata;
  }
}
