# 001 — Plan (How)

## Flow (hexagonal)
`DescriptionController` (adapter/in/web)
→ `GenerateDescriptionUseCase` (application/port/in) impl `GenerateDescriptionService`
→ `DescriptionGeneratorPort` (application/port/out) impl `LangChain4jDescriptionAdapter`
  (wraps `DescriptionAiService`, multimodal `UserMessage`, returns `GeneratedDescription` record)
→ build `ProductDescription` aggregate (domain-generated `id` + `createdAt`)
→ `ProductDescriptionRepositoryPort` impl `ProductDescriptionJooqAdapter` (`DSLContext` insert)
→ map to `GenerateDescriptionResponse`.

## Structural consistency (three layers)
1. `GeneratedDescription` record → langchain4j derives JSON schema; `response-format=json_schema` +
   `strict-json-schema=true`.
2. Domain invariant: `GeneratedDescription` validates completeness on construction; the service maps a
   violation to `422`.
3. `GenerationPrompts.SYSTEM` fixes the section order and "facts only from inputs" rules.

## Data
`product_description` (Liquibase `changes/001`), all H2-codegen-safe types; JSON payload columns are `text`
serialized via the injected Jackson `ObjectMapper` in the jOOQ adapter. `id`/`createdAt` set in the domain.

## Tests
- `GenerateDescriptionServiceTest` (mocked ports; asserts brief composition, image mapping, invariant → 422).
- `GeneratedDescriptionTest` (invariant rejects missing sections; round-trips JSON).
- `DescriptionControllerTest` (`@WebMvcTest`, validation + mapping).
- `GenerateDescriptionIntegrationTest` (Testcontainers pgvector + WireMock; asserts sections + persisted row).
