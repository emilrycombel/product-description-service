package com.ercode.productdescription.application.service;

import com.ercode.productdescription.application.port.out.DescriptionScorerPort;
import com.ercode.productdescription.application.port.out.ProductDescriptionRepositoryPort;
import com.ercode.productdescription.domain.DescriptionNotFoundException;
import com.ercode.productdescription.domain.model.DescriptionScore;
import com.ercode.productdescription.domain.model.DimensionScore;
import com.ercode.productdescription.domain.model.GeneratedDescription;
import com.ercode.productdescription.domain.model.ProductDescription;
import com.ercode.productdescription.domain.model.DescriptionItem;
import com.ercode.productdescription.domain.model.DescriptionSection;
import com.ercode.productdescription.domain.model.ProductInput;
import com.ercode.productdescription.domain.model.ScoredDescription;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ScoreDescriptionServiceTest {

    private final DescriptionScorerPort scorer = mock(DescriptionScorerPort.class);
    private final ProductDescriptionRepositoryPort repository = mock(ProductDescriptionRepositoryPort.class);
    private final ScoreDescriptionService service = new ScoreDescriptionService(scorer, repository);

    @Test
    void stored_scoring_scores_and_persists() {
        UUID id = UUID.randomUUID();
        ProductDescription stored = ProductDescription.create(input(), complete(), "gpt-test");
        when(repository.findById(id)).thenReturn(Optional.of(stored));
        DescriptionScore score = new DescriptionScore(
                new BigDecimal("8.5"),
                List.of(new DimensionScore("CLARITY", BigDecimal.valueOf(9), "clear")),
                "solid");
        when(scorer.score(stored.generated())).thenReturn(score);

        ScoredDescription result = service.score(id);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.score().overall()).isEqualByComparingTo("8.5");
        verify(repository).updateScore(eq(id), eq(score), any(OffsetDateTime.class));
    }

    @Test
    void missing_id_throws_and_does_not_persist_or_score() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.score(id)).isInstanceOf(DescriptionNotFoundException.class);

        verify(repository, never()).updateScore(any(), any(), any());
        verifyNoInteractions(scorer);
    }

    @Test
    void adhoc_scoring_returns_without_persisting() {
        DescriptionScore score = new DescriptionScore(BigDecimal.valueOf(6), List.of(), "meh");
        GeneratedDescription description = complete();
        when(scorer.score(description)).thenReturn(score);

        DescriptionScore result = service.score(description);

        assertThat(result).isEqualTo(score);
        verify(repository, never()).updateScore(any(), any(), any());
        verify(repository, never()).save(any());
    }

    private static ProductInput input() {
        return new ProductInput("x", null, null, "pl", null, null, List.of(), null);
    }

    private static GeneratedDescription complete() {
        return new GeneratedDescription(List.of(
                DescriptionSection.of(DescriptionItem.text("<h1>Title</h1><p>Hook</p>")),
                DescriptionSection.of(DescriptionItem.text("<h2>Zalety</h2><ul><li>b</li></ul>"))));
    }
}
