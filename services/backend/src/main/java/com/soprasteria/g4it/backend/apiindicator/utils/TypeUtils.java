/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.utils;

public class TypeUtils {

    private TypeUtils() {
        
    }

    /**
     * Get the type without prefix if it exists
     * prefix can be the organization_ or workspace_
     *
     * @param organization   the organization
     * @param workspace the workspace
     * @param type         the type
     * @return the type without prefix
     */
    public static String getShortType(final String organization, final String workspace, final String type) {
        if (type == null) return null;
        return type
                .replace(organization + "_", "")
                .replace(workspace + "_", "");
    }
}
