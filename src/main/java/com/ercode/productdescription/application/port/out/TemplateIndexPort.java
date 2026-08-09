package com.ercode.productdescription.application.port.out;

import com.ercode.productdescription.domain.model.DescriptionTemplate;
import com.ercode.productdescription.domain.model.TemplateSearchHit;

import java.util.List;

/** Outbound (driven) port: the vector index for semantic template retrieval. */
public interface TemplateIndexPort {

    /** Embed and store the template for retrieval. */
    void index(DescriptionTemplate template);

    /** Semantic search returning matched template ids + scores, best first. */
    List<TemplateSearchHit> search(String query, int maxResults, double minScore);
}
