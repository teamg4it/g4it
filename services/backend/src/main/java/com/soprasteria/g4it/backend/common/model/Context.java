/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuperBuilder
@Getter
public class Context {

    private String subscriber;
    private Long organizationId;
    private String organizationName;
    private Long inventoryId;
    private String digitalServiceUid;
    private String digitalServiceName;
    private Locale locale;
    private LocalDateTime datetime;
    private boolean hasVirtualEquipments;
    private boolean hasApplications;
    private List<FileToLoad> filesToLoad = new ArrayList<>();
    private Long taskId;

    public String log() {
        return this.log("/");
    }

    public void addFileToLoad(FileToLoad fileToLoad) {
        filesToLoad.add(fileToLoad);
    }
    public void initFileToLoad(List<FileToLoad> fileToLoadList) {
        filesToLoad = fileToLoadList;
    }
    public void initTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String log(String delim) {
        if (inventoryId == null) {
            return String.join(delim, subscriber, organizationId.toString(), digitalServiceUid);
        }
        return String.join(delim, subscriber, organizationId.toString(), inventoryId.toString());
    }
}
