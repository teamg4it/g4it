// This file has been created (totally or partially) with the assistance of artificial intelligence tools.
// All content has been generated under the direct supervision of a named individual,
// and under the AI.Backbone Orchestrator Compliance framework

package com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice;

import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.PhysicalEquipmentEvaluationTraceRepository;
import com.soprasteria.g4it.backend.apireferential.business.ItemCategoryResolver;
import com.soprasteria.g4it.backend.apireferential.business.PhysicalEquipmentReferenceResolver;
import com.soprasteria.g4it.backend.apireferential.modeldb.ItemType;
import com.soprasteria.g4it.backend.apireferential.repository.ItemTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhysicalEquipmentAllocationServiceTest {
    @Mock
    private ItemTypeRepository itemTypes;
    @Mock
    private PhysicalEquipmentReferenceResolver references;
    @Mock
    private PhysicalEquipmentEvaluationTraceRepository traces;

    private PhysicalEquipmentAllocationService service;

    @BeforeEach
    void setUp() {
        service = new PhysicalEquipmentAllocationService(new ItemCategoryResolver(itemTypes), references, traces);
    }

    @Test
    void storesTheTaskScopedReferenceCategoryAndUnallocatedSourceValues() {
        when(itemTypes.findByWorkspaceIdOrWorkspaceIdIsNull(7L)).thenReturn(List.of(
                ItemType.builder().type("laptop").itemCategory("DEVICES").refDefaultItem("model").build()));
        when(references.resolve("synthetic-model", "laptop", "synthetic-org", 7L, 1))
                .thenReturn(new PhysicalEquipmentReferenceResolver.Reference(null, null, "model", true));
        var equipment = InPhysicalEquipment.builder().id(12L).model("synthetic-model").type("laptop")
                .quantity(10d).electricityConsumption(100d).usageContext("BYOD").build();
        var categories = service.begin(99L, "synthetic-org", 7L);

        var allocation = service.resolve(equipment, 99L, "synthetic-org", 7L, 1, categories);
        service.save(List.of(allocation));

        verify(traces).deleteByTaskId(99L);
        verify(traces).saveAll(List.of(allocation.trace()));
        assertEquals(99L, allocation.trace().getTaskId());
        assertEquals(12L, allocation.trace().getSourceEquipmentId());
        assertEquals("DEVICES", allocation.trace().getItemCategory());
        assertEquals("BYOD", allocation.trace().getUsageContext());
        assertEquals(0.57d, allocation.trace().getEffectiveFraction());
        assertEquals(10d, allocation.trace().getOriginalQuantity());
        assertEquals(10d, equipment.getQuantity());
        assertEquals(100d, equipment.getElectricityConsumption());
    }

    @Test
    void derivesDefaultsAndOverridesOnlyForDevices() {
        when(itemTypes.findByWorkspaceIdOrWorkspaceIdIsNull(7L)).thenReturn(List.of(
                ItemType.builder().type("laptop").itemCategory("DEVICES").build(),
                ItemType.builder().type("server").itemCategory("INFRASTRUCTURE").build()));
        when(references.resolve(any(), any(), eq("synthetic-org"), eq(7L), eq(1L)))
                .thenReturn(new PhysicalEquipmentReferenceResolver.Reference(null, null, null, false));
        var categories = service.begin(99L, "synthetic-org", 7L);
        var equipment = InPhysicalEquipment.builder().id(1L).type("laptop").quantity(2d).build();

        assertEquals(1d, service.resolve(equipment, 99L, "synthetic-org", 7L, 1, categories)
                .trace().getEffectiveFraction());
        equipment.setUsageContext("");
        assertEquals(1d, service.resolve(equipment, 99L, "synthetic-org", 7L, 1, categories)
                .trace().getEffectiveFraction());
        equipment.setUsageContext("COPE");
        assertEquals(0.85d, service.resolve(equipment, 99L, "synthetic-org", 7L, 1, categories)
                .trace().getEffectiveFraction());
        equipment.setUsageFraction("");
        assertEquals(0.85d, service.resolve(equipment, 99L, "synthetic-org", 7L, 1, categories)
                .trace().getEffectiveFraction());
        equipment.setUsageFraction("0.5");
        assertEquals(0.5d, service.resolve(equipment, 99L, "synthetic-org", 7L, 1, categories)
                .trace().getEffectiveFraction());
        equipment.setUsageFraction("0");
        assertEquals(0d, service.resolve(equipment, 99L, "synthetic-org", 7L, 1, categories)
                .trace().getEffectiveFraction());
        equipment.setUsageFraction("1");
        assertEquals(1d, service.resolve(equipment, 99L, "synthetic-org", 7L, 1, categories)
                .trace().getEffectiveFraction());
        equipment.setType("server");
        equipment.setUsageFraction("not-a-number");
        assertEquals(1d, service.resolve(equipment, 99L, "synthetic-org", 7L, 1, categories)
                .trace().getEffectiveFraction());
    }

    @Test
    void rejectsInvalidDeviceContextBeforeWritingSnapshots() {
        when(itemTypes.findByWorkspaceIdOrWorkspaceIdIsNull(7L)).thenReturn(List.of(
                ItemType.builder().type("laptop").itemCategory("DEVICES").build()));
        when(references.resolve(any(), any(), eq("synthetic-org"), eq(7L), eq(1L)))
                .thenReturn(new PhysicalEquipmentReferenceResolver.Reference(null, null, null, false));
        var categories = service.begin(99L, "synthetic-org", 7L);
        var equipment = InPhysicalEquipment.builder().id(1L).type("laptop").quantity(2d)
                .usageContext("byod").build();

        assertThrows(IllegalArgumentException.class,
                () -> service.resolve(equipment, 99L, "synthetic-org", 7L, 1, categories));
        verify(traces, never()).saveAll(any());
    }

    @Test
    void rejectsInvalidDeviceFractionsBeforeWritingSnapshots() {
        when(itemTypes.findByWorkspaceIdOrWorkspaceIdIsNull(7L)).thenReturn(List.of(
                ItemType.builder().type("laptop").itemCategory("DEVICES").build()));
        when(references.resolve(any(), any(), eq("synthetic-org"), eq(7L), eq(1L)))
                .thenReturn(new PhysicalEquipmentReferenceResolver.Reference(null, null, null, false));
        var categories = service.begin(99L, "synthetic-org", 7L);
        var equipment = InPhysicalEquipment.builder().id(1L).type("laptop").quantity(2d).build();

        for (String invalid : List.of("NaN", "-0.1", "1.1", "not-a-number")) {
            equipment.setUsageFraction(invalid);
            assertThrows(IllegalArgumentException.class,
                    () -> service.resolve(equipment, 99L, "synthetic-org", 7L, 1, categories));
        }
        verify(traces, never()).saveAll(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Devices", "DEVICES ", "custom category", "INFRASTRUCTURE"})
    void matchedNonDeviceReferencePreventsAllocationDespiteDeviceType(String category) {
        when(itemTypes.findByWorkspaceIdOrWorkspaceIdIsNull(7L)).thenReturn(List.of(
                ItemType.builder().type("laptop").itemCategory("DEVICES").build(),
                ItemType.builder().type("reference-type").itemCategory(category)
                        .refDefaultItem("model").build()));
        var reference = new PhysicalEquipmentReferenceResolver.Reference(null, null, "model", true);
        when(references.resolve("synthetic-model", "laptop", "synthetic-org", 7L, 1L))
                .thenReturn(reference);
        var equipment = InPhysicalEquipment.builder().id(12L).model("synthetic-model").type("laptop")
                .quantity(2.5d).electricityConsumption(100d)
                .usageContext("BYOD").usageFraction("not-a-number").build();
        var categories = service.begin(99L, "synthetic-org", 7L);

        var allocation = service.resolve(equipment, 99L, "synthetic-org", 7L, 1L, categories);

        assertSame(reference, allocation.reference());
        assertEquals(category, allocation.trace().getItemCategory());
        assertEquals("reference-type", allocation.trace().getItemType());
        assertEquals(1d, allocation.trace().getEffectiveFraction());
        assertEquals("BYOD", allocation.trace().getUsageContext());
        assertEquals("not-a-number", allocation.trace().getUsageFraction());
        assertEquals(2.5d, allocation.trace().getOriginalQuantity());
        assertEquals(2.5d, equipment.getQuantity());
        assertEquals(100d, equipment.getElectricityConsumption());
    }
}
