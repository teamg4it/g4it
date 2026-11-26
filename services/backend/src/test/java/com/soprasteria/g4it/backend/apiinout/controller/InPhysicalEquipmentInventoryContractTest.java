// This file has been created (totally or partially) with the assistance of artificial intelligence tools.
// All content has been generated under the direct supervision of a named individual,
// and under the AI.Backbone Orchestrator Compliance framework

package com.soprasteria.g4it.backend.apiinout.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.soprasteria.g4it.backend.apiinout.business.InPhysicalEquipmentService;
import com.soprasteria.g4it.backend.apiinout.mapper.InPhysicalEquipmentMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InPhysicalEquipmentInventoryContractTest {

    @Mock
    private InPhysicalEquipmentRepository equipmentRepository;
    @Mock
    private InventoryRepository inventoryRepository;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .registerModule(new JsonNullableModule());

    private InPhysicalEquipmentInventoryController controller;

    @BeforeEach
    void setUp() {
        InPhysicalEquipmentService service = new InPhysicalEquipmentService(equipmentRepository,
                Mappers.getMapper(InPhysicalEquipmentMapper.class), null, inventoryRepository, null, null);
        controller = new InPhysicalEquipmentInventoryController(service);
    }

    @Test
    void createsAndReadsInventoryEquipmentWithVerbatimUsageValues() throws Exception {
        when(inventoryRepository.findById(7L)).thenReturn(Optional.of(new Inventory()));
        when(equipmentRepository.save(any(InPhysicalEquipment.class))).thenAnswer(invocation -> {
            InPhysicalEquipment entity = invocation.getArgument(0);
            entity.setId(19L);
            return entity;
        });

        InPhysicalEquipmentRest input = objectMapper.readValue("""
                {"name":"synthetic-device","quantity":2.5,"electricityConsumption":125,
                 "usageContext":"COPE","usageFraction":"0.5000"}
                """, InPhysicalEquipmentRest.class);

        ResponseEntity<InPhysicalEquipmentRest> created = controller
                .postInventoryInputsPhysicalEquipmentsRest("synthetic-organization", 3L, 7L, input);
        assertEquals(HttpStatus.CREATED, created.getStatusCode());

        ArgumentCaptor<InPhysicalEquipment> saved = ArgumentCaptor.forClass(InPhysicalEquipment.class);
        verify(equipmentRepository).save(saved.capture());
        assertEquals(7L, saved.getValue().getInventoryId());
        assertEquals("COPE", saved.getValue().getUsageContext());
        assertEquals("0.5000", saved.getValue().getUsageFraction());
        assertEquals(2.5, saved.getValue().getQuantity());
        assertEquals(125d, saved.getValue().getElectricityConsumption());

        when(equipmentRepository.findByInventoryIdAndId(7L, 19L))
                .thenReturn(Optional.of(saved.getValue()));
        ResponseEntity<InPhysicalEquipmentRest> read = controller
                .getInventoryInputsPhysicalEquipmentRest("synthetic-organization", 3L, 7L, 19L);

        assertEquals(HttpStatus.OK, read.getStatusCode());
        JsonNode response = objectMapper.valueToTree(read.getBody());
        assertEquals("COPE", response.path("usageContext").asText());
        assertEquals("0.5000", response.path("usageFraction").asText());
        assertEquals(2.5, response.path("quantity").asDouble());
        assertEquals(125d, response.path("electricityConsumption").asDouble());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"name\":\"synthetic-legacy\",\"quantity\":2.5,\"electricityConsumption\":125}",
            "{\"name\":\"synthetic-legacy\",\"quantity\":2.5,\"electricityConsumption\":125,"
                    + "\"usageContext\":null,\"usageFraction\":null}"
    })
    void acceptsAbsentOrExplicitlyNullUsageWithoutInventingDefaults(String json) throws Exception {
        when(inventoryRepository.findById(7L)).thenReturn(Optional.of(new Inventory()));
        InPhysicalEquipmentRest input = objectMapper.readValue(json, InPhysicalEquipmentRest.class);

        ResponseEntity<InPhysicalEquipmentRest> created = controller
                .postInventoryInputsPhysicalEquipmentsRest("synthetic-organization", 3L, 7L, input);

        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        ArgumentCaptor<InPhysicalEquipment> saved = ArgumentCaptor.forClass(InPhysicalEquipment.class);
        verify(equipmentRepository).save(saved.capture());
        assertNull(saved.getValue().getUsageContext());
        assertNull(saved.getValue().getUsageFraction());
        assertEquals(2.5, saved.getValue().getQuantity());
        assertEquals(125d, saved.getValue().getElectricityConsumption());
        JsonNode response = objectMapper.valueToTree(created.getBody());
        assertTrue(response.path("usageContext").isNull());
        assertTrue(response.path("usageFraction").isNull());
    }

    @Test
    void updatesUsageOnExistingInventoryEquipmentWithoutAllocatingSourceValues() throws Exception {
        InPhysicalEquipment existing = InPhysicalEquipment.builder()
                .id(19L).inventoryId(7L).name("synthetic-device")
                .quantity(2.5).electricityConsumption(125d)
                .usageContext("BYOD").usageFraction("0.5700")
                .commonFilters(new ArrayList<>(List.of("")))
                .filters(new ArrayList<>(List.of("")))
                .build();
        when(equipmentRepository.findByInventoryIdAndId(7L, 19L))
                .thenReturn(Optional.of(existing));
        InPhysicalEquipmentRest input = objectMapper.readValue("""
                {"inventoryId":7,"name":"synthetic-device","quantity":2.5,"electricityConsumption":125,
                 "usageContext":"COPE","usageFraction":"0.5000"}
                """, InPhysicalEquipmentRest.class);

        ResponseEntity<InPhysicalEquipmentRest> updated = controller
                .putInventoryInputsPhysicalEquipmentRest("synthetic-organization", 3L, 7L, 19L, input);

        assertEquals(HttpStatus.OK, updated.getStatusCode());
        verify(equipmentRepository).save(existing);
        assertEquals(19L, existing.getId());
        assertEquals(7L, existing.getInventoryId());
        assertEquals("COPE", existing.getUsageContext());
        assertEquals("0.5000", existing.getUsageFraction());
        assertEquals(2.5, existing.getQuantity());
        assertEquals(125d, existing.getElectricityConsumption());
        JsonNode response = objectMapper.valueToTree(updated.getBody());
        assertEquals("COPE", response.path("usageContext").asText());
        assertEquals("0.5000", response.path("usageFraction").asText());
    }
}
