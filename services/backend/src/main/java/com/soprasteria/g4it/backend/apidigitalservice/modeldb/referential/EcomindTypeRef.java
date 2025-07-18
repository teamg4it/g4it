package com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "ref_ecomind_type_ai")
public class EcomindTypeRef {
    /**
     * To prevent update.
     */
    @PreUpdate
    private void preUpdate() {
        throw new UnsupportedOperationException();
    }

    /**
     * Auto Generated ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Device's description.
     */
    private String description;

    /**
     * NumEcoEval Reference.
     */
    private String reference;

    /**
     * External description.
     */
    private String externalReferentialDescription;

    /**
     * Device's lifespan.
     */
    private Double lifespan;

    /**
     * Default cpu cores value
     */
    private Double defaultCpuCores;

    /**
     * Default GPU count value
     */
    private Long defaultGpuCount;

    /**
     * Default GPU memory value
     */
    private Long defaultGpuMemory;
    /**
     * Default RAM size value
     */
    private Double defaultRamSize;
    /**
     * Default Datacenter PUE value
     */
    private Double defaultDatacenterPue;
}
