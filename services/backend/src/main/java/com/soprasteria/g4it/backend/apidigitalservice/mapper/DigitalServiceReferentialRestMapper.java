/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apidigitalservice.mapper;

import com.soprasteria.g4it.backend.apidigitalservice.model.DeviceTypeBO;
import com.soprasteria.g4it.backend.apidigitalservice.model.EcomindTypeBO;
import com.soprasteria.g4it.backend.apidigitalservice.model.NetworkTypeBO;
import com.soprasteria.g4it.backend.apidigitalservice.model.ServerHostBO;
import com.soprasteria.g4it.backend.server.gen.api.dto.DeviceTypeRefRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.EcomindTypeRefRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.NetworkTypeRefRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.ServerHostRefRest;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * DigitalServiceReferentialRest Mapper.
 */
@Mapper(componentModel = "spring")
public interface DigitalServiceReferentialRestMapper {

    /**
     * Map to Data Transfer Object.
     *
     * @param businessObject the source.
     * @return the DeviceTypeRefRest.
     */
    DeviceTypeRefRest toDeviceTypeDto(final DeviceTypeBO businessObject);

    /**
     * Map to Data Transfer Objects.
     *
     * @param businessObject the source.
     * @return the DeviceTypeRefRest list.
     */
    List<DeviceTypeRefRest> toDeviceTypeDto(final List<DeviceTypeBO> businessObject);

    /**
     * Map to Data Transfer Object.
     *
     * @param businessObject the source.
     * @return the DeviceTypeRefRest.
     */
    EcomindTypeRefRest toEcomindTypeDto(final EcomindTypeBO businessObject);

    /**
     * Map to Data Transfer Objects.
     *
     * @param businessObject the source.
     * @return the DeviceTypeRefRest list.
     */
    List<EcomindTypeRefRest> toEcomindTypeDto(final List<EcomindTypeBO> businessObject);

    /**
     * Map to Data Transfer Object.
     *
     * @param businessObject the source.
     * @return the NetworkTypeRefRest.
     */
    NetworkTypeRefRest toNetworkTypeDto(final NetworkTypeBO businessObject);

    /**
     * Map to Data Transfer Objects.
     *
     * @param businessObject the source.
     * @return the NetworkTypeRefRest list.
     */
    List<NetworkTypeRefRest> toNetworkTypeDto(final List<NetworkTypeBO> businessObject);

    /**
     * Map to Data Transfer Object.
     *
     * @param businessObject the source.
     * @return the ServerHostRefRest.
     */
    ServerHostRefRest toServerHostDto(final ServerHostBO businessObject);

    /**
     * Map to Data Transfer Objects.
     *
     * @param businessObject the source.
     * @return the ServerHostRefRest list.
     */
    List<ServerHostRefRest> toServerHostDto(final List<ServerHostBO> businessObject);

}
