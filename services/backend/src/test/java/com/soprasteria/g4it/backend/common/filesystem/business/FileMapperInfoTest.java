/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.common.filesystem.business;

import com.soprasteria.g4it.backend.common.filesystem.external.VaultAccessClient;
import com.soprasteria.g4it.backend.common.filesystem.model.CsvFileMapperInfo;
import com.soprasteria.g4it.backend.common.filesystem.model.FileMapperInfo;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.filesystem.model.Header;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"local", "test"})
class FileMapperInfoTest {

    @MockitoBean
    private VaultAccessClient vaultAccessClient;

    @MockitoBean
    private CacheManager cacheManager;

    @Autowired
    private FileMapperInfo info;

    @Test
    void mapperInfoShouldBeInjectedInAllProfiles() {
        // We defined the mapper info in the default profile
        // We check that with the "local" profile, the context is injected properly
        Assertions.assertEquals(CsvFileMapperInfo.class, info.getClass());
    }

    @Test
    void applicationMapperInfosShouldContainApplicationName() {
        final Header expectedHeader = Header.builder().name("nomApplication").optional(false).build();
        Assertions.assertTrue(info.getMapping(FileType.APPLICATION).contains(expectedHeader));
    }

    @Test
    void datacenterMapperInfosShouldContainDatacenterShortName() {
        final Header expectedHeader = Header.builder().name("nomCourtDatacenter").optional(false).build();
        Assertions.assertTrue(info.getMapping(FileType.DATACENTER).contains(expectedHeader));
    }

    @Test
    void equipementPhysiqueMapperInfosShouldContainEquipementPhysiqueName() {
        final Header expectedHeader = Header.builder().name("nomEquipementPhysique").optional(false).build();
        Assertions.assertTrue(info.getMapping(FileType.EQUIPEMENT_PHYSIQUE).contains(expectedHeader));
    }

    @Test
    void equipementVirtuelMapperInfosShouldContainEquipementVirtuelName() {
        final Header expectedHeader = Header.builder().name("nomEquipementVirtuel").optional(false).build();
        Assertions.assertTrue(info.getMapping(FileType.EQUIPEMENT_VIRTUEL).contains(expectedHeader));
    }

    @Test
    void unknownFileTypeShouldReturnEmptyList() {
        Assertions.assertTrue(info.getMapping(FileType.UNKNOWN).isEmpty());
    }
}
