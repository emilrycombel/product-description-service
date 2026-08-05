package com.ercode.productdescription.domain;

import java.util.List;

/**
 * Raised when a generated description violates the canonical-structure completeness rule (one or more
 * required sections missing/empty). Mapped to HTTP 422 at the web boundary.
 */
public class IncompleteDescriptionException extends RuntimeException {

    private final List<String> missingSections;

    public IncompleteDescriptionException(List<String> missingSections) {
        super("Generated description is missing required sections: " + missingSections);
        this.missingSections = List.copyOf(missingSections);
    }

    public List<String> missingSections() {
        return missingSections;
    }
}
