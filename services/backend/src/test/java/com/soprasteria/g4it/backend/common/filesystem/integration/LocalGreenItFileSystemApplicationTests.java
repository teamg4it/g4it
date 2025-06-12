/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.common.filesystem.integration;

import com.soprasteria.g4it.backend.common.filesystem.business.FileSystem;
import com.soprasteria.g4it.backend.common.filesystem.business.LocalFileStorage;
import com.soprasteria.g4it.backend.common.filesystem.business.LocalFileSystem;
import com.soprasteria.g4it.backend.common.filesystem.external.VaultAccessClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.FileSystemUtils;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles({"local", "test"})
class LocalGreenItFileSystemApplicationTests {

    @Autowired
    private FileSystem fileSystem;

    @MockitoBean
    private VaultAccessClient vaultAccessClient;

    @MockitoBean
    private CacheManager cacheManager;

    @AfterAll
    @BeforeAll
    static void cleanup() {
        // remove local-filesystem folder
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
        assertFalse(new File("target/local-filesystem/template").exists());

        Assertions.assertEquals(LocalFileStorage.class, fileSystem.mount("local", "G4IT").getClass());

        assertTrue(new File("target/local-filesystem/local/G4IT/input").exists());
        assertTrue(new File("target/local-filesystem/local/G4IT/work").exists());
        assertTrue(new File("target/local-filesystem/local/G4IT/output").exists());
        assertTrue(new File("target/local-filesystem/local/G4IT/template").exists());
    }

}
