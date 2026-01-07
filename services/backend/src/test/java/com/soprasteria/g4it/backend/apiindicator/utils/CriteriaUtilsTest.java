/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.utils;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CriteriaUtilsTest {

    @Test
    void testCriteriaUtils_transformCriteriaNameToCriteriaKey() {
        Assertions.assertEquals("climate-change", CriteriaUtils.transformCriteriaNameToCriteriaKey("Climate change"));
        Assertions.assertEquals("", CriteriaUtils.transformCriteriaNameToCriteriaKey("Unknown"));
    }

    @Test
    void testCriteriaUtils_transformCriteriaKeyToCriteriaName() {
        Assertions.assertEquals("Climate change", CriteriaUtils.transformCriteriaKeyToCriteriaName("climate-change"));
        Assertions.assertEquals("", CriteriaUtils.transformCriteriaNameToCriteriaKey("Unknown"));
    }

    @Test
    void shouldTransformCriteriaNameToCriteriaKey() {
        Assertions.assertEquals("climate-change",
                CriteriaUtils.transformCriteriaNameToCriteriaKey("Climate change"));
        Assertions.assertEquals("ionising-radiation",
                CriteriaUtils.transformCriteriaNameToCriteriaKey("Ionising radiation"));
        Assertions.assertEquals("acidification",
                CriteriaUtils.transformCriteriaNameToCriteriaKey("Acidification"));
        Assertions.assertEquals("particulate-matter",
                CriteriaUtils.transformCriteriaNameToCriteriaKey("Particulate matter and respiratory inorganics"));
        Assertions.assertEquals("resource-use",
                CriteriaUtils.transformCriteriaNameToCriteriaKey("Resource use (minerals and metals)"));
        Assertions.assertEquals("ozone-depletion",
                CriteriaUtils.transformCriteriaNameToCriteriaKey("Ozone depletion"));
        Assertions.assertEquals("photochemical-ozone-formation",
                CriteriaUtils.transformCriteriaNameToCriteriaKey("Photochemical ozone formation"));
        Assertions.assertEquals("eutrophication-terrestrial",
                CriteriaUtils.transformCriteriaNameToCriteriaKey("Eutrophication, terrestrial"));
        Assertions.assertEquals("eutrophication-freshwater",
                CriteriaUtils.transformCriteriaNameToCriteriaKey("Eutrophication, freshwater"));
        Assertions.assertEquals("eutrophication-marine",
                CriteriaUtils.transformCriteriaNameToCriteriaKey("Eutrophication, marine"));
        Assertions.assertEquals("resource-use-fossils",
                CriteriaUtils.transformCriteriaNameToCriteriaKey("Resource use, fossils"));
        Assertions.assertEquals("water-use",
                CriteriaUtils.transformCriteriaNameToCriteriaKey("Water use"));
    }

    @Test
    void shouldReturnEmptyStringForUnknownCriteriaName() {
        Assertions.assertEquals("",
                CriteriaUtils.transformCriteriaNameToCriteriaKey("Unknown"));
        Assertions.assertEquals("",
                CriteriaUtils.transformCriteriaNameToCriteriaKey(""));
    }

    // ---------- transformCriteriaKeyToCriteriaName ----------

    @Test
    void shouldTransformCriteriaKeyToCriteriaName() {
        Assertions.assertEquals("Climate change",
                CriteriaUtils.transformCriteriaKeyToCriteriaName("climate-change"));
        Assertions.assertEquals("Ionising radiation",
                CriteriaUtils.transformCriteriaKeyToCriteriaName("ionising-radiation"));
        Assertions.assertEquals("Acidification",
                CriteriaUtils.transformCriteriaKeyToCriteriaName("acidification"));
        Assertions.assertEquals("Particulate matter and respiratory inorganics",
                CriteriaUtils.transformCriteriaKeyToCriteriaName("particulate-matter"));
        Assertions.assertEquals("Resource use (minerals and metals)",
                CriteriaUtils.transformCriteriaKeyToCriteriaName("resource-use"));
        Assertions.assertEquals("Ozone depletion",
                CriteriaUtils.transformCriteriaKeyToCriteriaName("ozone-depletion"));
        Assertions.assertEquals("Photochemical ozone formation",
                CriteriaUtils.transformCriteriaKeyToCriteriaName("photochemical-ozone-formation"));
        Assertions.assertEquals("Eutrophication, terrestrial",
                CriteriaUtils.transformCriteriaKeyToCriteriaName("eutrophication-terrestrial"));
        Assertions.assertEquals("Eutrophication, freshwater",
                CriteriaUtils.transformCriteriaKeyToCriteriaName("eutrophication-freshwater"));
        Assertions.assertEquals("Eutrophication, marine",
                CriteriaUtils.transformCriteriaKeyToCriteriaName("eutrophication-marine"));
        Assertions.assertEquals("Resource use, fossils",
                CriteriaUtils.transformCriteriaKeyToCriteriaName("resource-use-fossils"));
        Assertions.assertEquals("Water use",
                CriteriaUtils.transformCriteriaKeyToCriteriaName("water-use"));
    }

    @Test
    void shouldReturnEmptyStringForUnknownCriteriaKey() {
        Assertions.assertEquals("",
                CriteriaUtils.transformCriteriaKeyToCriteriaName("unknown-key"));
        Assertions.assertEquals("",
                CriteriaUtils.transformCriteriaKeyToCriteriaName(""));
    }


}
