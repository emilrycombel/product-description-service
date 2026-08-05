# 002 — Plan (How)

## Flow (hexagonal)
`AllegroTitleController` (adapter/in/web)
→ `GenerateAllegroTitleUseCase` (application/port/in) impl `GenerateAllegroTitleService`
→ `AllegroTitleGeneratorPort` (application/port/out) impl `LangChain4jAllegroTitleAdapter`
  (wraps `AllegroTitleAiService`, returns `AllegroTitle` record)
→ map to response.

The service passes the configured `maxLength` into the prompt and recomputes `charCount` from `primary`
(never trusts the model's count).

## Structured output
`AllegroTitle` record → langchain4j JSON schema; same `strict-json-schema` model settings as 001.
`AllegroTitlePrompts.SYSTEM` encodes the keyword-first ordering and anti-spam rules.

## Tests
- `GenerateAllegroTitleServiceTest` (mocked port; asserts charCount recomputed, maxLength passed).
- `AllegroTitleControllerTest` (`@WebMvcTest`, validation + mapping).
- Covered end-to-end by the integration test's WireMock stub as a secondary case (optional).
