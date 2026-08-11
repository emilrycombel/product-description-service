package com.ercode.productdescription.application.service;

import com.ercode.productdescription.application.port.out.DescriptionGeneratorPort;
import com.ercode.productdescription.application.port.out.ProductDescriptionRepositoryPort;
import com.ercode.productdescription.domain.IncompleteDescriptionException;
import com.ercode.productdescription.domain.model.DescriptionItem;
import com.ercode.productdescription.domain.model.DescriptionSection;
import com.ercode.productdescription.domain.model.GeneratedDescription;
import com.ercode.productdescription.domain.model.ProductDescription;
import com.ercode.productdescription.domain.model.ProductInput;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GenerateDescriptionServiceTest {

    private final DescriptionGeneratorPort generator = mock(DescriptionGeneratorPort.class);
    private final ProductDescriptionRepositoryPort repository = mock(ProductDescriptionRepositoryPort.class);
    private final GenerateDescriptionService service = new GenerateDescriptionService(generator, repository);

    private final ProductInput input = new ProductInput(
            "Spigen GLAS.tR iPhone 16 Pro", "Szkła", "Spigen", "pl",
            "supplier text", "user notes", List.of(), "SKU-123");

    @Test
    void valid_result_is_persisted_and_returned() {
        when(generator.modelName()).thenReturn("gpt-test");
        when(generator.generate(any())).thenReturn(valid());

        ProductDescription result = service.generate(input);

        assertThat(result.modelName()).isEqualTo("gpt-test");
        assertThat(result.structureVersion()).isEqualTo(GeneratedDescription.STRUCTURE_VERSION);
        assertThat(result.generated().isValid()).isTrue();
        assertThat(result.externalId()).isEqualTo("SKU-123");
        assertThat(result.id()).isNotNull();
        assertThat(result.createdAt()).isNotNull();
        verify(repository).save(result);
    }

    @Test
    void invalid_result_is_rejected_and_not_persisted() {
        when(generator.generate(any())).thenReturn(invalid());

        assertThatThrownBy(() -> service.generate(input))
                .isInstanceOf(IncompleteDescriptionException.class);

        verify(repository, never()).save(any());
    }

    private static GeneratedDescription valid() {
        return new GeneratedDescription(List.of(
                DescriptionSection.of(DescriptionItem.text("<h1>Title</h1><p>Hook</p>")),
                DescriptionSection.of(DescriptionItem.text("<h2>Zalety</h2><ul><li>bullet</li></ul>"))));
    }

    private static GeneratedDescription invalid() {
        return new GeneratedDescription(List.of());
    }
}
