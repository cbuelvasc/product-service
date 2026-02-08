# CI Workflow

This document describes the **CI - Build, Test, JaCoCo & Docker** GitHub Actions workflow (`.github/workflows/ci.yml`): when it runs, which jobs and steps execute, and how they relate.

## Trigger

The workflow runs on:

| Event | Branches |
|-------|-----------|
| `push` | `develop` |
| `pull_request` | `master`, `develop` |

Only the **build-and-validate** job runs on every trigger. The **docker** job runs only on **push** to **develop** (not on pull requests).

## Pipeline overview

```mermaid
flowchart LR
    subgraph trigger["Trigger"]
        A[push → develop\nor\nPR → master/develop]
    end

    subgraph job1["Job: Compile, Test & JaCoCo"]
        B1[Checkout]
        B2[Set up JDK 21]
        B3[Compile, test, verify JaCoCo]
        B4[Validate JaCoCo thresholds]
        B5[Upload JaCoCo report]
        B6[Upload JaCoCo exec]
    end

    subgraph job2["Job: Build and push Docker"]
        C1[Checkout]
        C2[Set up Docker Buildx]
        C3[Log in to GHCR]
        C4[Extract metadata]
        C5[Build and push image]
    end

    trigger --> job1
    job1 --> job2
    B1 --> B2 --> B3 --> B4 --> B5 --> B6
    C1 --> C2 --> C3 --> C4 --> C5
```

**Note:** Job 2 runs only when the event is `push` and the branch is `develop`; otherwise the pipeline ends after Job 1.

## Detailed flow (sequence)

```mermaid
sequenceDiagram
    participant GH as GitHub
    participant Runner as Runner (ubuntu-latest)
    participant Maven as Maven
    participant GHCR as GitHub Container Registry

    GH->>Runner: Trigger (push develop / PR master|develop)

    rect rgb(240, 248, 255)
        Note over Runner: Job 1: Compile, Test & JaCoCo
        Runner->>Runner: Checkout repository
        Runner->>Runner: Set up JDK 21 (Temurin, cache: maven)
        Runner->>Maven: ./mvnw clean verify --no-transfer-progress
        Maven-->>Runner: Build + tests + JaCoCo (≥80% line/branch)
        Runner->>Runner: Validate JaCoCo coverage thresholds (log)
        Runner->>Runner: Upload jacoco-report artifact (if present)
        Runner->>Runner: Upload jacoco-exec artifact (if present)
    end

    alt push to develop
        rect rgb(255, 248, 240)
            Note over Runner: Job 2: Build and push Docker image
            Runner->>Runner: Checkout repository
            Runner->>Runner: Set up Docker Buildx
            Runner->>GHCR: Log in (GITHUB_TOKEN)
            Runner->>Runner: Extract metadata (tags: latest, sha)
            Runner->>GHCR: Build and push product-service image
        end
    else pull_request or other branch
        Note over Runner: Job 2 skipped
    end
```

## Job 1: Compile, Test & JaCoCo

| Step | Action | Description |
|------|--------|-------------|
| 1 | Checkout repository | `actions/checkout@v4` |
| 2 | Set up JDK 21 | `actions/setup-java@v4` — Java 21 (Temurin), Maven cache enabled |
| 3 | Compile, test and verify JaCoCo | `./mvnw clean verify --no-transfer-progress` with `MAVEN_OPTS: -Xmx1024m`. Fails if line or branch coverage &lt; 80%. |
| 4 | Validate JaCoCo coverage thresholds | Logs coverage info; report path `target/site/jacoco/` if present |
| 5 | Upload JaCoCo report | Uploads `target/site/jacoco/` as artifact `jacoco-report` (retention 7 days), `if: always()` |
| 6 | Upload JaCoCo execution data | Uploads `target/jacoco.exec` as artifact `jacoco-exec` (retention 7 days), `if: always()` |

**Result:** Build and tests must pass and coverage thresholds must be met. Artifacts are available in the run summary for debugging.

## Job 2: Build and push Docker image

**Condition:** `github.event_name == 'push' && github.ref == 'refs/heads/develop'`

| Step | Action | Description |
|------|--------|-------------|
| 1 | Checkout repository | `actions/checkout@v4` |
| 2 | Set up Docker Buildx | `docker/setup-buildx-action@v3` |
| 3 | Log in to GitHub Container Registry | `docker/login-action@v3` — registry `ghcr.io`, auth via `GITHUB_TOKEN` |
| 4 | Extract metadata for Docker | `docker/metadata-action@v5` — image `ghcr.io/<repo>/product-service`, tags: `latest` (on develop), plus SHA prefix |
| 5 | Build and push Docker image | `docker/build-push-action@v6` — build from repo root, push to GHCR, use GHA cache |

**Result:** Image is available at `ghcr.io/<OWNER>/<REPO>/product-service:latest` (and SHA-tagged) for pulls and deployments.

## Summary

| Item | Value |
|------|--------|
| Workflow file | `.github/workflows/ci.yml` |
| Runner | `ubuntu-latest` |
| Java | 21 (Eclipse Temurin) |
| Coverage enforcement | 80% line and branch (Maven JaCoCo in `verify`) |
| Docker registry | GitHub Container Registry (`ghcr.io`) |
| Docker image name | `ghcr.io/<repository>/product-service` |
| Docker tags on push to develop | `latest`, `<sha-prefix>` |
