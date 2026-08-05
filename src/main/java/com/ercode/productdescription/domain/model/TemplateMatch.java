package com.ercode.productdescription.domain.model;

/** A search result: a matched template with its similarity score. */
public record TemplateMatch(DescriptionTemplate template, double score) {
}
