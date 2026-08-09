package com.ercode.productdescription.adapter.out.llm.ai;

import com.ercode.productdescription.adapter.out.llm.prompt.GenerationPrompts;
import com.ercode.productdescription.domain.model.GeneratedDescription;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

import java.util.List;

/**
 * langchain4j AI service: multimodal in (brief text + images), structured out (the canonical
 * {@link GeneratedDescription} record — langchain4j derives its JSON schema and parses the reply back).
 */
public interface DescriptionAiService {

    @SystemMessage(GenerationPrompts.SYSTEM)
    GeneratedDescription generate(@UserMessage String brief, @UserMessage List<ImageContent> images);
}
