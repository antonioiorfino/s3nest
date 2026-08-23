package it.iorfino.s3;

import it.iorfino.s3.handler.S3NestCreateBucketHandler;
import it.iorfino.s3.model.S3NestS3OperationResult;
import it.iorfino.s3.model.S3NestS3Request;
import it.iorfino.s3.model.S3Operation;
import it.iorfino.s3.result.S3NestS3EmptyResult;
import it.iorfino.s3.storage.S3NestS3BucketStorage;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;

public class S3NestCreateBucketHandlerTest {

    /**
     * Verifies that the CREATE_BUCKET handler delegates bucket creation to
     * the storage port.
     */
    @Test
    void shouldCreateBucket() {
        AtomicReference<String> createdBucket = new AtomicReference<>();

        S3NestS3BucketStorage storage = new S3NestS3BucketStorage() {
            @Override
            public void createBucket(String bucket) {
                createdBucket.set(bucket);
            }

            @Override
            public void deleteBucket(String bucket) {
            }

            @Override
            public boolean bucketExists(String bucket) {
                return false;
            }
        };

        S3NestCreateBucketHandler handler =
                new S3NestCreateBucketHandler(storage);

        S3NestS3Request request = new S3NestS3Request(
                S3Operation.CREATE_BUCKET,
                "my-bucket",
                null,
                Map.of(),
                Map.of(),
                new ByteArrayInputStream(new byte[0])
        );

        S3NestS3OperationResult result = handler.handle(request);

        assertInstanceOf(S3NestS3EmptyResult.class, result);
        assertEquals("my-bucket", createdBucket.get());
    }

    /**
     * Verifies that bucket creation failures are propagated by the handler.
     */
    @Test
    void shouldPropagateStorageFailure() {
        RuntimeException storageFailure =
                new RuntimeException("Storage unavailable");

        S3NestS3BucketStorage storage = new S3NestS3BucketStorage() {
            @Override
            public void createBucket(String bucket) {
                throw storageFailure;
            }

            @Override
            public void deleteBucket(String bucket) {
            }

            @Override
            public boolean bucketExists(String bucket) {
                return false;
            }
        };

        S3NestCreateBucketHandler handler =
                new S3NestCreateBucketHandler(storage);

        S3NestS3Request request = new S3NestS3Request(
                S3Operation.CREATE_BUCKET,
                "my-bucket",
                null,
                Map.of(),
                Map.of(),
                new ByteArrayInputStream(new byte[0])
        );

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> handler.handle(request)
        );

        assertSame(storageFailure, exception);
    }
}
