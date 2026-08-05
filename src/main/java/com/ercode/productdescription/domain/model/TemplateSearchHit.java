package com.ercode.productdescription.domain.model;

import java.util.UUID;

/** A vector-search hit: the matched template's id and its similarity score. */
public record TemplateSearchHit(UUID templateId, double score) {
}
