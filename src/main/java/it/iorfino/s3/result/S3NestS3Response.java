package it.iorfino.s3.result;

import it.iorfino.s3.model.S3Operation;

// TBD
public final class S3NestS3Response {

  private final S3Operation operation;
  private final Object result;

  public S3NestS3Response(S3Operation operation, Object result) {
    this.operation = operation;
    this.result = result;
  }
}
