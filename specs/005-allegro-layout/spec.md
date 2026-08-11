# 005 — Allegro row/section layout + save-by-externalId

## Why
The generated description must **follow Allegro's real description structure** so the output is directly
usable as an Allegro listing, and it must be **storable/retrievable per product** so a caller can fetch the
current description for a given product id without re-generating.

## User story
As a catalog editor, I generate a description for a product identified by its `externalId`; the result is an
Allegro-shaped layout (rows of text/image), it is saved against the product (replacing any prior one), and I
can later fetch it by `externalId`.

## Capabilities
- Generation produces an **Allegro layout**: `sections[]`, each with 1–2 `items`; an item is a TEXT item
  (HTML content) or an IMAGE item (URL). A two-item section is exactly one TEXT + one IMAGE side by side
  (array order = left/right) — giving the four row shapes: single text, single image, text+image, image+text.
- Save the description against an `externalId` (the product id) as an **upsert** — one per `externalId`.
- Fetch a product's current description: `GET /api/v1/products/{externalId}/description` (404 if none).

## Acceptance criteria
1. A generated description serializes to Allegro's `{"sections":[{"items":[...]}]}` shape.
2. Section rules are enforced: 1–2 items per section; a two-item section is exactly one TEXT and one IMAGE;
   at least one text item overall. A malformed layout is rejected with `422` (`violations` listed).
3. TEXT content is HTML using only `h1, h2, p, ul, ol, li, b` (prompted); a spec table is a `<ul>` of
   "Label: value".
4. IMAGE URLs come only from the request; any URL the model invents is dropped before persistence/response.
5. Generating with an `externalId` upserts — regenerating for the same `externalId` leaves exactly one row.
6. `GET /api/v1/products/{externalId}/description` returns the stored description (200) or `404` if none.

## Design notes / non-goals
- The description is persisted as `raw_json` (the sections) + `external_id`; the seven legacy per-section
  columns (title, marketing_hook, benefit_bullets, set_contents, compatibility, spec_table, brand_blurb) are
  dropped. `STRUCTURE_VERSION` bumps `1.0` → `2.0`.
- Upsert is a delete-then-insert by `external_id` inside a single jOOQ transaction. A Postgres partial unique
  index on `external_id` (`WHERE external_id IS NOT NULL`) guards against duplicates; it is fenced
  `dbms="postgresql"` so offline H2 codegen skips it.
- Content **ordering** (title/hook → benefits → set contents → compatibility → specs → brand) is *recommended*
  via the prompt, not a hard invariant — the hard guarantee is a **valid Allegro layout** (see constitution §5).
- Allegro serves images from its own CDN; supplying Allegro-hosted image URLs is recommended for live listings.
- Non-goals: multiple descriptions per product, partial section edits, Allegro API upload, image hosting.
