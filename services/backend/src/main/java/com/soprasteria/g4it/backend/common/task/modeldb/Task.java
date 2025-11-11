/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.task.modeldb;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceVersion;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.List;

/**
 * G4IT tasks
 */
@Data
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "task")
public class Task {
    /**
     * Unique identifier.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Linked inventory.
     */
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id")
    private Inventory inventory;

    /**
     * Linked Digital Service.
     */
//    private String digitalServiceUid;

    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "digital_service_uid")
    private DigitalService digitalService;

    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "digital_service_version_uid")
    private DigitalServiceVersion digitalServiceVersion;

    /**
     * Type.
     */
    private String type;

    /**
     * Calculation status.
     */
    private String status;

    /**
     * Batch progress in percentage.
     */
    private String progressPercentage;

    /**
     * Criteria list to evaluate impacts on
     */
    private List<String> criteria;

    /**
     * The result file url
     */
    private String resultFileUrl;

    /**
     * The result file size
     */
    private Long resultFileSize;

    /**
     * Batch creation time.
     */
    private LocalDateTime creationDate;

    /**
     * Batch last update time.
     */
    private LocalDateTime lastUpdateDate;

    /**
     * Batch end time.
     */
    private LocalDateTime endTime;

    /**
     * Details list
     */
    private List<String> details;

    /**
     * Errors list
     */
    private List<String> errors;

    /**
     * Filenames list
     */
    private List<String> filenames;

    /**
     * User which created the task
     */
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    private User createdBy;
}
