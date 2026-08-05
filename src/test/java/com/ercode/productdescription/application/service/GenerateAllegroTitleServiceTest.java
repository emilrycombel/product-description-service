package com.ercode.productdescription.application.service;

import com.ercode.productdescription.application.port.out.AllegroTitleGeneratorPort;
import com.ercode.productdescription.domain.model.AllegroTitle;
import com.ercode.productdescription.domain.model.AllegroTitleCommand;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GenerateAllegroTitleServiceTest {

    private final AllegroTitleGeneratorPort generator = mock(AllegroTitleGeneratorPort.class);

    @Test
    void passes_max_length_and_recomputes_char_count() {
        int maxLength = 75;
        GenerateAllegroTitleService service = new GenerateAllegroTitleService(generator, maxLength);

        // Model returns a deliberately wrong charCount (999); the service must recompute from `primary`.
        String primary = "Spigen GLAS.tR szkło hartowane iPhone 16 Pro 2 szt. z aplikatorem 9H";
        when(generator.generate(any(), eq(maxLength)))
                .thenReturn(new AllegroTitle(primary, List.of("wariant 1"), "keyword-first", 999));

        AllegroTitleCommand command = new AllegroTitleCommand(
                "Szkło Spigen", "Spigen", "Szkła", "iPhone 16 Pro", List.of("2 szt.", "9H"), "pl");

        AllegroTitle result = service.generate(command);

        assertThat(result.charCount()).isEqualTo(primary.length());
        assertThat(result.primary()).isEqualTo(primary);

        ArgumentCaptor<Integer> maxCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(generator).generate(any(), maxCaptor.capture());
        assertThat(maxCaptor.getValue()).isEqualTo(maxLength);
    }
}
