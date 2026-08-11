package com.ercode.productdescription.adapter.out.llm;

import com.ercode.productdescription.adapter.out.llm.ai.DescriptionAiService;
import com.ercode.productdescription.domain.model.DescriptionItem;
import com.ercode.productdescription.domain.model.DescriptionSection;
import com.ercode.productdescription.domain.model.GeneratedDescription;
import com.ercode.productdescription.domain.model.ProductImage;
import com.ercode.productdescription.domain.model.ProductInput;
import dev.langchain4j.data.message.ImageContent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LangChain4jDescriptionAdapterTest {

    private final DescriptionAiService aiService = mock(DescriptionAiService.class);

    private final ProductInput inputWithImage = new ProductInput(
            "Spigen GLAS.tR", "Szkła", "Spigen", "pl", "supplier", "notes",
            List.of(ProductImage.ofUrl("https://example.com/a.png")), "SKU-1");

    @Test
    @SuppressWarnings("unchecked")
    void ignores_images_when_vision_disabled() {
        when(aiService.generate(anyString(), org.mockito.ArgumentMatchers.anyList())).thenReturn(valid());
        var adapter = new LangChain4jDescriptionAdapter(aiService, "model", false);

        adapter.generate(inputWithImage);

        ArgumentCaptor<List<ImageContent>> images = ArgumentCaptor.forClass(List.class);
        verify(aiService).generate(anyString(), images.capture());
        assertThat(images.getValue()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void sends_images_when_vision_enabled() {
        when(aiService.generate(anyString(), org.mockito.ArgumentMatchers.anyList())).thenReturn(valid());
        var adapter = new LangChain4jDescriptionAdapter(aiService, "model", true);

        adapter.generate(inputWithImage);

        ArgumentCaptor<List<ImageContent>> images = ArgumentCaptor.forClass(List.class);
        verify(aiService).generate(anyString(), images.capture());
        assertThat(images.getValue()).hasSize(1);
    }

    @Test
    void whitelists_model_output_to_provided_image_urls() {
        // Model returns one allowed image and one invented image URL.
        GeneratedDescription fromModel = new GeneratedDescription(List.of(
                DescriptionSection.of(
                        DescriptionItem.text("<h1>Title</h1>"),
                        DescriptionItem.image("https://example.com/a.png")),
                DescriptionSection.of(DescriptionItem.image("https://invented.example/x.png"))));
        when(aiService.generate(anyString(), org.mockito.ArgumentMatchers.anyList())).thenReturn(fromModel);
        var adapter = new LangChain4jDescriptionAdapter(aiService, "model", true);

        GeneratedDescription result = adapter.generate(inputWithImage);

        boolean anyInvented = result.sections().stream()
                .flatMap(s -> s.items().stream())
                .anyMatch(i -> "https://invented.example/x.png".equals(i.url()));
        assertThat(anyInvented).isFalse();
        boolean keptAllowed = result.sections().stream()
                .flatMap(s -> s.items().stream())
                .anyMatch(i -> "https://example.com/a.png".equals(i.url()));
        assertThat(keptAllowed).isTrue();
    }

    private static GeneratedDescription valid() {
        return new GeneratedDescription(List.of(
                DescriptionSection.of(DescriptionItem.text("<h1>Title</h1><p>Hook</p>")),
                DescriptionSection.of(DescriptionItem.text("<h2>Zalety</h2><ul><li>b</li></ul>"))));
    }
}
