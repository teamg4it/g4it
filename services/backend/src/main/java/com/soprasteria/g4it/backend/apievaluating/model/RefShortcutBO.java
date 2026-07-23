/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apievaluating.model;

import com.google.common.collect.BiMap;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

public record RefShortcutBO(Map<String, String> unitMap, BiMap<String, String> criterionMap,
                            BiMap<String, String> lifecycleStepMap,
                            Map<Pair<String, String>, Integer> elecMixQuartiles,
                            Map<String, String> levelMap) {
}
