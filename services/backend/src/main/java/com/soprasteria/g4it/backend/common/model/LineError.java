/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.model;

import java.util.Optional;

public record LineError(String filename, int line, String error, Optional<String> equipementName){
    public LineError(String filename, int line, String error) {
        this(filename, line, error, Optional.empty());
    }

    public LineError(String filename, int line, String error, String equipementName) {
        this(filename, line, error, Optional.of(equipementName));
    }

}
