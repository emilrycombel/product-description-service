package com.ercode.productdescription.adapter.out.llm.ai;

import com.ercode.productdescription.adapter.out.llm.prompt.ScoringPrompts;
import com.ercode.productdescription.domain.model.DescriptionScore;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * langchain4j AI service: LLM-as-judge. In = the description as JSON; out = the structured
 * {@link DescriptionScore} (langchain4j derives its JSON schema and parses the reply back).
 */
public interface DescriptionScorerAiService {

    @SystemMessage(ScoringPrompts.SYSTEM)
    DescriptionScore score(@UserMessage String descriptionJson);
}
