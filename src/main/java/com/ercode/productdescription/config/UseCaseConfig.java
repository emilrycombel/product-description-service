package com.ercode.productdescription.config;

import com.ercode.productdescription.application.port.in.GenerateAllegroTitleUseCase;
import com.ercode.productdescription.application.port.in.GenerateDescriptionUseCase;
import com.ercode.productdescription.application.port.out.AllegroTitleGeneratorPort;
import com.ercode.productdescription.application.port.out.DescriptionGeneratorPort;
import com.ercode.productdescription.application.port.out.ProductDescriptionRepositoryPort;
import com.ercode.productdescription.application.service.GenerateAllegroTitleService;
import com.ercode.productdescription.application.service.GenerateDescriptionService;
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
}
