package com.ercode.productdescription.application.service;

import com.ercode.productdescription.application.port.in.GetProductDescriptionUseCase;
import com.ercode.productdescription.application.port.out.ProductDescriptionRepositoryPort;
import com.ercode.productdescription.domain.DescriptionNotFoundException;
import com.ercode.productdescription.domain.model.ProductDescription;

/** Fetches a product's current description by external id (404 if none). Framework-free. */
public class GetProductDescriptionService implements GetProductDescriptionUseCase {

    private final ProductDescriptionRepositoryPort repository;

    public GetProductDescriptionService(ProductDescriptionRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public ProductDescription getByExternalId(String externalId) {
        return repository.findByExternalId(externalId)
                .orElseThrow(() -> DescriptionNotFoundException.forExternalId(externalId));
    }
}
