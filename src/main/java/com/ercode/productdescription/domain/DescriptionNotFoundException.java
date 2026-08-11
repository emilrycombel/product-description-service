package com.ercode.productdescription.domain;

import java.util.UUID;

/** Raised when a requested product description does not exist. Mapped to HTTP 404. */
public class DescriptionNotFoundException extends RuntimeException {

    public DescriptionNotFoundException(UUID id) {
        super("Product description not found: " + id);
    }

    private DescriptionNotFoundException(String message) {
        super(message);
    }

    public static DescriptionNotFoundException forExternalId(String externalId) {
        return new DescriptionNotFoundException("No product description for externalId: " + externalId);
    }
}
