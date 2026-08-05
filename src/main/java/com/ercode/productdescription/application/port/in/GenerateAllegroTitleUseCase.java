package com.ercode.productdescription.application.port.in;

import com.ercode.productdescription.domain.model.AllegroTitle;
import com.ercode.productdescription.domain.model.AllegroTitleCommand;

/** Inbound (driving) port: generate an Allegro search-optimized title. */
public interface GenerateAllegroTitleUseCase {

    AllegroTitle generate(AllegroTitleCommand command);
}
