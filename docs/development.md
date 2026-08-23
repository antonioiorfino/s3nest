# Development and API Conventions

## Java Version

S3Nest targets Java 21.

The project must remain compatible with the Java 21 runtime unless a future
major release explicitly changes the baseline.

## Code Style

The project follows standard Java conventions:

- Use clear and descriptive names.
- Prefer small, focused classes and methods.
- Avoid unnecessary abstraction.
- Prefer composition over inheritance.
- Keep public APIs minimal and explicit.
- Avoid introducing dependencies when the JDK provides a suitable solution.

## Package Structure

The base package is:

    it.iorfino.s3nest

Packages should be organized by responsibility rather than technical
implementation details.

## Public API

Public APIs are considered part of the S3Nest compatibility contract.

Public classes, methods and interfaces must:

- have clear and predictable behaviour;
- be documented when their behaviour is not self-evident;
- avoid exposing internal implementation details;
- remain backward compatible within the same major version whenever possible.

Internal implementation details should not be exposed through the public API.

## Null Handling

Public APIs should avoid returning or accepting `null` unless there is a
specific reason to do so.

When absence is part of the API semantics, `Optional` may be used for return
values.

## Exceptions

Exceptions should represent meaningful error conditions.

Public APIs should avoid exposing implementation-specific exceptions.

Exceptions must provide enough information to understand the failure without
requiring access to internal implementation details.

## Thread Safety

S3Nest must support concurrent usage.

Shared components must be designed explicitly for thread-safe operation.

Mutable shared state must use appropriate concurrency mechanisms.

Thread safety must not rely on callers synchronizing access unless explicitly
documented by the API.

## Lifecycle

Every S3Nest instance must have an explicit lifecycle.

Resources created by an instance must be released when the instance is
stopped or closed.

Lifecycle operations must be safe to invoke according to their documented
contract.

## Testing

All externally observable behaviour must be covered by automated tests.

Tests should:

- be deterministic;
- avoid relying on external services;
- verify both successful and failure scenarios;
- cover concurrent access where relevant.

## Documentation

All project documentation must be written in English.

Public APIs should include JavaDoc when their behaviour, lifecycle, exceptions,
thread-safety guarantees or configuration is not obvious from the API itself.