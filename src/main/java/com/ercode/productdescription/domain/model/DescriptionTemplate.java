package com.ercode.productdescription.domain.model;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * A standard, reusable description template for a product family (e.g. "phone-case"). {@code body} is the
 * reusable guidance the LLM should follow for that family; {@code family} is the retrieval key. Identity and
 * creation time are assigned in the domain.
 */
public record DescriptionTemplate(UUID id, String name, String family, String body, OffsetDateTime createdAt) {

    public static DescriptionTemplate create(String name, String family, String body) {
        return new DescriptionTemplate(UUID.randomUUID(), name, family, body, OffsetDateTime.now(ZoneOffset.UTC));
    }
}
