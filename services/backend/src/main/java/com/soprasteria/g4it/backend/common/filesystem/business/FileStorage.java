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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * File Storage handles basic operations on file,
 * within the constraint of the FileFolder.
 * All operations are synchronized (blocking).
 */
public interface FileStorage {
    InputStream readFile(final FileFolder folder, final String fileName) throws IOException;

    void writeFile(final FileFolder folder, final String fileName, final String content) throws IOException;

    void writeFile(final FileFolder folder, final String fileName, final InputStream content) throws IOException;

    List<FileDescription> listFiles(final FileFolder folder) throws IOException;

    boolean hasFileInSubfolder(final FileFolder folder, final String subfolder, final FileType fileType) throws IOException;

    Resource[] listResources(final FileFolder folder, final String subfolder, final FileType fileType) throws IOException;

    void rename(final FileFolder folder, final String currentName, final String newName) throws IOException;

    void move(final FileFolder srcFolder, final FileFolder destFolder, final String fileName) throws IOException;

    /**
     * Move and rename file from one folder to the other
     *
     * @param srcFolder   Input, Output or Work
     * @param destFolder  Input, Output or Work
     * @param currentName actual filename
     * @param newName     new filename
     */
    void moveAndRename(final FileFolder srcFolder, final FileFolder destFolder, final String currentName, final String newName) throws IOException;

    void delete(final FileFolder folder, final String fileName) throws IOException;

    /**
     * Delete a folder recursively : workspace/folder/path
     *
     * @param folder the FileFolder
     * @param path   the rest of the path
     */
    void deleteFolder(final FileFolder folder, final String path) throws IOException;

    /**
     * Upload a file to the storage
     *
     * @param fileLocalPath path to the file in local
     * @param folder        the destination folder
     * @param fileName      file name in the destination folder
     */
    void upload(final String fileLocalPath, final FileFolder folder, final String fileName) throws IOException;

    String upload(final FileFolder folder, final String fileName, final String type, final InputStream fileContent) throws IOException;

    String getFileUrl(final FileFolder folder, final String fileName);

    long getFileSize(final FileFolder folder, final String fileName);

    void renameWorkspace(final String newWorkspace) throws IOException;

}
