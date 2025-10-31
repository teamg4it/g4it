package com.soprasteria.g4it.backend.apidigitalservice.modeldb;

import com.soprasteria.g4it.backend.common.dbmodel.Note;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "digital_service_version",
        uniqueConstraints = @UniqueConstraint(
                name = "digital_service_version_unique",
                columnNames = {"item_id", "description"}
        ))
@Data
public class DigitalServiceVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String uid;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "last_calculation_date")
    private LocalDateTime lastCalculationDate;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "item_id", nullable = false)
    private String itemId;

    @Column(name = "version_type", length = 50)
    private String versionType;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "note_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "note_id_digital_service_version_fk"))
    private Note note;


    @Column(name = "criteria")
    private List<String> criteria;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "task_id")
    private Long taskId;

    @ManyToOne
    @JoinColumn(name = "item_id", referencedColumnName = "uid",
            insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "digital_service_version_item_id_fk"))
    private DigitalService digitalService;
}
