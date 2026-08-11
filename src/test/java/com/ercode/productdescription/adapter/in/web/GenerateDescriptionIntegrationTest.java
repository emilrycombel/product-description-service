package com.ercode.productdescription.adapter.in.web;

import com.ercode.productdescription.domain.model.DescriptionItem;
import com.ercode.productdescription.domain.model.DescriptionScore;
import com.ercode.productdescription.domain.model.DescriptionSection;
import com.ercode.productdescription.domain.model.DimensionScore;
import com.ercode.productdescription.domain.model.GeneratedDescription;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
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

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.ercode.productdescription.adapter.out.persistence.jooq.Tables.DESCRIPTION_TEMPLATE;
import static com.ercode.productdescription.adapter.out.persistence.jooq.Tables.PRODUCT_DESCRIPTION;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end proof of the generate + score verticals, fully offline: a real Postgres+pgvector via
 * Testcontainers and a WireMock-stubbed OpenAI endpoint (no real key/network). The generation and scoring
 * calls hit the same completions URL, disambiguated by request-body content + stub priority. Runs in CI;
 * auto-skips where Docker is unavailable.
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
        // Scoring requests carry the judge system prompt ("...quality reviewer...") — match them first.
        WIRE_MOCK.stubFor(post(urlPathEqualTo("/v1/chat/completions"))
                .atPriority(1)
                .withRequestBody(containing("reviewer"))
                .willReturn(jsonResponse(openAiScoreBody())));
        // Everything else is a generation request.
        WIRE_MOCK.stubFor(post(urlPathEqualTo("/v1/chat/completions"))
                .atPriority(5)
                .willReturn(jsonResponse(openAiDescriptionBody())));
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

    @BeforeEach
    void resetTables() {
        dsl.deleteFrom(PRODUCT_DESCRIPTION).execute();
        dsl.deleteFrom(DESCRIPTION_TEMPLATE).execute();
        dsl.execute("DELETE FROM product_templates");   // langchain4j-owned vector table
    }

    @Test
    void generates_a_structured_description_and_persists_it() throws Exception {
        HttpResponse<String> response = generate();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body())
                .contains("structureVersion")
                .contains("2.0")
                .contains("iPhone 16 Pro")
                .contains("sections");

        assertThat(dsl.fetchCount(PRODUCT_DESCRIPTION)).isEqualTo(1);
        var row = dsl.selectFrom(PRODUCT_DESCRIPTION).fetchSingle();
        assertThat(row.getStructureVersion()).isEqualTo(GeneratedDescription.STRUCTURE_VERSION);
        assertThat(row.getExternalId()).isEqualTo("SKU-INT-1");
        assertThat(row.getModelName()).isNotBlank();
        assertThat(row.getRawJson()).isNotBlank();
        assertThat(row.getScore()).isNull();
    }

    @Test
    void regenerating_for_the_same_external_id_upserts_and_is_retrievable() throws Exception {
        assertThat(generate().statusCode()).isEqualTo(200);
        assertThat(generate().statusCode()).isEqualTo(200);

        // Upsert: two generations for the same externalId leave exactly one row.
        assertThat(dsl.fetchCount(PRODUCT_DESCRIPTION)).isEqualTo(1);

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/v1/products/SKU-INT-1/description"))
                .GET()
                .build();
        HttpResponse<String> fetched = HttpClient.newHttpClient()
                .send(getRequest, HttpResponse.BodyHandlers.ofString());

        assertThat(fetched.statusCode()).isEqualTo(200);
        assertThat(fetched.body()).contains("SKU-INT-1").contains("sections").contains("iPhone 16 Pro");
    }

    @Test
    void get_by_unknown_external_id_returns_404() throws Exception {
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/v1/products/does-not-exist/description"))
                .GET()
                .build();
        HttpResponse<String> fetched = HttpClient.newHttpClient()
                .send(getRequest, HttpResponse.BodyHandlers.ofString());

        assertThat(fetched.statusCode()).isEqualTo(404);
    }

    @Test
    void scores_a_stored_description_and_persists_the_score() throws Exception {
        assertThat(generate().statusCode()).isEqualTo(200);
        UUID id = dsl.selectFrom(PRODUCT_DESCRIPTION).fetchSingle().getId();

        HttpRequest scoreRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/v1/descriptions/" + id + ":score"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(scoreRequest, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("overall").contains("rubricVersion");

        var row = dsl.selectFrom(PRODUCT_DESCRIPTION).where(PRODUCT_DESCRIPTION.ID.eq(id)).fetchSingle();
        assertThat(row.getScore()).isNotNull();
        assertThat(row.getScoredAt()).isNotNull();
        assertThat(row.getScoreAssessment()).isNotBlank();
    }

    @Test
    void creates_a_template_and_finds_it_by_semantic_search() throws Exception {
        String create = """
                {
                  "name": "Etui do telefonu",
                  "family": "phone-case",
                  "body": "Podkreśl ochronę przed upadkiem, wycięcia na porty i kompatybilność z ładowaniem bezprzewodowym."
                }
                """;
        HttpResponse<String> created = postJson("/api/v1/templates", create);
        assertThat(created.statusCode()).isEqualTo(201);
        assertThat(dsl.fetchCount(DESCRIPTION_TEMPLATE)).isEqualTo(1);

        HttpResponse<String> found = postJson("/api/v1/templates:search",
                "{ \"query\": \"etui ochronne na telefon\" }");
        assertThat(found.statusCode()).isEqualTo(200);
        assertThat(found.body()).contains("phone-case").contains("Etui do telefonu");
    }

    private HttpResponse<String> postJson(String path, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> generate() throws Exception {
        String requestBody = """
                {
                  "productName": "Spigen GLAS.tR do iPhone 16 Pro (2 szt.)",
                  "brand": "Spigen",
                  "category": "Szkła hartowane",
                  "supplierText": "9H tempered glass, oleophobic, 2-pack with applicator frame",
                  "userText": "Podkreśl łatwą aplikację",
                  "externalId": "SKU-INT-1",
                  "images": [{ "url": "https://example.com/spigen.png" }]
                }
                """;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/v1/descriptions:generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder jsonResponse(String body) {
        return aResponse().withHeader("Content-Type", "application/json").withBody(body);
    }

    private static String openAiDescriptionBody() {
        GeneratedDescription description = new GeneratedDescription(List.of(
                DescriptionSection.of(DescriptionItem.text(
                        "<h1>Szkło hartowane Spigen GLAS.tR do iPhone 16 Pro (2 szt.)</h1>"
                                + "<p>Najwyższa ochrona z powłoką 9H i łatwą aplikacją.</p>")),
                DescriptionSection.of(
                        DescriptionItem.text("<h2>Zalety</h2><ul><li>Twardość 9H</li>"
                                + "<li>Powłoka oleofobowa</li><li>Zestaw 2 szt.</li></ul>"),
                        DescriptionItem.image("https://example.com/spigen.png")),
                DescriptionSection.of(DescriptionItem.text(
                        "<h2>Zawartość zestawu</h2><ul><li>2x szkło hartowane</li>"
                                + "<li>Ramka aplikatora</li><li>Chusteczki czyszczące</li></ul>")),
                DescriptionSection.of(DescriptionItem.text(
                        "<h2>Kompatybilność</h2><p>Apple iPhone 16 Pro</p>")),
                DescriptionSection.of(DescriptionItem.text(
                        "<h2>Specyfikacja</h2><ul><li>Twardość: 9H</li><li>Ilość: 2 szt.</li></ul>")),
                DescriptionSection.of(DescriptionItem.text(
                        "<p>Spigen to uznany producent akcesoriów ochronnych.</p>"))));
        return completion(description);
    }

    private static String openAiScoreBody() {
        DescriptionScore score = new DescriptionScore(
                new BigDecimal("8.5"),
                List.of(
                        new DimensionScore("COMPLETENESS", BigDecimal.valueOf(9), "all sections present"),
                        new DimensionScore("FAITHFULNESS", BigDecimal.valueOf(8), "claims supported"),
                        new DimensionScore("CLARITY", BigDecimal.valueOf(9), "scannable"),
                        new DimensionScore("PERSUASIVENESS", BigDecimal.valueOf(8), "strong hook"),
                        new DimensionScore("SEO_ALLEGRO_FIT", BigDecimal.valueOf(8), "good keywords")),
                "Strong listing; tighten the hook.");
        return completion(score);
    }

    private static String completion(Object structuredContent) {
        JsonMapper mapper = JsonMapper.builder().build();
        String content = mapper.writeValueAsString(structuredContent);
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
