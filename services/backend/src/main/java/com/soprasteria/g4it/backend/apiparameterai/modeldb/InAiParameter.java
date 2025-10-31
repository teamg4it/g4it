package com.soprasteria.g4it.backend.apiparameterai.modeldb;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * Ai parameter Entity
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "in_ai_parameters")

public class InAiParameter {
    /**
     * Primary Key: id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Linked Digital Service. Temporary field
     */
    private String digitalServiceUid;

    /**
     * Linked Digital Service Version UID
     */
    @Column(name = "digital_service_version_uid")
    private String digitalServiceVersionUid;

    /**
     * Model Name
     */
    private String modelName;

    /**
     * Type : LLM, CLASSIFICATION,REGRESSION.
     */
    private String type;

    /**
     * Parameters.
     */
    @NotNull
    private String nbParameters;

    /**
     * Framework.
     */
    @NotNull
    private String framework;

    /**
     * Quantization.
     */
    @NotNull
    private String quantization;

    /**
     * Total number of generated tokens.
     */
    @NotNull
    private BigInteger totalGeneratedTokens;

    /**
     * Number of users per year.
     */
    @NotNull
    private BigInteger numberUserYear;

    /**
     * Average number request per year.
     */
    @NotNull
    private BigInteger averageNumberRequest;

    /**
     * Average number token generated per request.
     */
    @NotNull
    private BigInteger averageNumberToken;

    /**
     * Inference.
     */
    @NotNull
    private Boolean isInference;

    /**
     * Finetuning.
     */
    @NotNull
    private Boolean isFinetuning;


    @EqualsAndHashCode.Exclude
    private LocalDateTime creationDate;

    @EqualsAndHashCode.Exclude
    private LocalDateTime lastUpdateDate;
}
