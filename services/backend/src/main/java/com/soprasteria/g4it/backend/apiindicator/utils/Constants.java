/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.utils;

import java.util.List;

/**
 * Constants Class.
 */
public final class Constants {

    /**
     * The organization parameter name.
     */
    public static final String PARAM_ORGANIZATION = "organization";

    /**
     * The workspace parameter name.
     */
    public static final String PARAM_WORKSPACE = "workspace";

    /**
     * The workspace id parameter name.
     */
    public static final String PARAM_WORKSPACE_ID = "workspaceId";

    /**
     * The inventory identifier parameter name.
     */
    public static final String PARAM_INVENTORY_ID = "inventoryId";

    /**
     * The inventory name parameter name.
     */
    public static final String PARAM_INVENTORY_NAME = "inventoryName";

    /**
     * The batch name parameter name.
     */
    public static final String PARAM_BATCH_NAME = "batchName";

    /**
     * The UID parameter name.
     */
    public static final String PARAM_UID = "uid";

    /**
     * The type parameter name.
     */
    public static final String PARAM_TYPE = "type";

    /**
     * The applicationName parameter name.
     */
    public static final String PARAM_APPLICATION_NAME = "applicationName";

    /**
     * The Unspecified constant
     */
    public static final String UNSPECIFIED = "(Unspecified)";

    /**
     * The Criteria number
     * Must be changed after dynamic criteria list
     */
    public static final Long CRITERIA_NUMBER = 11L;

    /**
     * The 11 criteria list
     */
    public static final List<String> CRITERIA_LIST = List.of(
            "climate-change", "ionising-radiation", "acidification",
            "particulate-matter", "resource-use", "ozone-depletion", "photochemical-ozone-formation",
            "eutrophication-terrestrial", "eutrophication-freshwater", "eutrophication-marine", "resource-use-fossils");

    private Constants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

}
