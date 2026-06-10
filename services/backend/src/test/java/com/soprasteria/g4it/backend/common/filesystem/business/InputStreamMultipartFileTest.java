/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.filesystem.business;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class InputStreamMultipartFileTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldReturnMetadataAndContent() throws Exception {
        byte[] content = "hello world".getBytes();

        InputStreamMultipartFile file = new InputStreamMultipartFile(
                new ByteArrayInputStream(content),
                "file",
                "test.txt",
                "text/plain"
        );

        assertEquals("file", file.getName());
        assertEquals("test.txt", file.getOriginalFilename());
        assertEquals("text/plain", file.getContentType());

        assertFalse(file.isEmpty());
        assertEquals(content.length, file.getSize());
        assertArrayEquals(content, file.getBytes());

        assertArrayEquals(
                content,
                file.getInputStream().readAllBytes()
        );
    }

    @Test
    void shouldBeEmptyWhenInputStreamContainsNoData() throws Exception {
        InputStreamMultipartFile file = new InputStreamMultipartFile(
                new ByteArrayInputStream(new byte[0]),
                "file",
                "empty.txt",
                "text/plain"
        );

        assertTrue(file.isEmpty());
        assertEquals(0, file.getSize());
        assertArrayEquals(new byte[0], file.getBytes());
    }

    @Test
    void shouldTransferToDestinationFile() throws Exception {
        byte[] content = "transfer content".getBytes();

        InputStreamMultipartFile file = new InputStreamMultipartFile(
                new ByteArrayInputStream(content),
                "file",
                "source.txt",
                "text/plain"
        );

        File destination = tempDir.resolve("destination.txt").toFile();

        file.transferTo(destination);

        assertTrue(destination.exists());
        assertArrayEquals(content, Files.readAllBytes(destination.toPath()));
    }
}
