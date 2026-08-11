package com.ercode.productdescription.application.service;

import com.ercode.productdescription.application.port.in.GenerateDescriptionUseCase;
import com.ercode.productdescription.application.port.out.DescriptionGeneratorPort;
import com.ercode.productdescription.application.port.out.ProductDescriptionRepositoryPort;
import com.ercode.productdescription.domain.IncompleteDescriptionException;
import com.ercode.productdescription.domain.model.GeneratedDescription;
import com.ercode.productdescription.domain.model.ProductDescription;
import com.ercode.productdescription.domain.model.ProductInput;

import java.util.List;

/**
 * Orchestrates description generation: call the LLM, enforce the canonical-structure completeness rule,
 * then persist. Framework-free — wired as a bean in {@code config.UseCaseConfig}.
 */
public class GenerateDescriptionService implements GenerateDescriptionUseCase {

    private final DescriptionGeneratorPort generator;
    private final ProductDescriptionRepositoryPort repository;

    public GenerateDescriptionService(DescriptionGeneratorPort generator,
                                      ProductDescriptionRepositoryPort repository) {
        this.generator = generator;
        this.repository = repository;
    }

    @Override
    public ProductDescription generate(ProductInput input) {
        GeneratedDescription generated = generator.generate(input);

        List<String> violations = generated.violations();
        if (!violations.isEmpty()) {
            // Guarantees a valid Allegro layout even if the model produced a malformed structure.
            throw new IncompleteDescriptionException(violations);
        }

        ProductDescription description = ProductDescription.create(input, generated, generator.modelName());
        repository.save(description);
        return description;
    }
}
