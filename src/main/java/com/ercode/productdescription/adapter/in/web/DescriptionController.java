package com.ercode.productdescription.adapter.in.web;

import com.ercode.productdescription.adapter.in.web.dto.GenerateDescriptionRequest;
import com.ercode.productdescription.adapter.in.web.dto.GenerateDescriptionResponse;
import com.ercode.productdescription.application.port.in.GenerateDescriptionUseCase;
import com.ercode.productdescription.domain.model.ProductDescription;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Driving adapter: HTTP endpoint for generating a structured product description. */
@RestController
public class DescriptionController {

    private final GenerateDescriptionUseCase useCase;

    public DescriptionController(GenerateDescriptionUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/api/v1/descriptions:generate")
    public GenerateDescriptionResponse generate(@Valid @RequestBody GenerateDescriptionRequest request) {
        ProductDescription result = useCase.generate(WebMapper.toProductInput(request));
        return GenerateDescriptionResponse.from(result);
    }
}
