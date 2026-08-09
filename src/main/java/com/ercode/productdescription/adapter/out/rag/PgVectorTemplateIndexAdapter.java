package com.ercode.productdescription.adapter.out.rag;

import com.ercode.productdescription.application.port.out.TemplateIndexPort;
import com.ercode.productdescription.domain.model.DescriptionTemplate;
import com.ercode.productdescription.domain.model.TemplateSearchHit;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Driven adapter: the pgvector-backed template index. Templates are embedded (name + family + body) with
 * {@code templateId}/{@code family} metadata so hits can be resolved back to a template. Uses the same
 * embedding store that the description generator's content retriever reads from.
 */
@Component
public class PgVectorTemplateIndexAdapter implements TemplateIndexPort {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    public PgVectorTemplateIndexAdapter(EmbeddingModel embeddingModel,
                                        EmbeddingStore<TextSegment> embeddingStore) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
    }

    @Override
    public void index(DescriptionTemplate template) {
        String family = template.family() == null ? "" : template.family();
        String text = template.name() + "\n" + family + "\n" + template.body();
        Metadata metadata = Metadata.from(Map.of(
                "templateId", template.id().toString(),
                "family", family));
        TextSegment segment = TextSegment.from(text, metadata);
        Embedding embedding = embeddingModel.embed(segment).content();
        embeddingStore.add(embedding, segment);
    }

    @Override
    public List<TemplateSearchHit> search(String query, int maxResults, double minScore) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(maxResults)
                .minScore(minScore)
                .build();
        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);
        return result.matches().stream()
                .map(match -> new TemplateSearchHit(
                        UUID.fromString(match.embedded().metadata().getString("templateId")),
                        match.score()))
                .toList();
    }
}
