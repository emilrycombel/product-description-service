package com.ercode.productdescription.adapter.out.persistence;

import com.ercode.productdescription.adapter.out.persistence.jooq.tables.records.ProductDescriptionRecord;
import com.ercode.productdescription.application.port.out.ProductDescriptionRepositoryPort;
import com.ercode.productdescription.domain.model.DescriptionScore;
import com.ercode.productdescription.domain.model.GeneratedDescription;
import com.ercode.productdescription.domain.model.ProductDescription;
import com.ercode.productdescription.domain.model.ProductImage;
import com.ercode.productdescription.domain.model.ProductInput;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ercode.productdescription.adapter.out.persistence.jooq.Tables.PRODUCT_DESCRIPTION;

/**
 * Driven adapter implementing the repository port with jOOQ typed SQL. Structured payloads are stored as
 * JSON text (the schema keeps those columns {@code clob}/{@code text} so offline codegen stays H2-safe).
 */
@Repository
public class ProductDescriptionJooqAdapter implements ProductDescriptionRepositoryPort {

    private final DSLContext dsl;
    private final JsonSupport json = new JsonSupport();

    public ProductDescriptionJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void save(ProductDescription description) {
        ProductInput input = description.input();
        GeneratedDescription generated = description.generated();
        String externalId = description.externalId();

        // Upsert semantics: one description per product. Replace any existing row for this externalId.
        dsl.transaction(config -> {
            DSLContext ctx = config.dsl();
            if (externalId != null && !externalId.isBlank()) {
                ctx.deleteFrom(PRODUCT_DESCRIPTION)
                        .where(PRODUCT_DESCRIPTION.EXTERNAL_ID.eq(externalId))
                        .execute();
            }
            ctx.insertInto(PRODUCT_DESCRIPTION)
                    .set(PRODUCT_DESCRIPTION.ID, description.id())
                    .set(PRODUCT_DESCRIPTION.EXTERNAL_ID, externalId)
                    .set(PRODUCT_DESCRIPTION.PRODUCT_NAME, input.productName())
                    .set(PRODUCT_DESCRIPTION.CATEGORY, input.category())
                    .set(PRODUCT_DESCRIPTION.BRAND, input.brand())
                    .set(PRODUCT_DESCRIPTION.LANGUAGE, input.language())
                    .set(PRODUCT_DESCRIPTION.SUPPLIER_TEXT, input.supplierText())
                    .set(PRODUCT_DESCRIPTION.USER_TEXT, input.userText())
                    .set(PRODUCT_DESCRIPTION.IMAGE_INPUTS, json.toJson(input.images()))
                    .set(PRODUCT_DESCRIPTION.RAW_JSON, json.toJson(generated))
                    .set(PRODUCT_DESCRIPTION.MODEL_NAME, description.modelName())
                    .set(PRODUCT_DESCRIPTION.STRUCTURE_VERSION, description.structureVersion())
                    .set(PRODUCT_DESCRIPTION.SCORE, description.score())
                    .set(PRODUCT_DESCRIPTION.CREATED_AT, description.createdAt())
                    .execute();
        });
    }

    @Override
    public void updateScore(UUID id, DescriptionScore score, OffsetDateTime scoredAt) {
        dsl.update(PRODUCT_DESCRIPTION)
                .set(PRODUCT_DESCRIPTION.SCORE, score.overall())
                .set(PRODUCT_DESCRIPTION.SCORE_ASSESSMENT, json.toJson(score))
                .set(PRODUCT_DESCRIPTION.SCORED_AT, scoredAt)
                .where(PRODUCT_DESCRIPTION.ID.eq(id))
                .execute();
    }

    @Override
    public Optional<ProductDescription> findById(UUID id) {
        ProductDescriptionRecord record = dsl.selectFrom(PRODUCT_DESCRIPTION)
                .where(PRODUCT_DESCRIPTION.ID.eq(id))
                .fetchOne();
        return Optional.ofNullable(record).map(this::toDomain);
    }

    @Override
    public Optional<ProductDescription> findByExternalId(String externalId) {
        ProductDescriptionRecord record = dsl.selectFrom(PRODUCT_DESCRIPTION)
                .where(PRODUCT_DESCRIPTION.EXTERNAL_ID.eq(externalId))
                .fetchAny();
        return Optional.ofNullable(record).map(this::toDomain);
    }

    private ProductDescription toDomain(ProductDescriptionRecord record) {
        List<ProductImage> images = record.getImageInputs() == null
                ? List.of()
                : json.listFromJson(record.getImageInputs(), ProductImage.class);

        ProductInput input = new ProductInput(
                record.getProductName(),
                record.getCategory(),
                record.getBrand(),
                record.getLanguage(),
                record.getSupplierText(),
                record.getUserText(),
                images,
                record.getExternalId());

        GeneratedDescription generated = json.fromJson(record.getRawJson(), GeneratedDescription.class);

        return new ProductDescription(
                record.getId(),
                input,
                generated,
                record.getModelName(),
                record.getScore(),
                record.getCreatedAt());
    }
}
