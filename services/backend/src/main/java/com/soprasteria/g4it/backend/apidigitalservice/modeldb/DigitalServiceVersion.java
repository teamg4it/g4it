/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apidigitalservice.modeldb;

import com.soprasteria.g4it.backend.common.dbmodel.Note;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Digital Service Version Entity
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "digital_service_version")
public class DigitalServiceVersion {

    /**
     * Primary Key : uid.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String uid;

    /**
     * Version name/description.
     */
    @NotNull
    private String description;

    /**
     * Reference to the Digital Service.
     */
    @NotNull
    private String itemId;

    /**
     * Version type (archived, active, draft, current).
     */
    @NotNull
    private String versionType;

    /**
     * Version creator.
     */
    private Long createdBy;

    /**
     * The Criteria key.
     */
    private List<String> criteria;

    /**
     * Last calculation date.
     */
    private LocalDateTime lastCalculationDate;

    /**
     * Auto creation date.
     */
    private LocalDateTime creationDate;

    /**
     * Last update date.
     */
    private LocalDateTime lastUpdateDate;

    /**
     * Attached note.
     */
    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "note_id", referencedColumnName = "id")
    private Note note;

    /**
     * The Task
     */
    @Builder.Default
    @ToString.Exclude
    @OneToMany(mappedBy = "digitalServiceVersion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Task> tasks = new ArrayList<>();

}
