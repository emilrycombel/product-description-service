package com.ercode.productdescription.domain.model;

import java.util.List;

/**
 * An Allegro description section (a "row"). Holds 1 or 2 items; a two-item section is exactly one TEXT and
 * one IMAGE laid out side by side (array order = left/right).
 */
public record DescriptionSection(List<DescriptionItem> items) {

    public DescriptionSection {
        items = items == null ? List.of() : List.copyOf(items);
    }

    public static DescriptionSection of(DescriptionItem... items) {
        return new DescriptionSection(List.of(items));
    }

    /** Whether this section satisfies Allegro's rules: 1–2 items, valid, and 2-item = one TEXT + one IMAGE. */
    public boolean isValid() {
        if (items.isEmpty() || items.size() > 2) {
            return false;
        }
        if (!items.stream().allMatch(DescriptionItem::isValid)) {
            return false;
        }
        if (items.size() == 2) {
            long texts = items.stream().filter(DescriptionItem::isText).count();
            long images = items.stream().filter(DescriptionItem::isImage).count();
            return texts == 1 && images == 1;
        }
        return true;
    }
}
