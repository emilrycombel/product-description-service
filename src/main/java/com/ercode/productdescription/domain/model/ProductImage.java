package com.ercode.productdescription.domain.model;

/**
 * A product image supplied as either a URL or inline base64 bytes (with a MIME type).
 * Exactly-one-of validation happens at the web boundary; the domain simply carries the reference.
 */
public record ProductImage(String url, String base64, String mimeType) {

    public boolean hasUrl() {
        return url != null && !url.isBlank();
    }

    public boolean hasBase64() {
        return base64 != null && !base64.isBlank();
    }

    public static ProductImage ofUrl(String url) {
        return new ProductImage(url, null, null);
    }

    public static ProductImage ofBase64(String base64, String mimeType) {
        return new ProductImage(null, base64, mimeType);
    }
}
