package com.ercode.productdescription.application.port.in;

import com.ercode.productdescription.domain.model.DescriptionScore;
import com.ercode.productdescription.domain.model.GeneratedDescription;
import com.ercode.productdescription.domain.model.ScoredDescription;

import java.util.UUID;

/** Inbound (driving) port: score a product description 1–10. */
public interface ScoreDescriptionUseCase {

    /** Score a stored description by id and persist the result. */
    ScoredDescription score(UUID id);

    /** Score an ad-hoc description without persisting anything. */
    DescriptionScore score(GeneratedDescription description);
}
