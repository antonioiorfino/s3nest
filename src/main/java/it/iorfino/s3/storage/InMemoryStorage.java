package it.iorfino.s3.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of the {@link S3NestStorage} abstraction.
 *
 * <p>The storage state belongs exclusively to this instance. Creating two {@code InMemoryStorage}
 * instances therefore creates two independent stores.
 */
public class InMemoryStorage implements S3NestStorage {

  /** Buckets belonging to this storage instance. */
  private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

  /** Multipart uploads belonging to this storage instance. */
  private final Map<String, MultipartUploadState> multipartUploads = new ConcurrentHashMap<>();

  /** Internal representation of a bucket. */
  private static class Bucket {

    /** Objects stored in this bucket. */
    private final Map<String, StoredObject> objects = new ConcurrentHashMap<>();
  }

  /** Internal representation of an active multipart upload. */
  private static class MultipartUploadState {

    private final MultipartUpload upload;

    private final Map<Integer, MultipartPart> parts = new ConcurrentHashMap<>();

    private MultipartUploadState(MultipartUpload upload) {
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

  // Object operations will be implemented in the next step.

  @Override
  public void putObject(String bucket, String key, byte[] content, ObjectMetadata metadata) {

    Bucket existingBucket = buckets.get(bucket);

    if (existingBucket == null) {
      throw new S3NestBucketNotFoundException(bucket);
    }

    existingBucket.objects.put(key, new StoredObject(bucket, key, content, metadata));
  }

  @Override
  public StoredObject getObject(String bucket, String key) {

    Bucket existingBucket = buckets.get(bucket);

    if (existingBucket == null) {
      throw new S3NestBucketNotFoundException(bucket);
    }

    StoredObject object = existingBucket.objects.get(key);

    if (object == null) {
      throw new S3NestObjectNotFoundException(bucket, key);
    }

    return object;
  }

  @Override
  public boolean objectExists(String bucket, String key) {

    Bucket existingBucket = buckets.get(bucket);

    if (existingBucket == null) {
      throw new S3NestBucketNotFoundException(bucket);
    }

    return existingBucket.objects.containsKey(key);
  }

  @Override
  public void deleteObject(String bucket, String key) {

    Bucket existingBucket = buckets.get(bucket);

    if (existingBucket == null) {
      throw new S3NestBucketNotFoundException(bucket);
    }

    existingBucket.objects.remove(key);
  }

  @Override
  public void copyObject(
      String sourceBucket, String sourceKey, String destinationBucket, String destinationKey) {

    Bucket existingSourceBucket = buckets.get(sourceBucket);

    if (existingSourceBucket == null) {
      throw new S3NestBucketNotFoundException(sourceBucket);
    }

    StoredObject sourceObject = existingSourceBucket.objects.get(sourceKey);

    if (sourceObject == null) {
      throw new S3NestObjectNotFoundException(sourceBucket, sourceKey);
    }

    Bucket existingDestinationBucket = buckets.get(destinationBucket);

    if (existingDestinationBucket == null) {
      throw new S3NestBucketNotFoundException(destinationBucket);
    }

    StoredObject copiedObject =
        new StoredObject(
            destinationBucket, destinationKey, sourceObject.content(), sourceObject.metadata());

    existingDestinationBucket.objects.put(destinationKey, copiedObject);
  }

  @Override
  public List<ObjectSummary> listObjects(String bucket, String prefix) {

    Bucket existingBucket = buckets.get(bucket);

    if (existingBucket == null) {
      throw new S3NestBucketNotFoundException(bucket);
    }

    return existingBucket.objects.values().stream()
        .filter(object -> object.key().startsWith(prefix))
        .map(
            object ->
                new ObjectSummary(
                    object.key(),
                    object.metadata().contentLength(),
                    object.metadata().eTag(),
                    object.metadata().lastModified()))
        .toList();
  }

  // Multipart operations will be implemented later.

  @Override
  public MultipartUpload createMultipartUpload(String bucket, String key, ObjectMetadata metadata) {

    Bucket existingBucket = buckets.get(bucket);

    if (existingBucket == null) {
      throw new S3NestBucketNotFoundException(bucket);
    }

    String uploadId = java.util.UUID.randomUUID().toString();

    MultipartUpload upload =
        new MultipartUpload(uploadId, bucket, key, metadata, java.time.Instant.now());

    multipartUploads.put(uploadId, new MultipartUploadState(upload));

    return upload;
  }

  @Override
  public void putPart(String uploadId, int partNumber, byte[] content) {

    MultipartUploadState upload = multipartUploads.get(uploadId);

    if (upload == null) {
      throw new S3NestMultipartUploadNotFoundException(uploadId);
    }

    String eTag = java.util.UUID.randomUUID().toString();

    MultipartPart part = new MultipartPart(partNumber, content, eTag);

    upload.parts.put(partNumber, part);
  }

  @Override
  public java.util.Optional<MultipartPart> getPart(String uploadId, int partNumber) {

    MultipartUploadState upload = multipartUploads.get(uploadId);

    if (upload == null) {
      throw new S3NestMultipartUploadNotFoundException(uploadId);
    }

    return java.util.Optional.ofNullable(upload.parts.get(partNumber));
  }

  @Override
  public List<MultipartPart> listParts(String uploadId) {

    MultipartUploadState upload = multipartUploads.get(uploadId);

    if (upload == null) {
      throw new S3NestMultipartUploadNotFoundException(uploadId);
    }

    return upload.parts.values().stream()
        .sorted(java.util.Comparator.comparingInt(MultipartPart::partNumber))
        .toList();
  }

  @Override
  public StoredObject completeMultipartUpload(String uploadId, List<Integer> partNumbers) {

    MultipartUploadState upload = multipartUploads.get(uploadId);

    if (upload == null) {
      throw new S3NestMultipartUploadNotFoundException(uploadId);
    }

    java.io.ByteArrayOutputStream content = new java.io.ByteArrayOutputStream();

    for (Integer partNumber : partNumbers) {
      MultipartPart part = upload.parts.get(partNumber);

      if (part == null) {
        throw new MultipartPartNotFoundException(uploadId, partNumber);
      }

      content.writeBytes(part.content());
    }

    byte[] completedContent = content.toByteArray();

    StoredObject completedObject =
        new StoredObject(
            upload.upload.bucket(),
            upload.upload.key(),
            completedContent,
            upload.upload.metadata());

    Bucket bucket = buckets.get(upload.upload.bucket());

    bucket.objects.put(upload.upload.key(), completedObject);

    multipartUploads.remove(uploadId);

    return completedObject;
  }

  @Override
  public void abortMultipartUpload(String uploadId) {

    MultipartUploadState upload = multipartUploads.remove(uploadId);

    if (upload == null) {
      throw new S3NestMultipartUploadNotFoundException(uploadId);
    }
  }
}
