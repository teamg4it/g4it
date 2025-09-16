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
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class MockFileStorage implements FileStorage {

    @Override
    public InputStream readFile(FileFolder folder, String fileName) throws IOException {
        if ("unknown.csv".equals(fileName)) throw new IOException();
        return new ByteArrayInputStream("File content".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void writeFile(FileFolder folder, String fileName, String content) throws IOException {
        // do nothing
    }

    @Override
    public void writeFile(FileFolder folder, String fileName, InputStream content) throws IOException {
        // do nothing
    }

    @Override
    public List<FileDescription> listFiles(final FileFolder folder) throws IOException {
        FileDescription file1 = FileDescription.builder().name("file1.txt").type(FileType.UNKNOWN).metadata(Collections.emptyMap()).build();
        FileDescription file2 = FileDescription.builder().name("file2.csv").type(FileType.UNKNOWN).metadata(Collections.emptyMap()).build();
        return List.of(file1, file2);
    }

    @Override
    public boolean hasFileInSubfolder(FileFolder folder, String subfolder, FileType fileType) throws IOException {
        return false;
    }

    @Override
    public Resource[] listResources(FileFolder folder, String subfolder, FileType fileType) throws IOException {
        return new Resource[0];
    }

    @Override
    public void rename(FileFolder folder, String currentName, String newName) {
        // do nothing
    }

    @Override
    public void move(FileFolder srcFolder, FileFolder destFolder, String fileName) {
        // do nothing
    }

    @Override
    public void moveAndRename(FileFolder srcFolder, FileFolder destFolder, String currentName, String newName) {
        // do nothing
    }

    @Override
    public void delete(FileFolder folder, String fileName) {
        // do nothing
    }

    @Override
    public void deleteFolder(FileFolder folder, String path) {
        // do nothing
    }

    @Override
    public void upload(String fileLocalPath, FileFolder folder, String fileName) throws IOException {
        // do nothing
    }

    @Override
    public String upload(FileFolder folder, String fileName, String type, InputStream fileContent) throws IOException {
        // do nothing
        return toString();
    }

    @Override
    public String getFileUrl(FileFolder folder, String fileName) {
        return "file:c:\\file.txt";
    }

    @Override
    public long getFileSize(FileFolder folder, String fileName) {
        return 0;
    }

    @Override
    public void renameWorkspace(String newWorkspace) throws IOException {
        // do nothing
    }
}
