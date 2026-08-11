package com.ercode.productdescription.adapter.in.web;

import com.ercode.productdescription.adapter.in.web.dto.GenerateAllegroTitleRequest;
import com.ercode.productdescription.adapter.in.web.dto.GenerateDescriptionRequest;
import com.ercode.productdescription.adapter.in.web.dto.ImageInputDto;
import com.ercode.productdescription.domain.model.AllegroTitleCommand;
import com.ercode.productdescription.domain.model.ProductImage;
import com.ercode.productdescription.domain.model.ProductInput;

import java.util.List;

/** Maps web DTOs to domain value objects. */
final class WebMapper {

    private WebMapper() {
    }

    static ProductInput toProductInput(GenerateDescriptionRequest request) {
        List<ProductImage> images = request.images() == null
                ? List.of()
                : request.images().stream().map(WebMapper::toProductImage).toList();

        return new ProductInput(
                request.productName(),
                request.category(),
                request.brand(),
                request.language(),
                request.supplierText(),
                request.userText(),
                images,
                request.externalId());
    }

    private static ProductImage toProductImage(ImageInputDto dto) {
        return new ProductImage(dto.url(), dto.base64(), dto.mimeType());
    }

    static AllegroTitleCommand toAllegroTitleCommand(GenerateAllegroTitleRequest request) {
        return new AllegroTitleCommand(
                request.productName(),
                request.brand(),
                request.category(),
                request.model(),
                request.keyAttributes(),
                request.language());
    }
}
