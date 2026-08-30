package it.iorfino.s3.response;

import it.iorfino.s3.result.S3NestS3Error;

/**
 * Writes S3-compatible XML response bodies.
 *
 * <p>This class is responsible only for XML serialization. HTTP status codes, HTTP headers and
 * error classification are handled by the corresponding response-layer components.
 *
 * <p>Error responses are serialized using the S3 {@code Error} response structure. Optional bucket
 * and object context elements are omitted when their values are not available.
 */
public final class S3NestS3XmlWriter {

  /**
   * Writes an S3-compatible error response.
   *
   * <p>The generated document has the following structure:
   *
   * <pre>{@code
   * <?xml version="1.0" encoding="UTF-8"?>
   * <Error>
   *   <Code>...</Code>
   *   <Message>...</Message>
   *   <BucketName>...</BucketName>
   *   <Key>...</Key>
   * </Error>
   * }</pre>
   *
   * <p>{@code BucketName} is included only when the error contains a bucket name. {@code Key} is
   * included only when the error contains an object key.
   *
   * <p>XML special characters in error values are escaped before being written to the response.
   *
   * @param error the S3 error to serialize; must not be {@code null}
   * @return the UTF-8 XML representation of the S3 error
   * @throws NullPointerException if {@code error} is {@code null}
   */
  public String writeError(S3NestS3Error error) {
    if (error == null) {
      throw new NullPointerException("error must not be null");
    }

    StringBuilder xml = new StringBuilder();

    xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    xml.append("<Error>\n");
    xml.append("  <Code>").append(escapeXml(error.code())).append("</Code>\n");
    xml.append("  <Message>").append(escapeXml(error.message())).append("</Message>\n");

    if (error.bucket() != null) {
      xml.append("  <BucketName>").append(escapeXml(error.bucket())).append("</BucketName>\n");
    }

    if (error.objectKey() != null) {
      xml.append("  <Key>").append(escapeXml(error.objectKey())).append("</Key>\n");
    }

    xml.append("</Error>\n");

    return xml.toString();
  }

  /**
   * Escapes a value for use as XML character data.
   *
   * <p>The XML predefined entities are used for characters that have special meaning in XML: {@code
   * &}, {@code <}, {@code >}, {@code "} and {@code '}.
   *
   * <p>This method expects a non-null value. Optional XML elements must be checked by the caller
   * before invoking this method.
   *
   * @param value the value to escape; must not be {@code null}
   * @return the XML-escaped value
   * @throws NullPointerException if {@code value} is {@code null}
   */
  private String escapeXml(String value) {
    if (value == null) {
      throw new NullPointerException("value must not be null");
    }

    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;");
  }
}
