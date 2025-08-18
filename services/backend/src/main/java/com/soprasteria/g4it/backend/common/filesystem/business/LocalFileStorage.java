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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.FileSystemUtils;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * The local FileStorage implementation.
 */
@RequiredArgsConstructor
@Slf4j
public class LocalFileStorage implements FileStorage {

    /**
     * The local path.
     */
    private final String localPath;

    @Override
    public InputStream readFile(final FileFolder folder, final String fileName) throws IOException {
        File file = new File(localPath + File.separator + folder.getFolderName() + File.separator + fileName);
        return new FileInputStream(file);
    }

    @Override
    public void writeFile(final FileFolder folder, final String fileName, final String content) throws IOException {
        File fileWithPath = new File(localPath + File.separator + folder.getFolderName() + File.separator + fileName);
        createNecessaryFolders(fileWithPath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileWithPath))) {
            writer.write(content);
        }
    }

    @Override
    public void writeFile(final FileFolder folder, final String fileName, final InputStream content) throws IOException {
        File fileWithPath = new File(localPath + File.separator + folder.getFolderName() + File.separator + fileName);
        createNecessaryFolders(fileWithPath);
        try (OutputStream os = new FileOutputStream(fileWithPath.toString())) {
            content.transferTo(os);
        }
    }

    @Override
    public List<FileDescription> listFiles(final FileFolder folder) throws IOException {

        final Path folderPath = toPath(folder);
        final String organization = Path.of(localPath).getFileName().toString();

        try (Stream<Path> walk = Files.walk(folderPath)) {
            return walk
                    .filter(Files::isRegularFile)
                    .map(p -> {
                                try {
                                    final BasicFileAttributes attributes = Files.readAttributes(p, BasicFileAttributes.class);

                                    String relativeFilePath = String.join(File.separator, organization, folder.getFolderName(), p.toFile().getAbsolutePath().replace(folderPath.toFile().getAbsolutePath(), "").substring(1));

                                    return FileDescription.builder()
                                            .name(relativeFilePath)
                                            .type(FileType.UNKNOWN)
                                            .metadata(Map.of(
                                                    "creationTime", attributes.creationTime().toString(),
                                                    "size", String.valueOf(attributes.size())
                                            ))
                                            .build();
                                } catch (final IOException e) {
                                    log.error("Cannot read attributes of file {}", p.toFile().getAbsolutePath(), e);
                                }
                                return FileDescription.builder().build();
                            }
                    )
                    .toList();
        }
    }

    @Override
    public boolean hasFileInSubfolder(final FileFolder folder, final String subfolder, final FileType fileType) throws
            IOException {
        final Path path = toPath(folder, subfolder, fileType.name());
        if (Files.isDirectory(path)) {
            try (final Stream<Path> entries = Files.list(path)) {
                return entries.findFirst().isPresent();
            }
        }
        return false;
    }

    @Override
    public Resource[] listResources(final FileFolder folder, final String subfolder, final FileType fileType) throws
            IOException {
        final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        return resourcePatternResolver.getResources("file:" + resourcePath(folder, subfolder, fileType));
    }

    @Override
    public void rename(final FileFolder folder, final String currentName, final String newName) throws IOException {
        Path source = toPath(folder, currentName);
        Files.move(source, source.resolveSibling(newName));
    }

    @Override
    public void move(final FileFolder srcFolder, final FileFolder destFolder, final String fileName) throws
            IOException {
        Path destPath = toPath(destFolder, fileName);
        createNecessaryFolders(destPath);
        Files.move(toPath(srcFolder, fileName), destPath, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void moveAndRename(final FileFolder srcFolder, final FileFolder destFolder, final String currentName,
                              final String newName) throws IOException {
        Path destPath = toPath(destFolder, newName);
        createNecessaryFolders(destPath);
        Files.move(toPath(srcFolder, currentName), destPath, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void delete(final FileFolder folder, final String fileName) throws IOException {
        final Path path = toPath(folder, fileName);
        Files.deleteIfExists(path);
    }

    @Override
    public void deleteFolder(final FileFolder folder, final String fileName) throws IOException {
        final Path path = fileName == null ?
                toPath(folder) :
                toPath(folder, fileName);
        FileSystemUtils.deleteRecursively(path);
    }

    @Override
    public void upload(final String fileLocalPath, final FileFolder folder, final String fileName) throws
            IOException {
        final Path copied = toPath(folder, fileName);
        final Path originalPath = Path.of(fileLocalPath);
        createNecessaryFolders(copied);
        Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public String upload(final FileFolder folder, final String fileName, final String type,
                         final InputStream fileContent) throws IOException {
        // Upload with a multipart file
        writeFile(folder, fileName, fileContent);
        return toPath(folder, fileName).toString();
    }

    @Override
    public String getFileUrl(final FileFolder folder, final String fileName) {
        final Path filePath = toPath(folder, fileName);
        if (filePath.toFile().exists()) {
            return filePath.toUri().toString();
        }
        return "";
    }

    @Override
    public long getFileSize(final FileFolder folder, final String fileName) {
        return new File(localPath + File.separator + folder.getFolderName() + File.separator + fileName).length();
    }

    @Override
    public void renameOrganization(String newOrganization) throws IOException {
        Files.move(Path.of(localPath), Path.of(Path.of(localPath).getParent().toString(), newOrganization));
    }

    private Path toPath(final FileFolder folder) {
        return toPath(folder, "");
    }

    private Path toPath(final FileFolder folder, final String... subpath) {
        return Paths.get(Path.of(localPath, folder.getFolderName()).toString(), subpath);
    }

    private String toStrPath(final FileFolder folder, final String... subpath) {
        return toPath(folder, subpath).toString();
    }

    private String resourcePath(final FileFolder folder, final String subfolder, final FileType fileType) {
        return String.join(FileSystems.getDefault().getSeparator(), toStrPath(folder, subfolder, fileType.name()), "*").replace("\\", "/");
    }

    private void createNecessaryFolders(final File filePath) {
        filePath.getParentFile().mkdirs();
    }

    private void createNecessaryFolders(final Path filePath) {
        createNecessaryFolders(filePath.toFile());
    }

}
