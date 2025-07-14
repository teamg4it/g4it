/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apidigitalservice.mapper;

import com.soprasteria.g4it.backend.apidigitalservice.model.*;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.*;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.List;

/**
 * DigitalService Mapper.
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public abstract class DigitalServiceReferentialMapper {

    /**
     * Map device type referential entities to business objects.
     *
     * @param source entities to map.
     * @return the business object list.
     */
    public abstract List<DeviceTypeBO> toDeviceTypeBusinessObject(final List<DeviceTypeRef> source);

    /**
     * Map device type referential entity to business object.
     *
     * @param source the entity to map.
     * @return the business object.
     */
    @Mapping(target = "code", source = "reference")
    @Mapping(target = "value", source = "description")
    @Mapping(target = "lifespan", source = "lifespan")
    public abstract DeviceTypeBO toDeviceTypeBusinessObject(final DeviceTypeRef source);

    /**
     * Map ecomind type referential entities to business objects.
     *
     * @param source entities to map.
     * @return the business object list.
     */
    public abstract List<EcomindTypeBO> toEcomindTypeBusinessObject(final List<EcomindTypeRef> source);

    /**
     * Map ecomind type referential entity to business object.
     *
     * @param source the entity to map.
     * @return the business object.
     */
    @Mapping(target = "code", source = "reference")
    @Mapping(target = "value", source = "description")
    @Mapping(target = "lifespan", source = "lifespan")
    public abstract EcomindTypeBO toEcomindTypeBusinessObject(final EcomindTypeRef source);

    /**
     * Map network type referential entities to business objects.
     *
     * @param source entities to map.
     * @return the business object list.
     */
    public abstract List<NetworkTypeBO> toNetworkTypeBusinessObject(final List<NetworkTypeRef> source);

    /**
     * Map device type referential entity to business object.
     *
     * @param source the entity to map.
     * @return the business object.
     */
    @Mapping(target = "code", source = "reference")
    @Mapping(target = "value", source = "description")
    public abstract NetworkTypeBO toNetworkTypeBusinessObject(final NetworkTypeRef source);

    /**
     * Map server host referential entity to business objects.
     *
     * @param source the entities to map.
     * @return the business object list.
     */
    public abstract List<ServerHostBO> toServerHostBusinessObject(final List<ServerHostRef> source);

    /**
     * Map server host referential entity to business object.
     *
     * @param source the entity to map.
     * @return the business object.
     */
    @Mapping(target = "code", source = "id")
    @Mapping(target = "value", source = "description")
    public abstract ServerHostBO toServerHostBusinessObject(final ServerHostRef source);

    /**
     * Map server host referential DTO to business objects.
     *
     * @param source the entities to map.
     * @return the business object list.
     */
    public abstract List<ServerHostBO> serverDTOtoServerHostBusinessObject(final List<ServerHostRefDTO> source);

    /**
     * Map server host referential DTO to business object.
     *
     * @param source the entity to map.
     * @return the business object.
     */
    @Mapping(target = "code", source = "id")
    @Mapping(target = "value", source = "description")
    public abstract ServerHostBO serverDTOtoServerHostBusinessObject(final ServerHostRefDTO source);

    @AfterMapping
    protected void finalizeBusinessObject(@MappingTarget final ServerHostBO target, final ServerHostRefDTO source) {
        List<ServerCharacteristicBO> characteristics = new ArrayList<>();
        if (source.getNbOfVcpu() != null) {
            characteristics.add(ServerCharacteristicBO.builder()
                    .code(ServerCharacteristicBO.Code.VCPU.getValue())
                    .value(Double.valueOf(source.getNbOfVcpu()))
                    .build()
            );
        }
        if (source.getTotalDisk() != null) {
            characteristics.add(ServerCharacteristicBO.builder()
                    .code(ServerCharacteristicBO.Code.DISK.getValue())
                    .value(Double.valueOf(source.getTotalDisk()))
                    .build()
            );
        }
        if (source.getLifespan() != null) {
            characteristics.add(ServerCharacteristicBO.builder()
                    .code(ServerCharacteristicBO.Code.LIFESPAN.getValue())
                    .value(source.getLifespan())
                    .build()
            );
        }
        if (source.getAnnualElectricityConsumption() != null) {
            characteristics.add(ServerCharacteristicBO.builder()
                    .code(ServerCharacteristicBO.Code.ANNUAL_ELECTRICITY_CONSUMPTION.getValue())
                    .value(Double.valueOf(source.getAnnualElectricityConsumption()))
                    .build()
            );
        }
        target.setCharacteristic(characteristics);
    }

}
