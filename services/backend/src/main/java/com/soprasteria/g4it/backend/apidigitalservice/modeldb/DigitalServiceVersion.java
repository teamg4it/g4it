package com.soprasteria.g4it.backend.apidigitalservice.modeldb;

import com.soprasteria.g4it.backend.common.dbmodel.Note;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "digital_service_version")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", referencedColumnName = "uid",
            insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "digital_service_version_item_id_fk"))
    private DigitalService digitalService;

    @Column(name = "task_id")
    private Long taskId;
}
