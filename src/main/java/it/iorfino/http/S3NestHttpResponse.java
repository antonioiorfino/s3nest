package it.iorfino.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an HTTP response independently of the underlying HTTP transport.
 *
 * <p>An HTTP response consists of a status code, response headers and an optional streaming body.
 * The response metadata is defined before the body is written, allowing the underlying HTTP server
 * to commit the response headers before streaming the body.
 *
 * <p>The response does not own the output stream used to write the body. The caller is responsible
 * for managing the lifecycle of that stream.
 */
public final class S3NestHttpResponse {

  private final BodyWriter body;
  private final Map<String, List<String>> headers;
  private final int statusCode;

  /**
   * Creates an HTTP response.
   *
   * @param statusCode the HTTP status code
   * @param headers the response headers
   * @param body the writer responsible for streaming the response body
   */
  public S3NestHttpResponse(int statusCode, Map<String, List<String>> headers, BodyWriter body) {
    this.statusCode = statusCode;
    this.headers = new HashMap<>();
    this.body = body;
  }

  /**
   * Returns the HTTP status code.
   *
   * @return the HTTP status code
   */
  public int statusCode() {
    return this.statusCode;
  }

  /**
   * Returns the response headers.
   *
   * <p>The returned map is mutable and contains the headers that will be sent to the HTTP client.
   *
   * @return the response headers
   */
  public Map<String, List<String>> headers() {
    return headers;
  }

  /**
   * Writes the response body to the supplied output stream.
   *
   * <p>The response headers must be committed by the HTTP server before this method is invoked.
   *
   * <p>This method does not close the supplied output stream.
   *
   * @param outputStream the output stream used to send the response body
   * @throws IOException if writing the response body fails
   */
  public void writeBody(OutputStream outputStream) throws IOException {
    this.body.write(outputStream);
  }

  /**
   * Writes the response body to an output stream.
   *
   * <p>The writer must not close the supplied output stream.
   */
  @FunctionalInterface
  public interface BodyWriter {
    /**
     * Writes the response body to the supplied output stream.
     *
     * @param output the output stream used to write the response body
     * @throws IOException if writing the response body fails
     */
    void write(OutputStream output) throws IOException;
  }
}
