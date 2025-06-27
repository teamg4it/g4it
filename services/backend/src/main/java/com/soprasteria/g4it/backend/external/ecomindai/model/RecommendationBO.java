package com.soprasteria.g4it.backend.external.ecomindai.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * Recommendation of best practices to decrease the impact
 */
@Data
@SuperBuilder
public class RecommendationBO {

  @JsonProperty("type")
  private String type;

  @JsonProperty("topic")
  private String topic;

  @JsonProperty("example")
  private String example;

  @JsonProperty("expectedReduction")
  private String expectedReduction;

  public RecommendationBO() {
  }
}

