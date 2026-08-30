package it.iorfino.s3.result;

import it.iorfino.s3.model.S3NestS3OperationResult;
import it.iorfino.s3.model.S3Operation;

/**
 * * Represents an S3 operation result containing an XML response payload. * *
 *
 * <p>The result contains only the XML payload and remains independent of the HTTP transport layer.
 * * HTTP-specific response semantics, such as status codes and response headers, are applied by the
 * * response mapper. * * @param body the XML response payload
 */
public record S3NestS3XmlResult(S3Operation operation, String body)
    implements S3NestS3OperationResult {}
