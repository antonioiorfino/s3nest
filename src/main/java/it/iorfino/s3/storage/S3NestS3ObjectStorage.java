package it.iorfino.s3.storage;

import it.iorfino.s3.model.S3NestS3Object;

/**
 * Provides access to objects stored in S3.
 *
 * <p>This interface represents the storage boundary required by S3 object
 * operations. It does not expose any concrete storage implementation.</p>
 */
@FunctionalInterface
public interface S3NestS3ObjectStorage {

    /**
     * Retrieves an object from storage.
     *
     * @param bucket the bucket containing the object
     * @param objectKey the key identifying the object
     * @return the stored object, or {@code null} when the object does not exist
     */
    S3NestS3Object getObject(String bucket, String objectKey);
}