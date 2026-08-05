# 002 — Tasks

- [x] `domain/model/AllegroTitle` (record).
- [x] `application/port/in/GenerateAllegroTitleUseCase`, `application/port/out/AllegroTitleGeneratorPort`.
- [x] `adapter/out/llm`: `AllegroTitleAiService`, `AllegroTitlePrompts`, `LangChain4jAllegroTitleAdapter`.
- [x] `application/service/GenerateAllegroTitleService` (recomputes charCount, injects maxLength).
- [x] `adapter/in/web`: `AllegroTitleController`, DTOs.
- [x] Tests: unit + web-slice (offline).
