package com.ercode.productdescription.domain.model;

import java.util.UUID;

/** The result of scoring a stored description: the description id and its assessment. */
public record ScoredDescription(UUID id, DescriptionScore score) {
}
