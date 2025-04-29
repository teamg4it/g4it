/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiinout.business;


import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apiinout.mapper.InPhysicalEquipmentMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InPhysicalEquipmentServiceTest {

    @Mock
    private InPhysicalEquipmentRepository inPhysicalEquipmentRepository;

    @Mock
    private InPhysicalEquipmentMapper inPhysicalEquipmentMapper;

    @Mock
    private DigitalServiceRepository digitalServiceRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InPhysicalEquipmentService inPhysicalEquipmentService;

    @Test
    void getByInventory_returnsPhysicalEquipmentList_whenInventoryIdExists() {
        Long inventoryId = 1L;
        List<InPhysicalEquipment> equipmentList = List.of(new InPhysicalEquipment());
        List<InPhysicalEquipmentRest> expectedRestList = List.of(new InPhysicalEquipmentRest());

        when(inPhysicalEquipmentRepository.findByInventoryId(inventoryId)).thenReturn(equipmentList);
        when(inPhysicalEquipmentMapper.toRest(equipmentList)).thenReturn(expectedRestList);

        List<InPhysicalEquipmentRest> result = inPhysicalEquipmentService.getByInventory(inventoryId);

        assertEquals(expectedRestList, result);
        verify(inPhysicalEquipmentRepository).findByInventoryId(inventoryId);
        verify(inPhysicalEquipmentMapper).toRest(equipmentList);
    }

    @Test
    void getByInventoryAndId_throwsException_whenPhysicalEquipmentNotFound() {
        Long inventoryId = 1L;
        Long id = 2L;

        when(inPhysicalEquipmentRepository.findByInventoryIdAndId(inventoryId, id)).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inPhysicalEquipmentService.getByInventoryAndId(inventoryId, id));

        assertEquals("404", exception.getCode());
        verify(inPhysicalEquipmentRepository).findByInventoryIdAndId(inventoryId, id);
    }

    @Test
    void createInPhysicalEquipmentInventory_throwsException_whenInventoryDoesNotExist() {
        Long inventoryId = 1L;
        InPhysicalEquipmentRest equipmentRest = new InPhysicalEquipmentRest();

        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inPhysicalEquipmentService.createInPhysicalEquipmentInventory(inventoryId, equipmentRest));

        assertEquals("404", exception.getCode());
        verify(inventoryRepository).findById(inventoryId);
    }

    @Test
    void deleteInPhysicalEquipment_throwsException_whenPhysicalEquipmentNotFoundInInventory() {
        Long inventoryId = 1L;
        Long id = 2L;

        when(inPhysicalEquipmentRepository.findByInventoryIdAndId(inventoryId, id)).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inPhysicalEquipmentService.deleteInPhysicalEquipment(inventoryId, id));

        assertEquals("404", exception.getCode());
        verify(inPhysicalEquipmentRepository).findByInventoryIdAndId(inventoryId, id);
    }

    @Test
    void updateInPhysicalEquipment_throwsException_whenInventoryIdMismatch() {
        Long inventoryId = 1L;
        Long id = 2L;
        InPhysicalEquipmentRest equipmentUpdateRest = new InPhysicalEquipmentRest();
        InPhysicalEquipment existingEquipment = new InPhysicalEquipment();
        existingEquipment.setInventoryId(3L);

        when(inPhysicalEquipmentRepository.findByInventoryIdAndId(inventoryId, id)).thenReturn(Optional.of(existingEquipment));

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inPhysicalEquipmentService.updateInPhysicalEquipment(inventoryId, id, equipmentUpdateRest));

        assertEquals("409", exception.getCode());
        verify(inPhysicalEquipmentRepository).findByInventoryIdAndId(inventoryId, id);
    }

    @Test
    void getByInventory_whenInventoryIdIsNull() {
        Long inventoryId = null;

        List<InPhysicalEquipmentRest> lstPhysicalEquipments =
                inPhysicalEquipmentService.getByInventory(inventoryId);

        assertEquals(0, lstPhysicalEquipments.size());
    }

    @Test
    void createInPhysicalEquipmentInventory_throwsException_whenEquipmentRestIsNull() {
        Long inventoryId = 1L;
        InPhysicalEquipmentRest equipmentRest = null;

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inPhysicalEquipmentService.createInPhysicalEquipmentInventory(inventoryId, equipmentRest));

        assertEquals("404", exception.getCode());
        assertEquals("the inventory of id : 1, doesn't exist", exception.getMessage());
    }

    @Test
    void updateInPhysicalEquipment_throwsException_whenEquipmentUpdateRestIsNull() {
        Long inventoryId = 1L;
        Long id = 2L;
        InPhysicalEquipmentRest equipmentUpdateRest = null;

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inPhysicalEquipmentService.updateInPhysicalEquipment(inventoryId, id, equipmentUpdateRest));

        assertEquals("404", exception.getCode());
        assertEquals("the inventory id provided: 1 has no physical equipment with id : 2", exception.getMessage());
    }

    @Test
    void deleteInPhysicalEquipment_throwsException_whenIdIsNull() {
        Long inventoryId = 1L;
        Long id = null;

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inPhysicalEquipmentService.deleteInPhysicalEquipment(inventoryId, id));

        assertEquals("404", exception.getCode());
        assertEquals("Physical equipment null not found in inventory 1", exception.getMessage());
    }

    @Test
    void getByInventoryAndId_throwsException_whenInventoryIdAndIdMismatch() {
        Long inventoryId = 1L;
        Long id = 2L;

        InPhysicalEquipment equipment = new InPhysicalEquipment();
        equipment.setInventoryId(3L);

        when(inPhysicalEquipmentRepository.findByInventoryIdAndId(inventoryId, id)).thenReturn(Optional.of(equipment));

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inPhysicalEquipmentService.getByInventoryAndId(inventoryId, id));

        assertEquals("409", exception.getCode());
        assertEquals("the inventory id provided: 1 is not compatible with the inventory id : null linked to this physical equipment id: 2", exception.getMessage());
    }

    @Test
    void getByDigitalService_returnsEmptyList_whenNoPhysicalEquipmentExists() {
        String digitalServiceUid = "service-123";

        when(inPhysicalEquipmentRepository.findByDigitalServiceUidOrderByName(digitalServiceUid)).thenReturn(List.of());

        List<InPhysicalEquipmentRest> result = inPhysicalEquipmentService.getByDigitalService(digitalServiceUid);

        assertEquals(0, result.size());
        verify(inPhysicalEquipmentRepository).findByDigitalServiceUidOrderByName(digitalServiceUid);
    }

    @Test
    void getByDigitalServiceAndId_throwsException_whenDigitalServiceUidIsNull() {
        String digitalServiceUid = null;
        Long id = 1L;

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inPhysicalEquipmentService.getByDigitalServiceAndId(digitalServiceUid, id));

        assertEquals("404", exception.getCode());
        assertEquals("the digital service uid provided: null has no physical equipment with id : 1", exception.getMessage());
    }

    @Test
    void createInPhysicalEquipmentDigitalService_throwsException_whenDigitalServiceDoesNotExist() {
        String digitalServiceUid = "service-123";
        InPhysicalEquipmentRest equipmentRest = new InPhysicalEquipmentRest();

        when(digitalServiceRepository.findById(digitalServiceUid)).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inPhysicalEquipmentService.createInPhysicalEquipmentDigitalService(digitalServiceUid, equipmentRest));

        assertEquals("404", exception.getCode());
        assertEquals("the digital service of uid : service-123, doesn't exist", exception.getMessage());
        verify(digitalServiceRepository).findById(digitalServiceUid);
    }

    @Test
    void updateInPhysicalEquipment_throwsException_whenDigitalServiceUidMismatch() {
        String digitalServiceUid = "service-123";
        Long id = 1L;
        InPhysicalEquipmentRest equipmentUpdateRest = new InPhysicalEquipmentRest();
        InPhysicalEquipment existingEquipment = new InPhysicalEquipment();
        existingEquipment.setDigitalServiceUid("service-456");

        when(inPhysicalEquipmentRepository.findByDigitalServiceUidAndId(digitalServiceUid, id)).thenReturn(Optional.of(existingEquipment));

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inPhysicalEquipmentService.updateInPhysicalEquipment(digitalServiceUid, id, equipmentUpdateRest));

        assertEquals("409", exception.getCode());
        assertEquals("the digital service uid provided: service-123 is not compatible with the digital uid : service-456 linked to this physical equipment id: 1", exception.getMessage());
        verify(inPhysicalEquipmentRepository).findByDigitalServiceUidAndId(digitalServiceUid, id);
    }

    @Test
    void deleteInPhysicalEquipment_throwsException_whenDigitalServiceUidIsNull() {
        String digitalServiceUid = null;
        Long id = 1L;

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inPhysicalEquipmentService.deleteInPhysicalEquipment(digitalServiceUid, id));

        assertEquals("404", exception.getCode());
        assertEquals("Physical equipment 1 not found in digital service null", exception.getMessage());
    }
}
