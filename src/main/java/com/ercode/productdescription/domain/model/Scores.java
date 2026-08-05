package com.ercode.productdescription.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Shared normalization for 1–10 scores: clamp to [1, 10] and round to one decimal place. */
final class Scores {

    static final BigDecimal MIN = BigDecimal.ONE;
    static final BigDecimal MAX = BigDecimal.TEN;

    private Scores() {
    }

    /** Clamp {@code value} to [1, 10] and round to one decimal (deterministic; null → 1.0). */
    static BigDecimal clamp(BigDecimal value) {
        BigDecimal v = value == null ? MIN : value;
        if (v.compareTo(MIN) < 0) {
            v = MIN;
        } else if (v.compareTo(MAX) > 0) {
            v = MAX;
        }
        return v.setScale(1, RoundingMode.HALF_UP);
    }
}
