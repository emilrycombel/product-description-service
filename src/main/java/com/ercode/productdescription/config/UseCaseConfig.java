package com.ercode.productdescription.config;

import com.ercode.productdescription.application.port.in.GenerateAllegroTitleUseCase;
import com.ercode.productdescription.application.port.in.GenerateDescriptionUseCase;
import com.ercode.productdescription.application.port.in.GetProductDescriptionUseCase;
import com.ercode.productdescription.application.port.in.ScoreDescriptionUseCase;
import com.ercode.productdescription.application.port.in.TemplateUseCase;
import com.ercode.productdescription.application.port.out.AllegroTitleGeneratorPort;
import com.ercode.productdescription.application.port.out.DescriptionGeneratorPort;
import com.ercode.productdescription.application.port.out.DescriptionScorerPort;
import com.ercode.productdescription.application.port.out.ProductDescriptionRepositoryPort;
import com.ercode.productdescription.application.port.out.TemplateIndexPort;
import com.ercode.productdescription.application.port.out.TemplateRepositoryPort;
import com.ercode.productdescription.application.service.GenerateAllegroTitleService;
import com.ercode.productdescription.application.service.GenerateDescriptionService;
import com.ercode.productdescription.application.service.GetProductDescriptionService;
import com.ercode.productdescription.application.service.ScoreDescriptionService;
import com.ercode.productdescription.application.service.TemplateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Composition root for the (framework-free) application use-case services, wiring them to the outbound
 * port beans provided by the adapters. Keeps the {@code application} layer free of Spring annotations.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public GenerateDescriptionUseCase generateDescriptionUseCase(
            DescriptionGeneratorPort descriptionGenerator,
            ProductDescriptionRepositoryPort repository) {
        return new GenerateDescriptionService(descriptionGenerator, repository);
    }

    @Bean
    public GenerateAllegroTitleUseCase generateAllegroTitleUseCase(
            AllegroTitleGeneratorPort titleGenerator,
            @Value("${app.allegro.title.max-length}") int maxLength) {
        return new GenerateAllegroTitleService(titleGenerator, maxLength);
    }

    @Bean
    public ScoreDescriptionUseCase scoreDescriptionUseCase(
            DescriptionScorerPort scorer,
            ProductDescriptionRepositoryPort repository) {
        return new ScoreDescriptionService(scorer, repository);
    }

    @Bean
    public GetProductDescriptionUseCase getProductDescriptionUseCase(
            ProductDescriptionRepositoryPort repository) {
        return new GetProductDescriptionService(repository);
    }

    @Bean
    public TemplateUseCase templateUseCase(
            TemplateRepositoryPort repository,
            TemplateIndexPort index) {
        return new TemplateService(repository, index);
    }
}
