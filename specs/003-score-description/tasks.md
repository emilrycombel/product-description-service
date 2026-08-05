# 003 — Tasks

- [x] `changes/002-description-score.xml` (widen `score`; add `score_assessment`, `scored_at`); include in master; jOOQ regen.
- [x] `domain/model`: `DimensionScore`, `DescriptionScore` (clamp/round, `RUBRIC_VERSION`, `DIMENSIONS`),
      `ScoredDescription`, `Scores`; `domain/DescriptionNotFoundException`.
- [x] `application`: `port/in/ScoreDescriptionUseCase`, `port/out/DescriptionScorerPort`, repo `updateScore`,
      `service/ScoreDescriptionService`.
- [x] `adapter/out/llm`: `DescriptionScorerAiService`, `ScoringPrompts`, `LangChain4jDescriptionScorerAdapter`.
- [x] `adapter/out/persistence`: `updateScore` impl.
- [x] `adapter/in/web`: `ScoreController`, `ScoredDescriptionResponse`; 404 handler; `AiConfig` + `UseCaseConfig` beans.
- [x] Tests: domain + service + web-slice (offline) + integration (Docker-gated).
- [x] `api/openapi.yaml` score operations + schemas.
