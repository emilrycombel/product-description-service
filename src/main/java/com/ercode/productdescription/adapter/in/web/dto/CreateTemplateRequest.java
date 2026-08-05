package com.ercode.productdescription.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

/** Request body for creating a template. */
public record CreateTemplateRequest(
        @NotBlank String name,
        @NotBlank String family,
        @NotBlank String body) {
}
