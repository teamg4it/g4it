/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinventory.modeldb;

import com.soprasteria.g4it.backend.apibatchexport.modeldb.ExportReport;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.common.dbmodel.AbstractBaseEntity;
import com.soprasteria.g4it.backend.common.dbmodel.Note;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@SuperBuilder
@Entity
@Table(name = "inventory")
public class Inventory extends AbstractBaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 255, message = "Name must have less than 255 characters")
    @Pattern(regexp = "^[^<>]+$", message = "Special characters are not allowed")
    private String name;

    /**
     * DEPRECATED
     */
    private String inventoryDate;

    private String type;

    /**
     * The Criterias key.
     */
    private List<String> criteria;

    /**
     * is new arch
     */
    private Boolean isNewArch;

    /**
     * do export
     */
    private Boolean doExport;

    /**
     * do export verbose
     */
    private Boolean doExportVerbose;

    /**
     * is migrated to new arc
     */
    private Boolean isMigrated;

    /**
     * Attached note.
     */
    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "note_id", referencedColumnName = "id")
    private Note note;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(optional = false)
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    private Organization organization;

    @ToString.Exclude
    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "inventory", cascade = CascadeType.REMOVE)
    private List<DataCenter> dataCenterList;

    @Builder.Default
    private Long dataCenterCount = 0L;

    @ToString.Exclude
    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "inventory")
    private List<PhysicalEquipment> physicalEquipmentList;

    @Builder.Default
    private Long physicalEquipmentCount = 0L;

    @ToString.Exclude
    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "inventory", cascade = CascadeType.REMOVE)
    private List<VirtualEquipment> virtualEquipments;

    @Builder.Default
    private Long virtualEquipmentCount = 0L;

    @ToString.Exclude
    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "inventory", cascade = CascadeType.REMOVE)
    private List<Application> applications;

    @Builder.Default
    private Long applicationCount = 0L;

    @Builder.Default
    @ToString.Exclude
    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<InventoryIntegrationReport> integrationReports = new ArrayList<>();

    @Builder.Default
    @ToString.Exclude
    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<InventoryEvaluationReport> evaluationReports = new ArrayList<>();

    @ToString.Exclude
    @OneToOne(mappedBy = "inventory", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private ExportReport exportReport;

    @Builder.Default
    @ToString.Exclude
    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Task> tasks = new ArrayList<>();

    /**
     * Add integration report to inventory.
     *
     * @param integrationReport the report to add.
     */
    public void addIntegrationReport(final InventoryIntegrationReport integrationReport) {
        integrationReport.setInventory(this);
        this.integrationReports.add(integrationReport);
    }

    /**
     * Add integration report to inventory.
     *
     * @param evaluationReport the report to add.
     */
    public void addEvaluationReport(final InventoryEvaluationReport evaluationReport) {
        evaluationReport.setInventory(this);
        this.evaluationReports.add(evaluationReport);
    }

}
