package com.ercode.productdescription.application.service;

import com.ercode.productdescription.application.port.in.TemplateUseCase;
import com.ercode.productdescription.application.port.out.TemplateIndexPort;
import com.ercode.productdescription.application.port.out.TemplateRepositoryPort;
import com.ercode.productdescription.domain.TemplateNotFoundException;
import com.ercode.productdescription.domain.model.DescriptionTemplate;
import com.ercode.productdescription.domain.model.NewTemplate;
import com.ercode.productdescription.domain.model.TemplateMatch;
import com.ercode.productdescription.domain.model.TemplateSearchHit;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Orchestrates templates: create persists then indexes for retrieval; search runs the vector index and
 * hydrates the hits from the repository, preserving the index's ordering and scores. Framework-free —
 * wired as a bean in {@code config.UseCaseConfig}.
 */
public class TemplateService implements TemplateUseCase {

    private final TemplateRepositoryPort repository;
    private final TemplateIndexPort index;

    public TemplateService(TemplateRepositoryPort repository, TemplateIndexPort index) {
        this.repository = repository;
        this.index = index;
    }

    @Override
    public DescriptionTemplate create(NewTemplate command) {
        DescriptionTemplate template = DescriptionTemplate.create(command.name(), command.family(), command.body());
        repository.save(template);
        index.index(template);
        return template;
    }

    @Override
    public DescriptionTemplate get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new TemplateNotFoundException(id));
    }

    @Override
    public List<DescriptionTemplate> list() {
        return repository.findAll();
    }

    @Override
    public List<TemplateMatch> search(String query, int maxResults) {
        List<TemplateSearchHit> hits = index.search(query, maxResults, 0.0);
        Map<UUID, DescriptionTemplate> byId = repository
                .findByIds(hits.stream().map(TemplateSearchHit::templateId).toList())
                .stream()
                .collect(Collectors.toMap(DescriptionTemplate::id, Function.identity()));
        return hits.stream()
                .filter(hit -> byId.containsKey(hit.templateId()))
                .map(hit -> new TemplateMatch(byId.get(hit.templateId()), hit.score()))
                .toList();
    }
}
