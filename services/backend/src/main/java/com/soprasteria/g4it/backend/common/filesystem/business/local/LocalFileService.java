/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.filesystem.business.local;

import com.soprasteria.g4it.backend.exception.G4itRestException;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.zeroturnaround.zip.FileSource;
import org.zeroturnaround.zip.ZipEntrySource;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Class used for local file management, regarding any deployed environment
 */
@Component
@Slf4j
public class LocalFileService {

    /**
     * Create zip file from all the csv files
     *
     * @param directory directory path to create zip file
     * @return zip file
     */
    public File createZipFile(final Path directory, final String outputFilepath) {
        File file = new File(outputFilepath);
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(directory)) {
            final List<ZipEntrySource> entries = new ArrayList<>();
            paths.forEach(path -> Try.run(() -> entries.add(new FileSource(path.getFileName().toString(), path.toFile()))));
            ZipUtil.pack(entries.toArray(new ZipEntrySource[0]), file);
        } catch (Exception exc) {
            log.warn("error in creating zip file from csv files");
        }
        return file;
    }

    /**
     * Write all lines in file.
     *
     * @param filePath the filePath.
     * @param lines    lines to write.
     */
    public void writeFile(final Path filePath, final List<String> lines) {
        try (PrintWriter pw = new PrintWriter(filePath.toFile())) {
            lines.forEach(pw::println);
        } catch (FileNotFoundException e) {
            throw new G4itRestException("500", "File not found to write " + filePath.getFileName());
        }
    }

    /**
     * Returns true if directory with path is empty
     *
     * @param path the directory path
     * @return true if dir empty
     * @throws IOException io exception
     */
    public boolean isEmpty(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (Stream<Path> entries = Files.list(path)) {
                return entries.findFirst().isEmpty();
            }
        }
        return false;
    }

}
