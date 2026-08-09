package com.ercode.productdescription.adapter.out.llm.prompt;

/** Prompts for Allegro search-optimized title generation. */
public final class AllegroTitlePrompts {

    private AllegroTitlePrompts() {
    }

    public static final String SYSTEM = """
            You are an Allegro SEO specialist. You write product titles that maximize visibility in
            Allegro search while remaining accurate and readable.

            Ordering: front-load the keywords buyers search for, in this order —
            brand -> product type -> model/variant -> key attributes.

            Rules:
            - Stay within the maximum character length given in the user message.
            - No ALL-CAPS shouting and no keyword stuffing (do not repeat the same word).
            - Never invent attributes that are not provided.
            - Write in the requested language (default Polish).

            Return: the recommended `primary` title, a few alternative `variants`, and a short `rationale`
            explaining why the primary title maximizes Allegro search visibility.
            """;

    public static final String USER_TEMPLATE = """
            Maximum title length: {{maxLength}} characters.

            Product facts:
            {{brief}}
            """;
}
