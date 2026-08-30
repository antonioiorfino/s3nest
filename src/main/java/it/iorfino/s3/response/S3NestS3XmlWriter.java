package it.iorfino.s3.response;

import it.iorfino.s3.result.S3NestS3Error;

/**
 * Generates XML representations required by the S3 protocol.
 *
 * <p>This class is responsible only for XML generation. HTTP status codes, response headers and
 * transport concerns are handled by the response layer.
 */
public final class S3NestS3XmlWriter {

  /**
   * Writes an S3 error as XML.
   *
   * @param error the S3 error to serialize
   * @return the XML representation of the error
   */
  public String writeError(S3NestS3Error error) {
    return """
      <?xml version="1.0" encoding="UTF-8"?>
      <Error>
        <Code>%s</Code>
        <Message>%s</Message>
        <BucketName>%s</BucketName>
        <Key>%s</Key>
      </Error>
      """
        .formatted(
            escapeXml(error.code()),
            escapeXml(error.message()),
            escapeXml(error.bucket()),
            escapeXml(error.objectKey()));
  }

  /**
   * Escapes characters that have a special meaning in XML.
   *
   * <p>The values inserted into the S3 XML response originate from operation results and therefore
   * must be escaped before they are included in the document.
   *
   * @param value the value to escape
   * @return the XML-safe representation of the value
   */
  private String escapeXml(String value) {
    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;");
  }
}
