package com.ercode.productdescription.adapter.in.web;

import com.ercode.productdescription.adapter.in.web.dto.GenerateDescriptionRequest;
import com.ercode.productdescription.adapter.in.web.dto.GenerateDescriptionResponse;
import com.ercode.productdescription.application.port.in.GenerateDescriptionUseCase;
import com.ercode.productdescription.application.port.in.GetProductDescriptionUseCase;
import com.ercode.productdescription.domain.model.ProductDescription;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Driving adapter: HTTP endpoints for generating and fetching a structured product description. */
@RestController
public class DescriptionController {

    private final GenerateDescriptionUseCase generateUseCase;
    private final GetProductDescriptionUseCase getUseCase;

    public DescriptionController(GenerateDescriptionUseCase generateUseCase,
                                 GetProductDescriptionUseCase getUseCase) {
        this.generateUseCase = generateUseCase;
        this.getUseCase = getUseCase;
    }

    @PostMapping("/api/v1/descriptions:generate")
    public GenerateDescriptionResponse generate(@Valid @RequestBody GenerateDescriptionRequest request) {
        ProductDescription result = generateUseCase.generate(WebMapper.toProductInput(request));
        return GenerateDescriptionResponse.from(result);
    }

    @GetMapping("/api/v1/products/{externalId}/description")
    public GenerateDescriptionResponse getByExternalId(@PathVariable String externalId) {
        ProductDescription result = getUseCase.getByExternalId(externalId);
        return GenerateDescriptionResponse.from(result);
    }
}
