/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apievaluating.mapper;

import com.soprasteria.g4it.backend.apievaluating.model.RefShortcutBO;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import org.junit.jupiter.api.Test;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementPhysique;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

class PhysicalEquipmentGroupingTest {

    private final AggregationToOutput mapper = new AggregationToOutput() {};

    @Test
    void inventoryGroupsByModelButDigitalServiceRetainsEquipmentIdentity() {
        var equipment = InPhysicalEquipment.builder()
                .name("source-name").model("shared-model").type("Laptop").build();
        var impact = ImpactEquipementPhysique.builder().critere("climate").etapeACV("USING").build();
        var shortcut = mock(RefShortcutBO.class, RETURNS_DEEP_STUBS);

        assertEquals("shared-model",
                mapper.keyPhysicalEquipment(equipment, null, impact, shortcut, false).getFirst());
        assertEquals("source-name",
                mapper.keyPhysicalEquipment(equipment, null, impact, shortcut, true).getFirst());
        equipment.setModel(null);
        assertEquals("",
                mapper.keyPhysicalEquipment(equipment, null, impact, shortcut, false).getFirst());
        assertEquals("source-name",
                mapper.keyPhysicalEquipment(equipment, null, impact, shortcut, true).getFirst());
    }
}
