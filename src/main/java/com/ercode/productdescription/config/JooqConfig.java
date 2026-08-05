package com.ercode.productdescription.config;

import org.jooq.conf.RenderNameCase;
import org.jooq.conf.RenderQuotedNames;
import org.springframework.boot.jooq.autoconfigure.DefaultConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * jOOQ render settings. Offline codegen runs against in-memory H2, which folds unquoted identifiers to
 * UPPERCASE, so the generated code carries upper-case names. At Postgres runtime we therefore render
 * names lower-case and unquoted, matching the actual (lower-case) Postgres columns.
 *
 * <p>Applied via a {@link DefaultConfigurationCustomizer} (rather than a {@code Settings} bean) so we
 * mutate the auto-configured settings in place and never make the {@code ObjectProvider<Settings>} in
 * Spring Boot's jOOQ auto-configuration ambiguous.
 */
@Configuration
public class JooqConfig {

    @Bean
    public DefaultConfigurationCustomizer jooqRenderCustomizer() {
        return configuration -> configuration.settings()
                .withRenderQuotedNames(RenderQuotedNames.NEVER)
                .withRenderNameCase(RenderNameCase.LOWER);
    }
}
