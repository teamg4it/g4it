package com.soprasteria.g4it.backend.apiparameterai.modeldb;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.math.BigInteger;

/**
 * Ai parameter Entity
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "ai_parameters")

public class AiParameter {
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


}
