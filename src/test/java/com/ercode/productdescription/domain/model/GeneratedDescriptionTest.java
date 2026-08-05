package com.ercode.productdescription.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GeneratedDescriptionTest {

    @Test
    void complete_description_has_no_missing_sections() {
        GeneratedDescription d = complete();
        assertThat(d.isComplete()).isTrue();
        assertThat(d.missingSections()).isEmpty();
    }

    @Test
    void missing_and_empty_sections_are_reported_in_canonical_order() {
        GeneratedDescription d = new GeneratedDescription(
                "  ",            // blank title
                "hook",
                List.of(),        // empty bullets
                List.of("box"),
                "iPhone 16 Pro",
                List.of(),        // empty spec table
                "brand");

        assertThat(d.isComplete()).isFalse();
        assertThat(d.missingSections()).containsExactly("title", "benefitBullets", "specTable");
    }

    @Test
    void null_lists_are_normalized_to_empty() {
        GeneratedDescription d = new GeneratedDescription("t", "h", null, null, "c", null, "b");
        assertThat(d.benefitBullets()).isEmpty();
        assertThat(d.setContents()).isEmpty();
        assertThat(d.specTable()).isEmpty();
    }

    static GeneratedDescription complete() {
        return new GeneratedDescription(
                "Szkło hartowane Spigen GLAS.tR do iPhone 16 Pro (2 szt.)",
                "Ochrona klasy premium z łatwą aplikacją.",
                List.of("Twardość 9H", "Powłoka oleofobowa"),
                List.of("2x szkło hartowane", "Ramka aplikatora"),
                "Apple iPhone 16 Pro",
                List.of(new SpecRow("Twardość", "9H"), new SpecRow("Ilość", "2 szt.")),
                "Spigen to uznany producent akcesoriów ochronnych.");
    }
}
