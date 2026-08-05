# 004 — Plan (How)

## Flow (hexagonal)
- Manage: `TemplateController` → `TemplateUseCase` impl `TemplateService` → `TemplateRepositoryPort`
  (`TemplateJooqAdapter`) + `TemplateIndexPort` (`PgVectorTemplateIndexAdapter`).
- Search: `TemplateService.search` → `TemplateIndexPort.search` (vector) → hydrate hits via
  `TemplateRepositoryPort.findByIds`, preserving order + score.
- Generation augmentation: `EmbeddingConfig` exposes a `ContentRetriever` over the same
  `EmbeddingStore` + `EmbeddingModel`; `AiConfig` wires it into `DescriptionAiService` via
  `.contentRetriever(...)` — auto-RAG per generate call.

## Stores
- Relational `description_template` (Liquibase `changes/003`, jOOQ) — id/name/family/body/created_at.
- Vector `product_templates` — langchain4j `PgVectorEmbeddingStore.datasourceBuilder().datasource(ds)...`,
  auto-creates its table + the `vector` extension; segment text = `name+family+body`, metadata =
  `{templateId, family}`; in-process `BgeSmallEnV15QuantizedEmbeddingModel` (384-dim, offline).

## Config knobs
`app.templates.retriever.max-results` (default 3), `app.templates.retriever.min-score` (default 0.6),
`app.templates.embedding.dimension` (384).

## Tests
- `TemplateServiceTest` (mocked ports): create saves+indexes; get→404; search hydrates + preserves order.
- `TemplateControllerTest` (standalone MockMvc + PathPatternParser): create 201, get 200/404, list, search 200.
- Extend `GenerateDescriptionIntegrationTest` (Docker): create a phone-case template → `:search` returns it
  with a score + row persisted; generation still succeeds with the retriever wired.
