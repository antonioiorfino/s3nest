# Architecture

## Overview

S3Nest is a lightweight, embedded S3-compatible server designed primarily
for automated tests.

The project is implemented in Java 21 and is intentionally designed without
an application framework or container runtime.

The architecture is based on three main layers:

1. Public Java API
2. S3 protocol layer
3. Storage layer

The S3 protocol is the compatibility contract. The internal Java API is only
responsible for configuring and controlling an S3Nest instance.

## Architectural Principles

S3Nest follows these principles:

- Java 21 as the runtime baseline.
- No application framework.
- Minimal runtime dependencies.
- No global mutable state.
- Multiple S3Nest instances can run independently in the same JVM.
- Each instance owns its HTTP server and storage.
- Instances are thread-safe.
- Storage is independent from the HTTP and S3 protocol layers.
- The S3 protocol is the primary compatibility contract.
- Internal implementation details must not leak into the public API.

## High-Level Architecture

                         S3 Client
                            |
                            | HTTP / S3 Protocol
                            v
                  +----------------------+
                  |        S3Nest        |
                  |                      |
                  |   HTTP Server        |
                  |         |            |
                  |         v            |
                  |  S3 Protocol Layer   |
                  |         |            |
                  |         v            |
                  |  Operation Handlers  |
                  |         |            |
                  +---------|------------+
                            |
                            v
                  +----------------------+
                  |   Storage Abstraction |
                  +----------+-----------+
                             |
                    +--------+--------+
                    |                 |
                    v                 v
             InMemoryStorage   FileSystemStorage

## Public Java API

The public Java API is responsible for creating, configuring and controlling
an S3Nest instance.

It must not expose implementation details of the HTTP server or storage
implementation.

A typical usage pattern is:

    S3Nest server = S3Nest.builder()
            .port(0)
            .build();

    server.start();

    URI endpoint = server.endpoint();

    server.stop();

The exact API will be defined separately from the internal implementation.

### Configuration

Configuration must be explicit and instance-specific.

An S3Nest instance must not depend on global configuration or static mutable
state.

Configuration may include:

- HTTP port
- storage implementation
- storage configuration
- server lifecycle options

When port 0 is requested, the operating system may select an available
ephemeral port.

The selected port must be discoverable through the S3Nest instance.

## Instance Isolation

Each S3Nest instance owns its own:

- HTTP server
- listening port
- storage
- configuration
- lifecycle state

For example:

    S3Nest instance A
        |
        +-- HTTP server
        +-- port A
        +-- storage A

    S3Nest instance B
        |
        +-- HTTP server
        +-- port B
        +-- storage B

No state must be shared between instances unless explicitly designed and
documented as immutable shared infrastructure.

This allows multiple test cases or test suites to run concurrently using
independent S3Nest instances.

## S3 Protocol Layer

The S3 protocol layer translates HTTP requests into S3 operations and
translates operation results into S3-compatible HTTP responses.

Its responsibilities include:

- HTTP request handling
- HTTP method and path interpretation
- bucket and object name extraction
- query parameter handling
- request header handling
- request body handling
- S3 operation dispatch
- S3-compatible response generation
- S3-compatible error generation

The protocol layer must not contain storage-specific logic.

## S3 API Compatibility

S3Nest targets compatibility with the Amazon S3 API required for automated
testing.

The supported operation set will be explicitly documented and tested.

The implementation must preserve the observable S3 protocol semantics,
including:

- HTTP methods
- request paths
- query parameters
- relevant headers
- status codes
- response headers
- response bodies
- error responses
- object metadata
- streaming behaviour where applicable

The S3 protocol surface must be treated independently from the public
S3Nest Java API.

Adding support for another S3 operation should not require a breaking change
to the S3Nest Java API.

## Operation Handling

S3 operations should be represented internally as explicit operations rather
than being implemented as large HTTP request handlers.

Conceptually:

    HTTP Request
         |
         v
    Request parsing
         |
         v
    S3 Operation
         |
         v
    Operation Handler
         |
         v
    Storage
         |
         v
    Operation Result
         |
         v
    HTTP Response

This separation keeps protocol handling independent from storage behaviour.

## Storage Abstraction

The storage layer provides the persistence model used by S3Nest.

