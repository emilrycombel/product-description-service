package com.ercode.productdescription.application.port.out;

import com.ercode.productdescription.domain.model.DescriptionScore;
import com.ercode.productdescription.domain.model.ProductDescription;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/** Outbound (driven) port: persist and retrieve product descriptions. */
public interface ProductDescriptionRepositoryPort {

    /** Persist a description. If it carries an {@code externalId}, upsert (one description per product). */
    void save(ProductDescription description);

    Optional<ProductDescription> findById(UUID id);

    Optional<ProductDescription> findByExternalId(String externalId);

    /** Persist a quality score (overall + full assessment) against an existing description. */
    void updateScore(UUID id, DescriptionScore score, OffsetDateTime scoredAt);
}
