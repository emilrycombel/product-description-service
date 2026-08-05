package com.ercode.productdescription.application.service;

import com.ercode.productdescription.application.port.in.GenerateAllegroTitleUseCase;
import com.ercode.productdescription.application.port.out.AllegroTitleGeneratorPort;
import com.ercode.productdescription.domain.model.AllegroTitle;
import com.ercode.productdescription.domain.model.AllegroTitleCommand;

/**
 * Orchestrates Allegro title generation: pass the configured max length to the LLM and recompute
 * {@code charCount} from the returned primary title (never trust the model's own count).
 * Framework-free — wired as a bean in {@code config.UseCaseConfig}.
 */
public class GenerateAllegroTitleService implements GenerateAllegroTitleUseCase {

    private final AllegroTitleGeneratorPort generator;
    private final int maxLength;

    public GenerateAllegroTitleService(AllegroTitleGeneratorPort generator, int maxLength) {
        this.generator = generator;
        this.maxLength = maxLength;
    }

    @Override
    public AllegroTitle generate(AllegroTitleCommand command) {
        AllegroTitle title = generator.generate(command, maxLength);
        return AllegroTitle.of(title.primary(), title.variants(), title.rationale());
    }
}
