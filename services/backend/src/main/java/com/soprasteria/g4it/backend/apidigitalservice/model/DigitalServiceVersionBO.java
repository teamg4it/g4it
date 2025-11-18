/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apidigitalservice.model;

import com.soprasteria.g4it.backend.common.model.NoteBO;
import com.soprasteria.g4it.backend.common.task.model.TaskBO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;


/**
 * Digital Service Version Business Object.
 */
@Data
@SuperBuilder
@AllArgsConstructor
public class DigitalServiceVersionBO {

    /**
     * Unique identifier of digitalService.
     */
    private String itemId;

    /**
     * Digital Service Version unique identifier.
     */
    private String dsvUid;

    /**
     * Name.
     */
    private String name;

    /**
     * Description / Version name.
     */
    private String versionName;

    /**
     * Name of user who created the digital service.
     */
    private String userName;

    /**
     * Version type (archived, active, draft, current).
     */
    private String versionType;

    /**
     * The Criterias key.
     */
    private List<String> criteria;

    /**
     * Creation date.
     */
    @EqualsAndHashCode.Exclude
    private LocalDateTime creationDate;

    /**
     * Last update date.
     */
    @EqualsAndHashCode.Exclude
    private LocalDateTime lastUpdateDate;

    /**
     * Last calculation date.
     */
    @EqualsAndHashCode.Exclude
    private LocalDateTime lastCalculationDate;

    /**
     * Note
     */
    private NoteBO note;

    /**
     * Task ID
     */
    private Long taskId;

    /**
     * tasks
     */
    private List<TaskBO> tasks;

    /**
     * isAi
     */
    private Boolean isAi;

    /**
     * enableDataInconsistency
     */
    private Boolean enableDataInconsistency;

    /**
     * isShared
     */
    private Boolean isShared;

}
