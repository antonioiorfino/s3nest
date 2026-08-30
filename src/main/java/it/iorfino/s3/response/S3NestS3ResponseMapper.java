package it.iorfino.s3.response;

import it.iorfino.http.S3NestHttpResponse;
import it.iorfino.s3.model.S3NestS3OperationResult;
import it.iorfino.s3.result.S3NestS3EmptyResult;
import it.iorfino.s3.result.S3NestS3ErrorResult;
import it.iorfino.s3.result.S3NestS3ObjectResult;
import it.iorfino.s3.result.S3NestS3XmlResult;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

/**
 * Maps S3 operation results to transport-independent HTTP responses.
 *
 * <p>Operation handlers produce S3 operation results and do not construct HTTP responses directly.
 * This mapper is responsible for translating those results into {@link S3NestHttpResponse}
 * instances.
 *
 * <p>HTTP status codes are determined from the semantic S3 operation represented by the result.
 * This keeps HTTP response semantics out of operation handlers.
 */
public final class S3NestS3ResponseMapper {

  /**
   * Maps an S3 operation result to an HTTP response.
   *
   * <p>Empty results are mapped according to the S3 operation that produced them. Object results
   * are returned with a successful {@code 200 OK} status, their metadata is propagated as HTTP
   * response headers, and their content length is exposed through the {@code Content-Length}
   * header.
   *
   * <p>Object content is streamed directly from the result's input stream to the response output
   * stream without loading the entire object into memory.
   *
   * @param result the result produced by an S3 operation handler
   * @return the HTTP response corresponding to the operation result
   * @throws IllegalArgumentException if the result type is not supported
   */
  public S3NestHttpResponse map(S3NestS3OperationResult result) {

    if (result instanceof S3NestS3EmptyResult emptyResult) {
      return mapEmptyResult(emptyResult);
    }

    if (result instanceof S3NestS3ObjectResult objectResult) {
      Map<String, List<String>> headers = new HashMap<>(objectResult.metadata());
      headers.put("Content-Length", List.of(Long.toString(objectResult.contentLength())));
      return new S3NestHttpResponse(200, headers, output -> objectResult.body().transferTo(output));
    }

    if (result instanceof S3NestS3XmlResult(String body)) {
      return new S3NestHttpResponse(
          200,
          Map.of("Content-Type", List.of("application/xml")),
          output -> output.write(body.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }

    if (result instanceof S3NestS3ErrorResult(String code, String message)) {
      String body = buildErrorXml(code, message);

      return new S3NestHttpResponse(
          statusCodeFor(code),
          Map.of("Content-Type", List.of("application/xml")),
          output -> output.write(body.getBytes(StandardCharsets.UTF_8)));
    }

    throw new IllegalArgumentException(
        "Unsupported S3 operation result: " + result.getClass().getName());
  }

  /**
   * Maps an empty S3 operation result to an HTTP response.
   *
   * <p>The HTTP status code is determined by the semantic S3 operation rather than by the absence
   * of a response body.
   *
   * @param result the empty operation result
   * @return the corresponding HTTP response
   * @throws IllegalArgumentException if the operation is not supported as an empty response
   */
  private S3NestHttpResponse mapEmptyResult(S3NestS3EmptyResult result) {

    int statusCode =
        switch (result.operation()) {
          case CREATE_BUCKET -> 200;
          case DELETE_BUCKET, DELETE_OBJECT, ABORT_MULTIPART_UPLOAD -> 204;
          case HEAD_BUCKET, HEAD_OBJECT -> 200;
          case PUT_OBJECT, COPY_OBJECT -> 200;
          default ->
              throw new IllegalArgumentException(
                  "Unsupported empty response operation: " + result.operation());
        };

    return new S3NestHttpResponse(statusCode, Map.of(), output -> {});
  }

  private int statusCodeFor(String errorCode) {
    return switch (errorCode) {
      case "NoSuchBucket", "NoSuchKey", "NoSuchUpload" -> 404;
      case "AccessDenied" -> 403;
      case "InvalidAccessKeyId", "SignatureDoesNotMatch" -> 403;
      case "InvalidRequest", "InvalidArgument" -> 400;
      default -> 500;
    };
  }

  private String buildErrorXml(String code, String message) {
    StringWriter writer = new StringWriter();

    try {
      var xmlWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(writer);

      xmlWriter.writeStartElement("Error");

      xmlWriter.writeStartElement("Code");
      xmlWriter.writeCharacters(code);
      xmlWriter.writeEndElement();

      xmlWriter.writeStartElement("Message");
      xmlWriter.writeCharacters(message);
      xmlWriter.writeEndElement();

      xmlWriter.writeEndElement();
      xmlWriter.close();

      return writer.toString();
    } catch (XMLStreamException e) {
      throw new IllegalStateException("Unable to generate S3 error XML", e);
    }
  }
}
