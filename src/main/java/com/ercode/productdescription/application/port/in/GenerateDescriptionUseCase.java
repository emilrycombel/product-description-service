package com.ercode.productdescription.application.port.in;

import com.ercode.productdescription.domain.model.ProductDescription;
import com.ercode.productdescription.domain.model.ProductInput;

/** Inbound (driving) port: generate and persist a structured product description. */
public interface GenerateDescriptionUseCase {

    ProductDescription generate(ProductInput input);
}
