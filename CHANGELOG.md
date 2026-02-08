# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

**Git Flow usage:**

- **`[Unreleased]`** — Changes merged into `develop` that are not yet released. Update this section as you merge features/fixes into `develop`.
- **Version sections** — When releasing from `develop` into `master`/`main`, move `[Unreleased]` entries into a new version block (e.g. `[1.1.0]`), add the release date, and tag the commit (e.g. `v1.1.0`).

---

## [Unreleased]

### Added

- CI job to build and push Docker image to GitHub Container Registry (GHCR) on push to `develop`.
- Explicit JaCoCo coverage validation step in CI; build fails if line or branch coverage is below 80%.
- Wiki folder with `Architecture.md`: hexagonal architecture, request flow, product comparison sequence diagram, design decisions, and project structure.
- Wiki document `CI-Workflow.md`: CI pipeline triggers, Mermaid pipeline and sequence diagrams, job and step descriptions (build/test/JaCoCo, Docker build and push).

### Changed

- CI workflow title updated to "CI - Build, Test, JaCoCo & Docker".
- README Docker section: instructions to download image from GHCR and consistent English wording.
- README: removed detailed layers and responsibilities section; added link to Architecture wiki and Wiki section referencing `wiki/Architecture.md` and `wiki/CI-Workflow.md`.

---

## [1.0.0] - Initial release

- REST API for product comparison with configurable fields.
- Hexagonal architecture (ports and adapters).
- Spring Boot 3.5, Java 21, H2, Flyway, Redis cache, Springdoc OpenAPI.
- JaCoCo coverage reporting (80% line/branch thresholds in `verify` phase).

[Unreleased]: https://github.com/OWNER/REPO/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/OWNER/REPO/releases/tag/v1.0.0
