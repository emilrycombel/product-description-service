package com.ercode.productdescription.domain.model;

import java.util.List;

/** Inputs for generating an Allegro search-optimized title. */
public record AllegroTitleCommand(
        String productName,
        String brand,
        String category,
        String model,
        List<String> keyAttributes,
        String language) {

    public AllegroTitleCommand {
        keyAttributes = keyAttributes == null ? List.of() : List.copyOf(keyAttributes);
        language = (language == null || language.isBlank()) ? "pl" : language;
    }
}
