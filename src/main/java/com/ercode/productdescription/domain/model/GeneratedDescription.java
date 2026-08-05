package com.ercode.productdescription.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The canonical, fixed-structure product description produced by the LLM.
 *
 * <p>Structural consistency is a first-class requirement: every generated description carries the exact
 * same section set in the same order. This record <em>is</em> that structure. The shape is named by
 * {@link #STRUCTURE_VERSION} and persisted with each result, so the structure is stable and traceable
 * across releases. Completeness (all sections populated) is a domain rule enforced via
 * {@link #missingSections()} — the application service rejects an incomplete result.
 */
public record GeneratedDescription(
        String title,
        String marketingHook,
        List<String> benefitBullets,
        List<String> setContents,
        String compatibility,
        List<SpecRow> specTable,
        String brandBlurb) {

    /** Canonical structure version. Bump when the section set/order changes. */
    public static final String STRUCTURE_VERSION = "1.0";

    public GeneratedDescription {
        benefitBullets = benefitBullets == null ? List.of() : List.copyOf(benefitBullets);
        setContents = setContents == null ? List.of() : List.copyOf(setContents);
        specTable = specTable == null ? List.of() : List.copyOf(specTable);
    }

    /**
     * Names the required sections that are missing or empty. An empty result means the description is
     * structurally complete. Order mirrors the canonical section order.
     */
    public List<String> missingSections() {
        List<String> missing = new ArrayList<>();
        if (isBlank(title)) missing.add("title");
        if (isBlank(marketingHook)) missing.add("marketingHook");
        if (benefitBullets.isEmpty()) missing.add("benefitBullets");
        if (setContents.isEmpty()) missing.add("setContents");
        if (isBlank(compatibility)) missing.add("compatibility");
        if (specTable.isEmpty()) missing.add("specTable");
        if (isBlank(brandBlurb)) missing.add("brandBlurb");
        return List.copyOf(missing);
    }

    public boolean isComplete() {
        return missingSections().isEmpty();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
