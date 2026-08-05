package com.ercode.productdescription.adapter.in.web;

import com.ercode.productdescription.application.port.in.GenerateAllegroTitleUseCase;
import com.ercode.productdescription.domain.model.AllegroTitle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AllegroTitleControllerTest {

    private final GenerateAllegroTitleUseCase useCase = mock(GenerateAllegroTitleUseCase.class);
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new AllegroTitleController(useCase))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void valid_request_returns_title_with_char_count() throws Exception {
        String primary = "Spigen GLAS.tR szkło hartowane iPhone 16 Pro 2 szt.";
        when(useCase.generate(any())).thenReturn(AllegroTitle.of(primary, List.of("wariant"), "keyword-first"));

        String body = """
                {
                  "productName": "Szkło Spigen",
                  "brand": "Spigen",
                  "model": "iPhone 16 Pro",
                  "keyAttributes": ["2 szt.", "9H"]
                }
                """;

        mvc.perform(post("/api/v1/allegro-titles:generate")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.primary").value(primary))
                .andExpect(jsonPath("$.charCount").value(primary.length()));
    }

    @Test
    void blank_product_name_returns_400() throws Exception {
        mvc.perform(post("/api/v1/allegro-titles:generate")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }
}
