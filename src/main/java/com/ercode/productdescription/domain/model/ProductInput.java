package com.ercode.productdescription.domain.model;

import java.util.List;

/**
 * The raw, mixed inputs a user provides for a product: supplier text, their own notes, and/or images.
 * A value object — normalized and immutable.
 */
public record ProductInput(
        String productName,
        String category,
        String brand,
        String language,
        String supplierText,
        String userText,
        List<ProductImage> images) {

    public ProductInput {
        images = images == null ? List.of() : List.copyOf(images);
        language = (language == null || language.isBlank()) ? "pl" : language;
    }

    public boolean hasImages() {
        return !images.isEmpty();
    }
}
