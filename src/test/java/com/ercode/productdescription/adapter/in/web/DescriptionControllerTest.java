package com.ercode.productdescription.adapter.in.web;

import com.ercode.productdescription.application.port.in.GenerateDescriptionUseCase;
import com.ercode.productdescription.domain.model.GeneratedDescription;
import com.ercode.productdescription.domain.model.ProductDescription;
import com.ercode.productdescription.domain.model.ProductInput;
import com.ercode.productdescription.domain.model.SpecRow;
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

class DescriptionControllerTest {

    private final GenerateDescriptionUseCase useCase = mock(GenerateDescriptionUseCase.class);
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new DescriptionController(useCase))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void valid_request_returns_canonical_structure() throws Exception {
        when(useCase.generate(any())).thenReturn(sample());

        String body = """
                {
                  "productName": "Spigen GLAS.tR iPhone 16 Pro",
                  "supplierText": "9H tempered glass, 2-pack with applicator",
                  "images": [{ "url": "https://example.com/a.png" }]
                }
                """;

        mvc.perform(post("/api/v1/descriptions:generate")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.structureVersion").value("1.0"))
                .andExpect(jsonPath("$.modelName").value("gpt-test"))
                .andExpect(jsonPath("$.description.title").value("Title"))
                .andExpect(jsonPath("$.description.benefitBullets[0]").value("bullet"))
                .andExpect(jsonPath("$.description.specTable[0].label").value("Twardość"));
    }

    @Test
    void blank_product_name_returns_400() throws Exception {
        mvc.perform(post("/api/v1/descriptions:generate")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalid_image_source_returns_400() throws Exception {
        String body = """
                {
                  "productName": "x",
                  "images": [{ "url": "https://a", "base64": "AAAA" }]
                }
                """;
        mvc.perform(post("/api/v1/descriptions:generate")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    private static ProductDescription sample() {
        GeneratedDescription generated = new GeneratedDescription(
                "Title", "Hook",
                List.of("bullet"), List.of("box"),
                "iPhone 16 Pro",
                List.of(new SpecRow("Twardość", "9H")),
                "brand");
        ProductInput input = new ProductInput("x", null, null, "pl", null, null, List.of());
        return ProductDescription.create(input, generated, "gpt-test");
    }
}
