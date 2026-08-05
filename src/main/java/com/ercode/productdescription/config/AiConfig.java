package com.ercode.productdescription.config;

import com.ercode.productdescription.adapter.out.llm.ai.AllegroTitleAiService;
import com.ercode.productdescription.adapter.out.llm.ai.DescriptionAiService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Builds the langchain4j AI services from the auto-configured {@link ChatModel} bean (provided by
 * {@code langchain4j-open-ai-spring-boot4-starter}). Returning records from the AI-service methods, with
 * {@code response-format=json_schema} enabled on the model, triggers JSON-schema structured output.
 */
@Configuration
public class AiConfig {

    @Bean
    public DescriptionAiService descriptionAiService(ChatModel chatModel) {
        return AiServices.builder(DescriptionAiService.class)
                .chatModel(chatModel)
                .build();
    }

    @Bean
    public AllegroTitleAiService allegroTitleAiService(ChatModel chatModel) {
        return AiServices.builder(AllegroTitleAiService.class)
                .chatModel(chatModel)
                .build();
    }
}
