# Licensing and Dependency Policy

## Project License

S3Nest is distributed under the Apache License 2.0.

## Dependency Policy

S3Nest aims to maintain a lightweight runtime and a permissive licensing model.

All third-party dependencies must be reviewed before being added to the project.

### Allowed Licenses

The following licenses are allowed for project dependencies:

- Apache License 2.0
- MIT License
- BSD 2-Clause License
- BSD 3-Clause License
- ISC License

These licenses are considered permissive and do not impose copyleft
requirements on software using S3Nest.

### Disallowed Licenses

The following licenses must not be used for runtime dependencies:

- GNU General Public License (GPL)
- GNU Affero General Public License (AGPL)
- GNU Lesser General Public License (LGPL)
- Eclipse Public License (EPL)
- Server Side Public License (SSPL)
- Business Source License (BUSL)
- Elastic License
- Commons Clause
- Source-available licenses
- Non-OSI-approved licenses

### Runtime Dependencies

Runtime dependencies are subject to the strictest licensing requirements.

A runtime dependency must:

- use an allowed license;
- be compatible with the Apache License 2.0 distribution model;
- have no additional usage restrictions that conflict with the project goals;
- provide clear and publicly available licensing information.

S3Nest should minimize the number of runtime dependencies whenever possible.

### Test and Build Dependencies

Test and build dependencies are reviewed separately from runtime dependencies.

A dependency that is used exclusively during testing or building may use a
license outside the runtime allowlist, provided that:

- it is not included in the distributed S3Nest artifact;
- its license does not impose obligations on the distributed S3Nest artifact;
- its usage is compatible with the project's development and distribution model.

Each dependency must still be reviewed before adoption.

### Dependency Review

Before introducing a new dependency, the following information must be verified:

- License
- Version
- Transitive dependencies
- Runtime or test/build scope
- Known usage restrictions

Licensing information should be reviewed when dependencies are upgraded or
their licensing terms change.

### Transitive Dependencies

The license of transitive dependencies must also be reviewed.

A dependency is not considered compliant solely because its direct dependency
has an allowed license.

All runtime dependency trees must comply with the runtime licensing policy.

### Policy Changes

Changes to this policy require maintainer approval.