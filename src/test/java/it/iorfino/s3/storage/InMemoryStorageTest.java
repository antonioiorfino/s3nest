package it.iorfino.s3.storage;

/**
 * Contract tests for {@link InMemoryStorage}.
 *
 * <p>This class applies all storage contract tests to the in-memory implementation.
 */
class InMemoryStorageTest extends StorageContractTest {

  /**
   * Creates a fresh in-memory storage for each test.
   *
   * @return a new {@link InMemoryStorage}
   */
  @Override
  protected S3NestStorage createStorage() {
    return new InMemoryStorage();
  }
}
