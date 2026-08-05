package com.ercode.productdescription.adapter.in.web;

import com.ercode.productdescription.domain.model.GeneratedDescription;
import com.ercode.productdescription.domain.model.SpecRow;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static com.ercode.productdescription.adapter.out.persistence.jooq.Tables.PRODUCT_DESCRIPTION;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end proof of the generate-description vertical, fully offline: a real Postgres+pgvector via
 * Testcontainers and a WireMock-stubbed OpenAI endpoint (no real key/network). Runs in CI; auto-skips
 * where Docker is unavailable.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class GenerateDescriptionIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("pgvector/pgvector:pg17").asCompatibleSubstituteFor("postgres"));

    static final WireMockServer WIRE_MOCK = new WireMockServer(wireMockConfig().dynamicPort());

    static {
        WIRE_MOCK.start();
        WIRE_MOCK.stubFor(post(urlPathEqualTo("/v1/chat/completions"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(openAiChatCompletionBody())));
    }

    @DynamicPropertySource
    static void llmProperties(DynamicPropertyRegistry registry) {
        registry.add("langchain4j.open-ai.chat-model.base-url", () -> WIRE_MOCK.baseUrl() + "/v1");
        registry.add("langchain4j.open-ai.chat-model.api-key", () -> "test-key");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private DSLContext dsl;

    @Test
    void generates_a_structured_description_and_persists_it() throws Exception {
        String requestBody = """
                {
                  "productName": "Spigen GLAS.tR do iPhone 16 Pro (2 szt.)",
                  "brand": "Spigen",
                  "category": "Szkła hartowane",
                  "supplierText": "9H tempered glass, oleophobic, 2-pack with applicator frame",
                  "userText": "Podkreśl łatwą aplikację",
                  "images": [{ "url": "https://example.com/spigen.png" }]
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/v1/descriptions:generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body())
                .contains("structureVersion")
                .contains("1.0")
                .contains("iPhone 16 Pro");

        // Persistence: exactly one row, canonical structure version stored, score not yet set.
        assertThat(dsl.fetchCount(PRODUCT_DESCRIPTION)).isEqualTo(1);
        var row = dsl.selectFrom(PRODUCT_DESCRIPTION).fetchSingle();
        assertThat(row.getStructureVersion()).isEqualTo(GeneratedDescription.STRUCTURE_VERSION);
        assertThat(row.getModelName()).isNotBlank();
        assertThat(row.getRawJson()).isNotBlank();
        assertThat(row.getScore()).isNull();
    }

    private static String openAiChatCompletionBody() {
        JsonMapper mapper = JsonMapper.builder().build();
        GeneratedDescription description = new GeneratedDescription(
                "Szkło hartowane Spigen GLAS.tR do iPhone 16 Pro (2 szt.)",
                "Najwyższa ochrona z powłoką 9H i łatwą aplikacją.",
                List.of("Twardość 9H", "Powłoka oleofobowa", "Zestaw 2 szt."),
                List.of("2x szkło hartowane", "Ramka aplikatora", "Chusteczki czyszczące"),
                "Apple iPhone 16 Pro",
                List.of(new SpecRow("Twardość", "9H"), new SpecRow("Ilość", "2 szt.")),
                "Spigen to uznany producent akcesoriów ochronnych.");

        String content = mapper.writeValueAsString(description);
        Map<String, Object> body = Map.of(
                "id", "chatcmpl-test",
                "object", "chat.completion",
                "created", 0,
                "model", "gpt-4o-mini",
                "choices", List.of(Map.of(
                        "index", 0,
                        "message", Map.of("role", "assistant", "content", content),
                        "finish_reason", "stop")),
                "usage", Map.of("prompt_tokens", 1, "completion_tokens", 1, "total_tokens", 2));
        return mapper.writeValueAsString(body);
    }
}
