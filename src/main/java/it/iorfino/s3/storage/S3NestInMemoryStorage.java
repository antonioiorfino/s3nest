package it.iorfino.s3.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of the {@link S3NestStorage} abstraction.
 *
 * <p>The storage state belongs exclusively to this instance. Creating two {@code
 * S3NestInMemoryStorage} instances therefore creates two independent stores.
 */
public class S3NestInMemoryStorage implements S3NestStorage {

  /** Buckets belonging to this storage instance. */
  private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

  /** Multipart uploads belonging to this storage instance. */
  private final Map<String, MultipartUploadState> multipartUploads = new ConcurrentHashMap<>();

  /** Internal representation of a bucket. */
  private static class Bucket {

    /** Objects stored in this bucket. */
    private final Map<String, S3NestStoredObject> objects = new ConcurrentHashMap<>();
  }

  /** Internal representation of an active multipart upload. */
  private static class MultipartUploadState {

    private final S3NestMultipartUpload upload;

    private final Map<Integer, S3NestMultipartPart> parts = new ConcurrentHashMap<>();

    private MultipartUploadState(S3NestMultipartUpload upload) {
      this.upload = upload;
    }
  }

  /**
   * Creates a new bucket.
   *
   * @param bucket the bucket name
   * @throws S3NestBucketAlreadyExistsException if the bucket already exists
   */
  @Override
  public void createBucket(String bucket) {
    Bucket previous = buckets.putIfAbsent(bucket, new Bucket());

    if (previous != null) {
      throw new S3NestBucketAlreadyExistsException(bucket);
    }
  }

  /**
   * Deletes an existing empty bucket.
   *
   * @param bucket the bucket name
   * @throws S3NestBucketNotFoundException if the bucket does not exist
   * @throws S3NestBucketNotEmptyException if the bucket contains objects
   */
  @Override
  public void deleteBucket(String bucket) {
    Bucket existing = buckets.get(bucket);

    if (existing == null) {
      throw new S3NestBucketNotFoundException(bucket);
    }

    if (!existing.objects.isEmpty()) {
      throw new S3NestBucketNotEmptyException(bucket);
    }

    buckets.remove(bucket, existing);
  }

  /**
   * Checks whether a bucket exists.
   *
   * @param bucket the bucket name
   * @return {@code true} if the bucket exists
   */
  @Override
  public boolean bucketExists(String bucket) {
    return buckets.containsKey(bucket);
  }

  /**
   * Lists all buckets in this storage instance.
   *
   * @return the bucket names
   */
  @Override
  public List<String> listBuckets() {
    return new ArrayList<>(buckets.keySet());
  }

  /**
   * Stores an object in the specified bucket.
   *
   * @param bucket bucket containing the object
   * @param key opaque object key
   * @param content object content
   * @param metadata object metadata
   * @throws S3NestBucketNotFoundException if the bucket does not exist
   */
  @Override
  public void putObject(String bucket, String key, byte[] content, S3NestObjectMetadata metadata) {

    Bucket existingBucket = buckets.get(bucket);

    if (existingBucket == null) {
      throw new S3NestBucketNotFoundException(bucket);
    }

    existingBucket.objects.put(key, new S3NestStoredObject(bucket, key, content, metadata));
  }

  /**
   * Retrieves an object from the specified bucket.
   *
   * @param bucket bucket containing the object
   * @param key opaque object key
   * @return the stored object
   * @throws S3NestBucketNotFoundException if the bucket does not exist
   * @throws S3NestObjectNotFoundException if the object does not exist
   */
  @Override
  public S3NestStoredObject getObject(String bucket, String key) {

    Bucket existingBucket = buckets.get(bucket);

    if (existingBucket == null) {
      throw new S3NestBucketNotFoundException(bucket);
    }

    S3NestStoredObject object = existingBucket.objects.get(key);

    if (object == null) {
      throw new S3NestObjectNotFoundException(bucket, key);
    }

    return object;
  }

