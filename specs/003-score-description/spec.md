# 003 — Score Product Description (1–10)

## Why
Editors need an objective, repeatable quality signal for a description — to triage which listings to improve
and to compare descriptions across a catalog. An LLM-as-judge scores against the same rubric the generator targets.

## User story
As a catalog editor, I score a description (one I generated earlier, or ad-hoc text) and get an overall
1–10 rating with per-dimension sub-scores and a short summary of the biggest improvement.

## Inputs
- Stored: the description id (`POST /api/v1/descriptions/{id}:score`).
- Ad-hoc: a `GeneratedDescription` in the body (`POST /api/v1/descriptions:score`).

## Output — canonical, versioned rubric
`overall` (1.0–10.0, one decimal) + `dimensions[]` (COMPLETENESS, FAITHFULNESS, CLARITY, PERSUASIVENESS,
SEO_ALLEGRO_FIT — each 1–10 with a comment) + `summary`. Named by `rubricVersion`.

## Acceptance criteria
1. Response conforms to `DescriptionScore` in `api/openapi.yaml`.
2. `overall` is always in [1.0, 10.0], one decimal — clamped/rounded server-side regardless of model output.
3. Stored scoring persists `score`, `score_assessment`, `scored_at`; scoring a missing id returns `404`.
4. Ad-hoc scoring persists nothing.
5. Testable offline (WireMock-stubbed judge); stored-path persistence verified in CI (Testcontainers).

## Non-goals
- Re-generating/improving a low-scoring description (future).
- Batch scoring.
