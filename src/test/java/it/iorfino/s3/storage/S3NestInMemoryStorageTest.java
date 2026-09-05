package it.iorfino.s3.storage;

/**
 * Contract tests for {@link S3NestInMemoryStorage}.
 *
 * <p>This class applies all storage contract tests to the in-memory implementation.
 */
class S3NestInMemoryStorageTest extends S3NestStorageContractTest {

  /**
   * Creates a fresh in-memory storage for each test.
   *
   * @return a new {@link S3NestInMemoryStorage}
   */
  @Override
  protected S3NestStorage createStorage() {
    return new S3NestInMemoryStorage();
  }
}
