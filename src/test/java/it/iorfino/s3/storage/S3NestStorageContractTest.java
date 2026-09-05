package it.iorfino.s3.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Contract tests for the {@link S3NestStorage} abstraction.
 *
 * <p>This test class defines the expected behaviour of every {@link S3NestStorage} implementation.
 * It deliberately does not depend on any concrete storage implementation or on the HTTP/S3 layer.
 *
 * <p>A concrete implementation can verify that it respects the storage contract by extending this
 * class and implementing {@link #createStorage()}.
 *
 * <p>For example:
 *
 * <pre>{@code
 * class InMemoryStorageTest extends StorageContractTest {
 *
 *     @Override
 *     protected Storage createStorage() {
 *         return new InMemoryStorage();
 *     }
 * }
 * }</pre>
 *
 * <p>The contract covers:
 *
 * <ul>
 *   <li>bucket creation, deletion and existence;
 *   <li>object storage, retrieval, existence and deletion;
 *   <li>object metadata;
 *   <li>object listing;
 *   <li>object copying;
 *   <li>multipart upload state;
 *   <li>opaque object keys;
 *   <li>storage-level error handling.
 * </ul>
 *
 * <p>No HTTP-specific concept must appear in this test class. HTTP response codes and S3 XML errors
 * belong to the protocol layer, not to the storage abstraction.
 */
abstract class S3NestStorageContractTest {

  private S3NestStorage storage;

  /**
   * Creates a fresh storage instance for a test.
   *
   * <p>Each test is executed against a new storage instance. This prevents state created by one
   * test from affecting another test.
   *
   * @return a new {@link S3NestStorage} implementation
   */
  protected abstract S3NestStorage createStorage();

  /**
   * Creates the storage used by the current test.
   *
   * <p>A fresh instance is required for every test because the storage contract is stateful.
   */
  @BeforeEach
  void setUp() {
    storage = createStorage();
  }

  // -------------------------------------------------------------------------
  // Bucket tests
  // -------------------------------------------------------------------------

  /**
   * Verifies that a bucket can be created and subsequently found.
   *
   * <p>Creating a bucket must make it visible through both {@link
   * S3NestStorage#bucketExists(String)} and {@link S3NestStorage#listBuckets()}.
   */
  @Test
  void shouldCreateAndCheckBucket() {
    storage.createBucket("bucket");

    assertTrue(storage.bucketExists("bucket"));
    assertEquals(List.of("bucket"), storage.listBuckets());
  }

  /**
   * Verifies that creating an already existing bucket fails with a storage-level error.
   *
   * <p>The storage layer must not expose HTTP status codes or S3 XML errors. {@link
   * S3NestBucketAlreadyExistsException} represents the domain-level condition instead.
   */
  @Test
  void shouldNotCreateSameBucketTwice() {
    storage.createBucket("bucket");

    assertThrows(S3NestBucketAlreadyExistsException.class, () -> storage.createBucket("bucket"));
  }

  /**
   * Verifies that an existing empty bucket can be deleted.
   *
   * <p>After deletion, the bucket must no longer be reported as existing and must no longer be
   * returned by {@link S3NestStorage#listBuckets()}.
   */
  @Test
  void shouldDeleteBucket() {
    storage.createBucket("bucket");

    storage.deleteBucket("bucket");

    assertFalse(storage.bucketExists("bucket"));
    assertEquals(List.of(), storage.listBuckets());
  }

  /** Verifies that deleting an unknown bucket produces a {@link S3NestBucketNotFoundException}. */
  @Test
  void shouldFailToDeleteUnknownBucket() {
    assertThrows(S3NestBucketNotFoundException.class, () -> storage.deleteBucket("bucket"));
  }

  /**
   * Verifies that a non-empty bucket cannot be deleted.
   *
   * <p>The storage abstraction does not perform recursive deletion of objects when deleting a
   * bucket.
   */
  @Test
  void shouldNotDeleteNonEmptyBucket() {
    storage.createBucket("bucket");

    storage.putObject("bucket", "object.txt", bytes("content"), metadata());

    assertThrows(S3NestBucketNotEmptyException.class, () -> storage.deleteBucket("bucket"));
  }

  // -------------------------------------------------------------------------
  // Object tests
  // -------------------------------------------------------------------------

  /**
   * Verifies that an object can be stored and subsequently retrieved.
   *
   * <p>The retrieved object must preserve its bucket, key and content.
   */
  @Test
  void shouldStoreAndRetrieveObject() {
    storage.createBucket("bucket");

    byte[] content = bytes("hello");

    storage.putObject("bucket", "object.txt", content, metadata());

    S3NestStoredObject object = storage.getObject("bucket", "object.txt");

    assertEquals("bucket", object.bucket());
    assertEquals("object.txt", object.key());
    assertArrayEquals(content, object.content());
  }

  /**
   * Verifies the object existence operation.
   *
   * <p>An object must not be reported as existing before it is stored and must be reported as
   * existing after it is stored.
   */
  @Test
  void shouldCheckObjectExistence() {
    storage.createBucket("bucket");

    assertFalse(storage.objectExists("bucket", "object.txt"));

    storage.putObject("bucket", "object.txt", bytes("hello"), metadata());

    assertTrue(storage.objectExists("bucket", "object.txt"));
  }

  /**
   * Verifies that retrieving an unknown object produces a {@link S3NestObjectNotFoundException}.
   *
   * <p>The storage contract must not use {@code null} to represent a missing object.
   */
  @Test
  void shouldFailToRetrieveUnknownObject() {
    storage.createBucket("bucket");

    assertThrows(
        S3NestObjectNotFoundException.class, () -> storage.getObject("bucket", "missing.txt"));
  }

  /**
   * Verifies that an object can be deleted.
   *
   * <p>After deletion, {@link S3NestStorage#objectExists(String, String)} must return {@code
   * false}.
   */
  @Test
  void shouldDeleteObject() {
    storage.createBucket("bucket");

    storage.putObject("bucket", "object.txt", bytes("hello"), metadata());

    storage.deleteObject("bucket", "object.txt");

    assertFalse(storage.objectExists("bucket", "object.txt"));
  }

  /**
   * Verifies that deleting an object that does not exist is idempotent.
   *
   * <p>Deleting an unknown object is intentionally not considered a storage error. This keeps the
   * operation idempotent.
   */
  @Test
  void deletingUnknownObjectShouldBeIdempotent() {
    storage.createBucket("bucket");

    assertDoesNotThrow(() -> storage.deleteObject("bucket", "missing.txt"));
  }

  /**
   * Verifies that object metadata is preserved by the storage layer.
   *
   * <p>The metadata model is deliberately independent of HTTP headers or HTTP response generation.
   */
  @Test
  void shouldPreserveObjectMetadata() {
    storage.createBucket("bucket");

    S3NestObjectMetadata metadata =
        new S3NestObjectMetadata(
            "text/plain",
            5,
            "abc123",
            Instant.parse("2026-01-01T10:00:00Z"),
            Map.of(
                "author", "test",
                "custom", "value"));

    storage.putObject("bucket", "object.txt", bytes("hello"), metadata);

    S3NestStoredObject object = storage.getObject("bucket", "object.txt");

    assertEquals(metadata, object.metadata());
  }

  // -------------------------------------------------------------------------
  // Listing tests
  // -------------------------------------------------------------------------

  /**
   * Verifies that objects can be listed using a prefix.
   *
   * <p>The storage layer treats object keys as opaque strings. Prefix filtering is the only
   * key-related operation performed by this abstraction.
   *
   * <p>The returned objects are expected to be ordered deterministically by key.
   */
  @Test
  void shouldListObjectsByPrefix() {
    storage.createBucket("bucket");

    storage.putObject("bucket", "images/a.jpg", bytes("a"), metadata());

    storage.putObject("bucket", "images/b.jpg", bytes("b"), metadata());

    storage.putObject("bucket", "documents/a.txt", bytes("c"), metadata());

    List<S3NestObjectSummary> result = storage.listObjects("bucket", "images/");

    assertEquals(2, result.size());

    assertEquals(
        List.of("images/a.jpg", "images/b.jpg"),
        result.stream().map(S3NestObjectSummary::key).toList());
  }

  // -------------------------------------------------------------------------
  // Copy tests
  // -------------------------------------------------------------------------

  /**
   * Verifies that an object can be copied between buckets.
   *
   * <p>The copied object must contain the same content and metadata as the source object.
   */
  @Test
  void shouldCopyObject() {
    storage.createBucket("source");
    storage.createBucket("destination");

    S3NestObjectMetadata metadata = metadata();

    storage.putObject("source", "original.txt", bytes("hello"), metadata);

    storage.copyObject("source", "original.txt", "destination", "copy.txt");

    S3NestStoredObject copied = storage.getObject("destination", "copy.txt");

    assertArrayEquals(bytes("hello"), copied.content());

    assertEquals(metadata, copied.metadata());
  }

  // -------------------------------------------------------------------------
  // Multipart upload tests
  // -------------------------------------------------------------------------

  /**
   * Verifies that a multipart upload can be created.
   *
   * <p>The returned upload must have a non-null identifier and must retain the bucket and object
   * key associated with the upload.
   */
  @Test
  void shouldCreateMultipartUpload() {
    storage.createBucket("bucket");

    S3NestMultipartUpload upload = storage.createMultipartUpload("bucket", "large.bin", metadata());

    assertNotNull(upload.uploadId());
    assertEquals("bucket", upload.bucket());
    assertEquals("large.bin", upload.key());
  }

  /** Verifies that a multipart part can be stored and retrieved. */
  @Test
  void shouldStoreAndRetrieveMultipartPart() {
    storage.createBucket("bucket");

    S3NestMultipartUpload upload = storage.createMultipartUpload("bucket", "large.bin", metadata());

    storage.putPart(upload.uploadId(), 1, bytes("part-one"));

    Optional<S3NestMultipartPart> part = storage.getPart(upload.uploadId(), 1);

    assertTrue(part.isPresent());
    assertEquals(1, part.get().partNumber());

    assertArrayEquals(bytes("part-one"), part.get().content());
  }

  /**
   * Verifies that multipart parts are returned in part-number order.
   *
   * <p>Parts may be uploaded in any order, but listing them must produce a deterministic order.
   */
  @Test
  void shouldListMultipartPartsInPartNumberOrder() {
    storage.createBucket("bucket");

    S3NestMultipartUpload upload = storage.createMultipartUpload("bucket", "large.bin", metadata());

    storage.putPart(upload.uploadId(), 3, bytes("three"));

    storage.putPart(upload.uploadId(), 1, bytes("one"));

    storage.putPart(upload.uploadId(), 2, bytes("two"));

    List<S3NestMultipartPart> parts = storage.listParts(upload.uploadId());

    assertEquals(List.of(1, 2, 3), parts.stream().map(S3NestMultipartPart::partNumber).toList());
  }

  /**
   * Verifies that a multipart upload can be completed.
   *
   * <p>The content of the resulting object must be the concatenation of the selected parts in the
   * order supplied to {@link S3NestStorage#completeMultipartUpload(String, List)}.
   */
  @Test
  void shouldCompleteMultipartUpload() {
    storage.createBucket("bucket");

    S3NestMultipartUpload upload = storage.createMultipartUpload("bucket", "large.bin", metadata());

    storage.putPart(upload.uploadId(), 1, bytes("hello "));

    storage.putPart(upload.uploadId(), 2, bytes("world"));

    S3NestStoredObject object = storage.completeMultipartUpload(upload.uploadId(), List.of(1, 2));

    assertArrayEquals(bytes("hello world"), object.content());

    assertTrue(storage.objectExists("bucket", "large.bin"));
  }

  /**
   * Verifies that aborting a multipart upload removes its state.
   *
   * <p>After the upload has been aborted, operations referring to that upload must report that the
   * upload no longer exists.
   */
  @Test
  void shouldAbortMultipartUpload() {
    storage.createBucket("bucket");

    S3NestMultipartUpload upload = storage.createMultipartUpload("bucket", "large.bin", metadata());

    storage.putPart(upload.uploadId(), 1, bytes("hello"));

    storage.abortMultipartUpload(upload.uploadId());

    assertThrows(
        S3NestMultipartUploadNotFoundException.class, () -> storage.listParts(upload.uploadId()));
  }

  // -------------------------------------------------------------------------
  // Key semantics
  // -------------------------------------------------------------------------

  /**
   * Verifies that object keys are treated as opaque values.
   *
   * <p>The storage abstraction must not interpret keys as filesystem paths. Characters such as
   * {@code /}, {@code ..} and other path-like sequences are therefore valid parts of an object key.
   */
  @Test
  void shouldTreatObjectKeyAsOpaqueValue() {
    storage.createBucket("bucket");

    String key = "../some/path/with/slashes";

    storage.putObject("bucket", key, bytes("content"), metadata());

    assertTrue(storage.objectExists("bucket", key));

    assertArrayEquals(bytes("content"), storage.getObject("bucket", key).content());
  }

  /** Verifies that different storage instances do not share bucket state. */
  @Test
  void shouldKeepStorageInstancesIsolated() {
    S3NestStorage firstStorage = createStorage();
    S3NestStorage secondStorage = createStorage();

    firstStorage.createBucket("bucket-one");

    assertTrue(firstStorage.bucketExists("bucket-one"));
    assertFalse(secondStorage.bucketExists("bucket-one"));
  }

  /**
   * Verifies that concurrent object writes are handled safely.
   *
   * <p>Each worker writes a different object to the same bucket. After all workers complete, every
   * object must be available.
   */
  @Test
  void shouldSupportConcurrentObjectWrites() throws Exception {
    storage.createBucket("bucket");

    int numberOfObjects = 20;

    try (ExecutorService executor = Executors.newFixedThreadPool(4)) {

      List<Future<?>> futures = new ArrayList<>();

      for (int i = 0; i < numberOfObjects; i++) {
        String key = "object-" + i;
        byte[] content = bytes("content-" + i);

        futures.add(executor.submit(() -> storage.putObject("bucket", key, content, metadata())));
      }

      for (Future<?> future : futures) {
        future.get();
      }
    }

    for (int i = 0; i < numberOfObjects; i++) {
      String key = "object-" + i;

      assertTrue(storage.objectExists("bucket", key));
      assertArrayEquals(bytes("content-" + i), storage.getObject("bucket", key).content());
    }
  }

  @Test
  void shouldSupportConcurrentWritesToSameObject() throws Exception {
    storage.createBucket("bucket");

    int numberOfWrites = 20;

    try (ExecutorService executor = Executors.newFixedThreadPool(4)) {
      List<Future<?>> futures = new ArrayList<>();

      for (int i = 0; i < numberOfWrites; i++) {
        byte[] content = bytes("content-" + i);

        futures.add(
            executor.submit(() -> storage.putObject("bucket", "object.txt", content, metadata())));
      }

      for (Future<?> future : futures) {
        future.get();
      }
    }

    S3NestStoredObject object = storage.getObject("bucket", "object.txt");

    assertNotNull(object);
    assertTrue(object.content().length > 0);
  }

  // -------------------------------------------------------------------------
  // Test helpers
  // -------------------------------------------------------------------------

  /**
   * Creates standard metadata used by tests where the specific metadata values are not relevant to
   * the scenario.
   *
   * @return deterministic test metadata
   */
  private S3NestObjectMetadata metadata() {
    return new S3NestObjectMetadata(
        "application/octet-stream", 0, "etag", Instant.parse("2026-01-01T00:00:00Z"), Map.of());
  }

  /**
   * Converts a string to UTF-8 bytes.
   *
   * <p>Using an explicit charset makes the tests independent from the default charset of the
   * machine running Maven.
   *
   * @param value string to convert
   * @return UTF-8 representation of the string
   */
  private byte[] bytes(String value) {
    return value.getBytes(StandardCharsets.UTF_8);
  }
}
