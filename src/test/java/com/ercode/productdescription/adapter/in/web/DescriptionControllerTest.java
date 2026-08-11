package com.ercode.productdescription.adapter.in.web;

import com.ercode.productdescription.application.port.in.GenerateDescriptionUseCase;
import com.ercode.productdescription.application.port.in.GetProductDescriptionUseCase;
import com.ercode.productdescription.domain.DescriptionNotFoundException;
import com.ercode.productdescription.domain.model.DescriptionItem;
import com.ercode.productdescription.domain.model.DescriptionSection;
import com.ercode.productdescription.domain.model.GeneratedDescription;
import com.ercode.productdescription.domain.model.ProductDescription;
import com.ercode.productdescription.domain.model.ProductInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DescriptionControllerTest {

    private final GenerateDescriptionUseCase generateUseCase = mock(GenerateDescriptionUseCase.class);
    private final GetProductDescriptionUseCase getUseCase = mock(GetProductDescriptionUseCase.class);
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new DescriptionController(generateUseCase, getUseCase))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void valid_request_returns_allegro_sections() throws Exception {
        when(generateUseCase.generate(any())).thenReturn(sample());

        String body = """
                {
                  "productName": "Spigen GLAS.tR iPhone 16 Pro",
                  "supplierText": "9H tempered glass, 2-pack with applicator",
                  "externalId": "SKU-123",
                  "images": [{ "url": "https://example.com/a.png" }]
                }
                """;

        mvc.perform(post("/api/v1/descriptions:generate")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.structureVersion").value("2.0"))
                .andExpect(jsonPath("$.modelName").value("gpt-test"))
                .andExpect(jsonPath("$.externalId").value("SKU-123"))
                .andExpect(jsonPath("$.description.sections[0].items[0].type").value("TEXT"))
                .andExpect(jsonPath("$.description.sections[0].items[0].content").value("<h1>Title</h1>"))
                .andExpect(jsonPath("$.description.sections[1].items[1].type").value("IMAGE"))
                .andExpect(jsonPath("$.description.sections[1].items[1].url")
                        .value("https://example.com/a.png"));
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

    @Test
    void get_by_external_id_returns_stored_description() throws Exception {
        when(getUseCase.getByExternalId("SKU-123")).thenReturn(sample());

        mvc.perform(get("/api/v1/products/{externalId}/description", "SKU-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value("SKU-123"))
                .andExpect(jsonPath("$.description.sections[0].items[0].content").value("<h1>Title</h1>"));
    }

    @Test
    void get_by_unknown_external_id_returns_404() throws Exception {
        when(getUseCase.getByExternalId("nope"))
                .thenThrow(DescriptionNotFoundException.forExternalId("nope"));

        mvc.perform(get("/api/v1/products/{externalId}/description", "nope"))
                .andExpect(status().isNotFound());
    }

    private static ProductDescription sample() {
        GeneratedDescription generated = new GeneratedDescription(List.of(
                DescriptionSection.of(DescriptionItem.text("<h1>Title</h1>")),
                DescriptionSection.of(
                        DescriptionItem.text("<h2>Zalety</h2><ul><li>bullet</li></ul>"),
                        DescriptionItem.image("https://example.com/a.png"))));
        ProductInput input = new ProductInput("x", null, null, "pl", null, null, List.of(), "SKU-123");
        return ProductDescription.create(input, generated, "gpt-test");
    }
}
