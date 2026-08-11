package com.ercode.productdescription.domain.model;

/**
 * A single Allegro description item. TEXT items carry HTML {@code content} (Allegro's allowed subset:
 * {@code h1,h2,p,ul,ol,li,b}); IMAGE items carry a {@code url}. Serializes to Allegro's item shape
 * ({@code {"type":"TEXT","content":...}} / {@code {"type":"IMAGE","url":...}}).
 */
public record DescriptionItem(ItemType type, String content, String url) {

    public static DescriptionItem text(String content) {
        return new DescriptionItem(ItemType.TEXT, content, null);
    }

    public static DescriptionItem image(String url) {
        return new DescriptionItem(ItemType.IMAGE, null, url);
    }

    public boolean isText() {
        return type == ItemType.TEXT;
    }

    public boolean isImage() {
        return type == ItemType.IMAGE;
    }

    public boolean isValid() {
        return type == ItemType.TEXT
                ? content != null && !content.isBlank()
                : url != null && !url.isBlank();
    }
}
