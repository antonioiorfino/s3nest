package it.iorfino.s3.result;

import it.iorfino.s3.model.S3Operation;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

//TBD
public final class S3NestS3Response {

    private final S3Operation operation;
    private final Object result;


    public S3NestS3Response(
            S3Operation operation,
            Object result) {
        this.operation = operation;
        this.result = result;
    }

}
