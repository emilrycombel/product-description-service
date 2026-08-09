package com.ercode.productdescription.application.port.out;

import com.ercode.productdescription.domain.model.AllegroTitle;
import com.ercode.productdescription.domain.model.AllegroTitleCommand;

/** Outbound (driven) port: produce an Allegro search-optimized title via an LLM. */
public interface AllegroTitleGeneratorPort {

    AllegroTitle generate(AllegroTitleCommand command, int maxLength);
}
