package com.ercode.productdescription.domain.model;

/** Command to create a new template. */
public record NewTemplate(String name, String family, String body) {
}
