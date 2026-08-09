package com.ercode.productdescription.adapter.out.persistence;

import com.ercode.productdescription.adapter.out.persistence.jooq.tables.records.DescriptionTemplateRecord;
import com.ercode.productdescription.application.port.out.TemplateRepositoryPort;
import com.ercode.productdescription.domain.model.DescriptionTemplate;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ercode.productdescription.adapter.out.persistence.jooq.Tables.DESCRIPTION_TEMPLATE;

/** Driven adapter: template metadata persistence with jOOQ typed SQL. */
@Repository
public class TemplateJooqAdapter implements TemplateRepositoryPort {

    private final DSLContext dsl;

    public TemplateJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void save(DescriptionTemplate template) {
        dsl.insertInto(DESCRIPTION_TEMPLATE)
                .set(DESCRIPTION_TEMPLATE.ID, template.id())
                .set(DESCRIPTION_TEMPLATE.NAME, template.name())
                .set(DESCRIPTION_TEMPLATE.FAMILY, template.family())
                .set(DESCRIPTION_TEMPLATE.BODY, template.body())
                .set(DESCRIPTION_TEMPLATE.CREATED_AT, template.createdAt())
                .execute();
    }

    @Override
    public Optional<DescriptionTemplate> findById(UUID id) {
        return Optional.ofNullable(
                        dsl.selectFrom(DESCRIPTION_TEMPLATE)
                                .where(DESCRIPTION_TEMPLATE.ID.eq(id))
                                .fetchOne())
                .map(TemplateJooqAdapter::toDomain);
    }

    @Override
    public List<DescriptionTemplate> findAll() {
        return dsl.selectFrom(DESCRIPTION_TEMPLATE)
                .orderBy(DESCRIPTION_TEMPLATE.CREATED_AT.desc())
                .fetch()
                .map(TemplateJooqAdapter::toDomain);
    }

    @Override
    public List<DescriptionTemplate> findByIds(List<UUID> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return dsl.selectFrom(DESCRIPTION_TEMPLATE)
                .where(DESCRIPTION_TEMPLATE.ID.in(ids))
                .fetch()
                .map(TemplateJooqAdapter::toDomain);
    }

    private static DescriptionTemplate toDomain(DescriptionTemplateRecord record) {
        return new DescriptionTemplate(
                record.getId(),
                record.getName(),
                record.getFamily(),
                record.getBody(),
                record.getCreatedAt());
    }
}
