package it.iorfino.s3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import it.iorfino.s3.result.S3NestS3ObjectResult;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class S3NestS3ObjectResultTest {

  /** Verifies that an object result preserves its content stream and metadata. */
  @Test
  void shouldExposeObjectContentAndMetadata() {
    String bodyMessage = "hello";
    InputStream body = new ByteArrayInputStream(bodyMessage.getBytes(StandardCharsets.UTF_8));

    Map<String, List<String>> metadata = Map.of("content-type", List.of("text/plain"));

    S3NestS3ObjectResult result = new S3NestS3ObjectResult(body, bodyMessage.length(), metadata);

    assertSame(body, result.body());
    assertEquals(metadata, result.metadata());
  }
}
