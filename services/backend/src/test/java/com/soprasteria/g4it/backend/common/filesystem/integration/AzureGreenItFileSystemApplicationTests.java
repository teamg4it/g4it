/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.common.filesystem.integration;

import com.soprasteria.g4it.backend.common.filesystem.business.AzureFileSystem;
import com.soprasteria.g4it.backend.common.filesystem.business.FileStorage;
import com.soprasteria.g4it.backend.common.filesystem.business.FileSystem;
import com.soprasteria.g4it.backend.common.filesystem.model.FileDescription;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"azure", "test"})
@EnabledIf("hasAzureEnvVars")
@Slf4j
class AzureGreenItFileSystemApplicationTests {

    private final String FILE_TEST_NAME = "test.txt";
    private final String UPLOAD_FILE_TEST_NAME = "test-upload.txt";
    private final String UPLOAD_MULTIPART_FILE_TEST_NAME = "test-multipart-upload.txt";
    private final String FILE_NEW_NAME1 = "test1.txt";
    private final String FILE_NEW_NAME2 = "test2.txt";
    /**
     * Organization associated to test container
     */
    private final String ORGANIZATION = "0";

    /**
     * The real subscriber name in Azure.
     */
    private final String SUBSCRIBER = "FS-TEST";
    @Autowired
    ResourceLoader resourceLoader;
    @Autowired
    private FileSystem fileSystem;
    @MockitoBean
    private CacheManager cacheManager;

    private static boolean hasAzureEnvVars() {
        var hasEnvVar = System.getenv("SPRING_CLOUD_AZURE_KEYVAULT_SECRET_ENDPOINT") != null;
        if (hasEnvVar) {
            log.info("ENV VARIABLES:");
            log.info("AZURE_CLIENT_ID : {}", System.getenv("AZURE_CLIENT_ID"));
            log.info("AZURE_CLIENT_SECRET : {}...", System.getenv("AZURE_CLIENT_SECRET").substring(0, 5));
            log.info("AZURE_TENANT_ID : {}", System.getenv("AZURE_TENANT_ID"));
            log.info("AZURE_SUBSCRIPTION_ID : {}", System.getenv("AZURE_SUBSCRIPTION_ID"));
            log.info("SPRING_CLOUD_AZURE_KEYVAULT_SECRET_ENDPOINT : {}", System.getenv("SPRING_CLOUD_AZURE_KEYVAULT_SECRET_ENDPOINT"));
        }
        if (!hasEnvVar) {
            log.info("""
                    To activate azure filestorage tests, you need to set variables:
                    - AZURE_CLIENT_ID
                    - AZURE_CLIENT_SECRET
                    - AZURE_TENANT_ID
                    - AZURE_SUBSCRIPTION_ID
                    - SPRING_CLOUD_AZURE_KEYVAULT_SECRET_ENDPOINT
                    """);
        }
        return hasEnvVar;
    }

    @Test
    void fileSystemShouldBeOfAzureFileSystemType() {
        assertEquals(AzureFileSystem.class, fileSystem.getClass());
    }


