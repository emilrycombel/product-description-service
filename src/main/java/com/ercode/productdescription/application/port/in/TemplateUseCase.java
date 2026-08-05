package com.ercode.productdescription.application.port.in;

import com.ercode.productdescription.domain.model.DescriptionTemplate;
import com.ercode.productdescription.domain.model.NewTemplate;
import com.ercode.productdescription.domain.model.TemplateMatch;

import java.util.List;
import java.util.UUID;

/** Inbound (driving) port: manage and semantically search product-family templates. */
public interface TemplateUseCase {

    /** Create a template: persist it and index it for retrieval. */
    DescriptionTemplate create(NewTemplate command);

    /** Fetch a template by id (404 if absent). */
    DescriptionTemplate get(UUID id);

    List<DescriptionTemplate> list();

    /** Semantic search over templates, best match first. */
    List<TemplateMatch> search(String query, int maxResults);
}
