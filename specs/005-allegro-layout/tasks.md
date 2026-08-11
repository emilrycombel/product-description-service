# 005 — Tasks

- [x] `domain/model`: `ItemType`, `DescriptionItem`, `DescriptionSection`, rewrite `GeneratedDescription`
      (sections/items, `violations`/`isValid`, `withAllowedImages`, `STRUCTURE_VERSION=2.0`); `ProductInput`
      + `externalId`/`imageUrls`; `ProductDescription.externalId()`; remove `SpecRow`.
- [x] `domain`: `DescriptionNotFoundException.forExternalId`; `IncompleteDescriptionException(violations)`.
- [x] Liquibase `changes/004-allegro-layout.xml` (add `external_id`, drop 7 legacy columns, Postgres partial
      unique index) + include in master; `./gradlew jooqCodegen` regen.
- [x] `application`: reject `violations()` in `GenerateDescriptionService`; `GetProductDescriptionUseCase` +
      `GetProductDescriptionService`; `ProductDescriptionRepositoryPort.findByExternalId` + upsert `save`.
- [x] `adapter/out/persistence`: upsert-by-external_id (`dsl.transaction`), `findByExternalId`, `toDomain`
      reads `external_id`, drop legacy section writes.
- [x] `adapter/out/llm`: `GenerationPrompts` (Allegro sections + allowed HTML + images-from-list),
      `LangChain4jDescriptionAdapter` (brief lists URLs + `withAllowedImages`), `ScoringPrompts` (sections).
- [x] `adapter/in/web`: `GenerateDescriptionRequest`/`Response` + `externalId`; `WebMapper` passes it;
      `DescriptionController` GET `/api/v1/products/{externalId}/description` + inject use case;
      `UseCaseConfig` bean.
- [x] Tests: rewrite old-shape helpers to sections/items across domain/service/web/adapter tests; add
      section/item invariant, `withAllowedImages`, adapter whitelist, GET 200/404, upsert-one-row.
- [x] Docs: `api/openapi.yaml`, `README.md`, `memory/constitution.md` §5, this spec.
- [x] `./gradlew build`; commit + push to `claude/ecommerce-description-service-j9t1la`.
