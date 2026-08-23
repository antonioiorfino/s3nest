# S3 API Compatibility Specification

## Purpose

This document defines the subset of the Amazon S3 API supported by S3Nest.

The S3 protocol is the primary compatibility contract of S3Nest.

This document defines the HTTP-level semantics required by the initial S3
protocol implementation, including supported operations, request and response
formats, status codes, error behaviour and streaming requirements.

The initial implementation does not attempt to provide complete Amazon S3
compatibility.

## Compatibility Principles

S3Nest follows the Amazon S3 HTTP API model for the operations defined in this
document.

Compatibility is defined at the HTTP protocol level and includes:

* HTTP methods;
* request paths;
* query parameters;
* relevant headers;
* request bodies;
* response status codes;
* response headers;
* response bodies;
* S3 error responses.

Features not explicitly defined as supported are outside the initial
compatibility scope.

## Authentication

S3Nest does not validate AWS authentication signatures in the initial
implementation.

Requests may contain S3 authentication headers, but S3Nest does not validate:

* AWS access keys;
* AWS secret keys;
* AWS Signature Version 4;
* AWS Signature Version 2;
* signing timestamps;
* credential scopes.

Authentication and authorization are therefore outside the initial
compatibility scope.

---

# Bucket Operations

## CreateBucket

### Request

```text
PUT /{bucket}
```

### Request Headers

Relevant headers:

* `Content-Length`
* `Content-Type`

Authentication headers may be present but are not validated.

### Request Body

An empty request body is supported.

A request body containing a supported S3 CreateBucketConfiguration may be
accepted where required by the implementation.

### Success Response

```text
HTTP 200 OK
```

### Response Headers

The response may include:

* `Location`

### Response Body

No response body is required.

### Errors

The implementation must provide S3-compatible errors for:

* invalid bucket name;
* bucket already exists;
* invalid request;
* storage failure.

---

## DeleteBucket

### Request

```text
DELETE /{bucket}
```

### Request Body

No request body is required.

### Success Response

```text
HTTP 204 No Content
```

### Response Body

No response body.

### Errors

The implementation must provide S3-compatible errors for:

* bucket does not exist;
* bucket is not empty;
* invalid bucket name;
* storage failure.

---

## ListBuckets

### Request

```text
GET /
```

### Query Parameters

No query parameters are required for the initial implementation.

### Success Response

```text
HTTP 200 OK
```

### Response Body

The response is an S3-compatible XML document containing the available
buckets.

The response must include:

* owner information where supported;
* bucket name;
* bucket creation timestamp.

### Errors

Storage failures must result in an S3-compatible error response.

---

## HeadBucket

### Request

```text
HEAD /{bucket}
```

### Success Response

```text
HTTP 200 OK
```

### Response Body

No response body.

### Errors

The implementation must return an appropriate S3-compatible error when:

* the bucket does not exist;
* the bucket name is invalid;
* storage access fails.

---

# Object Operations

## PutObject

### Request

```text
PUT /{bucket}/{key}
```

### Request Headers

Relevant headers include:

* `Content-Length`
* `Content-Type`
* `Content-MD5`
* `ETag` where applicable
* user-defined metadata using `x-amz-meta-*`

Authentication headers may be present but are not validated.

### Request Body

The request body contains the object data.

The implementation must support streaming object data without requiring the
entire object to be held in memory.

### Success Response

```text
HTTP 200 OK
```

### Response Headers

The response should include:

* `ETag`

### Response Body

No response body is required.

### Errors

The implementation must provide S3-compatible errors for:

* bucket does not exist;
* invalid object key;
* invalid request;
* storage failure.

---

## GetObject

### Request

```text
GET /{bucket}/{key}
```

### Request Headers

Relevant headers include:

* `Range`
* `If-Match`
* `If-None-Match`
* `If-Modified-Since`
* `If-Unmodified-Since`

Detailed conditional and range compatibility may be extended in the
compatibility milestone.

### Success Response

```text
HTTP 200 OK
```

### Response Headers

Relevant headers include:

* `Content-Length`
* `Content-Type`
* `ETag`
* `Last-Modified`
* `Accept-Ranges`

### Response Body

The response body contains the object data.

