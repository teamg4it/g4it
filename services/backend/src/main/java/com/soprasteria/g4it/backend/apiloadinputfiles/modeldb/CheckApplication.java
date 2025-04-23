/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.modeldb;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "check_inv_load_application")
public class CheckApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "check_inv_load_application_id_seq")
    @SequenceGenerator(name = "check_inv_load_application_id_seq", sequenceName = "check_inv_load_application_id_seq", allocationSize = 100)
    private Long id;

    /**
     * Task id
     */
    @Column(name = "task_id")
    private Long taskId;

    /**
     * Virtual equipment name
     */
    @Column(name = "virtual_equipment_name")
    private String virtualEquipmentName;

    /**
     * Application name
     */
    @Column(name = "application_name")
    private String applicationName;
    
    /**
     * Environment type
     */
    @Column(name = "environment_type")
    private String environmentType;

    /**
     * Input source filename
     */
    @Column(name = "filename")
    private String fileName;

    /**
     * Line Number.
     */
    @Column(name = "line_nb")
    private Integer lineNumber;

    /**
     * Creation Date
     */
    @EqualsAndHashCode.Exclude
    @Column(name = "creation_date")
    private LocalDateTime creationDate;

}