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
    void getByDigitalService_returnsPhysicalEquipmentList_whenDigitalServiceUidExists() {
        String digitalServiceUid = "validUid";
        List<InPhysicalEquipment> equipmentList = List.of(new InPhysicalEquipment());
        List<InPhysicalEquipmentRest> expectedRestList = List.of(new InPhysicalEquipmentRest());

        when(inPhysicalEquipmentRepository.findByDigitalServiceUidOrderByName(digitalServiceUid)).thenReturn(equipmentList);
        when(inPhysicalEquipmentMapper.toRest(equipmentList)).thenReturn(expectedRestList);

        List<InPhysicalEquipmentRest> result = inPhysicalEquipmentService.getByDigitalService(digitalServiceUid);

        assertEquals(expectedRestList, result);
        verify(inPhysicalEquipmentRepository).findByDigitalServiceUidOrderByName(digitalServiceUid);
        verify(inPhysicalEquipmentMapper).toRest(equipmentList);
    }

    @Test
    void getByDigitalServiceAndId_throwsException_whenPhysicalEquipmentNotFound() {
        String digitalServiceUid = "validUid";
        Long id = 1L;

        when(inPhysicalEquipmentRepository.findByDigitalServiceUidAndId(digitalServiceUid, id)).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inPhysicalEquipmentService.getByDigitalServiceAndId(digitalServiceUid, id));

        assertEquals("404", exception.getCode());
        verify(inPhysicalEquipmentRepository).findByDigitalServiceUidAndId(digitalServiceUid, id);
    }

    @Test
    void createInPhysicalEquipmentDigitalService_throwsException_whenDigitalServiceDoesNotExist() {
        String digitalServiceUid = "nonExistentUid";
        InPhysicalEquipmentRest equipmentRest = new InPhysicalEquipmentRest();

        when(digitalServiceRepository.findById(digitalServiceUid)).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inPhysicalEquipmentService.createInPhysicalEquipmentDigitalService(digitalServiceUid, equipmentRest));

        assertEquals("404", exception.getCode());
        verify(digitalServiceRepository).findById(digitalServiceUid);
    }

    @Test
    void deleteInPhysicalEquipment_throwsException_whenPhysicalEquipmentNotFoundInDigitalService() {
        String digitalServiceUid = "validUid";
        Long id = 1L;

        when(inPhysicalEquipmentRepository.findByDigitalServiceUidAndId(digitalServiceUid, id)).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inPhysicalEquipmentService.deleteInPhysicalEquipment(digitalServiceUid, id));

        assertEquals("404", exception.getCode());
        verify(inPhysicalEquipmentRepository).findByDigitalServiceUidAndId(digitalServiceUid, id);
    }

    @Test
    void deleteInPhysicalEquipment_deletesPhysicalEquipment_whenFoundInInventory() {
        Long inventoryId = 1L;
        Long id = 2L;
        InPhysicalEquipment equipment = new InPhysicalEquipment();

        when(inPhysicalEquipmentRepository.findByInventoryIdAndId(inventoryId, id)).thenReturn(Optional.of(equipment));

        inPhysicalEquipmentService.deleteInPhysicalEquipment(inventoryId, id);

        verify(inPhysicalEquipmentRepository).findByInventoryIdAndId(inventoryId, id);
        verify(inPhysicalEquipmentRepository).deleteById(id);
    }
}
