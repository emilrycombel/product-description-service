package com.ercode.productdescription.adapter.in.web.dto;

import com.ercode.productdescription.domain.model.GeneratedDescription;
import com.ercode.productdescription.domain.model.ProductDescription;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Response body for {@code POST /api/v1/descriptions:generate}. */
public record GenerateDescriptionResponse(
        UUID id,
        String externalId,
        String modelName,
        String structureVersion,
        OffsetDateTime createdAt,
        GeneratedDescription description) {

    public static GenerateDescriptionResponse from(ProductDescription pd) {
        return new GenerateDescriptionResponse(
                pd.id(),
                pd.externalId(),
                pd.modelName(),
                pd.structureVersion(),
                pd.createdAt(),
                pd.generated());
    }
}
