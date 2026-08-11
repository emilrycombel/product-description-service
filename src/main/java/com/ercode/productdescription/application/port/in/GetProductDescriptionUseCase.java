package com.ercode.productdescription.application.port.in;

import com.ercode.productdescription.domain.model.ProductDescription;

/** Inbound (driving) port: fetch a product's stored description by its external (product) id. */
public interface GetProductDescriptionUseCase {

    ProductDescription getByExternalId(String externalId);
}
