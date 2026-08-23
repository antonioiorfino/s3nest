package it.iorfino.s3;

import it.iorfino.s3.handler.S3NestDeleteBucketHandler;
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

public class S3NestDeleteBucketHandlerTest {

    /**
     * Verifies that the DELETE_BUCKET handler delegates bucket deletion to
     * the storage port.
     */
    @Test
    void shouldDeleteBucket() {
        AtomicReference<String> deletedBucket = new AtomicReference<>();

        S3NestS3BucketStorage storage = new S3NestS3BucketStorage() {
            @Override
            public void createBucket(String bucket) {
            }

            @Override
            public void deleteBucket(String bucket) {
                deletedBucket.set(bucket);
            }

            @Override
            public boolean bucketExists(String bucket) {
                return true;
            }
        };

        S3NestDeleteBucketHandler handler =
                new S3NestDeleteBucketHandler(storage);

        S3NestS3Request request = new S3NestS3Request(
                S3Operation.DELETE_BUCKET,
                "my-bucket",
                null,
                Map.of(),
                Map.of(),
                new ByteArrayInputStream(new byte[0])
        );

        S3NestS3OperationResult result = handler.handle(request);

        assertInstanceOf(S3NestS3EmptyResult.class, result);
        assertEquals("my-bucket", deletedBucket.get());
    }

    /**
     * Verifies that bucket deletion failures are propagated by the handler.
     */
    @Test
    void shouldPropagateStorageFailure() {
        RuntimeException storageFailure =
                new RuntimeException("Storage unavailable");

        S3NestS3BucketStorage storage = new S3NestS3BucketStorage() {
            @Override
            public void createBucket(String bucket) {
            }

            @Override
            public void deleteBucket(String bucket) {
                throw storageFailure;
            }

            @Override
            public boolean bucketExists(String bucket) {
                return true;
            }
        };

        S3NestDeleteBucketHandler handler =
                new S3NestDeleteBucketHandler(storage);

        S3NestS3Request request = new S3NestS3Request(
                S3Operation.DELETE_BUCKET,
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
