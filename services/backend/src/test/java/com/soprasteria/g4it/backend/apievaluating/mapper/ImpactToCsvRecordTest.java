// This file has been created (totally or partially) with the assistance of artificial intelligence tools.
// All content has been generated under the direct supervision of a named individual,
// and under the AI.Backbone Orchestrator Compliance framework

package com.soprasteria.g4it.backend.apievaluating.mapper;

import com.soprasteria.g4it.backend.apiinout.mapper.InputToCsvRecord;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.PhysicalEquipmentEvaluationTrace;
import com.soprasteria.g4it.backend.common.model.Context;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementPhysique;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ImpactToCsvRecordTest {
    private final ImpactToCsvRecord mapper = Mappers.getMapper(ImpactToCsvRecord.class);
    private final InputToCsvRecord sourceMapper = Mappers.getMapper(InputToCsvRecord.class);
    private final Context context = Context.builder().inventoryId(1L).workspaceId(7L)
            .datetime(LocalDateTime.of(2026, 11, 24, 12, 0)).build();
    private final InPhysicalEquipment equipment = InPhysicalEquipment.builder().id(12L)
            .name("synthetic-device").quantity(2.5d).electricityConsumption(125d).build();
    private final ImpactEquipementPhysique impact = ImpactEquipementPhysique.builder()
            .critere("CLIMATE_CHANGE").etapeACV("USING").statutIndicateur("OK").trace("")
            .typeEquipement("laptop").unite("kg CO2 eq").quantite(1d).build();

    static Stream<Arguments> usageCases() {
        return Stream.of(
                Arguments.of("DEVICES", "BYOD", null, 0.57d, "BYOD", "0.57"),
                Arguments.of("DEVICES", "COPE", "", 0.85d, "COPE", "0.85"),
                Arguments.of("DEVICES", "BYOD", "0.5000", 0.5d, "BYOD", "0.5"),
                Arguments.of("DEVICES", null, "0.5", 0.5d, "", "0.5"),
                Arguments.of("DEVICES", "", "", 1d, "", ""),
                Arguments.of("DEVICES", null, null, 1d, "", ""),
                Arguments.of("DEVICES", "COPE", "0", 0d, "COPE", "0"),
                Arguments.of("DEVICES", "BYOD", "1", 1d, "BYOD", "1"),
                Arguments.of("DEVICES", "", "1E-7", 1e-7d, "", "0.0000001"),
                // Deliberately differs from today's default: the persisted value wins.
                Arguments.of("DEVICES", "BYOD", null, 0.42d, "BYOD", "0.42"),
                Arguments.of("INFRASTRUCTURE", "BYOD", "0.5000", 1d, "BYOD", "0.5000"),
                Arguments.of("CUSTOM", "COPE", null, 1d, "COPE", ""),
                Arguments.of("Devices", "BYOD", "", 1d, "BYOD", ""),
                Arguments.of("INFRASTRUCTURE", null, null, 1d, "", ""));
    }

    @ParameterizedTest
    @MethodSource("usageCases")
    void serializesOnlyPersistedUsageAndKeepsSourceExportRaw(String category, String usageContext,
            String usageFraction, double effectiveFraction, String expectedContext, String expectedFraction) {
        var trace = trace(99L, 12L, category, usageContext, usageFraction, effectiveFraction);
        equipment.setUsageContext(usageContext);
        equipment.setUsageFraction(usageFraction);
        var source = sourceMapper.toCsv(equipment, null);
        assertEquals(usageContext == null ? "" : usageContext, source.get(20));
        assertEquals(usageFraction == null ? "" : usageFraction, source.get(21));
        equipment.setUsageContext("changed after evaluation");
        equipment.setUsageFraction("0.99");

        var result = mapper.toCsv(context, 99L, "synthetic-inventory", equipment, impact, 1d, true, trace);

        assertEquals(28, result.size());
        assertEquals(expectedContext, result.get(26));
        assertEquals(expectedFraction, result.get(27));
        assertEquals("99", result.get(3));
        assertEquals("synthetic-device", result.get(19));
        assertEquals(2.5d, equipment.getQuantity());
        assertEquals(125d, equipment.getElectricityConsumption());
    }

    @Test
    void rejectsMissingOrMismatchedTaskAndSourceRatherThanFallingBackToSource() {
        assertThrows(IllegalArgumentException.class, () -> export(null));
        assertThrows(IllegalArgumentException.class,
                () -> export(trace(null, 12L, "DEVICES", "BYOD", null, 0.57d)));
        assertThrows(IllegalArgumentException.class,
                () -> export(trace(99L, null, "DEVICES", "BYOD", null, 0.57d)));
        assertThrows(IllegalArgumentException.class,
                () -> export(trace(100L, 12L, "DEVICES", "BYOD", null, 0.57d)));
        assertThrows(IllegalArgumentException.class,
                () -> export(trace(99L, 13L, "DEVICES", "BYOD", null, 0.57d)));
    }

    @Test
    void configuredInventoryHeadersMatchRealCsvRowsAndDigitalServiceContractStaysUnchanged() throws Exception {
        var yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("csv-headers.yml"));
        var properties = yaml.getObject();
        String prefix = "filesystem.config.headers.physicalEquipmentIndicator";
        String[] headers = IntStream.range(0, 28)
                .mapToObj(index -> properties.getProperty(prefix + "[" + index + "].name")).toArray(String[]::new);
        assertEquals("contexteUsage", headers[26]);
        assertEquals("fractionUsage", headers[27]);
        assertNull(properties.getProperty(prefix + "DigitalService[26].name"));
        assertEquals(26, mapper.toCsv(context, 99L, "synthetic-digital-service", equipment, impact, 1d, true).size());

        StringWriter output = new StringWriter();
        try (var printer = new CSVPrinter(output, CSVFormat.DEFAULT.builder().setDelimiter(';')
                .setHeader(headers).build())) {
            printer.printRecord(export(trace(99L, 12L, "DEVICES", "BYOD", null, 0.57d)));
        }
        try (var parser = CSVFormat.DEFAULT.builder().setDelimiter(';').setHeader().setSkipHeaderRecord(true)
                .build().parse(new StringReader(output.toString()))) {
            var rows = parser.getRecords();
            assertEquals(1, rows.size());
            assertTrue(rows.getFirst().isConsistent());
            assertEquals("BYOD", rows.getFirst().get("contexteUsage"));
            assertEquals("0.57", rows.getFirst().get("fractionUsage"));
        }
    }

    private List<String> export(PhysicalEquipmentEvaluationTrace trace) {
        return mapper.toCsv(context, 99L, "synthetic-inventory", equipment, impact, 1d, true, trace);
    }

    private PhysicalEquipmentEvaluationTrace trace(Long task, Long source, String category,
            String usageContext, String usageFraction, double effectiveFraction) {
        return PhysicalEquipmentEvaluationTrace.builder().taskId(task).sourceEquipmentId(source)
                .itemCategory(category).usageContext(usageContext).usageFraction(usageFraction)
                .effectiveFraction(effectiveFraction).build();
    }
}
