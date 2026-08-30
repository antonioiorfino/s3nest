package it.iorfino.s3.result;

import it.iorfino.s3.model.S3NestS3OperationResult;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public record S3NestS3ObjectResult(
    InputStream body, long contentLength, Map<String, List<String>> metadata)
    implements S3NestS3OperationResult {}
