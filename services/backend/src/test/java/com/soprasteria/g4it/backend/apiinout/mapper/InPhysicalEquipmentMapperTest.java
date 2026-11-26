// This file has been created (totally or partially) with the assistance of artificial intelligence tools.
// All content has been generated under the direct supervision of a named individual,
// and under the AI.Backbone Orchestrator Compliance framework

package com.soprasteria.g4it.backend.apiinout.mapper;

import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.openapitools.jackson.nullable.JsonNullable;

import static org.junit.jupiter.api.Assertions.*;

class InPhysicalEquipmentMapperTest {

    private final InPhysicalEquipmentMapper mapper = Mappers.getMapper(InPhysicalEquipmentMapper.class);
    private final InputToCsvRecord csvMapper = Mappers.getMapper(InputToCsvRecord.class);

    @Test
    void roundTripsSourceUsageWithoutAllocatingQuantityOrElectricity() {
        InPhysicalEquipmentRest input = InPhysicalEquipmentRest.builder()
                .name("synthetic-equipment")
                .quantity(2.5)
                .electricityConsumption(125d)
                .usageContext(JsonNullable.of("COPE"))
                .usageFraction(JsonNullable.of("0.5000"))
                .build();

        InPhysicalEquipment entity = mapper.toEntity(input);
        InPhysicalEquipmentRest restored = mapper.toRest(entity);

        assertEquals("COPE", restored.getUsageContext().orElse(null));
        assertEquals("0.5000", restored.getUsageFraction().orElse(null));
        assertEquals(2.5, entity.getQuantity());
        assertEquals(125d, entity.getElectricityConsumption());
        assertEquals(2.5, restored.getQuantity());
        assertEquals(125d, restored.getElectricityConsumption());
        assertEquals("0.5000", csvMapper.toCsv(entity, null).getLast());
    }

    @Test
    void preservesUnparsedExemptValuesAndAllowsLegacyInput() {
        InPhysicalEquipment entity = mapper.toEntity(InPhysicalEquipmentRest.builder()
                .name("synthetic-infrastructure")
                .quantity(4d)
                .usageContext(JsonNullable.of("custom source value"))
                .usageFraction(JsonNullable.of("not a number"))
                .build());

        assertEquals("not a number", mapper.toRest(entity).getUsageFraction().orElse(null));
        assertEquals("not a number", csvMapper.toCsv(entity, null).getLast());

        InPhysicalEquipment legacy = mapper.toEntity(InPhysicalEquipmentRest.builder()
                .name("synthetic-infrastructure").quantity(4d).build());

        assertNull(legacy.getUsageContext());
        assertNull(legacy.getUsageFraction());
        assertEquals("", csvMapper.toCsv(legacy, null).getLast());
        assertEquals(4d, legacy.getQuantity());
    }
}
