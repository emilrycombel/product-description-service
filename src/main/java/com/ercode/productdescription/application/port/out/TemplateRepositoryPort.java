package com.ercode.productdescription.application.port.out;

import com.ercode.productdescription.domain.model.DescriptionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Outbound (driven) port: persist and retrieve template metadata. */
public interface TemplateRepositoryPort {

    void save(DescriptionTemplate template);

    Optional<DescriptionTemplate> findById(UUID id);

    List<DescriptionTemplate> findAll();

    List<DescriptionTemplate> findByIds(List<UUID> ids);
}
