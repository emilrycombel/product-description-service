package com.ercode.productdescription.adapter.in.web.dto;

import com.ercode.productdescription.domain.model.DescriptionTemplate;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Response body representing a template. */
public record TemplateResponse(UUID id, String name, String family, String body, OffsetDateTime createdAt) {

    public static TemplateResponse from(DescriptionTemplate template) {
        return new TemplateResponse(
                template.id(), template.name(), template.family(), template.body(), template.createdAt());
    }
}
