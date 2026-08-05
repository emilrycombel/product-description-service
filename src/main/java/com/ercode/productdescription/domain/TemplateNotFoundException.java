package com.ercode.productdescription.domain;

import java.util.UUID;

/** Raised when a template id does not exist. Mapped to HTTP 404. */
public class TemplateNotFoundException extends RuntimeException {

    public TemplateNotFoundException(UUID id) {
        super("Template not found: " + id);
    }
}
