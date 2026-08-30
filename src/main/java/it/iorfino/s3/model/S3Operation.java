package it.iorfino.s3.model;

/**
 * Identifies an S3 operation supported by the S3Nest protocol layer.
 *
 * <p>An {@code S3Operation} represents the semantic operation requested by an HTTP request after
 * its method, path and query parameters have been interpreted by the request parser.
 *
 * <p>This enum defines the initial S3 compatibility scope supported by S3Nest. It is independent of
 * HTTP transport details and storage implementation details.
 */
public enum S3Operation {
  /** Creates a new bucket. */
  CREATE_BUCKET,

  /** Deletes an existing bucket. */
  DELETE_BUCKET,

  /** Lists the buckets accessible to the caller. */
  LIST_BUCKETS,

  /** Checks whether a bucket exists and is accessible. */
  HEAD_BUCKET,

  /** Creates or replaces an object in a bucket. */
  PUT_OBJECT,

  /** Retrieves an object from a bucket. */
  GET_OBJECT,

  /** Retrieves object metadata without retrieving the object body. */
  HEAD_OBJECT,

  /** Deletes an object from a bucket. */
  DELETE_OBJECT,

  /** Creates a new object by copying an existing object. */
  COPY_OBJECT,

  /** Lists objects in a bucket using the original S3 listing API. */
  LIST_OBJECTS,

  /** Lists objects in a bucket using the S3 ListObjectsV2 API. */
  LIST_OBJECTS_V2,

  /** Deletes multiple objects in a single request. */
  DELETE_OBJECTS,

  /** Initiates a multipart upload. */
  CREATE_MULTIPART_UPLOAD,

  /** Uploads a single part of a multipart upload. */
  UPLOAD_PART,

  /** Completes a multipart upload by assembling its uploaded parts. */
  COMPLETE_MULTIPART_UPLOAD,

  /** Aborts an in-progress multipart upload. */
  ABORT_MULTIPART_UPLOAD,

  /** Lists the parts currently uploaded for a multipart upload. */
  LIST_PARTS
}
