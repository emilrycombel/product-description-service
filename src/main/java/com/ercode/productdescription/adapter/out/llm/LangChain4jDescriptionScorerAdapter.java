package com.ercode.productdescription.adapter.out.llm;

import com.ercode.productdescription.adapter.out.llm.ai.DescriptionScorerAiService;
import com.ercode.productdescription.application.LlmUnavailableException;
import com.ercode.productdescription.application.port.out.DescriptionScorerPort;
import com.ercode.productdescription.domain.model.DescriptionScore;
import com.ercode.productdescription.domain.model.GeneratedDescription;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/** Driven adapter: scores a description via langchain4j (LLM-as-judge) against an OpenAI-compatible endpoint. */
@Component
public class LangChain4jDescriptionScorerAdapter implements DescriptionScorerPort {

    private final DescriptionScorerAiService aiService;
    private final JsonMapper mapper = JsonMapper.builder().build();

    public LangChain4jDescriptionScorerAdapter(DescriptionScorerAiService aiService) {
        this.aiService = aiService;
    }

    @Override
    public DescriptionScore score(GeneratedDescription description) {
        try {
            return aiService.score(mapper.writeValueAsString(description));
        } catch (RuntimeException e) {
            throw new LlmUnavailableException("Description scoring failed", e);
        }
    }
}
