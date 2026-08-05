package com.ercode.productdescription.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * RAG wiring for product-family templates. The embedding model runs in-process (offline, 384-dim). The
 * pgvector store reuses Spring's auto-configured {@link DataSource} and auto-creates its own table and the
 * {@code vector} extension. The {@link ContentRetriever} is injected into the description generator so each
 * generate call is augmented with the best-matching family template.
 */
@Configuration
public class EmbeddingConfig {

    @Bean
    public EmbeddingModel embeddingModel() {
        return new BgeSmallEnV15QuantizedEmbeddingModel();
    }

    @Bean
    public EmbeddingStore<TextSegment> templateEmbeddingStore(
            DataSource dataSource,
            @Value("${app.templates.embedding.dimension:384}") int dimension) {
        return PgVectorEmbeddingStore.datasourceBuilder()
                .datasource(dataSource)
                .table("product_templates")
                .dimension(dimension)
                .createTable(true)
                .useIndex(true)
                .indexListSize(100)
                .build();
    }

    @Bean
    public ContentRetriever templateContentRetriever(
            EmbeddingStore<TextSegment> templateEmbeddingStore,
            EmbeddingModel embeddingModel,
            @Value("${app.templates.retriever.max-results:3}") int maxResults,
            @Value("${app.templates.retriever.min-score:0.6}") double minScore) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(templateEmbeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(maxResults)
                .minScore(minScore)
                .build();
    }
}
