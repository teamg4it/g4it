/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.model;

import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import lombok.Data;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class FileToLoad {

    private String filename;
    private String originalFileName;
    private FileType fileType;
    private Path filePath;
    private String organization;
    private Long workspaceId;
    private File convertedFile;

    private Map<Integer, List<LineError>> coherenceErrorByLineNumer = new HashMap<>();


    public void computeFileType() {
        if (filename != null) {
            if (filename.startsWith(FileType.DATACENTER.toString())) {
                fileType = FileType.DATACENTER;
            } else if (filename.startsWith(FileType.EQUIPEMENT_PHYSIQUE.toString())) {
                fileType = FileType.EQUIPEMENT_PHYSIQUE;
            } else if (filename.startsWith(FileType.EQUIPEMENT_VIRTUEL.toString())) {
                fileType = FileType.EQUIPEMENT_VIRTUEL;
            } else if (filename.startsWith(FileType.APPLICATION.toString())) {
                fileType = FileType.APPLICATION;
            } else {
                fileType = FileType.UNKNOWN;
            }
        }
    }


}