Object data must be streamed where possible.

### Errors

The implementation must provide S3-compatible errors for:

* bucket does not exist;
* object does not exist;
* invalid range;
* storage failure.

---

## HeadObject

### Request

```text
HEAD /{bucket}/{key}
```

### Success Response

```text
HTTP 200 OK
```

### Response Headers

The response should include relevant object metadata, including:

* `Content-Length`
* `Content-Type`
* `ETag`
* `Last-Modified`
* user-defined `x-amz-meta-*` metadata.

### Response Body

No response body.

### Errors

The implementation must provide S3-compatible errors for:

* bucket does not exist;
* object does not exist;
* storage failure.

---

## DeleteObject

### Request

```text
DELETE /{bucket}/{key}
```

### Success Response

```text
HTTP 204 No Content
```

### Response Body

No response body.

### Errors

The implementation must provide S3-compatible errors for:

* bucket does not exist;
* invalid request;
* storage failure.

---

## CopyObject

### Request

```text
PUT /{destination-bucket}/{destination-key}
```

The source object is specified using:

```text
x-amz-copy-source
```

### Request Headers

Relevant headers include:

* `x-amz-copy-source`
* metadata-related headers where supported.

### Request Body

No object data is transmitted in the request body.

### Success Response

```text
HTTP 200 OK
```

### Response Body

The response is an S3-compatible XML document containing copy result
information, including the resulting ETag where supported.

### Errors

The implementation must provide S3-compatible errors for:

* source bucket does not exist;
* source object does not exist;
* destination bucket does not exist;
* invalid copy source;
* storage failure.

---

## ListObjects

### Request

```text
GET /{bucket}
```

### Query Parameters

The initial implementation must support the parameters required for basic
object listing, including:

* `prefix`
* `marker`
* `delimiter`
* `max-keys`

### Success Response

```text
HTTP 200 OK
```

### Response Body

The response is an S3-compatible XML document containing:

* bucket name;
* object keys;
* object sizes;
* ETags where available;
* last-modified timestamps;
* common prefixes where `delimiter` is used;
* truncation information.

### Errors

The implementation must provide S3-compatible errors for:

* bucket does not exist;
* invalid request;
* storage failure.

---

## ListObjectsV2

### Request

```text
GET /{bucket}?list-type=2
```

### Query Parameters

The initial implementation must support:

* `prefix`
* `continuation-token`
* `delimiter`
* `max-keys`
* `start-after`

### Success Response

```text
HTTP 200 OK
```

### Response Body

The response is an S3-compatible XML document containing:

* bucket name;
* object keys;
* object sizes;
* ETags where available;
* last-modified timestamps;
* common prefixes where `delimiter` is used;
* continuation information;
* truncation information.

### Errors

The implementation must provide S3-compatible errors for:

* bucket does not exist;
* invalid continuation token;
* invalid request;
* storage failure.

---

## DeleteObjects

### Request

```text
POST /{bucket}?delete
```

### Request Headers

Relevant headers include:

* `Content-Type`
* `Content-MD5` where required by the S3 protocol.

### Request Body

The request body is an S3-compatible XML document containing the object keys
to delete.

### Success Response

```text
HTTP 200 OK
```

### Response Body

The response is an S3-compatible XML document containing:

* successfully deleted objects;
* errors for objects that could not be deleted.

### Errors

The implementation must provide S3-compatible errors for:

* invalid XML;
* invalid request;
* bucket does not exist;
* storage failure.

---

# Multipart Upload Operations

## CreateMultipartUpload

### Request

```text
POST /{bucket}/{key}?uploads
```

### Request Headers

Relevant headers include:

* `Content-Type`
* user-defined `x-amz-meta-*` metadata.

### Request Body

No request body is required.

### Success Response

```text
HTTP 200 OK
```

### Response Body

The response is an S3-compatible XML document containing:

* bucket;
* object key;
* upload ID.

The upload ID must uniquely identify the multipart upload.

### Errors

The implementation must provide S3-compatible errors for:

* bucket does not exist;
* invalid object key;
* invalid request;
* storage failure.

---

## UploadPart

### Request

```text
PUT /{bucket}/{key}?partNumber={partNumber}&uploadId={uploadId}
```

