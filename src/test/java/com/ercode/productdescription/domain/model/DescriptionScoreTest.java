package com.ercode.productdescription.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DescriptionScoreTest {

    @Test
    void overall_is_clamped_and_rounded_to_one_decimal() {
        assertThat(new DescriptionScore(new BigDecimal("12.34"), List.of(), "x").overall())
                .isEqualByComparingTo("10.0");
        assertThat(new DescriptionScore(new BigDecimal("0.2"), List.of(), "x").overall())
                .isEqualByComparingTo("1.0");
        assertThat(new DescriptionScore(new BigDecimal("7.46"), null, "x").overall())
                .isEqualByComparingTo("7.5");
    }

    @Test
    void null_overall_and_dimensions_are_normalized() {
        DescriptionScore s = new DescriptionScore(null, null, null);
        assertThat(s.overall()).isEqualByComparingTo("1.0");
        assertThat(s.dimensions()).isEmpty();
    }

    @Test
    void dimension_score_is_clamped() {
        assertThat(new DimensionScore("CLARITY", new BigDecimal("11"), "c").score())
                .isEqualByComparingTo("10.0");
        assertThat(new DimensionScore("CLARITY", new BigDecimal("-3"), "c").score())
                .isEqualByComparingTo("1.0");
    }

    @Test
    void rubric_is_fixed_and_versioned() {
        assertThat(DescriptionScore.RUBRIC_VERSION).isEqualTo("1.0");
        assertThat(DescriptionScore.DIMENSIONS)
                .containsExactly("COMPLETENESS", "FAITHFULNESS", "CLARITY", "PERSUASIVENESS", "SEO_ALLEGRO_FIT");
    }
}
