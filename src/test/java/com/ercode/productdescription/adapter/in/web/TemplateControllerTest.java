package com.ercode.productdescription.adapter.in.web;

import com.ercode.productdescription.application.port.in.TemplateUseCase;
import com.ercode.productdescription.domain.TemplateNotFoundException;
import com.ercode.productdescription.domain.model.DescriptionTemplate;
import com.ercode.productdescription.domain.model.TemplateMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.pattern.PathPatternParser;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TemplateControllerTest {

    private final TemplateUseCase useCase = mock(TemplateUseCase.class);
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new TemplateController(useCase))
                .setControllerAdvice(new ApiExceptionHandler())
                .setPatternParser(new PathPatternParser())
                .build();
    }

    @Test
    void create_returns_201() throws Exception {
        when(useCase.create(any())).thenReturn(DescriptionTemplate.create("Phone case", "phone-case", "guidance"));

        String body = """
                { "name": "Phone case", "family": "phone-case", "body": "guidance" }
                """;
        mvc.perform(post("/api/v1/templates").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.family").value("phone-case"));
    }

    @Test
    void create_blank_returns_400() throws Exception {
        mvc.perform(post("/api/v1/templates").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void get_returns_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(useCase.get(id)).thenReturn(
                new DescriptionTemplate(id, "n", "phone-case", "b", OffsetDateTime.now(ZoneOffset.UTC)));

        mvc.perform(get("/api/v1/templates/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void get_missing_returns_404() throws Exception {
        UUID id = UUID.randomUUID();
        when(useCase.get(id)).thenThrow(new TemplateNotFoundException(id));

        mvc.perform(get("/api/v1/templates/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void search_returns_200_with_scores() throws Exception {
        DescriptionTemplate t = DescriptionTemplate.create("Phone case", "phone-case", "g");
        when(useCase.search(eq("etui"), anyInt())).thenReturn(List.of(new TemplateMatch(t, 0.88)));

        mvc.perform(post("/api/v1/templates:search")
                        .contentType(MediaType.APPLICATION_JSON).content("{ \"query\": \"etui\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").value(0.88))
                .andExpect(jsonPath("$[0].template.family").value("phone-case"));
    }
}
