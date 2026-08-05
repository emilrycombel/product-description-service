package com.ercode.productdescription.application.service;

import com.ercode.productdescription.application.port.out.TemplateIndexPort;
import com.ercode.productdescription.application.port.out.TemplateRepositoryPort;
import com.ercode.productdescription.domain.TemplateNotFoundException;
import com.ercode.productdescription.domain.model.DescriptionTemplate;
import com.ercode.productdescription.domain.model.NewTemplate;
import com.ercode.productdescription.domain.model.TemplateMatch;
import com.ercode.productdescription.domain.model.TemplateSearchHit;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TemplateServiceTest {

    private final TemplateRepositoryPort repository = mock(TemplateRepositoryPort.class);
    private final TemplateIndexPort index = mock(TemplateIndexPort.class);
    private final TemplateService service = new TemplateService(repository, index);

    @Test
    void create_saves_and_indexes() {
        DescriptionTemplate created = service.create(new NewTemplate("Phone case", "phone-case", "guidance"));

        assertThat(created.id()).isNotNull();
        assertThat(created.family()).isEqualTo("phone-case");
        verify(repository).save(created);
        verify(index).index(created);
    }

    @Test
    void get_missing_throws_404() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(id)).isInstanceOf(TemplateNotFoundException.class);
    }

    @Test
    void search_hydrates_hits_and_preserves_index_order() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        when(index.search(eq("q"), eq(5), eq(0.0)))
                .thenReturn(List.of(new TemplateSearchHit(id1, 0.9), new TemplateSearchHit(id2, 0.7)));

        DescriptionTemplate t1 = template(id1);
        DescriptionTemplate t2 = template(id2);
        // Repository returns them in a different order — the service must re-order to match the hits.
        when(repository.findByIds(List.of(id1, id2))).thenReturn(List.of(t2, t1));

        List<TemplateMatch> matches = service.search("q", 5);

        assertThat(matches).extracting(m -> m.template().id()).containsExactly(id1, id2);
        assertThat(matches.get(0).score()).isEqualTo(0.9);
    }

    private static DescriptionTemplate template(UUID id) {
        return new DescriptionTemplate(id, "n", "phone-case", "b", OffsetDateTime.now(ZoneOffset.UTC));
    }
}
