package com.ercode.productdescription.adapter.in.web;

import com.ercode.productdescription.adapter.in.web.dto.CreateTemplateRequest;
import com.ercode.productdescription.adapter.in.web.dto.SearchTemplatesRequest;
import com.ercode.productdescription.adapter.in.web.dto.TemplateMatchResponse;
import com.ercode.productdescription.adapter.in.web.dto.TemplateResponse;
import com.ercode.productdescription.application.port.in.TemplateUseCase;
import com.ercode.productdescription.domain.model.NewTemplate;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Driving adapter: template CRUD + semantic search. */
@RestController
public class TemplateController {

    private final TemplateUseCase useCase;

    public TemplateController(TemplateUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/api/v1/templates")
    @ResponseStatus(HttpStatus.CREATED)
    public TemplateResponse create(@Valid @RequestBody CreateTemplateRequest request) {
        return TemplateResponse.from(
                useCase.create(new NewTemplate(request.name(), request.family(), request.body())));
    }

    @GetMapping("/api/v1/templates/{id}")
    public TemplateResponse get(@PathVariable UUID id) {
        return TemplateResponse.from(useCase.get(id));
    }

    @GetMapping("/api/v1/templates")
    public List<TemplateResponse> list() {
        return useCase.list().stream().map(TemplateResponse::from).toList();
    }

    @PostMapping("/api/v1/templates:search")
    public List<TemplateMatchResponse> search(@Valid @RequestBody SearchTemplatesRequest request) {
        return useCase.search(request.query(), request.maxResultsOrDefault()).stream()
                .map(TemplateMatchResponse::from)
                .toList();
    }
}
