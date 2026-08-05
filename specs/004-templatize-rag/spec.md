# 004 — Templatize + RAG (product-family templates)

## Why
Product families (e.g. phone cases) share the same "shape" of a good description across many models. Editors
want to define a **standard template once per family** and have generation reuse it automatically, so every
model in the family gets a consistent, on-brand description without re-specifying the pattern each time.

## User story
As a catalog editor, I create a reusable template for a family (e.g. "phone-case"), and when I later generate
a description for any product in that family, the service automatically fetches the best-matching template
(semantic search) and the LLM follows it.

## Capabilities
- Create / get / list templates (`name`, `family`, `body`).
- Semantic search over templates (`POST /api/v1/templates:search`).
- Auto-augmented generation: the existing `POST /api/v1/descriptions:generate` retrieves the best family
  template via RAG and injects it into the prompt.

## Acceptance criteria
1. Creating a template persists it (relational) and indexes it (vector) for retrieval.
2. Search returns the semantically closest templates, best first, each with a similarity score.
3. Generation is **backward compatible**: with no templates, retrieval returns nothing and output is unchanged.
4. `GET /api/v1/templates/{id}` returns 404 for an unknown id.
5. Ingest + search work with an in-process embedding model (offline); the RAG roundtrip is verified in CI
   against real pgvector.

## Design notes / non-goals
- Vector index is langchain4j's own `PgVectorEmbeddingStore` table (`product_templates`), separate from the
  jOOQ-managed `description_template` metadata table; linked by `templateId` segment metadata.
- Create does two writes (jOOQ insert + vector add) without a shared transaction — insert-then-index; a failed
  index leaves an un-indexed row (acceptable for now, re-index on demand later).
- Non-goals: template versioning/editing, delete, per-user templates.
