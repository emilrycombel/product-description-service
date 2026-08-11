package com.ercode.productdescription.adapter.in.web;

import com.ercode.productdescription.application.port.in.ScoreDescriptionUseCase;
import com.ercode.productdescription.domain.DescriptionNotFoundException;
import com.ercode.productdescription.domain.model.DescriptionScore;
import com.ercode.productdescription.domain.model.DimensionScore;
import com.ercode.productdescription.domain.model.GeneratedDescription;
import com.ercode.productdescription.domain.model.ScoredDescription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.pattern.PathPatternParser;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ScoreControllerTest {

    private final ScoreDescriptionUseCase useCase = mock(ScoreDescriptionUseCase.class);
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        // Use PathPatternParser so the "{id}:score" mapping routes exactly as it does in production.
        mvc = MockMvcBuilders.standaloneSetup(new ScoreController(useCase))
                .setControllerAdvice(new ApiExceptionHandler())
                .setPatternParser(new PathPatternParser())
                .build();
    }

    @Test
    void score_stored_returns_200_with_assessment() throws Exception {
        UUID id = UUID.randomUUID();
        DescriptionScore score = new DescriptionScore(
                new BigDecimal("8.5"),
                List.of(new DimensionScore("CLARITY", BigDecimal.valueOf(9), "clear")),
                "solid");
        when(useCase.score(id)).thenReturn(new ScoredDescription(id, score));

        mvc.perform(post("/api/v1/descriptions/" + id + ":score"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.rubricVersion").value("1.0"))
                .andExpect(jsonPath("$.score.overall").value(8.5))
                .andExpect(jsonPath("$.score.dimensions[0].dimension").value("CLARITY"));
    }

    @Test
    void score_missing_id_returns_404() throws Exception {
        UUID id = UUID.randomUUID();
        when(useCase.score(id)).thenThrow(new DescriptionNotFoundException(id));

        mvc.perform(post("/api/v1/descriptions/" + id + ":score"))
                .andExpect(status().isNotFound());
    }

    @Test
    void score_adhoc_returns_200() throws Exception {
        when(useCase.score(any(GeneratedDescription.class)))
                .thenReturn(new DescriptionScore(BigDecimal.valueOf(7), List.of(), "ok"));

        String body = """
                {
                  "sections": [
                    { "items": [ { "type": "TEXT", "content": "<h1>Title</h1><p>Hook</p>" } ] },
                    { "items": [ { "type": "TEXT", "content": "<h2>Zalety</h2><ul><li>b</li></ul>" } ] }
                  ]
                }
                """;

        mvc.perform(post("/api/v1/descriptions:score")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overall").value(7.0));
    }
}
