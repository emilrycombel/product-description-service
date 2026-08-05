# 002 — Generate Allegro Search-Optimized Title

## Why
On Allegro, the product title is the single biggest lever on search visibility. Editors need titles that
front-load the keywords buyers actually search, fit the character limit, and avoid spammy formatting.

## User story
As a catalog editor, I provide product facts (name, brand, model, key attributes) and receive a
search-optimized Allegro title, alternative variants, and a short rationale.

## Inputs
`productName` (required), optional `brand`, `category`, `model`, `keyAttributes[]`, `language` (default `pl`).

## Output
`AllegroTitle`: `primary`, `variants[]`, `rationale`, `charCount` (of `primary`).

## Rules
- Keyword-first order: **brand → product type → model → key attributes**.
- Within the configured max length (`app.allegro.title.max-length`, default 75).
- No ALL-CAPS shouting, no keyword stuffing, no fabricated attributes.

## Acceptance criteria
1. Response conforms to `AllegroTitle` in `api/openapi.yaml`.
2. `primary` respects the configured max length; `charCount` equals its length.
3. Stateless (no persistence in this pass).
4. Testable offline (WireMock-stubbed LLM).

## Non-goals
- Persistence / history of generated titles.
- A/B ranking against live Allegro search.