The protocol layer must depend only on the storage abstraction and must not
depend directly on a concrete storage implementation.

The initial implementation will provide an in-memory storage backend.

A future file system storage backend will implement the same abstraction.

Conceptually:

             Storage
                |
        +-------+-------+
        |               |
        v               v
InMemoryStorage   FileSystemStorage

The storage abstraction will be defined according to the operations required
by the supported S3 API.

It must not expose HTTP-specific concepts.

## In-Memory Storage

The in-memory storage backend is the default storage implementation for
lightweight tests.

Its goals are:

- low latency
- deterministic behaviour
- no external resources
- complete instance isolation
- safe concurrent access

Data stored by an instance must be released when the instance is stopped and
become eligible for garbage collection when no longer referenced.

## File System Storage

The architecture allows a file system storage backend to be introduced
without changing the S3 protocol layer.

The file system backend will be responsible for:

- mapping buckets and objects to files/directories
- metadata persistence
- concurrent access
- resource management
- cleanup

The exact filesystem layout is an implementation detail and must not become
part of the public S3Nest API.

## Thread Safety

S3Nest instances must be thread-safe.

A single instance may receive multiple concurrent HTTP requests.

The implementation must therefore:

- safely handle concurrent requests;
- safely manage shared storage state;
- avoid unsafe mutable global state;
- use appropriate concurrent data structures or synchronization;
- preserve S3 operation consistency.

Thread safety is an architectural requirement, not an optional feature.

## Lifecycle

An S3Nest instance has an explicit lifecycle:

    NEW
     |
     | start()
     v
    RUNNING
     |
     | stop()
     v
    STOPPED

Starting an instance allocates and starts its required resources.

Stopping an instance releases those resources.

Lifecycle operations must be deterministic and documented.

Repeated lifecycle operations must have well-defined behaviour.

Resources must not remain allocated after an instance has been stopped.

## HTTP Server

The HTTP server is an implementation detail of S3Nest.

The initial implementation should use Java platform capabilities where
practical rather than introducing a heavyweight HTTP framework.

The server must support the HTTP functionality required by the supported S3
API, including request bodies and concurrent request processing.

The HTTP server must remain replaceable without changing the storage layer or
the public S3Nest API.

## Error Handling

Errors generated by the storage and protocol layers must be translated into
appropriate S3-compatible HTTP responses.

Internal implementation exceptions must not be exposed directly to clients.

Errors should contain sufficient information for tests to determine the
reason for a failure while avoiding unnecessary implementation details.

## Dependency Strategy

S3Nest intentionally minimizes runtime dependencies.

The preferred implementation strategy is:

1. Java standard library
2. Small, permissively licensed libraries when required
3. No application framework

Every runtime dependency must comply with the project's licensing policy.

A dependency should only be introduced when the required functionality
cannot reasonably be implemented using the Java platform or when the
dependency provides substantial value relative to its cost.

## Testing Architecture

The project will use automated tests at multiple levels.

### Unit Tests

Unit tests verify individual components independently.

Examples include:

- storage operations
- request parsing
- operation dispatch
- response generation
- error mapping

### Integration Tests

Integration tests verify the complete S3Nest server through HTTP.

Tests should use a real S3 client where practical rather than testing only
internal implementation details.

The integration test flow is:

    S3 Client
        |
        v
       HTTP
        |
        v
      S3Nest
        |
        v
      Storage

### Concurrency Tests

Concurrency tests verify that multiple requests can safely operate on the
same S3Nest instance.

Tests must also verify that independent S3Nest instances do not interfere
with one another.

## API Compatibility and Evolution

The S3 protocol compatibility surface and the S3Nest Java API are separate
contracts.

The S3 protocol implementation may evolve as additional S3 operations are
supported.

The public Java API must remain stable within a major release whenever
possible.

Breaking changes to the public Java API require an explicit versioning
decision.

## Future Extensions

The architecture is intentionally designed to allow future extensions
without changing its fundamental structure.

Potential future capabilities include:

- additional S3 operations
- file system storage
- additional storage backends
- improved request streaming
- additional configuration options
- advanced S3 compatibility features

Future extensions must preserve the core architectural principles defined
in this document.
``
