package com.ercode.productdescription.adapter.out.llm;

import com.ercode.productdescription.adapter.out.llm.ai.DescriptionAiService;
import com.ercode.productdescription.domain.model.GeneratedDescription;
import com.ercode.productdescription.domain.model.ProductImage;
import com.ercode.productdescription.domain.model.ProductInput;
import com.ercode.productdescription.domain.model.SpecRow;
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
            List.of(ProductImage.ofUrl("https://example.com/a.png")));

    @Test
    @SuppressWarnings("unchecked")
    void ignores_images_when_vision_disabled() {
        when(aiService.generate(anyString(), org.mockito.ArgumentMatchers.anyList())).thenReturn(complete());
        var adapter = new LangChain4jDescriptionAdapter(aiService, "model", false);

        adapter.generate(inputWithImage);

        ArgumentCaptor<List<ImageContent>> images = ArgumentCaptor.forClass(List.class);
        verify(aiService).generate(anyString(), images.capture());
        assertThat(images.getValue()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void sends_images_when_vision_enabled() {
        when(aiService.generate(anyString(), org.mockito.ArgumentMatchers.anyList())).thenReturn(complete());
        var adapter = new LangChain4jDescriptionAdapter(aiService, "model", true);

        adapter.generate(inputWithImage);

        ArgumentCaptor<List<ImageContent>> images = ArgumentCaptor.forClass(List.class);
        verify(aiService).generate(anyString(), images.capture());
        assertThat(images.getValue()).hasSize(1);
    }

    private static GeneratedDescription complete() {
        return new GeneratedDescription("t", "h", List.of("b"), List.of("s"), "c",
                List.of(new SpecRow("l", "v")), "bb");
    }
}
