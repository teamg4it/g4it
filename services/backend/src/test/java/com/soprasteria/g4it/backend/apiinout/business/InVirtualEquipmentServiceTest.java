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
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
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

import static org.junit.jupiter.api.Assertions.*;
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
    void updateInVirtualEquipment() {
        String digitalServiceUid = "service-123";
        Long id = 1L;
        InVirtualEquipmentRest inVirtualEquipmentRest = InVirtualEquipmentRest.builder().datacenterName("default").physicalEquipmentName("MyPE").name("MyVE").id(1L).allocationFactor(0.5).digitalServiceUid("dummyid").durationHour(33.0).electricityConsumption(22.0).build();
        InVirtualEquipment virtualEquipment = InVirtualEquipment.builder().id(1L).name("MyVE").physicalEquipmentName("MyPE")
                .allocationFactor(0.5).datacenterName("default").digitalServiceUid("dummyid").durationHour(33.0).electricityConsumption(22.0).infrastructureType("CLOUD_SERVERS").quantity(12.0).sizeDiskGb(234.0).
                sizeMemoryGb(3.0).vcpuCoreNumber(2.0).workload(33.0).location("France").build();

        when(inVirtualEquipmentRepository.findByDigitalServiceUidAndId(digitalServiceUid, id)).thenReturn(Optional.of(virtualEquipment));
        G4itRestException exception1 = assertThrows(G4itRestException.class, () ->
                inVirtualEquipmentService.updateInVirtualEquipment(digitalServiceUid, id, inVirtualEquipmentRest));

        assertEquals("409", exception1.getCode());//
    }

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
    void getByDigitalServiceAndId() {
        String digitalServiceUid = "service-123";
        Long id = 1L;

        when(inVirtualEquipmentRepository.findByDigitalServiceUidAndId(digitalServiceUid, id)).thenReturn(Optional.empty());

        G4itRestException exception1 = assertThrows(G4itRestException.class, () ->
                inVirtualEquipmentService.getByDigitalServiceAndId(digitalServiceUid, id));

        assertEquals("404", exception1.getCode());
        verify(inVirtualEquipmentRepository).findByDigitalServiceUidAndId(digitalServiceUid, id);

        InVirtualEquipment virtualEquipment = InVirtualEquipment.builder().id(1L).name("MyVE").physicalEquipmentName("MyPE")
                .allocationFactor(0.5).datacenterName("default").digitalServiceUid("dummyid").durationHour(33.0).electricityConsumption(22.0).infrastructureType("CLOUD_SERVERS").quantity(12.0).sizeDiskGb(234.0).
                sizeMemoryGb(3.0).vcpuCoreNumber(2.0).workload(33.0).location("France").build();
        InVirtualEquipmentRest inVirtualEquipmentRest = InVirtualEquipmentRest.builder().datacenterName("default").physicalEquipmentName("MyPE").name("MyVE").id(1L).allocationFactor(0.5).digitalServiceUid("dummyid").durationHour(33.0).electricityConsumption(22.0).build();

        when(inVirtualEquipmentRepository.findByDigitalServiceUidAndId(digitalServiceUid, id)).thenReturn(Optional.of(virtualEquipment));
        G4itRestException exception2 = assertThrows(G4itRestException.class, () ->
                inVirtualEquipmentService.getByDigitalServiceAndId(digitalServiceUid, id));

        assertEquals("409", exception2.getCode());
        when(inVirtualEquipmentRepository.findByDigitalServiceUidAndId(virtualEquipment.getDigitalServiceUid(), virtualEquipment.getId())).thenReturn(Optional.of(virtualEquipment));
        when(inVirtualEquipmentMapper.toRest(virtualEquipment)).thenReturn(inVirtualEquipmentRest);
        InVirtualEquipmentRest response = inVirtualEquipmentService.getByDigitalServiceAndId(virtualEquipment.getDigitalServiceUid(), virtualEquipment.getId());
        assertNotNull(response);
    }

    @Test
    void getByInventoryAndId() {
        Long id = 142L;
        Long inventoryId = 53L;
        when(inVirtualEquipmentRepository.findByInventoryIdAndId(inventoryId, id)).thenReturn(Optional.empty());

        G4itRestException exception1 = assertThrows(G4itRestException.class, () ->
                inVirtualEquipmentService.getByInventoryAndId(inventoryId, id));

        assertEquals("404", exception1.getCode());
        verify(inVirtualEquipmentRepository).findByInventoryIdAndId(inventoryId, id);

        InVirtualEquipment virtualEquipment = InVirtualEquipment.builder().id(1L).name("MyVE").physicalEquipmentName("MyPE")
                .allocationFactor(0.5).datacenterName("default").inventoryId(43L).durationHour(33.0).electricityConsumption(22.0).infrastructureType("CLOUD_SERVERS").quantity(12.0).sizeDiskGb(234.0).
                sizeMemoryGb(3.0).vcpuCoreNumber(2.0).workload(33.0).location("France").build();
        InVirtualEquipmentRest inVirtualEquipmentRest = InVirtualEquipmentRest.builder().datacenterName("default").physicalEquipmentName("MyPE").name("MyVE").id(1L).allocationFactor(0.5).digitalServiceUid("dummyid").durationHour(33.0).electricityConsumption(22.0).build();

        when(inVirtualEquipmentRepository.findByInventoryIdAndId(inventoryId, id)).thenReturn(Optional.of(virtualEquipment));
        G4itRestException exception2 = assertThrows(G4itRestException.class, () ->
                inVirtualEquipmentService.getByInventoryAndId(inventoryId, id));

        assertEquals("409", exception2.getCode());
        when(inVirtualEquipmentRepository.findByInventoryIdAndId(virtualEquipment.getInventoryId(), virtualEquipment.getId())).thenReturn(Optional.of(virtualEquipment));
        when(inVirtualEquipmentMapper.toRest(virtualEquipment)).thenReturn(inVirtualEquipmentRest);
        InVirtualEquipmentRest response = inVirtualEquipmentService.getByInventoryAndId(virtualEquipment.getInventoryId(), virtualEquipment.getId());
        assertNotNull(response);
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

    @Test
    void updateOrDeleteInVirtualEquipments_handlesEmptyRepositoryList_whenNoExistingVirtualEquipments() {
        String digitalServiceUid = "service-123";
        Long physicalEqpId = 1L;
        String physicalEqpName = "Physical Equipment 1";

        InPhysicalEquipment physicalEquipment = new InPhysicalEquipment();
        physicalEquipment.setName(physicalEqpName);

        InVirtualEquipmentRest inputEquipment = new InVirtualEquipmentRest();
        inputEquipment.setId(1L);
        List<InVirtualEquipmentRest> inVirtualEquipmentList = List.of(inputEquipment);

        when(inPhysicalEquipmentRepository.findById(physicalEqpId)).thenReturn(Optional.of(physicalEquipment));
        when(inVirtualEquipmentRepository.findByDigitalServiceUidAndPhysicalEquipmentName(digitalServiceUid, physicalEqpName)).thenReturn(List.of());
        when(inVirtualEquipmentRepository.findByDigitalServiceUidAndId(digitalServiceUid, inputEquipment.getId())).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inVirtualEquipmentService.updateOrDeleteInVirtualEquipments(digitalServiceUid, physicalEqpId, inVirtualEquipmentList));

        assertEquals("404", exception.getCode());
        verify(inPhysicalEquipmentRepository).findById(physicalEqpId);
    }


    @Test
    void updateOrDeleteInVirtualEquipments_throwsException_whenPhysicalEquipmentIdIsNull() {
        String digitalServiceUid = "service-123";
        Long physicalEqpId = null;
        List<InVirtualEquipmentRest> inVirtualEquipmentList = List.of();

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inVirtualEquipmentService.updateOrDeleteInVirtualEquipments(digitalServiceUid, physicalEqpId, inVirtualEquipmentList));

        assertEquals("404", exception.getCode());
        assertEquals("The digitalService id provided: service-123 has no physical equipment with id: null", exception.getMessage());
    }

    @Test
    void updateOrDeleteInVirtualEquipments_throwsException_whenInputListContainsNullElement() {
        String digitalServiceUid = "service-123";
        Long physicalEqpId = 1L;
        List<InVirtualEquipmentRest> inVirtualEquipmentList = List.of(new InVirtualEquipmentRest());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inVirtualEquipmentService.updateOrDeleteInVirtualEquipments(digitalServiceUid, physicalEqpId, inVirtualEquipmentList));

        assertEquals("404", exception.getCode());
        assertEquals("The digitalService id provided: service-123 has no physical equipment with id: 1", exception.getMessage());
    }

    @Test
    void updateOrDeleteInVirtualEquipments_throwsException_whenInputListHasDuplicateIds() {
        String digitalServiceUid = "service-123";
        Long physicalEqpId = 1L;

        InVirtualEquipmentRest equipment1 = new InVirtualEquipmentRest();
        equipment1.setId(1L);

        InVirtualEquipmentRest equipment2 = new InVirtualEquipmentRest();
        equipment2.setId(1L);

        List<InVirtualEquipmentRest> inVirtualEquipmentList = List.of(equipment1, equipment2);

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inVirtualEquipmentService.updateOrDeleteInVirtualEquipments(digitalServiceUid, physicalEqpId, inVirtualEquipmentList));

        assertEquals("404", exception.getCode());
        assertEquals("The digitalService id provided: service-123 has no physical equipment with id: 1", exception.getMessage());
    }
}
