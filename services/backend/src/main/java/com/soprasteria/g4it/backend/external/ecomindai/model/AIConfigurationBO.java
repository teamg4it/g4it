package com.soprasteria.g4it.backend.external.ecomindai.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * Estimations and recommendations made for of an AI Service configuration
 */

@Data
@SuperBuilder
public class AIConfigurationBO {

  private String modelName;

  private String nbParameters;

  private String framework;

  private String quantization;

  private Long totalGeneratedTokens;

}