    @Test
    void fileSystemShouldHandleCompleteFilesLifeCycle() throws IOException {
        FileStorage fs = fileSystem.mount(SUBSCRIBER, ORGANIZATION);

        List<FileDescription> inputFiles, workFiles, outputFiles;
        final String FILE_CONTENT = "Content for test";
        try {
            //***********
            // Write test
            //***********
            // when we write a file in a folder
            fs.writeFile(FileFolder.INPUT, FILE_TEST_NAME, FILE_CONTENT);

            // then it should be listed
            inputFiles = fs.listFiles(FileFolder.INPUT);
            assertTrue(inputFiles.stream().anyMatch(file -> file.getName().contains(FILE_TEST_NAME)));

            //************
            // Rename test
            //************
            // given there is an existing file
            // when we rename it
            fs.rename(FileFolder.INPUT, FILE_TEST_NAME, FILE_NEW_NAME1);
            // then it should not exist anymore
            inputFiles = fs.listFiles(FileFolder.INPUT);
            assertFalse(inputFiles.stream().anyMatch(file -> file.getName().contains(FILE_TEST_NAME)));
            // and a new file should exist
            assertTrue(inputFiles.stream().anyMatch(file -> file.getName().contains(FILE_NEW_NAME1)));

            //**********
            // Move test
            //**********
            // given there is an existing file
            // when we move it
            fs.move(FileFolder.INPUT, FileFolder.WORK, FILE_NEW_NAME1);
            // then it should be absent of source folder
            inputFiles = fs.listFiles(FileFolder.INPUT);
            assertFalse(inputFiles.stream().anyMatch(file -> file.getName().contains(FILE_NEW_NAME1)));
            // and should be present in dest folder
            workFiles = fs.listFiles(FileFolder.WORK);
            assertTrue(workFiles.stream().anyMatch(file -> file.getName().contains(FILE_NEW_NAME1)));

            //************
            // upload test
            //************
            // given there is no test-upload file
            outputFiles = fs.listFiles(FileFolder.OUTPUT);
            assertFalse(outputFiles.stream().anyMatch(file -> file.getName().contains(UPLOAD_FILE_TEST_NAME)));
            // when we upload one
            String pathToLocalFile = resourceLoader.getResource("classpath:common/filesystem/azure/" + UPLOAD_FILE_TEST_NAME).getFile().getPath();
            fs.upload(pathToLocalFile, FileFolder.OUTPUT, UPLOAD_FILE_TEST_NAME);
            //when we upload one with MultipartFile
            // then there should be one
            outputFiles = fs.listFiles(FileFolder.OUTPUT);
            assertTrue(outputFiles.stream().anyMatch(file -> file.getName().contains(UPLOAD_FILE_TEST_NAME)));

            //*********************
            // Move and rename test
            //*********************
            // given there is a file in work folder
            // when we move and rename this file
            fs.moveAndRename(FileFolder.WORK, FileFolder.OUTPUT, FILE_NEW_NAME1, FILE_NEW_NAME2);
            // then it should not be listed in source folder,
            workFiles = fs.listFiles(FileFolder.WORK);
            assertFalse(workFiles.stream().anyMatch(file -> file.getName().contains(FILE_NEW_NAME1)));
            // and new file should be in dest folder
            outputFiles = fs.listFiles(FileFolder.OUTPUT);
            assertTrue(outputFiles.stream().anyMatch(file -> file.getName().contains(FILE_NEW_NAME2)));

            //**********
            // Read test
            //**********
            // given there is a file
            // when we read it
            String readFileContent = new BufferedReader(
                    new InputStreamReader(fs.readFile(FileFolder.OUTPUT, FILE_NEW_NAME2), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            // then content should be available
            assertEquals(FILE_CONTENT, readFileContent);

            //***********************
            // Write with InputStream
            //***********************
            // given there is an existing file
            // when we write to the same file
            fs.writeFile(FileFolder.OUTPUT, FILE_NEW_NAME2, new ByteArrayInputStream(FILE_CONTENT.toUpperCase().getBytes()));
            // then content should be overridden
            readFileContent = new BufferedReader(
                    new InputStreamReader(fs.readFile(FileFolder.OUTPUT, FILE_NEW_NAME2), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            assertEquals(FILE_CONTENT.toUpperCase(), readFileContent);

            //************
            // Multipart upload test
            //************
            // given there is no test-multipart-upload file
            inputFiles = fs.listFiles(FileFolder.INPUT);
            assertFalse(inputFiles.stream().anyMatch(file -> file.getName().contains(UPLOAD_MULTIPART_FILE_TEST_NAME)));
            //then we upload the file
            MultipartFile fileInput = new MockMultipartFile("DATACENTER", UPLOAD_MULTIPART_FILE_TEST_NAME, String.valueOf(MediaType.TEXT_PLAIN), FILE_CONTENT.getBytes());
            fs.upload(FileFolder.INPUT, fileInput.getOriginalFilename(), fileInput.getName(), fileInput.getInputStream());
            //now there should be a file
            inputFiles = fs.listFiles(FileFolder.INPUT);
            assertTrue(inputFiles.stream().anyMatch(file -> file.getName().contains(UPLOAD_MULTIPART_FILE_TEST_NAME)));

        } catch (Exception e) {
            log.error("Exception occured during lifecycle", e);
            fail(e);
        } finally {
            fs.delete(FileFolder.INPUT, FILE_TEST_NAME);
            fs.delete(FileFolder.INPUT, FILE_NEW_NAME1);
            fs.delete(FileFolder.WORK, FILE_NEW_NAME1);
            fs.delete(FileFolder.WORK, FILE_NEW_NAME2);

            fs.delete(FileFolder.OUTPUT, FILE_NEW_NAME2);
            fs.delete(FileFolder.OUTPUT, UPLOAD_FILE_TEST_NAME);
            outputFiles = fs.listFiles(FileFolder.OUTPUT);
            assertFalse(outputFiles.stream().anyMatch(file -> file.getName().contains(FILE_NEW_NAME2)));
            assertFalse(outputFiles.stream().anyMatch(file -> file.getName().contains(UPLOAD_FILE_TEST_NAME)));

            fs.delete(FileFolder.INPUT, UPLOAD_MULTIPART_FILE_TEST_NAME);
            inputFiles = fs.listFiles(FileFolder.INPUT);
            assertFalse(inputFiles.stream().anyMatch(file -> file.getName().contains(UPLOAD_MULTIPART_FILE_TEST_NAME)));
        }

    }

    @Test
    void listFilesOfFolderShouldReturnInputStream() throws IOException {
        final String TMP_FOLDER = UUID.randomUUID().toString();
        final String FILE_NAME = "work_file";
        FileStorage fs = fileSystem.mount(SUBSCRIBER, ORGANIZATION);
        try {
            // Given an existing file in the work/<gen_folder>/DATACENTER/
            fs.writeFile(FileFolder.WORK, TMP_FOLDER + "/DATACENTER/" + FILE_NAME + "1", "Content");
            fs.writeFile(FileFolder.WORK, TMP_FOLDER + "/DATACENTER/" + FILE_NAME + "2", "Content");

            // when we list files
            assertTrue(fs.hasFileInSubfolder(FileFolder.WORK, TMP_FOLDER, FileType.DATACENTER));
            assertFalse(fs.hasFileInSubfolder(FileFolder.WORK, TMP_FOLDER, FileType.EQUIPEMENT_PHYSIQUE));
        } catch (Exception e) {
            log.error("Error occured with filesystem", e);
        } finally {
            // Cleanup
            fs.delete(FileFolder.WORK, TMP_FOLDER + "/DATACENTER/" + FILE_NAME + "1");
            fs.delete(FileFolder.WORK, TMP_FOLDER + "/DATACENTER/" + FILE_NAME + "2");
        }
    }

    @Test
    void listResourcesOfFolderShouldReturnInputStream() throws IOException {
        final String TMP_FOLDER = UUID.randomUUID().toString();
        final String FILE_NAME = "work_file";
        FileStorage fs = fileSystem.mount(SUBSCRIBER, ORGANIZATION);
        try {
            // Given an existing file in the work/<gen_folder>/DATACENTER/
            fs.writeFile(FileFolder.WORK, TMP_FOLDER + "/DATACENTER/" + FILE_NAME + "1", "Content");
            fs.writeFile(FileFolder.WORK, TMP_FOLDER + "/DATACENTER/" + FILE_NAME + "2", "Content");

            // when we list files
            Resource[] files = fs.listResources(FileFolder.WORK, TMP_FOLDER, FileType.DATACENTER);

            // we are able to read contents
            Arrays.stream(files).forEach(file -> {
                try {
                    Assertions.assertThat(FileCopyUtils.copyToString(new InputStreamReader(file.getInputStream()))).isEqualTo("Content");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } finally {
            // Cleanup
            fs.delete(FileFolder.WORK, TMP_FOLDER + "/DATACENTER/" + FILE_NAME + "1");
            fs.delete(FileFolder.WORK, TMP_FOLDER + "/DATACENTER/" + FILE_NAME + "2");
        }
    }

    @Test
    void getFileUrl_shouldCreateUrlWithTokenIfFileExists() throws IOException {
        final String TMP_FOLDER = UUID.randomUUID().toString();
        final String FILE_NAME_WITH_PATH = TMP_FOLDER + "/DATACENTER/work_file.zip";
        FileStorage fs = fileSystem.mount(SUBSCRIBER, ORGANIZATION);
        try {
            // Given an existing file in the work/<gen_folder>/DATACENTER/
            fs.writeFile(FileFolder.WORK, FILE_NAME_WITH_PATH, "Content");
            // when we get url
            final String url = fs.getFileUrl(FileFolder.WORK, FILE_NAME_WITH_PATH);
            // then it should point to the write file
            Assertions.assertThat(url).contains("work_file.zip");
        } finally {
            // Cleanup
            fs.delete(FileFolder.WORK, FILE_NAME_WITH_PATH);
        }
    }

    @Test
    void getFileUrl_shouldReturnEmptyStringIfFileDoesNotExists() throws IOException {
        FileStorage fs = fileSystem.mount(SUBSCRIBER, ORGANIZATION);
        // Given an unknown file in the work folder
        // when we get url
        final String url = fs.getFileUrl(FileFolder.WORK, "unknown_file.zip");
        // it should be empty
        Assertions.assertThat(url).isBlank();
    }

    @Test
    void getFileSize_shouldReturnMoreThanZeroForExistingFile() throws IOException {
        final FileStorage fs = fileSystem.mount(SUBSCRIBER, ORGANIZATION);
        final String FILE_NAME = "filename";
        try {
            // Given an existing file in the output folder
            fs.writeFile(FileFolder.OUTPUT, FILE_NAME, "Content");
            // when we get size
            final long size = fs.getFileSize(FileFolder.OUTPUT, FILE_NAME);
            // then it should be positive
            Assertions.assertThat(size).isPositive();
        } finally {
            // Cleanup
            fs.delete(FileFolder.OUTPUT, FILE_NAME);
        }
    }

    @Test
    void getFileSize_shouldReturnZeroForUnknownFile() throws IOException {
        FileStorage fs = fileSystem.mount(SUBSCRIBER, ORGANIZATION);
        // Given an unknown file in the work folder
        // when we get size
        final long size = fs.getFileSize(FileFolder.WORK, "unknown_file.zip");
        // it should be 0
        Assertions.assertThat(size).isZero();
    }

    @Test
    void deleteFolder_shouldDeleteAllFilesInFolder() throws IOException {
        FileStorage fs = fileSystem.mount(SUBSCRIBER, ORGANIZATION);
        final String folder = "DATACENTER/";

        // Given an existing file in the output folder
        fs.writeFile(FileFolder.OUTPUT, folder + "filename1", "Content");
        fs.writeFile(FileFolder.OUTPUT, folder + "filename2", "Content");

        fs.deleteFolder(FileFolder.OUTPUT, null);
        Assertions.assertThat(fs.listFiles(FileFolder.OUTPUT)).isEmpty();
    }
}
