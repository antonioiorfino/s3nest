package it.iorfino.s3.storage;

import java.util.List;
import java.util.Optional;

/**
 * Storage abstraction used by S3Nest to separate S3 protocol handling from the persistence
 * implementation.
 *
 * <p>This interface exposes only storage-level operations and domain concepts. It does not expose
 * HTTP-specific types, HTTP status codes, request or response objects, or S3 XML structures.
 *
 * <p>Each implementation owns its storage state. Different instances must remain isolated from each
 * other and implementations must support concurrent access.
 *
 * <p>Object keys are treated as opaque strings. Validation and interpretation of S3 protocol
 * requests belong to the layer above this abstraction.
 */
public interface S3NestStorage {

  /**
   * Creates an empty bucket.
   *
   * @param bucket the bucket name
   * @throws S3NestBucketAlreadyExistsException if the bucket already exists
   */
  void createBucket(String bucket);

  /**
   * Deletes an empty bucket.
   *
   * @param bucket the bucket name
   * @throws S3NestBucketNotFoundException if the bucket does not exist
   * @throws S3NestBucketNotEmptyException if the bucket contains objects
   */
  void deleteBucket(String bucket);

  /**
   * Checks whether a bucket exists.
   *
   * @param bucket the bucket name
   * @return {@code true} if the bucket exists, otherwise {@code false}
   */
  boolean bucketExists(String bucket);

  /**
   * Lists all buckets stored by this storage instance.
   *
   * @return the names of all existing buckets
   */
  List<String> listBuckets();

  /**
   * Stores an object in a bucket.
   *
   * <p>If an object with the same key already exists, it is replaced.
   *
   * @param bucket the bucket containing the object
   * @param key the opaque object key
   * @param content the object content
   * @param metadata the metadata associated with the object
   * @throws S3NestBucketNotFoundException if the bucket does not exist
   */
  void putObject(String bucket, String key, byte[] content, S3NestObjectMetadata metadata);

  /**
   * Retrieves an object from a bucket.
   *
   * @param bucket the bucket containing the object
   * @param key the opaque object key
   * @return the stored object
   * @throws S3NestBucketNotFoundException if the bucket does not exist
   * @throws S3NestObjectNotFoundException if the object does not exist
   */
  S3NestStoredObject getObject(String bucket, String key);

  /**
   * Checks whether an object exists in a bucket.
   *
   * @param bucket the bucket containing the object
   * @param key the opaque object key
   * @return {@code true} if the object exists, otherwise {@code false}
   * @throws S3NestBucketNotFoundException if the bucket does not exist
   */
  boolean objectExists(String bucket, String key);

  /**
   * Deletes an object from a bucket.
   *
   * <p>Deleting an object that does not exist has no effect.
   *
   * @param bucket the bucket containing the object
   * @param key the opaque object key
   * @throws S3NestBucketNotFoundException if the bucket does not exist
   */
  void deleteObject(String bucket, String key);

  /**
   * Copies an object from one bucket and key to another bucket and key.
   *
   * <p>The copied object retains the source content and metadata.
   *
   * @param sourceBucket the bucket containing the source object
   * @param sourceKey the key of the source object
   * @param destinationBucket the bucket receiving the copied object
   * @param destinationKey the key of the copied object
   * @throws S3NestBucketNotFoundException if either bucket does not exist
   * @throws S3NestObjectNotFoundException if the source object does not exist
   */
  void copyObject(
      String sourceBucket, String sourceKey, String destinationBucket, String destinationKey);

  /**
   * Lists objects whose keys start with the supplied prefix.
   *
   * <p>The prefix is matched directly against object keys. No normalization or interpretation of
   * the key is performed by the storage layer.
   *
   * @param bucket the bucket containing the objects
   * @param prefix the key prefix to match
   * @return summaries of matching objects
   * @throws S3NestBucketNotFoundException if the bucket does not exist
   */
  List<S3NestObjectSummary> listObjects(String bucket, String prefix);

  /**
   * Creates a multipart upload for an object.
   *
   * @param bucket the bucket that will contain the completed object
   * @param key the opaque object key
   * @param metadata the metadata associated with the completed object
   * @return the newly created multipart upload
   * @throws S3NestBucketNotFoundException if the bucket does not exist
   */
  S3NestMultipartUpload createMultipartUpload(
      String bucket, String key, S3NestObjectMetadata metadata);

  /**
   * Stores or replaces a part of a multipart upload.
   *
   * @param uploadId the multipart upload identifier
   * @param partNumber the part number
   * @param content the part content
   * @throws S3NestMultipartUploadNotFoundException if the upload does not exist
   * @throws IllegalArgumentException if the part number is not positive
   */
  void putPart(String uploadId, int partNumber, byte[] content);

  /**
   * Retrieves a part belonging to a multipart upload.
   *
   * @param uploadId the multipart upload identifier
   * @param partNumber the part number
   * @return the part if it exists, otherwise {@link Optional#empty()}
   * @throws S3NestMultipartUploadNotFoundException if the upload does not exist
   */
  Optional<S3NestMultipartPart> getPart(String uploadId, int partNumber);

  /**
   * Lists all parts belonging to a multipart upload.
   *
   * <p>The returned parts are ordered by ascending part number.
   *
   * @param uploadId the multipart upload identifier
   * @return all parts currently stored for the upload
   * @throws S3NestMultipartUploadNotFoundException if the upload does not exist
   */
  List<S3NestMultipartPart> listParts(String uploadId);

  /**
   * Completes a multipart upload and stores the resulting object.
   *
   * <p>The supplied part numbers determine the order in which part contents are concatenated. The
   * multipart upload is removed after successful completion.
   *
   * @param uploadId the multipart upload identifier
   * @param partNumbers the part numbers in the order in which they must be combined
   * @return the resulting stored object
   * @throws S3NestMultipartUploadNotFoundException if the upload does not exist
   * @throws S3NestMultipartPartNotFoundException if a requested part does not exist
   */
  S3NestStoredObject completeMultipartUpload(String uploadId, List<Integer> partNumbers);

  /**
   * Aborts a multipart upload and discards all parts associated with it.
   *
   * @param uploadId the multipart upload identifier
   * @throws S3NestMultipartUploadNotFoundException if the upload does not exist
   */
  void abortMultipartUpload(String uploadId);
}
