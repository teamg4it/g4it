/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.filesystem.business;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobItemProperties;
import com.azure.storage.blob.models.BlobProperties;
import com.soprasteria.g4it.backend.common.filesystem.model.FileDescription;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@SuppressWarnings("unchecked")
class AzureFileStorageTest {
    BlobServiceClient blobServiceClient;
    BlobContainerClient blobContainerClient;
    BlobClient blobClient;
    PagedIterable<BlobItem> pagedIterable;
    BlobItem blobItem;
    BlobItemProperties blobItemProperties;
    FileFolder folder;
    AzureFileStorage storage;
    BlobProperties blobProperties;

    @BeforeEach
    void setUp() {
        blobServiceClient = mock(BlobServiceClient.class);
        blobContainerClient = mock(BlobContainerClient.class);
        blobClient = mock(BlobClient.class);
        pagedIterable = mock(PagedIterable.class); // generic warning suppressed
        blobItem = mock(BlobItem.class);
        blobItemProperties = mock(BlobItemProperties.class);
        blobProperties = mock(BlobProperties.class);
        folder = FileFolder.valueOf("INPUT");
        storage = new AzureFileStorage(blobServiceClient, blobContainerClient, "workspace", "azure-blob://container");
    }

    @Test
    void testReadFile() throws IOException {
        when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);
        doAnswer(invocation -> {
            ByteArrayOutputStream out = invocation.getArgument(0);
            out.write("abc".getBytes());
            return null;
        }).when(blobClient).downloadStream(any());
        try (InputStream is = storage.readFile(folder, "file.txt")) {
            byte[] data = is.readAllBytes();
            assertArrayEquals("abc".getBytes(), data);
        }
    }

    @Test
    void testWriteFileString() throws IOException {
        when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);
        storage.writeFile(folder, "file.txt", "content");
        verify(blobClient).upload(any(ByteArrayInputStream.class));
    }

    @Test
    void testWriteFileInputStream() throws IOException {
        when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);
        InputStream is = new ByteArrayInputStream("data".getBytes());
        storage.writeFile(folder, "file.txt", is);
        verify(blobClient).upload(is);
    }

    @Test
    void testListFiles() throws IOException {
        when(blobContainerClient.listBlobs(any(), any())).thenReturn(pagedIterable);
        when(pagedIterable.stream()).thenReturn(java.util.stream.Stream.of(blobItem));
        when(blobItem.getName()).thenReturn("file.txt");
        when(blobItem.getMetadata()).thenReturn(Map.of("type", "INPUT"));
        when(blobItem.getProperties()).thenReturn(blobItemProperties);
        when(blobItemProperties.getCreationTime()).thenReturn(java.time.OffsetDateTime.now());
        when(blobItemProperties.getContentLength()).thenReturn(123L);
        List<FileDescription> files = storage.listFiles(folder);
        assertEquals(1, files.size());
        assertEquals("file.txt", files.getFirst().getName());
    }

    @Test
    void testHasFileInSubfolder() throws IOException {
        when(blobContainerClient.listBlobs(any(), any())).thenReturn(pagedIterable);
        when(pagedIterable.stream()).thenReturn(java.util.stream.Stream.of(blobItem));
        assertTrue(storage.hasFileInSubfolder(folder, "sub", FileType.APPLICATION));
    }

    @Test
    void testRename() {
        when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobClient.getBlobUrl()).thenReturn("url");
        when(blobClient.exists()).thenReturn(true);
        when(blobClient.generateSas(any())).thenReturn("sas");
        when(blobClient.copyFromUrl(anyString())).thenReturn(null);
        doNothing().when(blobClient).delete();
        storage.rename(folder, "old.txt", "new.txt");
        verify(blobClient, times(1)).delete();
    }

    @Test
    void testMove() {
        when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobClient.getBlobUrl()).thenReturn("url");
        when(blobClient.exists()).thenReturn(true);
        when(blobClient.generateSas(any())).thenReturn("sas");
        when(blobClient.copyFromUrl(anyString())).thenReturn(null); // Fix: use thenReturn for non-void
        doNothing().when(blobClient).delete();
        storage.move(folder, folder, "file.txt");
        verify(blobClient, times(1)).delete();
    }


    @Test
    void testMoveAndRename() {
        when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobClient.getBlobUrl()).thenReturn("url");
        when(blobClient.exists()).thenReturn(true);
        when(blobClient.generateSas(any())).thenReturn("sas");
        when(blobClient.copyFromUrl(anyString())).thenReturn(null);
        doNothing().when(blobClient).delete();
        storage.moveAndRename(folder, folder, "old.txt", "new.txt");
        verify(blobClient, times(1)).delete();
    }

    @Test
    void testDelete() {
        when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobClient.exists()).thenReturn(true);
        doNothing().when(blobClient).delete();
        storage.delete(folder, "file.txt");
        verify(blobClient).delete();
    }

    @Test
    void testDeleteFolder() {
        when(blobContainerClient.listBlobs(any(), any())).thenReturn(pagedIterable);
        when(pagedIterable.iterator()).thenReturn(Collections.singletonList(blobItem).iterator());
        when(blobItem.getName()).thenReturn("file.txt");
        when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);
        doNothing().when(blobClient).delete();
        storage.deleteFolder(folder, "path");
        verify(blobClient).delete();
    }

    @Test
    void testUploadFromFile() {
        when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);
        doNothing().when(blobClient).uploadFromFile(anyString(), eq(true));
        storage.upload("localPath", folder, "file.txt");
        verify(blobClient).uploadFromFile("localPath", true);
    }

    @Test
    void testUploadInputStream() throws IOException {
        when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobClient.getBlobName()).thenReturn("input\\file.txt");
        doNothing().when(blobClient).upload(any(ByteArrayInputStream.class));
        doNothing().when(blobClient).setMetadata(any());
        InputStream is = new ByteArrayInputStream("data".getBytes());
        String result = storage.upload(folder, "file.txt", "INPUT", is);
        assertEquals("file.txt", result);
    }

    @Test
    void testGetFileUrlExists() {
        when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobClient.exists()).thenReturn(true);
        when(blobClient.getBlobUrl()).thenReturn("https://url");
        String url = storage.getFileUrl(folder, "file.txt");
        assertTrue(url.contains("https://url"));
    }

    @Test
    void testGetFileUrlNotExists() {
        when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobClient.exists()).thenReturn(false);
        String url = storage.getFileUrl(folder, "file.txt");
        assertEquals("", url);
    }

    @Test
    void testGetFileSizeExists() {
        when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobClient.exists()).thenReturn(true);
        when(blobClient.getBlobUrl()).thenReturn("url");
        when(blobClient.getProperties()).thenReturn(blobProperties);
        when(blobProperties.getBlobSize()).thenReturn(123L);
        long size = storage.getFileSize(folder, "file.txt");
        assertEquals(123L, size);
    }

    @Test
    void testGetFileSizeNotExists() {
        when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobClient.exists()).thenReturn(false);
        long size = storage.getFileSize(folder, "file.txt");
        assertEquals(0L, size);
    }
}
