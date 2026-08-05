package com.ercode.productdescription.application.service;

import com.ercode.productdescription.application.port.in.ScoreDescriptionUseCase;
import com.ercode.productdescription.application.port.out.DescriptionScorerPort;
import com.ercode.productdescription.application.port.out.ProductDescriptionRepositoryPort;
import com.ercode.productdescription.domain.DescriptionNotFoundException;
import com.ercode.productdescription.domain.model.DescriptionScore;
import com.ercode.productdescription.domain.model.GeneratedDescription;
import com.ercode.productdescription.domain.model.ProductDescription;
import com.ercode.productdescription.domain.model.ScoredDescription;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Orchestrates scoring. For a stored description: fetch (404 if absent), score via the LLM judge, persist
 * the assessment. For an ad-hoc description: score and return without persisting. Framework-free — wired
 * as a bean in {@code config.UseCaseConfig}.
 */
public class ScoreDescriptionService implements ScoreDescriptionUseCase {

    private final DescriptionScorerPort scorer;
    private final ProductDescriptionRepositoryPort repository;

    public ScoreDescriptionService(DescriptionScorerPort scorer,
                                   ProductDescriptionRepositoryPort repository) {
        this.scorer = scorer;
        this.repository = repository;
    }

    @Override
    public ScoredDescription score(UUID id) {
        ProductDescription description = repository.findById(id)
                .orElseThrow(() -> new DescriptionNotFoundException(id));
        DescriptionScore score = scorer.score(description.generated());
        repository.updateScore(id, score, OffsetDateTime.now(ZoneOffset.UTC));
        return new ScoredDescription(id, score);
    }

    @Override
    public DescriptionScore score(GeneratedDescription description) {
        return scorer.score(description);
    }
}
