package it.iorfino.s3.model;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Represents an object retrieved from S3 storage.
 *
 * <p>The object contains its content stream, content length and associated metadata. It is
 * independent of both HTTP transport and the concrete storage implementation.
 *
 * <p>The content length is provided separately from the content stream so that consumers can
 * determine the object size without consuming the stream.
 */
public final class S3NestS3Object {

  private final InputStream body;
  private final long contentLength;
  private final Map<String, List<String>> metadata;

  /**
   * Creates a stored S3 object.
   *
   * @param body the object content stream
   * @param contentLength the size of the object content in bytes
   * @param metadata the object metadata
   */
  public S3NestS3Object(InputStream body, long contentLength, Map<String, List<String>> metadata) {
    this.body = body;
    this.contentLength = contentLength;
    this.metadata = metadata;
  }

  /**
   * Returns the object content stream.
   *
   * @return the object content stream
   */
  public InputStream body() {
    return body;
  }

  /**
   * Returns the object content length.
   *
   * <p>The value represents the number of bytes available from the object content stream and can be
   * used by consumers without consuming the stream.
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