### Request Headers

Relevant headers include:

* `Content-Length`
* `Content-MD5`

### Request Body

The request body contains the part data.

Part data must be streamed where possible.

### Success Response

```text
HTTP 200 OK
```

### Response Headers

The response must include:

* `ETag`

### Errors

The implementation must provide S3-compatible errors for:

* upload does not exist;
* invalid part number;
* invalid object key;
* invalid request;
* storage failure.

---

## CompleteMultipartUpload

### Request

```text
POST /{bucket}/{key}?uploadId={uploadId}
```

### Request Body

The request body is an S3-compatible XML document describing the uploaded
parts and their ETags.

### Success Response

```text
HTTP 200 OK
```

### Response Body

The response is an S3-compatible XML document containing:

* bucket;
* key;
* resulting ETag.

### Errors

The implementation must provide S3-compatible errors for:

* upload does not exist;
* invalid XML;
* missing parts;
* invalid part ordering;
* invalid ETag;
* storage failure.

---

## AbortMultipartUpload

### Request

```text
DELETE /{bucket}/{key}?uploadId={uploadId}
```

### Success Response

```text
HTTP 204 No Content
```

### Response Body

No response body.

### Errors

The implementation must provide S3-compatible errors for:

* upload does not exist;
* bucket does not exist;
* invalid request;
* storage failure.

---

## ListParts

### Request

```text
GET /{bucket}/{key}?uploadId={uploadId}
```

### Query Parameters

The initial implementation should support:

* `part-number-marker`
* `max-parts`

### Success Response

```text
HTTP 200 OK
```

### Response Body

The response is an S3-compatible XML document containing:

* bucket;
* key;
* upload ID;
* part numbers;
* part sizes;
* ETags;
* last-modified timestamps;
* truncation information.

### Errors

The implementation must provide S3-compatible errors for:

* upload does not exist;
* bucket does not exist;
* invalid request;
* storage failure.

---

# Streaming Requirements

S3Nest must support streaming for operations involving potentially large
object data.

The following operations must not require the complete object to be loaded
into memory:

* PutObject;
* GetObject;
* UploadPart.

Streaming should also be used for multipart completion where practical.

The implementation must avoid introducing unnecessary in-memory copies of
large object data.

---

# Error Behaviour

Errors must be represented using the S3 error response format.

An error response should provide, where applicable:

* error code;
* error message;
* resource;
* request ID.

The implementation must not expose internal implementation details such as:

* Java exception class names;
* stack traces;
* local filesystem paths;
* internal storage structures.

The exact error-code mapping must remain consistent across the supported S3
operations.

---

# Metadata

S3Nest must preserve the metadata required by the supported S3 operations.

This includes, where applicable:

* object size;
* content type;
* ETag;
* last-modified timestamp;
* user-defined `x-amz-meta-*` metadata.

Metadata behaviour must remain consistent between:

* PutObject;
* GetObject;
* HeadObject;
* CopyObject;
* multipart upload completion.

---

# Unsupported Functionality

The following functionality is outside the initial compatibility scope:

* Bucket policies;
* Bucket ACLs;
* Object ACLs;
* Versioning;
* Object Lock;
* Lifecycle configuration;
* Replication;
* Notifications;
* Server-side encryption;
* IAM integration;
* AWS authentication/signature validation.

Unsupported operations must return a controlled S3-compatible error rather
than being interpreted as another supported operation.

---

# Compatibility and Testing Baseline

Every supported operation must have integration tests covering at minimum:

* successful request;
* invalid request;
* missing bucket/object where applicable;
* expected HTTP status;
* response format;
* relevant headers;
* persistence of relevant metadata.

Streaming operations must additionally be tested with object data larger than
the expected in-memory request/response buffer.

Multipart operations must be tested as a complete lifecycle:

1. create upload;
2. upload parts;
3. list parts;
4. complete upload;
5. retrieve resulting object;

and:

1. create upload;
2. upload parts;
3. abort upload.

This document is the baseline for the initial S3 protocol implementation.

Future compatibility work may extend these semantics without changing the
meaning of the initial supported operations unless explicitly documented as
a compatibility change.
