package com.ercode.productdescription.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GeneratedDescriptionTest {

    @Test
    void valid_allegro_layout_has_no_violations() {
        GeneratedDescription d = complete();
        assertThat(d.isValid()).isTrue();
        assertThat(d.violations()).isEmpty();
    }

    @Test
    void empty_sections_are_reported() {
        GeneratedDescription d = new GeneratedDescription(List.of());
        assertThat(d.isValid()).isFalse();
        assertThat(d.violations()).contains("no sections", "no text content");
    }

    @Test
    void an_image_only_layout_is_missing_text() {
        GeneratedDescription d = new GeneratedDescription(List.of(
                DescriptionSection.of(DescriptionItem.image("https://a.example/1.jpg"))));
        assertThat(d.isValid()).isFalse();
        assertThat(d.violations()).contains("no text content");
    }

    @Test
    void a_two_item_section_must_be_one_text_and_one_image() {
        DescriptionSection twoTexts = DescriptionSection.of(
                DescriptionItem.text("<p>a</p>"), DescriptionItem.text("<p>b</p>"));
        assertThat(twoTexts.isValid()).isFalse();

        DescriptionSection textAndImage = DescriptionSection.of(
                DescriptionItem.text("<p>a</p>"), DescriptionItem.image("https://a.example/1.jpg"));
        assertThat(textAndImage.isValid()).isTrue();
    }

    @Test
    void a_section_may_hold_at_most_two_items() {
        DescriptionSection three = new DescriptionSection(List.of(
                DescriptionItem.text("<p>a</p>"),
                DescriptionItem.image("https://a.example/1.jpg"),
                DescriptionItem.text("<p>b</p>")));
        assertThat(three.isValid()).isFalse();
    }

    @Test
    void with_allowed_images_drops_urls_not_provided_and_empties() {
        GeneratedDescription d = new GeneratedDescription(List.of(
                DescriptionSection.of(DescriptionItem.text("<h1>Title</h1>")),
                DescriptionSection.of(
                        DescriptionItem.text("<p>benefit</p>"),
                        DescriptionItem.image("https://allowed.example/ok.jpg")),
                DescriptionSection.of(DescriptionItem.image("https://invented.example/nope.jpg"))));

        GeneratedDescription sanitized = d.withAllowedImages(Set.of("https://allowed.example/ok.jpg"));

        // invented single-image section dropped entirely; allowed image kept
        assertThat(sanitized.sections()).hasSize(2);
        assertThat(sanitized.sections().get(1).items()).hasSize(2);
        assertThat(sanitized.isValid()).isTrue();
        boolean anyInvented = sanitized.sections().stream()
                .flatMap(s -> s.items().stream())
                .anyMatch(i -> "https://invented.example/nope.jpg".equals(i.url()));
        assertThat(anyInvented).isFalse();
    }

    @Test
    void null_sections_are_normalized_to_empty() {
        GeneratedDescription d = new GeneratedDescription(null);
        assertThat(d.sections()).isEmpty();
    }

    static GeneratedDescription complete() {
        return new GeneratedDescription(List.of(
                DescriptionSection.of(DescriptionItem.text(
                        "<h1>Szkło hartowane Spigen GLAS.tR do iPhone 16 Pro (2 szt.)</h1>"
                                + "<p>Ochrona klasy premium z łatwą aplikacją.</p>")),
                DescriptionSection.of(
                        DescriptionItem.text("<h2>Zalety</h2><ul><li>Twardość 9H</li>"
                                + "<li>Powłoka oleofobowa</li></ul>"),
                        DescriptionItem.image("https://allegro.example/img/1.jpg")),
                DescriptionSection.of(DescriptionItem.text(
                        "<h2>Zawartość zestawu</h2><ul><li>2x szkło hartowane</li>"
                                + "<li>Ramka aplikatora</li></ul>")),
                DescriptionSection.of(DescriptionItem.text(
                        "<h2>Specyfikacja</h2><ul><li>Twardość: 9H</li><li>Ilość: 2 szt.</li></ul>"))));
    }
}
