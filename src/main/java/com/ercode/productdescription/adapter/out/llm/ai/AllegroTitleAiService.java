package com.ercode.productdescription.adapter.out.llm.ai;

import com.ercode.productdescription.adapter.out.llm.prompt.AllegroTitlePrompts;
import com.ercode.productdescription.domain.model.AllegroTitle;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * langchain4j AI service: structured out ({@link AllegroTitle}). The max length and product brief are
 * injected into the user-message template.
 */
public interface AllegroTitleAiService {

    @SystemMessage(AllegroTitlePrompts.SYSTEM)
    @UserMessage(AllegroTitlePrompts.USER_TEMPLATE)
    AllegroTitle generate(@V("maxLength") int maxLength, @V("brief") String brief);
}