  /**
   * Checks whether an object exists in the specified bucket.
   *
   * @param bucket bucket containing the object
   * @param key opaque object key
   * @return {@code true} if the object exists, otherwise {@code false}
   * @throws S3NestBucketNotFoundException if the bucket does not exist
   */
  @Override
  public boolean objectExists(String bucket, String key) {

    Bucket existingBucket = buckets.get(bucket);

    if (existingBucket == null) {
      throw new S3NestBucketNotFoundException(bucket);
    }

    return existingBucket.objects.containsKey(key);
  }

  /**
   * Deletes an object from the specified bucket.
   *
   * <p>Deleting a missing object is a no-op.
   *
   * @param bucket bucket containing the object
   * @param key opaque object key
   * @throws S3NestBucketNotFoundException if the bucket does not exist
   */
  @Override
  public void deleteObject(String bucket, String key) {

    Bucket existingBucket = buckets.get(bucket);

    if (existingBucket == null) {
      throw new S3NestBucketNotFoundException(bucket);
    }

    existingBucket.objects.remove(key);
  }

  /**
   * Copies an object to another bucket and key.
   *
   * @param sourceBucket bucket containing the source object
   * @param sourceKey key of the source object
   * @param destinationBucket bucket receiving the copied object
   * @param destinationKey key of the copied object
   * @throws S3NestBucketNotFoundException if either bucket does not exist
   * @throws S3NestObjectNotFoundException if the source object does not exist
   */
  @Override
  public void copyObject(
      String sourceBucket, String sourceKey, String destinationBucket, String destinationKey) {

    Bucket existingSourceBucket = buckets.get(sourceBucket);

    if (existingSourceBucket == null) {
      throw new S3NestBucketNotFoundException(sourceBucket);
    }

    S3NestStoredObject sourceObject = existingSourceBucket.objects.get(sourceKey);

    if (sourceObject == null) {
      throw new S3NestObjectNotFoundException(sourceBucket, sourceKey);
    }

    Bucket existingDestinationBucket = buckets.get(destinationBucket);

    if (existingDestinationBucket == null) {
      throw new S3NestBucketNotFoundException(destinationBucket);
    }

    S3NestStoredObject copiedObject =
        new S3NestStoredObject(
            destinationBucket, destinationKey, sourceObject.content(), sourceObject.metadata());

    existingDestinationBucket.objects.put(destinationKey, copiedObject);
  }

  /**
   * Lists objects in the specified bucket whose keys start with the given prefix.
   *
   * <p>Object keys are treated as opaque strings; no path normalization is performed.
   *
   * @param bucket bucket to list
   * @param prefix key prefix used for filtering
   * @return summaries of matching objects
   * @throws S3NestBucketNotFoundException if the bucket does not exist
   */
  @Override
  public List<S3NestObjectSummary> listObjects(String bucket, String prefix) {

    Bucket existingBucket = buckets.get(bucket);

    if (existingBucket == null) {
      throw new S3NestBucketNotFoundException(bucket);
    }

    return existingBucket.objects.values().stream()
        .filter(object -> object.key().startsWith(prefix))
        .map(
            object ->
                new S3NestObjectSummary(
                    object.key(),
                    object.metadata().contentLength(),
                    object.metadata().eTag(),
                    object.metadata().lastModified()))
        .toList();
  }

  /**
   * Creates a new multipart upload for an object.
   *
   * @param bucket bucket that will contain the completed object
   * @param key opaque object key
   * @param metadata metadata to associate with the completed object
   * @return the newly created multipart upload
   * @throws S3NestBucketNotFoundException if the bucket does not exist
   */
  @Override
  public S3NestMultipartUpload createMultipartUpload(
      String bucket, String key, S3NestObjectMetadata metadata) {

    Bucket existingBucket = buckets.get(bucket);

    if (existingBucket == null) {
      throw new S3NestBucketNotFoundException(bucket);
    }

    String uploadId = java.util.UUID.randomUUID().toString();

    S3NestMultipartUpload upload =
        new S3NestMultipartUpload(uploadId, bucket, key, metadata, java.time.Instant.now());

    multipartUploads.put(uploadId, new MultipartUploadState(upload));

    return upload;
  }

