package com.ercode.productdescription.adapter.in.web;

import com.ercode.productdescription.adapter.in.web.dto.ScoredDescriptionResponse;
import com.ercode.productdescription.application.port.in.ScoreDescriptionUseCase;
import com.ercode.productdescription.domain.model.DescriptionScore;
import com.ercode.productdescription.domain.model.GeneratedDescription;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Driving adapter: HTTP endpoints for scoring descriptions (stored by id, or ad-hoc). */
@RestController
public class ScoreController {

    private final ScoreDescriptionUseCase useCase;

    public ScoreController(ScoreDescriptionUseCase useCase) {
        this.useCase = useCase;
    }

    /** Score a previously generated description and persist the score. */
    @PostMapping("/api/v1/descriptions/{id}:score")
    public ScoredDescriptionResponse scoreStored(@PathVariable UUID id) {
        return ScoredDescriptionResponse.from(useCase.score(id));
    }

    /** Score an ad-hoc description supplied in the request body (nothing persisted). */
    @PostMapping("/api/v1/descriptions:score")
    public DescriptionScore scoreAdHoc(@RequestBody GeneratedDescription description) {
        return useCase.score(description);
    }
}
