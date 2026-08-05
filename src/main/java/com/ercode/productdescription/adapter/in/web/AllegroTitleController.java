package com.ercode.productdescription.adapter.in.web;

import com.ercode.productdescription.adapter.in.web.dto.GenerateAllegroTitleRequest;
import com.ercode.productdescription.application.port.in.GenerateAllegroTitleUseCase;
import com.ercode.productdescription.domain.model.AllegroTitle;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Driving adapter: HTTP endpoint for generating an Allegro search-optimized title. */
@RestController
public class AllegroTitleController {

    private final GenerateAllegroTitleUseCase useCase;

    public AllegroTitleController(GenerateAllegroTitleUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/api/v1/allegro-titles:generate")
    public AllegroTitle generate(@Valid @RequestBody GenerateAllegroTitleRequest request) {
        return useCase.generate(WebMapper.toAllegroTitleCommand(request));
    }
}
