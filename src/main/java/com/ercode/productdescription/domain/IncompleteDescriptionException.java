package com.ercode.productdescription.domain;

import java.util.List;

/**
 * Raised when a generated description is not a valid Allegro layout (see
 * {@code GeneratedDescription.violations()}). Mapped to HTTP 422 at the web boundary.
 */
public class IncompleteDescriptionException extends RuntimeException {

    private final List<String> violations;

    public IncompleteDescriptionException(List<String> violations) {
        super("Generated description is not a valid Allegro layout: " + violations);
        this.violations = List.copyOf(violations);
    }

    public List<String> violations() {
        return violations;
    }
}