  /**
   * Stores or replaces a part of an active multipart upload.
   *
   * @param uploadId identifier of the multipart upload
   * @param partNumber part number
   * @param content part content
   * @return the stored multipart part
   * @throws S3NestMultipartUploadNotFoundException if the upload does not exist
   */
  @Override
  public void putPart(String uploadId, int partNumber, byte[] content) {

    MultipartUploadState upload = multipartUploads.get(uploadId);

    if (upload == null) {
      throw new S3NestMultipartUploadNotFoundException(uploadId);
    }

    String eTag = java.util.UUID.randomUUID().toString();

    S3NestMultipartPart part = new S3NestMultipartPart(partNumber, content, eTag);

    upload.parts.put(partNumber, part);
  }

  /**
   * Retrieves a part from an active multipart upload.
   *
   * @param uploadId identifier of the multipart upload
   * @param partNumber part number
   * @return the multipart part if it exists, otherwise {@link java.util.Optional#empty()}
   * @throws S3NestMultipartUploadNotFoundException if the upload does not exist
   */
  @Override
  public java.util.Optional<S3NestMultipartPart> getPart(String uploadId, int partNumber) {

    MultipartUploadState upload = multipartUploads.get(uploadId);

    if (upload == null) {
      throw new S3NestMultipartUploadNotFoundException(uploadId);
    }

    return java.util.Optional.ofNullable(upload.parts.get(partNumber));
  }

  /**
   * Lists the parts currently stored for an active multipart upload.
   *
   * <p>The returned parts are ordered by part number.
   *
   * @param uploadId identifier of the multipart upload
   * @return the stored multipart parts ordered by part number
   * @throws S3NestMultipartUploadNotFoundException if the upload does not exist
   */
  @Override
  public List<S3NestMultipartPart> listParts(String uploadId) {

    MultipartUploadState upload = multipartUploads.get(uploadId);

    if (upload == null) {
      throw new S3NestMultipartUploadNotFoundException(uploadId);
    }

    return upload.parts.values().stream()
        .sorted(java.util.Comparator.comparingInt(S3NestMultipartPart::partNumber))
        .toList();
  }

  /**
   * Completes a multipart upload by concatenating the requested parts in the given order.
   *
   * <p>The resulting object is stored in the bucket associated with the upload, and the multipart
   * upload is no longer active.
   *
   * @param uploadId identifier of the multipart upload
   * @param partNumbers part numbers to concatenate, in the desired order
   * @return the completed object
   * @throws S3NestMultipartUploadNotFoundException if the upload does not exist
   * @throws S3NestMultipartPartNotFoundException if a requested part does not exist
   * @throws S3NestBucketNotFoundException if the bucket associated with the upload no longer exists
   */
  @Override
  public S3NestStoredObject completeMultipartUpload(String uploadId, List<Integer> partNumbers) {

    MultipartUploadState upload = multipartUploads.get(uploadId);

    if (upload == null) {
      throw new S3NestMultipartUploadNotFoundException(uploadId);
    }

    java.io.ByteArrayOutputStream content = new java.io.ByteArrayOutputStream();

    for (Integer partNumber : partNumbers) {
      S3NestMultipartPart part = upload.parts.get(partNumber);

      if (part == null) {
        throw new S3NestMultipartPartNotFoundException(uploadId, partNumber);
      }

      content.writeBytes(part.content());
    }

    byte[] completedContent = content.toByteArray();

    S3NestStoredObject completedObject =
        new S3NestStoredObject(
            upload.upload.bucket(),
            upload.upload.key(),
            completedContent,
            upload.upload.metadata());

    Bucket bucket = buckets.get(upload.upload.bucket());

    bucket.objects.put(upload.upload.key(), completedObject);

    multipartUploads.remove(uploadId);

    return completedObject;
  }

  /**
   * Aborts an active multipart upload and discards its stored parts.
   *
   * @param uploadId identifier of the multipart upload
   * @throws S3NestMultipartUploadNotFoundException if the upload does not exist
   */
  @Override
  public void abortMultipartUpload(String uploadId) {

    MultipartUploadState upload = multipartUploads.remove(uploadId);

    if (upload == null) {
      throw new S3NestMultipartUploadNotFoundException(uploadId);
    }
  }
}
