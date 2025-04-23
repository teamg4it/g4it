/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiuser.mapper;

import com.soprasteria.g4it.backend.apiuser.model.SubscriberDetailsBO;
import com.soprasteria.g4it.backend.server.gen.api.dto.SubscriberDetailsRest;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubscriberDetailsRestMapper {

    SubscriberDetailsRest toDto(final SubscriberDetailsBO subscriberDetailsBO);

    List<SubscriberDetailsRest> toDto(final List<SubscriberDetailsBO> lstSubscriberDetailsBO);
}
