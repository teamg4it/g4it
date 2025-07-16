/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.common.filesystem.business;

import com.soprasteria.g4it.backend.common.filesystem.model.FileDescription;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LocalFileStorageTest {

    private static final String LOCAL_FILESYSTEM_PATH = "target/local-filestorage-test/";

    private static final String SUBSCRIBER = "local";
    private static final String ORGANIZATION = "G4IT";
    private final FileSystem fileSystem = new LocalFileSystem(LOCAL_FILESYSTEM_PATH);
    private final FileStorage storage = fileSystem.mount("local", "G4IT");

    private static final Path testFolder = Path.of("src/test/resources/common/filesystem/local");

    @BeforeAll
    @AfterAll
    static void cleanup() {
        // remove local-filestorage-test folder
        FileSystemUtils.deleteRecursively(new File(LOCAL_FILESYSTEM_PATH));
    }

    @Test
    void readShouldFindFileInLocalFileSystem() throws IOException {
        // given a local file system with an input file
        FileSystemUtils.copyRecursively(testFolder.resolve("input").toFile(),
                new File(LOCAL_FILESYSTEM_PATH + "/input"));
        File file = new File(LOCAL_FILESYSTEM_PATH + "/input/file1.txt");

        // When trying to read the file
        try (InputStream is = storage.readFile(FileFolder.INPUT, "file1.txt");
             FileInputStream fis = new FileInputStream(file)) {
            String result = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            String expected = new String(fis.readAllBytes(), StandardCharsets.UTF_8);
            // then it should find file and read content correctly
            assertEquals(expected, result);
        }

    }

    @Test
    void readShouldThrowIOExceptionWhenFileDoesNotExist() {
        assertThrows(IOException.class, () -> storage.readFile(FileFolder.INPUT, "unknown.txt"));
    }

    @Test
    void writeShouldCreateAndAddContentToFile() throws IOException {
        storage.writeFile(FileFolder.OUTPUT, "filename", "anything");

        try (InputStream is = storage.readFile(FileFolder.OUTPUT, "filename")) {
            assertEquals("anything", new String(is.readAllBytes()));
        }

    }

    @Test
    void writeInputStreamShouldCreateAndAddContentToFile() throws IOException {
        storage.writeFile(FileFolder.OUTPUT, "filename", new ByteArrayInputStream("something".getBytes()));

        try (InputStream is = storage.readFile(FileFolder.OUTPUT, "filename")) {
            assertEquals("something", new String(is.readAllBytes()));
        }
    }

    @Test
    void listFilesShouldListAllFiles() throws IOException {
        // given 2 files in the input folder
        FileSystemUtils.copyRecursively(testFolder.resolve("input").toFile(),
                new File(LOCAL_FILESYSTEM_PATH + Path.of(SUBSCRIBER, ORGANIZATION, FileFolder.INPUT.getFolderName())));
        // when we list files
        List<FileDescription> files = storage.listFiles(FileFolder.INPUT);
        // it should find them
        assertEquals(2, files.size());
    }

    @Test
    void hasFileInSubfolderShouldReturnTrue() throws IOException {
        // given a file in the work folder
        FileSystemUtils.copyRecursively(testFolder.resolve("work").toFile(),
                new File(LOCAL_FILESYSTEM_PATH + Path.of(SUBSCRIBER, ORGANIZATION, FileFolder.WORK.getFolderName())));
        // When listing file in this folder, it should find it
        assertTrue(storage.hasFileInSubfolder(FileFolder.WORK, "20231904-1808", FileType.DATACENTER));
    }

    @Test
    void moveShouldMoveFileToDifferentFolder() throws IOException {
        storage.writeFile(FileFolder.INPUT, "file_to_move", new ByteArrayInputStream("something".getBytes()));
        File fileToMove = new File(LOCAL_FILESYSTEM_PATH + Path.of(SUBSCRIBER, ORGANIZATION, FileFolder.INPUT.getFolderName(), "/file_to_move"));
        File movedFiles = new File(LOCAL_FILESYSTEM_PATH + Path.of(SUBSCRIBER, ORGANIZATION, FileFolder.OUTPUT.getFolderName(), "/file_to_move"));
        assertTrue(fileToMove.exists());
        assertFalse(movedFiles.exists());
        storage.move(FileFolder.INPUT, FileFolder.OUTPUT, "file_to_move");
        assertFalse(fileToMove.exists());
        assertTrue(movedFiles.exists());
    }

    @Test
    void renameShouldChangeFileName() throws IOException {
        storage.writeFile(FileFolder.OUTPUT, "file_to_rename", new ByteArrayInputStream("something".getBytes()));
        File fileToRename = new File(LOCAL_FILESYSTEM_PATH + Path.of(SUBSCRIBER, ORGANIZATION, FileFolder.OUTPUT.getFolderName(), "/file_to_rename"));
        File renamedFiles = new File(LOCAL_FILESYSTEM_PATH + Path.of(SUBSCRIBER, ORGANIZATION, FileFolder.OUTPUT.getFolderName(), "/renamed_file"));
        assertTrue(fileToRename.exists());
        assertFalse(renamedFiles.exists());
        storage.rename(FileFolder.OUTPUT, "file_to_rename", "renamed_file");
        assertFalse(fileToRename.exists());
        assertTrue(renamedFiles.exists());
    }

    @Test
    void moveAndRenameShouldMoveFileToDifferentFolderAndRenameIt() throws IOException {
        storage.writeFile(FileFolder.INPUT, "file_to_move", new ByteArrayInputStream("something".getBytes()));
        File fileToMove = new File(LOCAL_FILESYSTEM_PATH + Path.of(SUBSCRIBER, ORGANIZATION, FileFolder.INPUT.getFolderName(), "/file_to_move"));
        File movedFiles = new File(LOCAL_FILESYSTEM_PATH + Path.of(SUBSCRIBER, ORGANIZATION, FileFolder.OUTPUT.getFolderName(), "/file_moved_and_renamed"));
        assertTrue(fileToMove.exists());
        assertFalse(movedFiles.exists());
        storage.moveAndRename(FileFolder.INPUT, FileFolder.OUTPUT, "file_to_move", "file_moved_and_renamed");
        assertFalse(fileToMove.exists());
        assertTrue(movedFiles.exists());
    }

    @Test
    void uploadShouldCopyFile() throws IOException {
        // given the file1.txt file
        // when we upload it to output folder
        storage.upload(testFolder.resolve("input").resolve("file1.txt").toString(),
                FileFolder.OUTPUT, "file1_upload.txt");

        // then it should exist
        File output = new File(LOCAL_FILESYSTEM_PATH + Path.of(SUBSCRIBER, ORGANIZATION, FileFolder.OUTPUT.getFolderName(), "/file1_upload.txt"));
        assertTrue(output.exists());
    }

    @Test
    void uploadShouldCopyMultipartFile() throws IOException {
        MultipartFile fileInput = new MockMultipartFile("DATACENTER", "file1_upload.txt", String.valueOf(MediaType.TEXT_PLAIN), "Hello world".getBytes());
        storage.upload(FileFolder.OUTPUT, fileInput.getOriginalFilename(), fileInput.getName(), fileInput.getInputStream());
        File output = new File(LOCAL_FILESYSTEM_PATH + Path.of(SUBSCRIBER, ORGANIZATION, FileFolder.OUTPUT.getFolderName(), "/file1_upload.txt"));
        assertTrue(output.exists());
    }

    @Test
    void deleteShouldDeleteTheFile() throws IOException {
        // given a file in the work folder
        FileSystemUtils.copyRecursively(testFolder.resolve("work").toFile(),
                new File(LOCAL_FILESYSTEM_PATH + Path.of(SUBSCRIBER, ORGANIZATION, FileFolder.WORK.getFolderName())));

        storage.delete(FileFolder.WORK, "20230511-1815/DATACENTER/datacenter.txt");
        Resource[] resources = storage.listResources(FileFolder.WORK, "20230511-1815", FileType.DATACENTER);
        assertEquals(1, Arrays.stream(resources).count());
    }

    @Test
    void listResourcesShouldReturnAllFiles() throws IOException {
        // given a file in the work folder
        FileSystemUtils.copyRecursively(testFolder.resolve("work").toFile(),
                new File(LOCAL_FILESYSTEM_PATH + Path.of(SUBSCRIBER, ORGANIZATION, FileFolder.WORK.getFolderName())));

        // When we list resources, it should find them
        Resource[] resources = storage.listResources(FileFolder.WORK, "20230511-1815", FileType.DATACENTER);
        assertEquals(2, Arrays.stream(resources).count());
    }

    @Test
    void getFileUrlShouldReturnUrlIfFileExist() throws IOException {
        // given a file in the work folder
        FileSystemUtils.copyRecursively(testFolder.resolve("work").toFile(),
                new File(LOCAL_FILESYSTEM_PATH + Path.of(SUBSCRIBER, ORGANIZATION, FileFolder.WORK.getFolderName())));
        // when we get url
        String url = storage.getFileUrl(FileFolder.WORK, "20230511-1815/DATACENTER/datacenter.txt");
        assertNotNull(url);
        assertTrue(url.contains("file:///"));
        assertTrue(url.contains("work/20230511-1815/DATACENTER/datacenter.txt"));
    }

    @Test
    void getFileUrlShouldReturnEmptyIfFileUnknown() {
        // when we get url of unknown file
        String url = storage.getFileUrl(FileFolder.WORK, "unknown.zip");
        assertNotNull(url);
        assertTrue(url.isEmpty());
    }

    @Test
    void getFileSizeShouldReturnMoreThanZeroForExistingFile() throws IOException {
        storage.writeFile(FileFolder.OUTPUT, "file_to_estimate", new ByteArrayInputStream("something".getBytes()));

        assertTrue(storage.getFileSize(FileFolder.OUTPUT, "file_to_estimate") > 0);
    }

    @Test
    void getFileSizeShouldReturnMZeroForUnknownFile() {
        assertEquals(0, storage.getFileSize(FileFolder.OUTPUT, "unknown"));
    }

}
