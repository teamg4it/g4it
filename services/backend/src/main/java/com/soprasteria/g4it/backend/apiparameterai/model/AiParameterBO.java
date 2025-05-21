package com.soprasteria.g4it.backend.apiparameterai.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigInteger;

/**
 * Ai parameters (Business Object).
 */

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AiParameterBO {
        private BigInteger id;
        private String modelName;
        private String nbParameters;
        private String framework;
        private String quantization;
        private BigInteger totalGeneratedTokens;
        private BigInteger numberUserYear;
        private BigInteger averageNumberRequest;
        private BigInteger averageNumberToken;
        private Boolean isInference;
        private Boolean isFinetuning;
}
