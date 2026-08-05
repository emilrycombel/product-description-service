package com.ercode.productdescription.adapter.in.web.dto;

import com.ercode.productdescription.domain.model.DescriptionScore;
import com.ercode.productdescription.domain.model.ScoredDescription;

import java.util.UUID;

/** Response body for scoring a stored description ({@code POST /api/v1/descriptions/{id}:score}). */
public record ScoredDescriptionResponse(UUID id, String rubricVersion, DescriptionScore score) {

    public static ScoredDescriptionResponse from(ScoredDescription scored) {
        return new ScoredDescriptionResponse(scored.id(), scored.score().rubricVersion(), scored.score());
    }
}
