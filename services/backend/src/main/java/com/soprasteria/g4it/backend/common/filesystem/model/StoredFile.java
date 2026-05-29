/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.filesystem.model;

import java.nio.file.Path;

public class StoredFile {
    private final Path path;
    private final String originalFilename;
    private final String parameterName;
    private final String contentType;

    public StoredFile(Path path,
                      String originalFilename,
                      String parameterName, String contentType) {
        this.path = path;
        this.originalFilename = originalFilename;
        this.parameterName = parameterName;
        this.contentType = contentType;
    }

    public Path getPath() {
        return path;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getParameterName() {
        return parameterName;
    }
    public String getContentType() {
        return contentType;
    }
}
