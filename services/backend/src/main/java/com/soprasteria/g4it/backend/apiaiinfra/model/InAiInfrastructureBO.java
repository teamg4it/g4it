package com.soprasteria.g4it.backend.apiaiinfra.model;

import com.soprasteria.g4it.backend.apidigitalservice.model.EcomindTypeBO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.lang.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class InAiInfrastructureBO {

    private EcomindTypeBO infrastructureType;

    private @Nullable Long nbCpuCores;

    private @Nullable Long nbGpu;

    private @Nullable Long gpuMemory;

    private @Nullable Long ramSize;

    private String location;

    private @Nullable Double pue;

    private @Nullable Double complementaryPue;

}