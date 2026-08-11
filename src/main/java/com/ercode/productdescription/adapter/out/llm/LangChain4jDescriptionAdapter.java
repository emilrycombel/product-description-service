package com.ercode.productdescription.adapter.out.llm;

import com.ercode.productdescription.adapter.out.llm.ai.DescriptionAiService;
import com.ercode.productdescription.application.LlmUnavailableException;
import com.ercode.productdescription.application.port.out.DescriptionGeneratorPort;
import com.ercode.productdescription.domain.model.GeneratedDescription;
import com.ercode.productdescription.domain.model.ProductImage;
import com.ercode.productdescription.domain.model.ProductInput;
import dev.langchain4j.data.message.ImageContent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/** Driven adapter: generates a canonical description via langchain4j against an OpenAI-compatible endpoint. */
@Component
public class LangChain4jDescriptionAdapter implements DescriptionGeneratorPort {

    private final DescriptionAiService aiService;
    private final String modelName;
    private final boolean visionEnabled;

    public LangChain4jDescriptionAdapter(
            DescriptionAiService aiService,
            @Value("${langchain4j.open-ai.chat-model.model-name}") String modelName,
            @Value("${app.llm.vision-enabled}") boolean visionEnabled) {
        this.aiService = aiService;
        this.modelName = modelName;
        this.visionEnabled = visionEnabled;
    }

    @Override
    public GeneratedDescription generate(ProductInput input) {
        String brief = composeBrief(input);
        // Only send image content to the model when vision is enabled; otherwise ignore it so non-vision
        // models don't break. (Image URLs are still listed in the brief so the model can lay them out.)
        List<ImageContent> visionImages = visionEnabled && input.hasImages()
                ? input.images().stream().map(LangChain4jDescriptionAdapter::toImageContent).toList()
                : List.of();
        try {
            GeneratedDescription generated = aiService.generate(brief, visionImages);
            // Whitelist: keep only IMAGE items whose URL was actually provided (drop any the model invented).
            return generated.withAllowedImages(input.imageUrls());
        } catch (RuntimeException e) {
            throw new LlmUnavailableException("Description generation failed", e);
        }
    }

    @Override
    public String modelName() {
        return modelName;
    }

    private static ImageContent toImageContent(ProductImage image) {
        return image.hasUrl()
                ? ImageContent.from(image.url())
                : ImageContent.from(image.base64(), image.mimeType());
    }

    private static String composeBrief(ProductInput input) {
        StringBuilder b = new StringBuilder();
        b.append("Write the description in language: ").append(input.language()).append(".\n");
        b.append("Product name: ").append(input.productName()).append('\n');
        appendIfPresent(b, "Category", input.category());
        appendIfPresent(b, "Brand", input.brand());
        appendIfPresent(b, "Supplier-provided text", input.supplierText());
        appendIfPresent(b, "User-provided notes", input.userText());

        var urls = input.imageUrls();
        if (urls.isEmpty()) {
            b.append("No product images are available; produce text-only sections.\n");
        } else {
            b.append("Available image URLs — use ONLY these exact URLs for IMAGE items, never invent URLs:\n");
            urls.forEach(u -> b.append(" - ").append(u).append('\n'));
        }
        return b.toString();
    }

    private static void appendIfPresent(StringBuilder b, String label, String value) {
        if (value != null && !value.isBlank()) {
            b.append(label).append(": ").append(value).append('\n');
        }
    }
}
