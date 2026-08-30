package it.iorfino.s3.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import it.iorfino.s3.result.S3NestS3Error;
import org.junit.jupiter.api.Test;

/** Tests the generation of XML representations required by the S3 protocol. */
class S3NestS3XmlWriterTest {

  /** Verifies that an S3 error is represented using the expected XML elements. */
  @Test
  void shouldWriteS3ErrorXml() {
    S3NestS3Error error =
        new S3NestS3Error(
            "NoSuchKey", "The specified key does not exist.", "my-bucket", "folder/file.txt");

    S3NestS3XmlWriter writer = new S3NestS3XmlWriter();

    String xml = writer.writeError(error);

    assertEquals(
        """
            <?xml version="1.0" encoding="UTF-8"?>
            <Error>
              <Code>NoSuchKey</Code>
              <Message>The specified key does not exist.</Message>
              <BucketName>my-bucket</BucketName>
              <Key>folder/file.txt</Key>
            </Error>
            """,
        xml);
  }

  /** Verifies that values containing XML-sensitive characters are escaped correctly. */
  @Test
  void shouldEscapeXmlSensitiveCharacters() {
    S3NestS3Error error =
        new S3NestS3Error(
            "NoSuchKey",
            "The key <file> & \"data\" does not exist.",
            "my-bucket",
            "folder/<file>&data.txt");

    S3NestS3XmlWriter writer = new S3NestS3XmlWriter();

    String xml = writer.writeError(error);

    assertEquals(
        """
            <?xml version="1.0" encoding="UTF-8"?>
            <Error>
              <Code>NoSuchKey</Code>
              <Message>The key &lt;file&gt; &amp; &quot;data&quot; does not exist.</Message>
              <BucketName>my-bucket</BucketName>
              <Key>folder/&lt;file&gt;&amp;data.txt</Key>
            </Error>
            """,
        xml);
  }

  /**
   * Verifies that an error without object context does not contain empty object-specific elements.
   */
  @Test
  void shouldOmitMissingObjectContext() {
    S3NestS3Error error =
        new S3NestS3Error("InvalidRequest", "The request is invalid.", null, null);

    S3NestS3XmlWriter writer = new S3NestS3XmlWriter();

    String xml = writer.writeError(error);

    assertEquals(
        """
            <?xml version="1.0" encoding="UTF-8"?>
            <Error>
              <Code>InvalidRequest</Code>
              <Message>The request is invalid.</Message>
            </Error>
            """,
        xml);
  }
}
