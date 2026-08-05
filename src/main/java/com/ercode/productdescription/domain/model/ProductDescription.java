package com.ercode.productdescription.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Aggregate root: a persisted product description — the inputs it was generated from, the canonical
 * generated content, the model that produced it, an optional quality score (filled by feature 003),
 * and audit timestamp. Identity and creation time are assigned in the domain, not the database.
 */
public record ProductDescription(
        UUID id,
        ProductInput input,
        GeneratedDescription generated,
        String modelName,
        BigDecimal score,
        OffsetDateTime createdAt) {

    /** Create a new, unscored description with a fresh identity and creation timestamp. */
    public static ProductDescription create(ProductInput input, GeneratedDescription generated, String modelName) {
        return new ProductDescription(
                UUID.randomUUID(),
                input,
                generated,
                modelName,
                null,
                OffsetDateTime.now(ZoneOffset.UTC));
    }

    /** The canonical structure version of the generated content. */
    public String structureVersion() {
        return GeneratedDescription.STRUCTURE_VERSION;
    }
}
