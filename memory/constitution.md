# Constitution — Product Description Service

Non-negotiable principles for this codebase. Every change is checked against them.

## 1. Hexagonal (Ports & Adapters)
- `domain` holds the model **only** — entities, value objects, invariants — with **zero** framework imports
  (no Spring, jOOQ, langchain4j, Jackson).
- The **application** owns the hexagon boundary: **all ports live in `application`** — inbound `port.in`
  (use cases) and outbound `port.out` (driven contracts: persistence, LLM, retrieval). No repository
  interfaces in `domain` (we do not use the DDD variant — one rule, no mixing).
- `adapter` holds the driving (`in/web`) and driven (`out/persistence`, `out/llm`) implementations.
- Dependencies point strictly inward: `adapter → application(port) → domain`. Domain never imports outward.

## 2. Spec-driven
- `api/openapi.yaml` (OpenAPI 3.1) is the API contract **source of truth**.
- Every feature gets `specs/NNN-feature/{spec.md, plan.md, tasks.md}` **before** code.

## 3. Schema ownership
- **Liquibase** owns the database schema. No JPA/`ddl-auto`. jOOQ is the data-access layer (typed SQL).
- Every schema change is a Liquibase changeset. Postgres-only DDL (pgvector extension, `vector` columns,
  index methods) is fenced with `dbms="postgresql"` so offline jOOQ codegen (in-memory H2) skips it.

## 4. Offline-first
- `./gradlew jooqCodegen`, `compileJava`, and unit/web-slice tests must run with **no database and no Docker**.
- Tests requiring a real Postgres/pgvector or the LLM use Testcontainers + WireMock and are annotated
  `@Testcontainers(disabledWithoutDocker = true)` so `./gradlew build` still passes locally.

## 5. Canonical, versioned output structure
- Every generated description returns the **exact same section set in the same order** — never a
  per-response ad-hoc shape. Enforced at three layers: (a) the `GeneratedDescription` JSON schema at the LLM
  boundary (`strict-json-schema`), (b) a domain completeness invariant, (c) a fixed-order prompt.
- The shape is named by `GeneratedDescription.STRUCTURE_VERSION` and persisted with every row, so structure
  is stable and traceable across releases. Templatization constrains *content*, never the *structure*.

## 6. Toolchain
- JDK is selected **only** via the Gradle Java toolchain. Bumping Java (21 → 25/26) is a single-line change;
  never rely on the ambient JDK.

## 7. Configurable LLM
- LLM connection (base URL, API key, model) is configuration, never hard-coded — env-overridable so any
  OpenAI-compatible gateway works.
