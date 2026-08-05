# 001 — Generate Product Description

## Why
Catalog editors have raw, inconsistent inputs (supplier blurbs, personal notes, product photos) and need a
polished, **consistently structured** eCommerce description (Allegro "10/10" quality) without hand-writing
each one.

## User story
As a catalog editor, I submit a product's supplier text, my own notes, and/or product images, and receive a
structured Polish (configurable) description with a marketing hook, benefit bullets, set contents,
compatibility, a spec table, and a brand blurb — persisted so I can retrieve it later.

## Inputs
- `productName` (required), optional `category`, `brand`, `language` (default `pl`).
- Any combination of `supplierText`, `userText`, `images[]` (URL or base64+mimeType).

## Output — canonical structure (always identical shape/order)
`title` → `marketingHook` → `benefitBullets[]` → `setContents[]` → `compatibility` → `specTable[{label,value}]`
→ `brandBlurb`. Named by a `structureVersion`.

## Acceptance criteria
1. Response conforms to `GeneratedDescription` in `api/openapi.yaml`; **every** section present, in fixed order.
2. Facts derive only from supplied inputs — no invented specs.
3. The result is persisted (`product_description` row) with `raw_json`, `model_name`, `structure_version`;
   `score` is null (filled by feature 003).
4. Structural completeness is enforced server-side: an incomplete model result yields `422`, independent of
   whether the gateway honored strict JSON-schema mode.
5. The full flow is testable **offline** (Testcontainers Postgres + WireMock-stubbed LLM), no real key/network.

## Non-goals (future)
- Scoring 1–10 → feature `003`.
- Template retrieval / RAG for product families → feature `004`.
