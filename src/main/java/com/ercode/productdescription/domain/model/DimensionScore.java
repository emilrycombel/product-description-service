package com.ercode.productdescription.domain.model;

import java.math.BigDecimal;

/** A single rubric dimension's 1–10 score with a short justification. */
public record DimensionScore(String dimension, BigDecimal score, String comment) {

    public DimensionScore {
        score = Scores.clamp(score);
    }
}
