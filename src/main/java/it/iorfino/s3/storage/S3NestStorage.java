package it.iorfino.s3.storage;

import java.util.List;
import java.util.Optional;

public interface S3NestStorage {

  // Buckets

  void createBucket(String bucket);

  void deleteBucket(String bucket);

  boolean bucketExists(String bucket);

  List<String> listBuckets();

  // Objects

  void putObject(String bucket, String key, byte[] content, ObjectMetadata metadata);

  StoredObject getObject(String bucket, String key);

  boolean objectExists(String bucket, String key);

  void deleteObject(String bucket, String key);

  void copyObject(
      String sourceBucket, String sourceKey, String destinationBucket, String destinationKey);

  List<ObjectSummary> listObjects(String bucket, String prefix);

  // Multipart uploads

  MultipartUpload createMultipartUpload(String bucket, String key, ObjectMetadata metadata);

  void putPart(String uploadId, int partNumber, byte[] content);

  Optional<MultipartPart> getPart(String uploadId, int partNumber);

  List<MultipartPart> listParts(String uploadId);

  StoredObject completeMultipartUpload(String uploadId, List<Integer> partNumbers);

  void abortMultipartUpload(String uploadId);
}
