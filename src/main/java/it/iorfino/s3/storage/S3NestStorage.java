package it.iorfino.s3.storage;

import java.util.List;
import java.util.Optional;

/**
 * Storage abstraction used by S3Nest to separate S3 protocol handling from the persistence
 * implementation.
 *
 * <p>The abstraction represents the domain-level storage operations required by S3Nest. It does not
 * expose HTTP-specific types, status codes, request objects, response objects, or S3 XML
 * structures.
 *
 * <p>Each implementation owns its storage state. Different instances must therefore remain isolated
 * from each other and must support concurrent access.
 *
 * <p>Object keys are treated as opaque values and are not interpreted or normalized by the storage
 * implementation.
 */
public interface S3NestStorage {

  /**
   * Creates a bucket.
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
   * Lists the buckets stored by this storage instance.
   *
   * @return the bucket names
   */
  List<String> listBuckets();

  /**
   * Stores an object in a bucket.
   *
   * @param bucket the bucket containing the object
   * @param key the opaque object key
   * @param content the object content
   * @param metadata the object metadata
   * @throws S3NestBucketNotFoundException if the bucket does not exist
   */
  void putObject(String bucket, String key, byte[] content, S3NestObjectMetadata metadata);

  /**
   * Retrieves an object.
   *
   * @param bucket the bucket containing the object
   * @param key the opaque object key
   * @return the stored object
   * @throws S3NestBucketNotFoundException if the bucket does not exist
   * @throws S3NestObjectNotFoundException if the object does not exist
   */
  S3NestStoredObject getObject(String bucket, String key);

  /**
   * Checks whether an object exists.
   *
   * @param bucket the bucket containing the object
   * @param key the opaque object key
   * @return {@code true} if the object exists, otherwise {@code false}
   * @throws S3NestBucketNotFoundException if the bucket does not exist
   */
  boolean objectExists(String bucket, String key);

  /**
   * Deletes an object.
   *
   * <p>Deleting an object that does not exist is idempotent and does not result in an error.
   *
   * @param bucket the bucket containing the object
   * @param key the opaque object key
   * @throws S3NestBucketNotFoundException if the bucket does not exist
   */
  void deleteObject(String bucket, String key);

  /**
   * Copies an object to another bucket and key.
   *
   * <p>The copied object retains the source object's content and metadata.
   *
   * @param sourceBucket the source bucket
   * @param sourceKey the source object key
   * @param destinationBucket the destination bucket
   * @param destinationKey the destination object key
   * @throws S3NestBucketNotFoundException if either bucket does not exist
   * @throws S3NestObjectNotFoundException if the source object does not exist
   */
  void copyObject(
      String sourceBucket, String sourceKey, String destinationBucket, String destinationKey);

  /**
   * Lists objects whose keys start with the supplied prefix.
   *
   * <p>The prefix is treated as an opaque string prefix. Protocol-level listing options are
   * intentionally not part of this abstraction.
   *
   * @param bucket the bucket to list
   * @param prefix the object key prefix
   * @return summaries of matching objects
   * @throws S3NestBucketNotFoundException if the bucket does not exist
   */
  List<S3NestObjectSummary> listObjects(String bucket, String prefix);

  /**
   * Creates a multipart upload.
   *
   * @param bucket the bucket containing the object
   * @param key the opaque object key
   * @param metadata the metadata of the resulting object
   * @return the created multipart upload
   * @throws S3NestBucketNotFoundException if the bucket does not exist
   */
  S3NestMultipartUpload createMultipartUpload(
      String bucket, String key, S3NestObjectMetadata metadata);

  /**
   * Stores or replaces a part of an active multipart upload.
   *
   * @param uploadId the multipart upload identifier
   * @param partNumber the part number
   * @param content the part content
   * @throws S3NestMultipartUploadNotFoundException if the upload does not exist
   */
  void putPart(String uploadId, int partNumber, byte[] content);

  /**
   * Retrieves a part from an active multipart upload.
   *
   * @param uploadId the multipart upload identifier
   * @param partNumber the part number
   * @return the part if it exists, otherwise {@link Optional#empty()}
   * @throws S3NestMultipartUploadNotFoundException if the upload does not exist
   */
  Optional<S3NestMultipartPart> getPart(String uploadId, int partNumber);

  /**
   * Lists all parts of an active multipart upload.
   *
   * @param uploadId the multipart upload identifier
   * @return the upload parts
   * @throws S3NestMultipartUploadNotFoundException if the upload does not exist
   */
  List<S3NestMultipartPart> listParts(String uploadId);

  /**
   * Completes a multipart upload and stores the resulting object.
   *
   * <p>The parts are concatenated in the exact order specified by {@code partNumbers}.
   *
   * @param uploadId the multipart upload identifier
   * @param partNumbers the part numbers in completion order
   * @return the resulting stored object
   * @throws S3NestMultipartUploadNotFoundException if the upload does not exist
   * @throws MultipartPartNotFoundException if a requested part does not exist
   */
  S3NestStoredObject completeMultipartUpload(String uploadId, List<Integer> partNumbers);

  /**
   * Aborts an active multipart upload and removes its stored state.
   *
   * @param uploadId the multipart upload identifier
   * @throws S3NestMultipartUploadNotFoundException if the upload does not exist
   */
  void abortMultipartUpload(String uploadId);
}
