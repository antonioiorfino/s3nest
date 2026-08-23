package it.iorfino.s3;

import it.iorfino.s3.result.S3NestS3ObjectResult;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class S3NestS3ObjectResultTest {

    /**
     * Verifies that an object result preserves its content stream and metadata.
     */
    @Test
    void shouldExposeObjectContentAndMetadata() {
        InputStream body = new ByteArrayInputStream(
                "hello".getBytes(StandardCharsets.UTF_8)
        );

        Map<String, List<String>> metadata = Map.of(
                "content-type", List.of("text/plain")
        );

        S3NestS3ObjectResult result =
                new S3NestS3ObjectResult(body, metadata);

        assertSame(body, result.body());
        assertEquals(metadata, result.metadata());
    }

}
