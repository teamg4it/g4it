package com.soprasteria.g4it.backend.apireferential.mapper;

import com.soprasteria.g4it.backend.apireferential.modeldb.ItemType;
import org.apache.commons.csv.CSVFormat;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;

import java.io.StringReader;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReferentialItemCategoryMappingTest {
    private final ReferentialMapper mapper = Mappers.getMapper(ReferentialMapper.class);

    @ParameterizedTest
    @ValueSource(strings = {"DEVICES", "INFRASTRUCTURE", "CUSTOM"})
    void preservesExactCategoryThroughCsvMappingAndWorkspaceExport(String category) throws Exception {
        String csv = String.join(";", ItemType.getCsvHeaders()) + "\n"
                + "Laptop;Workstation;" + category + ";note;4;false;source;reference;org;1\n";

        try (var parser = CSVFormat.DEFAULT.builder().setDelimiter(';').setHeader()
                .setSkipHeaderRecord(true).build().parse(new StringReader(csv))) {
            var input = mapper.csvItemTypeToRest(parser.getRecords().getFirst());
            assertEquals(category, input.getItemCategory());
            assertEquals("Workstation", input.getCategory());
            assertEquals(4d, input.getDefaultLifespan());

            var item = ItemType.builder().type(input.getType()).category(input.getCategory())
                    .itemCategory(input.getItemCategory()).build();
            var headers = Arrays.asList(ItemType.getCsvHeadersForWorkspace());
            Object[] record = item.toCsvRecordForWorkspace();
            assertEquals(headers.size(), record.length);
            assertEquals(category, record[headers.indexOf("item_category")]);
        }
    }
}
