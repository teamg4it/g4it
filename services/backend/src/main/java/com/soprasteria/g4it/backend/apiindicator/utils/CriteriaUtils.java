/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.utils;

import java.util.Map;

/**
 * Utils class for criteria.
 */
public final class CriteriaUtils {

    /**
     * Private constructor to hive the implicit public one.
     */
    private CriteriaUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final Map<String, String> CRITERIA_MAP = Map.ofEntries(
            Map.entry("climate-change", "Climate change"),
            Map.entry("ionising-radiation", "Ionising radiation"),
            Map.entry("acidification", "Acidification"),
            Map.entry("particulate-matter", "Particulate matter and respiratory inorganics"),
            Map.entry("resource-use", "Resource use (minerals and metals)"),
            Map.entry("ozone-depletion", "Ozone depletion"),
            Map.entry("photochemical-ozone-formation", "Photochemical ozone formation"),
            Map.entry("eutrophication-terrestrial", "Eutrophication, terrestrial"),
            Map.entry("eutrophication-freshwater", "Eutrophication, freshwater"),
            Map.entry("eutrophication-marine", "Eutrophication, marine"),
            Map.entry("resource-use-fossils", "Resource use, fossils"),
            Map.entry("water-use", "Water use")
    );

    /**
     * Method to transform criteria label to criteria key.
     *
     * @param criteriaName the criteria label.
     * @return the criteria key.
     */
    public static String transformCriteriaNameToCriteriaKey(final String criteriaName) {
        return switch (criteriaName) {
            case "Climate change" -> "climate-change";
            case "Particulate matter and respiratory inorganics" -> "particulate-matter";
            case "Ionising radiation" -> "ionising-radiation";
            case "Acidification" -> "acidification";
            case "Resource use (minerals and metals)" -> "resource-use";
            case "Ozone depletion" -> "ozone-depletion";
            case "Photochemical ozone formation" -> "photochemical-ozone-formation";
            case "Eutrophication, terrestrial" -> "eutrophication-terrestrial";
            case "Eutrophication, freshwater" -> "eutrophication-freshwater";
            case "Eutrophication, marine" -> "eutrophication-marine";
            case "Resource use, fossils" -> "resource-use-fossils";
            case "Water use" -> "water-use";
            default -> "";
        };
    }

    /**
     * Method to transform criteria key to criteria label.
     *
     * @param criteriaKey the criteria key.
     * @return the criteria label.
     */
    public static String transformCriteriaKeyToCriteriaName(final String criteriaKey) {
        return CRITERIA_MAP.getOrDefault(criteriaKey, "");
    }

}
