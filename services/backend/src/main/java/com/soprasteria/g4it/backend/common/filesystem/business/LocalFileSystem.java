/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.common.filesystem.business;

import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.Arrays;

/**
 * The local FileSystem implementation.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Profile("local")
public class LocalFileSystem implements FileSystem {

    private final String localPath;

    /**
     * Default local file system constructor.
     *
     * @param localPath the local path.
     */
    public LocalFileSystem(@Value("${filesystem.local.path}") String localPath) {
        this.localPath = localPath;
    }

    /**
     * Mount the local storage.
     *
     * @param organization the client organization.
     * @return the local file storage.
     */
    @Override
    public FileStorage mount(final String organization, final String workspace) {
        // We create the expected folder structure if it doesn't exist
        Arrays.stream(FileFolder.values()).forEach(e -> Paths.get(localPath, organization, workspace, e.getFolderName()).toFile().mkdirs());
        return new LocalFileStorage(Paths.get(localPath, organization, workspace).toString());
    }


}
