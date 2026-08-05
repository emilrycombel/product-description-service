package com.ercode.productdescription.domain;

import java.util.UUID;

/** Raised when scoring is requested for a description id that does not exist. Mapped to HTTP 404. */
public class DescriptionNotFoundException extends RuntimeException {

    public DescriptionNotFoundException(UUID id) {
        super("Product description not found: " + id);
    }
}
