package it.iorfino.s3.storage;

/**
 * Provides access to S3 bucket storage operations.
 *
 * <p>This interface represents the storage boundary required by S3 bucket
 * operations. It does not expose any concrete storage implementation.</p>
 */
public interface S3NestS3BucketStorage {

    /**
     * Creates a new bucket.
     *
     * @param bucket the name of the bucket to create
     */
    void createBucket(String bucket);

    /**
     * Deletes an existing bucket.
     *
     * @param bucket the name of the bucket to delete
     */
    void deleteBucket(String bucket);

    /**
     * Checks whether a bucket exists.
     *
     * @param bucket the name of the bucket
     * @return {@code true} if the bucket exists, otherwise {@code false}
     */
    boolean bucketExists(String bucket);
}