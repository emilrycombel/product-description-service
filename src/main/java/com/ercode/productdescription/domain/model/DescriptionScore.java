package com.ercode.productdescription.domain.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * The canonical quality assessment of a product description: an {@code overall} 1.0–10.0 score (one decimal),
 * per-dimension sub-scores, and a short summary. Like the description structure, the rubric is fixed and
 * versioned ({@link #RUBRIC_VERSION}) so scores are comparable across releases.
 *
 * <p>The compact constructor is deterministic and defensive: {@code overall} is clamped to [1, 10] and
 * rounded to one decimal regardless of what the model returns.
 */
public record DescriptionScore(BigDecimal overall, List<DimensionScore> dimensions, String summary) {

    /** Rubric version. Bump when the dimension set/weights change. */
    public static final String RUBRIC_VERSION = "1.0";

    /** The fixed rubric dimensions (also referenced by the scoring prompt). */
    public static final List<String> DIMENSIONS =
            List.of("COMPLETENESS", "FAITHFULNESS", "CLARITY", "PERSUASIVENESS", "SEO_ALLEGRO_FIT");

    public DescriptionScore {
        overall = Scores.clamp(overall);
        dimensions = dimensions == null ? List.of() : List.copyOf(dimensions);
    }

    public String rubricVersion() {
        return RUBRIC_VERSION;
    }
}
