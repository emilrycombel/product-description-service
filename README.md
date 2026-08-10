# Product Description Service

Generates, scores, templatizes, and SEO-optimizes eCommerce product content using LLMs.
Built spec-driven, in a **Ports & Adapters (hexagonal)** architecture.

## Status — walking skeleton

Two LLM verticals implemented end-to-end:

| Vertical | Endpoint | Persistence |
| --- | --- | --- |
| Generate description (multimodal: supplier text + user text + images) | `POST /api/v1/descriptions:generate` | jOOQ → Postgres |
| Generate Allegro search-optimized title | `POST /api/v1/allegro-titles:generate` | stateless |
| Score a stored description (1–10, LLM-as-judge) | `POST /api/v1/descriptions/{id}:score` | jOOQ → Postgres |
| Score an ad-hoc description (1–10) | `POST /api/v1/descriptions:score` | stateless |
| Create a product-family template | `POST /api/v1/templates` | jOOQ + pgvector |
| Get / list templates | `GET /api/v1/templates/{id}`, `GET /api/v1/templates` | jOOQ → Postgres |
| Semantic template search (RAG) | `POST /api/v1/templates:search` | pgvector |

Scoring returns an overall 1.0–10.0 (one decimal) plus per-dimension sub-scores (completeness, faithfulness,
clarity, persuasiveness, Allegro SEO) and a summary — a canonical, `rubricVersion`-tagged structure.

**Templatize + RAG**: create standard templates per product family (e.g. `phone-case`); generation
auto-retrieves the best-matching family template (pgvector + in-process embeddings) and the LLM follows it.
Backward-compatible — with no templates, generation is unchanged.

## Stack

- **Java 21** (via Gradle toolchain — bump to 25/26 is a one-line change), **Spring Boot 4.0**, **Gradle** (wrapper 8.14.3)
- **jOOQ 3.19.28** (typed SQL) over **PostgreSQL + pgvector**; **Liquibase** owns the schema
- **langchain4j 1.18.1** against any **OpenAI-compatible** endpoint (configurable base URL / key / model)

## Architecture (hexagonal)

```
domain/       pure model + invariants (no framework imports)
application/  ports (port.in use-cases, port.out driven) + use-case services
adapter/      in/web (REST) · out/persistence (jOOQ) · out/llm (langchain4j)
config/       Spring wiring
```

`api/openapi.yaml` is the API contract source of truth. Feature specs live under `specs/`,
project principles under `memory/constitution.md`.

## jOOQ code generation (offline, no database)

jOOQ classes are generated from the Liquibase changelog via `LiquibaseDatabase` (in-memory H2) — no
running Postgres or Docker required. Postgres-only DDL (pgvector extension, `vector` columns) is fenced
with `dbms="postgresql"` so it applies at runtime but is skipped during codegen.

```bash
./gradlew jooqCodegen     # generate jOOQ sources from the changelog
./gradlew build           # compile + tests (Testcontainers tests auto-skip without Docker)
```

## Configuration

LLM connection is fully env-overridable:

| Env var | Property | Default |
| --- | --- | --- |
| `LLM_BASE_URL` | `langchain4j.open-ai.chat-model.base-url` | `https://api.openai.com/v1` |
| `LLM_API_KEY` | `langchain4j.open-ai.chat-model.api-key` | `changeme` |
| `LLM_MODEL` | `langchain4j.open-ai.chat-model.model-name` | `gpt-4o-mini` |
| `LLM_RESPONSE_FORMAT` | `langchain4j.open-ai.chat-model.response-format` | *(unset)* |
| `LLM_STRICT_SCHEMA` | `langchain4j.open-ai.chat-model.strict-json-schema` | `false` |
| `DB_URL` / `DB_USER` / `DB_PASSWORD` | datasource | local Postgres |

### Structured output & gateways

By default the service uses **prompt-based** structured output (langchain4j appends JSON-format instructions
and parses the reply), which works on **any** OpenAI-compatible endpoint. Only enable OpenAI's `json_schema`
mode on endpoints that actually support it (real OpenAI, Azure OpenAI):

```bash
LLM_RESPONSE_FORMAT=json_schema LLM_STRICT_SCHEMA=true
```

If a gateway that does not support `json_schema` is used with it enabled, it returns an empty message and
generation fails with `Failed to parse null …` — leave these unset for such gateways. (Output structure is
still enforced server-side by the domain completeness check + fixed prompts.) A `null`-content failure can
also mean images were sent to a **non-vision** model — test text-only first and use a vision-capable model.

## Run locally

```bash
docker compose up -d      # Postgres + pgvector on :5432
LLM_BASE_URL=... LLM_API_KEY=... ./gradlew bootRun
```
