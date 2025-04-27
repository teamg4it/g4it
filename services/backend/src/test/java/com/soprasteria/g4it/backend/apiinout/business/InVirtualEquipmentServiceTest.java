/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiinout.business;


import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apiinout.mapper.InVirtualEquipmentMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.InVirtualEquipmentRest;
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
class InVirtualEquipmentServiceTest {

    @Mock
    private InVirtualEquipmentRepository inVirtualEquipmentRepository;

    @Mock
    private InVirtualEquipmentMapper inVirtualEquipmentMapper;

    @Mock
    private DigitalServiceRepository digitalServiceRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InPhysicalEquipmentRepository inPhysicalEquipmentRepository;

    @InjectMocks
    private InVirtualEquipmentService inVirtualEquipmentService;

    @Test
    void getByDigitalService_returnsVirtualEquipmentList_whenDigitalServiceExists() {
        String digitalServiceUid = "service-123";
        List<InVirtualEquipment> virtualEquipments = List.of(new InVirtualEquipment());
        List<InVirtualEquipmentRest> virtualEquipmentRests = List.of(new InVirtualEquipmentRest());

        when(inVirtualEquipmentRepository.findByDigitalServiceUidOrderByName(digitalServiceUid)).thenReturn(virtualEquipments);
        when(inVirtualEquipmentMapper.toRest(virtualEquipments)).thenReturn(virtualEquipmentRests);

        List<InVirtualEquipmentRest> result = inVirtualEquipmentService.getByDigitalService(digitalServiceUid);

        assertEquals(virtualEquipmentRests, result);
        verify(inVirtualEquipmentRepository).findByDigitalServiceUidOrderByName(digitalServiceUid);
        verify(inVirtualEquipmentMapper).toRest(virtualEquipments);
    }

    @Test
    void getByDigitalServiceAndId_throwsException_whenVirtualEquipmentNotFound() {
        String digitalServiceUid = "service-123";
        Long id = 1L;

        when(inVirtualEquipmentRepository.findByDigitalServiceUidAndId(digitalServiceUid, id)).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inVirtualEquipmentService.getByDigitalServiceAndId(digitalServiceUid, id));

        assertEquals("404", exception.getCode());
        verify(inVirtualEquipmentRepository).findByDigitalServiceUidAndId(digitalServiceUid, id);
    }

    @Test
    void createInVirtualEquipmentDigitalService_throwsException_whenDigitalServiceDoesNotExist() {
        String digitalServiceUid = "service-123";
        InVirtualEquipmentRest inVirtualEquipmentRest = new InVirtualEquipmentRest();

        when(digitalServiceRepository.findById(digitalServiceUid)).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inVirtualEquipmentService.createInVirtualEquipmentDigitalService(digitalServiceUid, inVirtualEquipmentRest));

        assertEquals("404", exception.getCode());
        verify(digitalServiceRepository).findById(digitalServiceUid);
    }

    @Test
    void deleteInVirtualEquipment_throwsException_whenVirtualEquipmentNotFound() {
        String digitalServiceUid = "service-123";
        Long id = 1L;

        when(inVirtualEquipmentRepository.findByDigitalServiceUidAndId(digitalServiceUid, id)).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inVirtualEquipmentService.deleteInVirtualEquipment(digitalServiceUid, id));

        assertEquals("404", exception.getCode());
        verify(inVirtualEquipmentRepository).findByDigitalServiceUidAndId(digitalServiceUid, id);
    }
}
