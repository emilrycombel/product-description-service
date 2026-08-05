package com.ercode.productdescription.adapter.in.web.dto;

import jakarta.validation.constraints.AssertTrue;

/** Image input: exactly one of {@code url} or ({@code base64} + {@code mimeType}) must be supplied. */
public record ImageInputDto(String url, String base64, String mimeType) {

    @AssertTrue(message = "Provide exactly one of: url, or base64 together with mimeType")
    public boolean isSourceValid() {
        boolean hasUrl = url != null && !url.isBlank();
        boolean hasBase64 = base64 != null && !base64.isBlank();
        if (hasUrl == hasBase64) {
            return false; // both or neither
        }
        return !hasBase64 || (mimeType != null && !mimeType.isBlank());
    }
}
