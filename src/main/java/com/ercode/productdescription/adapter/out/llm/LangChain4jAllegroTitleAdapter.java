package com.ercode.productdescription.adapter.out.llm;

import com.ercode.productdescription.adapter.out.llm.ai.AllegroTitleAiService;
import com.ercode.productdescription.application.LlmUnavailableException;
import com.ercode.productdescription.application.port.out.AllegroTitleGeneratorPort;
import com.ercode.productdescription.domain.model.AllegroTitle;
import com.ercode.productdescription.domain.model.AllegroTitleCommand;
import org.springframework.stereotype.Component;

/** Driven adapter: generates an Allegro search-optimized title via langchain4j. */
@Component
public class LangChain4jAllegroTitleAdapter implements AllegroTitleGeneratorPort {

    private final AllegroTitleAiService aiService;

    public LangChain4jAllegroTitleAdapter(AllegroTitleAiService aiService) {
        this.aiService = aiService;
    }

    @Override
    public AllegroTitle generate(AllegroTitleCommand command, int maxLength) {
        try {
            return aiService.generate(maxLength, composeBrief(command));
        } catch (RuntimeException e) {
            throw new LlmUnavailableException("Allegro title generation failed", e);
        }
    }

    private static String composeBrief(AllegroTitleCommand c) {
        StringBuilder b = new StringBuilder();
        b.append("Language: ").append(c.language()).append('\n');
        b.append("Product name: ").append(c.productName()).append('\n');
        appendIfPresent(b, "Brand", c.brand());
        appendIfPresent(b, "Category", c.category());
        appendIfPresent(b, "Model/variant", c.model());
        if (!c.keyAttributes().isEmpty()) {
            b.append("Key attributes: ").append(String.join(", ", c.keyAttributes())).append('\n');
        }
        return b.toString();
    }

    private static void appendIfPresent(StringBuilder b, String label, String value) {
        if (value != null && !value.isBlank()) {
            b.append(label).append(": ").append(value).append('\n');
        }
    }
}
