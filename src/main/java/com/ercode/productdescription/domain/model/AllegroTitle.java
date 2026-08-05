package com.ercode.productdescription.domain.model;

import java.util.List;

/**
 * An Allegro search-optimized title: the recommended {@code primary} title, alternative {@code variants},
 * a short {@code rationale}, and the character count of the primary. {@code charCount} is always derived
 * from {@code primary} (never trusted from the model) via {@link #of}.
 */
public record AllegroTitle(String primary, List<String> variants, String rationale, int charCount) {

    public AllegroTitle {
        variants = variants == null ? List.of() : List.copyOf(variants);
    }

    /** Build a title with {@code charCount} computed from {@code primary}. */
    public static AllegroTitle of(String primary, List<String> variants, String rationale) {
        return new AllegroTitle(primary, variants, rationale, primary == null ? 0 : primary.length());
    }
}
