# 005 — Plan

## Domain (`domain/model`) — replace the description model
- `enum ItemType { TEXT, IMAGE }`.
- `DescriptionItem(ItemType type, String content, String url)` — `text(content)` / `image(url)` factories;
  `isText`/`isImage`/`isValid`.
- `DescriptionSection(List<DescriptionItem> items)` — `isValid()`: 1–2 items, all valid, a 2-item section is
  exactly one TEXT + one IMAGE.
- `GeneratedDescription(List<DescriptionSection> sections)` — `STRUCTURE_VERSION="2.0"`; `violations()` /
  `isValid()` (non-empty sections, each valid, ≥1 text item); `withAllowedImages(Set<String>)` filters IMAGE
  items to provided URLs and drops emptied sections.
- `ProductInput` gains `externalId` (nullable) + `imageUrls()`; `ProductDescription` exposes `externalId()`.
- `DescriptionNotFoundException.forExternalId(String)` (→404); `IncompleteDescriptionException(violations)`.
- Remove the orphaned `SpecRow`.

## Application
- `GenerateDescriptionService`: reject `generated.violations()` → 422; persist via `repository.save` (upsert).
- New `port/in/GetProductDescriptionUseCase` + `service/GetProductDescriptionService` (`getByExternalId` →
  404 if absent).
- `port/out/ProductDescriptionRepositoryPort`: add `findByExternalId(String)`; `save` upserts by `externalId`.

## Adapters
- `out/llm`: `GenerationPrompts.SYSTEM` rewritten for Allegro sections/items, allowed HTML subset, images only
  from the provided list. `LangChain4jDescriptionAdapter` lists available image URLs in the brief and
  whitelists the result with `withAllowedImages(input.imageUrls())`. `ScoringPrompts.SYSTEM` judges the
  sections layout.
- `out/persistence/ProductDescriptionJooqAdapter`: persist `raw_json` + `external_id`; `save` upserts by
  `external_id` inside `dsl.transaction` (delete-then-insert); `findByExternalId`; `toDomain` reads
  `external_id`.
- `in/web`: `GenerateDescriptionRequest` + `externalId`; `GenerateDescriptionResponse` + `externalId`;
  `DescriptionController` gains `GET /api/v1/products/{externalId}/description`.

## Liquibase (`changes/004-allegro-layout.xml`) + jOOQ regen
- `addColumn external_id`; drop the 7 legacy section columns; Postgres-only partial unique index on
  `external_id` (fenced `dbms="postgresql"`). Included in master after `003`. `./gradlew jooqCodegen` regen.

## Docs / specs
- `api/openapi.yaml`: `GeneratedDescription` = sections/items; `DescriptionSection` + `DescriptionItem`;
  `externalId` on request/response; new GET op.
- `README` (Allegro structure + GET endpoint + allowed tags + Allegro-hosted-image caveat);
  `memory/constitution.md` §5 (valid Allegro layout); this spec.

## Verification
- `./gradlew jooqCodegen` (offline) → schema regen; `./gradlew build` → offline unit/web-slice tests green.
- CI (Docker): integration test — generate with `externalId` twice → one row (upsert); GET-by-externalId
  returns it (200) / unknown id → 404.
