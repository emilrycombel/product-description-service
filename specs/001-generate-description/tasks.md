# 001 — Tasks

- [x] `changes/001-product-description.xml` (H2-codegen-safe columns; `structure_version`; nullable `score`).
- [x] `domain/model`: `ProductInput`, `ProductImage`, `SpecRow`, `GeneratedDescription` (+completeness invariant,
      `STRUCTURE_VERSION`), `ProductDescription` aggregate.
- [x] `application/port/in/GenerateDescriptionUseCase`, `application/port/out/DescriptionGeneratorPort`,
      `application/port/out/ProductDescriptionRepositoryPort`.
- [x] `adapter/out/llm`: `DescriptionAiService`, `GenerationPrompts`, `LangChain4jDescriptionAdapter`; `config/AiConfig`.
- [x] `adapter/out/persistence`: `ProductDescriptionJooqAdapter` + mapper.
- [x] `application/service/GenerateDescriptionService`.
- [x] `adapter/in/web`: `DescriptionController`, DTOs, `ApiExceptionHandler`.
- [x] Tests: unit + web-slice (offline) + integration (Docker-gated).
