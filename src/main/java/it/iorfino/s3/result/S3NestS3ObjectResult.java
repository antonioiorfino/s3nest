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
 * <p>The content length is provided separately so that the response layer can set the corresponding
 * HTTP header without consuming the object content stream.
 *
 * <p>Object metadata is represented separately from the object content and remains independent of
 * the HTTP transport layer.
 */
public final class S3NestS3ObjectResult implements S3NestS3OperationResult {

  private final InputStream body;
  private final long contentLength;
  private final Map<String, List<String>> metadata;

  /**
   * Creates an object operation result.
   *
   * @param body the object content stream
   * @param contentLength the size of the object content in bytes
   * @param metadata the object metadata
   */
  public S3NestS3ObjectResult(
      InputStream body, long contentLength, Map<String, List<String>> metadata) {
    this.body = body;
    this.contentLength = contentLength;
    this.metadata = metadata;
  }

  /**
   * Returns the object content stream.
   *
   * <p>The stream is not consumed by this result and can be transferred directly to the response
   * output stream by the response layer.
   *
   * @return the object content stream
   */
  public InputStream body() {
    return body;
  }

  /**
   * Returns the object content length.
   *
   * <p>The value represents the number of bytes available from the object content stream and allows
   * the response layer to set {@code Content-Length} without reading the stream.
   *
   * @return the object content length in bytes
   */
  public long contentLength() {
    return contentLength;
  }

  /**
   * Returns the object metadata.
   *
   * @return the object metadata
   */
  public Map<String, List<String>> metadata() {
    return metadata;
  }
}
