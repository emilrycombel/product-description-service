package com.ercode.productdescription.application.port.out;

import com.ercode.productdescription.domain.model.DescriptionScore;
import com.ercode.productdescription.domain.model.GeneratedDescription;

/** Outbound (driven) port: assess a description's quality (1–10) via an LLM-as-judge. */
public interface DescriptionScorerPort {

    DescriptionScore score(GeneratedDescription description);
}
