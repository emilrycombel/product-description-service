# 004 — Tasks

- [x] Deps: `langchain4j-pgvector` + `langchain4j-embeddings-bge-small-en-v15-q`.
- [x] `changes/003-templates.xml` (`description_template`); include in master; jOOQ regen.
- [x] `domain/model`: `DescriptionTemplate`, `NewTemplate`, `TemplateSearchHit`, `TemplateMatch`;
      `domain/TemplateNotFoundException`.
- [x] `application`: `port/in/TemplateUseCase`, `port/out/TemplateRepositoryPort`, `port/out/TemplateIndexPort`,
      `service/TemplateService`.
- [x] `adapter/out/persistence/TemplateJooqAdapter`; `adapter/out/rag/PgVectorTemplateIndexAdapter`.
- [x] `config/EmbeddingConfig` (model + store + retriever); `AiConfig` `.contentRetriever(...)`; `UseCaseConfig` bean.
- [x] `adapter/in/web/TemplateController` + DTOs; 404 handler for templates.
- [x] Tests: service + web-slice (offline) + integration RAG roundtrip (Docker-gated).
- [x] `api/openapi.yaml` template ops + schemas.
