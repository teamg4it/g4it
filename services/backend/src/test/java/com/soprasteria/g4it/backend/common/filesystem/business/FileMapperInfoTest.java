package com.soprasteria.g4it.backend.common.filesystem.business;

import com.soprasteria.g4it.backend.common.filesystem.model.CsvFileMapperInfo;
import com.soprasteria.g4it.backend.common.filesystem.model.FileMapperInfo;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.filesystem.model.Header;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        classes = CsvFileMapperInfo.class,
        properties = {
                "spring.cloud.azure.enabled=false",
                "spring.liquibase.enabled=false"
        }
)
@ActiveProfiles({"local", "test"})
class FileMapperInfoTest {

    @Autowired
    private FileMapperInfo info;

    @Test
    void mapperInfoShouldBeInjectedInAllProfiles() {
        Assertions.assertEquals(CsvFileMapperInfo.class, info.getClass());
    }

    @Test
    void applicationMapperInfosShouldContainApplicationName() {
        Header expected = Header.builder().name("nomApplication").optional(false).build();
        Assertions.assertTrue(info.getMapping(FileType.APPLICATION).contains(expected));
    }

    @Test
    void datacenterMapperInfosShouldContainDatacenterShortName() {
        Header expected = Header.builder().name("nomCourtDatacenter").optional(false).build();
        Assertions.assertTrue(info.getMapping(FileType.DATACENTER).contains(expected));
    }

    @Test
    void equipementPhysiqueMapperInfosShouldContainEquipementPhysiqueName() {
        Header expected = Header.builder().name("nomEquipementPhysique").optional(false).build();
        Assertions.assertTrue(info.getMapping(FileType.EQUIPEMENT_PHYSIQUE).contains(expected));
    }

    @Test
    void equipementVirtuelMapperInfosShouldContainEquipementVirtuelName() {
        Header expected = Header.builder().name("nomEquipementVirtuel").optional(false).build();
        Assertions.assertTrue(info.getMapping(FileType.EQUIPEMENT_VIRTUEL).contains(expected));
    }

    @Test
    void unknownFileTypeShouldReturnEmptyList() {
        Assertions.assertTrue(info.getMapping(FileType.UNKNOWN).isEmpty());
    }
}
