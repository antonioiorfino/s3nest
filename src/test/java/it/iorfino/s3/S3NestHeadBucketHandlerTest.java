package it.iorfino.s3;

import it.iorfino.s3.handler.S3NestHeadBucketHandler;
import it.iorfino.s3.handler.S3NestS3BucketNotFoundException;
import it.iorfino.s3.model.S3NestS3OperationResult;
import it.iorfino.s3.model.S3NestS3Request;
import it.iorfino.s3.model.S3Operation;
import it.iorfino.s3.result.S3NestS3EmptyResult;
import it.iorfino.s3.storage.S3NestS3BucketStorage;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class S3NestHeadBucketHandlerTest {

    /**
     * Verifies that the HEAD_BUCKET handler returns an empty result when the
     * requested bucket exists.
     */
    @Test
    void shouldHandleExistingBucket() {
        AtomicReference<String> checkedBucket = new AtomicReference<>();

        S3NestS3BucketStorage storage = new S3NestS3BucketStorage() {
            @Override
            public void createBucket(String bucket) {
            }

            @Override
            public void deleteBucket(String bucket) {
            }

            @Override
            public boolean bucketExists(String bucket) {
                checkedBucket.set(bucket);
                return true;
            }
        };

        S3NestHeadBucketHandler handler =
                new S3NestHeadBucketHandler(storage);

        S3NestS3Request request = new S3NestS3Request(
                S3Operation.HEAD_BUCKET,
                "my-bucket",
                null,
                Map.of(),
                Map.of(),
                new ByteArrayInputStream(new byte[0])
        );

        S3NestS3OperationResult result = handler.handle(request);

        assertInstanceOf(S3NestS3EmptyResult.class, result);
        assertEquals("my-bucket", checkedBucket.get());
    }

    /**
     * Verifies that the HEAD_BUCKET handler rejects a request when the
     * requested bucket does not exist.
     */
    @Test
    void shouldRejectMissingBucket() {
        S3NestS3BucketStorage storage = new S3NestS3BucketStorage() {
            @Override
            public void createBucket(String bucket) {
            }

            @Override
            public void deleteBucket(String bucket) {
            }

            @Override
            public boolean bucketExists(String bucket) {
                return false;
            }
        };

        S3NestHeadBucketHandler handler =
                new S3NestHeadBucketHandler(storage);

        S3NestS3Request request = new S3NestS3Request(
                S3Operation.HEAD_BUCKET,
                "missing-bucket",
                null,
                Map.of(),
                Map.of(),
                new ByteArrayInputStream(new byte[0])
        );

        assertThrows(
                S3NestS3BucketNotFoundException.class,
                () -> handler.handle(request)
        );
    }
}
