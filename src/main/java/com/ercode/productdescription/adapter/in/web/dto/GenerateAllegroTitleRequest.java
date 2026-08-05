package com.ercode.productdescription.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/** Request body for {@code POST /api/v1/allegro-titles:generate}. */
public record GenerateAllegroTitleRequest(
        @NotBlank String productName,
        String brand,
        String category,
        String model,
        List<String> keyAttributes,
        String language) {
}
