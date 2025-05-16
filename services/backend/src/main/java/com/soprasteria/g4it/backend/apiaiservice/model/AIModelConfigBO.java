package com.soprasteria.g4it.backend.apiaiservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.SuperBuilder;


@Data
@SuperBuilder
public class AIModelConfigBO {

  @JsonProperty("modelName")
  private String modelName;

  @JsonProperty("parameters")
  private String parameters;

  @JsonProperty("framework")
  private String framework;

  @JsonProperty("quantization")
  private String quantization;

  public AIModelConfigBO() {
  }
}

