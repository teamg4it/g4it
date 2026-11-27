// This file has been created (totally or partially) with the assistance of artificial intelligence tools.
// All content has been generated under the direct supervision of a named individual,
// and under the AI.Backbone Orchestrator Compliance framework

package com.soprasteria.g4it.backend.apiloadinputfiles.mapper;

import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

class CsvToInMapperTest {

    private final CsvToInMapper mapper = Mappers.getMapper(CsvToInMapper.class);

    @Test
    void retainsUsageTextUntilCategoryAwareValidation() throws Exception {
        InPhysicalEquipmentRest equipment = read(
                "nomEquipementPhysique;quantite;consoElecAnnuelle;contexteUsage;fractionUsage\n"
                        + "synthetic-equipment;2.5;100;BYOD;not a number\n");

        assertEquals("BYOD", equipment.getUsageContext().orElse(null));
        assertEquals("not a number", equipment.getUsageFraction().orElse(null));
        assertEquals(2.5, equipment.getQuantity());
        assertEquals(100d, equipment.getElectricityConsumption());
    }

    @Test
    void acceptsLegacyHeaderWithoutUsageColumns() throws Exception {
        InPhysicalEquipmentRest equipment = read("nomEquipementPhysique;quantite\nsynthetic-equipment;3\n");

        assertNull(equipment.getUsageContext().orElse(null));
        assertNull(equipment.getUsageFraction().orElse(null));
        assertEquals(3d, equipment.getQuantity());
    }

    private InPhysicalEquipmentRest read(String source) throws Exception {
        try (CSVParser parser = CSVFormat.RFC4180.builder()
                .setHeader().setDelimiter(';').build().parse(new StringReader(source))) {
            return mapper.csvInPhysicalEquipmentToRest(parser.iterator().next(), 1L, null);
        }
    }
}
