package com.ercode.productdescription.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The canonical product description, modeled on Allegro's real format: an ordered list of
 * {@link DescriptionSection sections}, each holding 1–2 {@link DescriptionItem items} (single text, single
 * image, or text+image side by side). Serializes to Allegro's {@code {"sections":[{"items":[...]}]}} shape,
 * so it is directly usable as an Allegro description payload.
 *
 * <p>Structural consistency is enforced as <em>validity</em> ({@link #isValid()} / {@link #violations()}):
 * a well-formed Allegro description with at least one text item; the recommended content ordering
 * (title → benefits → set contents → compatibility → specs → brand) is guided by the generation prompt.
 */
public record GeneratedDescription(List<DescriptionSection> sections) {

    /** Canonical structure version. 2.0 = Allegro sections/items (replaced the 1.0 fixed-fields shape). */
    public static final String STRUCTURE_VERSION = "2.0";

    public GeneratedDescription {
        sections = sections == null ? List.of() : List.copyOf(sections);
    }

    /** Human-readable structural problems; empty when the description is a valid Allegro layout. */
    public List<String> violations() {
        List<String> problems = new ArrayList<>();
        if (sections.isEmpty()) {
            problems.add("no sections");
        }
        for (int i = 0; i < sections.size(); i++) {
            if (!sections.get(i).isValid()) {
                problems.add("section " + i + " is not a valid Allegro section (1-2 items; a 2-item section "
                        + "must be exactly one text and one image)");
            }
        }
        boolean hasText = sections.stream()
                .flatMap(s -> s.items().stream())
                .anyMatch(DescriptionItem::isText);
        if (!hasText) {
            problems.add("no text content");
        }
        return List.copyOf(problems);
    }

    public boolean isValid() {
        return violations().isEmpty();
    }

    /**
     * Returns a copy keeping only IMAGE items whose URL is in {@code allowedUrls} (dropping any the model
     * invented), and dropping sections left empty. Text items are untouched.
     */
    public GeneratedDescription withAllowedImages(Set<String> allowedUrls) {
        List<DescriptionSection> kept = new ArrayList<>();
        for (DescriptionSection section : sections) {
            List<DescriptionItem> items = section.items().stream()
                    .filter(item -> item.isText() || allowedUrls.contains(item.url()))
                    .toList();
            if (!items.isEmpty()) {
                kept.add(new DescriptionSection(items));
            }
        }
        return new GeneratedDescription(kept);
    }
}
