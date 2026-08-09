package com.ercode.productdescription.adapter.out.persistence;

import com.ercode.productdescription.config.JooqConfig;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.junit.jupiter.api.Test;

import static com.ercode.productdescription.adapter.out.persistence.jooq.Tables.PRODUCT_DESCRIPTION;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Offline guard for the jOOQ identifier-case bug (no DB needed — jOOQ renders SQL without a connection).
 *
 * <p>The offline H2 codegen emits UPPERCASE identifiers, so against a lower-case Postgres schema jOOQ must
 * render names lower-case and unquoted (see {@link JooqConfig}). This test applies the exact customizer the
 * app uses and asserts the emitted SQL is {@code delete from product_description} — not
 * {@code delete from "PRODUCT_DESCRIPTION"}, which fails at runtime with "relation does not exist".
 */
class JooqRenderingTest {

    @Test
    void renders_table_names_lowercase_and_unquoted_for_postgres() {
        DefaultConfiguration configuration = new DefaultConfiguration();
        configuration.set(SQLDialect.POSTGRES);
        new JooqConfig().jooqRenderCustomizer().customize(configuration);

        DSLContext dsl = DSL.using(configuration);
        String sql = dsl.deleteFrom(PRODUCT_DESCRIPTION).getSQL();

        assertThat(sql)
                .doesNotContain("\"")
                .isEqualTo("delete from product_description");
    }
}
