package it.iorfino.s3.handler;

import it.iorfino.s3.model.S3NestS3Object;
import it.iorfino.s3.model.S3NestS3OperationResult;
import it.iorfino.s3.model.S3NestS3Request;
import it.iorfino.s3.result.S3NestS3ObjectResult;
import it.iorfino.s3.routing.S3NestS3OperationHandler;
import it.iorfino.s3.storage.S3NestS3ObjectStorage;

/**
 * Handles the S3 GET_OBJECT operation.
 *
 * <p>The handler delegates object retrieval to the storage port and converts
 * the stored object into an S3 operation result.</p>
 *
 * <p>The handler does not depend on HTTP transport details or on a concrete
 * storage implementation.</p>
 */
public final class S3NestGetObjectHandler
        implements S3NestS3OperationHandler {

    private final S3NestS3ObjectStorage storage;

    /**
     * Creates a GET object handler.
     *
     * @param storage the storage port used to retrieve objects
     */
    public S3NestGetObjectHandler(S3NestS3ObjectStorage storage) {
        this.storage = storage;
    }

    /**
     * Retrieves the object identified by the supplied S3 request.
     *
     * @param request the parsed S3 GET_OBJECT request
     * @return the retrieved object as an S3 operation result
     */
    @Override
    public S3NestS3OperationResult handle(S3NestS3Request request) {
        S3NestS3Object object = storage.getObject(
                request.bucket(),
                request.objectKey()
        );

        if (object == null) {
            throw new S3NestS3ObjectNotFoundException(
                    request.bucket(),
                    request.objectKey()
            );
        }

        return new S3NestS3ObjectResult(
                object.body(),
                object.metadata()
        );
    }
}