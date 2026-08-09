package com.ercode.productdescription.application.port.out;

import com.ercode.productdescription.domain.model.GeneratedDescription;
import com.ercode.productdescription.domain.model.ProductInput;

/** Outbound (driven) port: produce a canonical description from mixed inputs via an LLM. */
public interface DescriptionGeneratorPort {

    GeneratedDescription generate(ProductInput input);

    /** The model that backs this generator (persisted with each result). */
    String modelName();
}
