# 003 — Plan (How)

## Flow (hexagonal)
`ScoreController` (adapter/in/web)
→ `ScoreDescriptionUseCase` (application/port/in) impl `ScoreDescriptionService`
→ stored: `ProductDescriptionRepositoryPort.findById` (404 if absent) → `DescriptionScorerPort.score(generated)`
  → `ProductDescriptionRepositoryPort.updateScore(id, score, now)`; ad-hoc: `DescriptionScorerPort.score(...)` only
→ `DescriptionScorerPort` impl `LangChain4jDescriptionScorerAdapter` (serializes the description to JSON,
  `DescriptionScorerAiService` returns the structured `DescriptionScore`).

## Consistency
`DescriptionScore` is canonical + versioned (`RUBRIC_VERSION`); its compact constructor clamps `overall` to
[1,10] and rounds to one decimal, so the score is deterministic regardless of model output. The prompt
(`ScoringPrompts.SYSTEM`) fixes the dimension set and calibration.

## Data
`changes/002-description-score.xml`: widen `score` → `numeric(4,2)`; add `score_assessment` (JSON of the full
`DescriptionScore`) + `scored_at`. All H2-codegen-safe. `updateScore` in the jOOQ adapter reuses `JsonSupport`.

## Tests
- `DescriptionScoreTest` (clamp/round/range, list normalization).
- `ScoreDescriptionServiceTest` (stored persists via `updateScore`; missing id → `DescriptionNotFoundException`,
  no persist; ad-hoc returns without persisting).
- `ScoreControllerTest` (standalone MockMvc + PathPatternParser: stored 200, missing id → 404, ad-hoc 200).
- Extend `GenerateDescriptionIntegrationTest`: generate → `{id}:score` → assert `score`/`scored_at` persisted.
