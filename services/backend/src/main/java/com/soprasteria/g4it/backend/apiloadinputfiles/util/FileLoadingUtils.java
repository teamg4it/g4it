/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.util;

import com.soprasteria.g4it.backend.apifiles.business.FileSystemService;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.FileConversionService;
import com.soprasteria.g4it.backend.common.filesystem.business.FileStorage;
import com.soprasteria.g4it.backend.common.filesystem.business.FileSystem;
import com.soprasteria.g4it.backend.common.filesystem.business.local.LocalFileService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.mapper.FileDescriptionRestMapper;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.exception.AsyncTaskException;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class FileLoadingUtils {

    @Autowired
    FileSystemService fileSystemService;
    @Autowired
    LocalFileService localFileService;
    @Value("${local.working.folder}")
    private String localWorkingFolder;
    @Autowired
    private FileSystem fileSystem;
    @Autowired
    private FileDescriptionRestMapper fileDescriptionRestMapper;

    @Autowired
    private FileConversionService fileConversionService;


    /**
     * Download a file from file storage and put it in storagetmp/input/inventory
     *
     * @param context the context
     * @return the file path
     */
    public void downloadAllFileToLoad(Context context) {
        for (FileToLoad fileToLoad : context.getFilesToLoad()) {
            try (InputStream is = fileSystemService.downloadFile(context.getOrganization(), context.getWorkspaceId(), FileFolder.INPUT, fileToLoad.getFilename())) {
                // copy file to local storage tmp
                FileUtils.copyInputStreamToFile(is, fileToLoad.getFilePath().toFile());
            } catch (IOException e) {
                throw new AsyncTaskException(String.format("%s - Cannot download file %s from storage", context.log(), fileToLoad.getOriginalFileName()), e);
            }
        }


    }

    /**
     * Compute the folder path of the rejected file folder
     *
     * @param pathId : the given pathId
     * @return : the path of the rejected folder
     */
    public Path computeRejectedFolderPath(String pathId) {

        return Path.of(localWorkingFolder).resolve("rejected").resolve(pathId);

    }

    /**
     * Clean filenames from local and file storage
     *
     * @param fileStorage the file storage
     * @param filenames   the filename list
     */
    private void cleanFromLocalAndFileStorage(String inputPath, FileStorage fileStorage, List<String> filenames) {
        try {
            for (String filename : filenames) {
                Files.delete(Path.of(localWorkingFolder).resolve(inputPath).resolve(filename));
            }

            for (String filename : filenames) {
                fileStorage.delete(FileFolder.INPUT, filename);
            }
        } catch (IOException e) {
            throw new AsyncTaskException("An error occurred on cleaning files in local or remote storage", e);
        }
    }

    public boolean handelRejectedFiles(String organization, Long workspaceId, Long inventoryId, String digitalServiceUid,
                                       Long taskId, List<String> filenames) {

        String pathId = null != inventoryId ? String.valueOf(inventoryId) : digitalServiceUid;
        String inputPath = null != inventoryId ? "input/inventory" : "input/digital-service";

        FileStorage fileStorage = fileSystem.mount(organization, workspaceId.toString());
        boolean hasRejectedFile = uploadRejectedZip(pathId, taskId, fileStorage);
        cleanFromLocalAndFileStorage(inputPath, fileStorage, filenames);
        return hasRejectedFile;
    }

    /**
     * Upload rejected zip file
     *
     * @param pathId      the pathId
     * @param taskId      the task id
     * @param fileStorage the file storage
     * @return true if has any zip uploaded
     */
    private boolean uploadRejectedZip(String pathId, Long taskId, FileStorage fileStorage) {
        try {
            final Path rejectedFolderPath = computeRejectedFolderPath(pathId);
            if (Files.exists(rejectedFolderPath) && !localFileService.isEmpty(rejectedFolderPath)) {
                // create rejected zip file
                final File rejectedZipFile = localFileService.createZipFile(rejectedFolderPath, rejectedFolderPath.resolve(Constants.REJECTED_FILES_ZIP).toString());

                // send zip to file storage
                fileStorage.upload(rejectedZipFile.getAbsolutePath(), FileFolder.OUTPUT, taskId + "/" + rejectedZipFile.getName());

                // clear directory
                Arrays.stream(Objects.requireNonNull(rejectedFolderPath.toFile().listFiles())).forEach(File::delete);
                return true;
            }
            return false;
        } catch (IOException e) {
            throw new AsyncTaskException("An error occurred on file upload of rejected zip file", e);
        }
    }

    /**
     * Get original filename
     *
     * @param fileType the file type
     * @param filename the filename
     * @return the original filename
     */
    public String getOriginalFilename(final FileType fileType, final String filename) {
        String patternString = "^" + fileType.toString() + "_([^_]*)";
        Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(filename);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return "";
    }

    public List<FileToLoad> mapFileToLoad(List<String> filenames, boolean isInventory) {
        String inputPath = isInventory ? "input/inventory" : "input/digital-service";
        return filenames.stream().map(filename -> {
            FileToLoad fileToLoadDto = new FileToLoad();
            fileToLoadDto.setFilename(filename);
            fileToLoadDto.computeFileType();
            fileToLoadDto.setOriginalFileName(getOriginalFilename(fileToLoadDto.getFileType(), filename));
            fileToLoadDto.setFilePath(Path.of(localWorkingFolder).resolve(inputPath).resolve(filename));
            return fileToLoadDto;
        }).toList();

    }

    public void convertAllFileToLoad(Context context) throws AsyncTaskException {

        for (FileToLoad fileToLoad : context.getFilesToLoad()) {
            try {
                fileToLoad.setConvertedFile(fileConversionService.convertFileToCsv(fileToLoad.getFilePath().toFile(), fileToLoad.getOriginalFileName()));
            } catch (IOException | IllegalArgumentException e) {
                throw new AsyncTaskException(String.format("%s - Error while converting file '%s'", context.log(),
                        fileToLoad.getOriginalFileName()), e);
            }
        }

    }

    public void cleanConvertedFiles(Context context) {
        for (FileToLoad fileToLoad : context.getFilesToLoad()) {
            try {
                Files.delete(fileToLoad.getConvertedFile().toPath());
            } catch (IOException e) {
                throw new AsyncTaskException(String.format("%s - Error while deleting converted file '%s'", context.log(),
                        fileToLoad.getOriginalFileName()), e);
            }
        }
    }
}