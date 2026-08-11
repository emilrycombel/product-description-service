package com.ercode.productdescription.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/** Request body for {@code POST /api/v1/descriptions:generate}. */
public record GenerateDescriptionRequest(
        @NotBlank String productName,
        String category,
        String brand,
        String language,
        String supplierText,
        String userText,
        @Valid List<ImageInputDto> images,
        String externalId) {
}
