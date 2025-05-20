package com.soprasteria.g4it.backend.external.apiaiservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Estimations and recommendations made for of an AI Service configuration
 */
@Data
@SuperBuilder
public class AIServiceEstimationBO {

  @JsonProperty("electricityConsumption")
  private Float electricityConsumption;

  @JsonProperty("runtime")
  private Float runtime;

  @JsonProperty("recommendations")
  @Valid
  private List<@Valid RecommendationBO> recommendations = new ArrayList<>();

  public AIServiceEstimationBO() {
  }
}

