package com.ercode.productdescription.adapter.in.web.dto;

import com.ercode.productdescription.domain.model.TemplateMatch;

/** A search result: similarity score + the matched template. */
public record TemplateMatchResponse(double score, TemplateResponse template) {

    public static TemplateMatchResponse from(TemplateMatch match) {
        return new TemplateMatchResponse(match.score(), TemplateResponse.from(match.template()));
    }
}
