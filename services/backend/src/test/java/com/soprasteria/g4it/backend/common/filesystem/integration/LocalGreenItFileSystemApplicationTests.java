/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery
 */
package com.soprasteria.g4it.backend.common.filesystem.integration;

import com.soprasteria.g4it.backend.common.filesystem.business.FileSystem;
import com.soprasteria.g4it.backend.common.filesystem.business.LocalFileStorage;
import com.soprasteria.g4it.backend.common.filesystem.business.LocalFileSystem;
import com.soprasteria.g4it.backend.common.filesystem.external.VaultAccessClient;
import com.soprasteria.g4it.backend.external.boavizta.client.BoaviztapiClient;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest(
        properties = {
                "spring.cloud.azure.enabled=false"
        }
)
@ActiveProfiles({"local", "test"})
class LocalGreenItFileSystemApplicationTests {

    @Autowired
    private FileSystem fileSystem;

    @MockitoBean
    private VaultAccessClient vaultAccessClient;

    @MockitoBean
    private CacheManager cacheManager;

    @MockitoBean
    private BoaviztapiClient boaviztapiClient;

    @BeforeEach
    void setup() {
        // return empty so EvaluateService.init() does not fail
        when(boaviztapiClient.getAllCountries())
                .thenReturn(Collections.emptyMap());
    }


    @AfterAll
    @BeforeAll
    static void cleanup() {
        FileSystemUtils.deleteRecursively(new File("target/local-filesystem"));
    }

    @Test
    void fileSystemShouldBeOfLocalFileSystemType() {
        Assertions.assertEquals(LocalFileSystem.class, fileSystem.getClass());
    }

    @Test
    void mountShouldReturnLocalFilestorageTypeAndCreateFolderStructure() {
        assertFalse(new File("target/local-filesystem/input").exists());
        assertFalse(new File("target/local-filesystem/work").exists());
        assertFalse(new File("target/local-filesystem/output").exists());
        assertFalse(new File("target/local-filesystem/templates").exists());

        Assertions.assertEquals(LocalFileStorage.class, fileSystem.mount("local", "G4IT").getClass());

        assertTrue(new File("target/local-filesystem/local/G4IT/input").exists());
        assertTrue(new File("target/local-filesystem/local/G4IT/work").exists());
        assertTrue(new File("target/local-filesystem/local/G4IT/output").exists());
        assertTrue(new File("target/local-filesystem/local/G4IT/templates").exists());
    }
}
