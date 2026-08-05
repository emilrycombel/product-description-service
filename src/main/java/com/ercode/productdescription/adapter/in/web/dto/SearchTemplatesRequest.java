package com.ercode.productdescription.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

/** Request body for semantic template search. {@code maxResults} defaults to 5 when omitted. */
public record SearchTemplatesRequest(@NotBlank String query, Integer maxResults) {

    public int maxResultsOrDefault() {
        return maxResults == null || maxResults <= 0 ? 5 : maxResults;
    }
}
