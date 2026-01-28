/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiinout.business;


import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceVersion;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceVersionRepository;
import com.soprasteria.g4it.backend.apiinout.mapper.InVirtualEquipmentMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.common.utils.CommonValidationUtil;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InVirtualEquipmentServiceTest {

    @Mock
    private InVirtualEquipmentRepository inVirtualEquipmentRepository;

    @Mock
    private InVirtualEquipmentMapper inVirtualEquipmentMapper;

    @Mock
    private DigitalServiceRepository digitalServiceRepository;

    @Mock
    private DigitalServiceVersionRepository digitalServiceVersionRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InPhysicalEquipmentRepository inPhysicalEquipmentRepository;

    @InjectMocks
    private InVirtualEquipmentService inVirtualEquipmentService;

    @Mock
    private CommonValidationUtil commonValidationUtil;

    @Test
    void createInVirtualEquipmentInventoryTest() {
        var organization = Workspace.builder()
                .name("DEMO")
                .organization(Organization.builder().name("SUBSCRIBER").build())
                .build();
        var inventory = Inventory.builder()
                .name("Inventory Name")
                .workspace(organization)
                .doExportVerbose(true)
                .build();
        Long inventoryId = 1L;
        InVirtualEquipmentRest inVirtualEquipmentRest = InVirtualEquipmentRest.builder().datacenterName("default").physicalEquipmentName("MyPE").name("MyVE").id(1L).allocationFactor(0.5).digitalServiceUid("dummyid").durationHour(33.0).electricityConsumption(22.0).build();
        InVirtualEquipment inVirtualEquipment = InVirtualEquipment.builder().id(1L).name("MyVE").physicalEquipmentName("MyPE")
                .allocationFactor(0.5).datacenterName("default").digitalServiceUid("dummyid").durationHour(33.0).electricityConsumption(22.0).infrastructureType("CLOUD_SERVERS").quantity(12.0).sizeDiskGb(234.0).
                sizeMemoryGb(3.0).vcpuCoreNumber(2.0).workload(33.0).location("France").build();

        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.empty());
        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inVirtualEquipmentService.createInVirtualEquipmentInventory(inventoryId, inVirtualEquipmentRest));
        assertEquals("404", exception.getCode());
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(inventory));
        when(inVirtualEquipmentMapper.toEntity(inVirtualEquipmentRest)).thenReturn(inVirtualEquipment);
        when(inVirtualEquipmentMapper.toRest(inVirtualEquipment)).thenReturn(inVirtualEquipmentRest);
        InVirtualEquipmentRest responseRest = inVirtualEquipmentService.createInVirtualEquipmentInventory(inventoryId, inVirtualEquipmentRest);
        assertNotNull(responseRest);
    }

    @Test
    void createInVirtualEquipmentDigitalServiceTest() {
        String digitalServiceId = "dummy_id";
        var digitalService = DigitalService.builder()
                .name("DS")
                .uid("uid")
                .build();
        var digitalServiceversion = DigitalServiceVersion.builder()
                .description("DS_Name")
                .digitalService(digitalService)
                .build();
        InVirtualEquipmentRest inVirtualEquipmentRest = InVirtualEquipmentRest.builder().datacenterName("default").physicalEquipmentName("MyPE").name("MyVE").id(1L).allocationFactor(0.5).digitalServiceUid("dummyid").durationHour(33.0).electricityConsumption(22.0).build();
        InVirtualEquipment inVirtualEquipment = InVirtualEquipment.builder().id(1L).name("MyVE").physicalEquipmentName("MyPE")
                .allocationFactor(0.5).datacenterName("default").digitalServiceUid("dummyid").durationHour(33.0).electricityConsumption(22.0).infrastructureType("CLOUD_SERVERS").quantity(12.0).sizeDiskGb(234.0).
                sizeMemoryGb(3.0).vcpuCoreNumber(2.0).workload(33.0).location("France").build();

        when(digitalServiceVersionRepository.findById(digitalServiceId)).thenReturn(Optional.empty());
        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inVirtualEquipmentService.createInVirtualEquipmentDigitalServiceVersion(digitalServiceId, inVirtualEquipmentRest));
        assertEquals("404", exception.getCode());
        when(digitalServiceVersionRepository.findById(digitalServiceId)).thenReturn(Optional.of(digitalServiceversion));
        when(inVirtualEquipmentMapper.toEntity(inVirtualEquipmentRest)).thenReturn(inVirtualEquipment);
        when(inVirtualEquipmentMapper.toRest(inVirtualEquipment)).thenReturn(inVirtualEquipmentRest);
        InVirtualEquipmentRest responseRest = inVirtualEquipmentService.createInVirtualEquipmentDigitalServiceVersion(digitalServiceId, inVirtualEquipmentRest);
        assertNotNull(responseRest);
    }

    @Test
    void updateInVirtualEquipmentTest() {
        String digitalServiceUid = "service-123";
        Long id = 1L;
        Long inventoryId = 12L;
        InVirtualEquipmentRest inVirtualEquipmentRest = InVirtualEquipmentRest.builder().datacenterName("default").physicalEquipmentName("MyPE").name("MyVE").id(1L).allocationFactor(0.5).digitalServiceVersionUid("uid").digitalServiceUid("dummyid").inventoryId(11L).durationHour(33.0).electricityConsumption(22.0).build();
        InVirtualEquipment virtualEquipment = InVirtualEquipment.builder().id(1L).name("MyVE").physicalEquipmentName("MyPE").digitalServiceVersionUid("uid")
                .allocationFactor(0.5).datacenterName("default").digitalServiceUid("dummyid").inventoryId(11L).durationHour(33.0).electricityConsumption(22.0).infrastructureType("CLOUD_SERVERS").quantity(12.0).sizeDiskGb(234.0).
                sizeMemoryGb(3.0).vcpuCoreNumber(2.0).workload(33.0).location("France").build();

        when(inVirtualEquipmentRepository.findByDigitalServiceVersionUidAndId(digitalServiceUid, id)).thenReturn(Optional.of(virtualEquipment));
        G4itRestException exception1 = assertThrows(G4itRestException.class, () ->
                inVirtualEquipmentService.updateInVirtualEquipment(digitalServiceUid, id, inVirtualEquipmentRest));

        assertEquals("409", exception1.getCode());
        when(inVirtualEquipmentMapper.toRest(virtualEquipment)).thenReturn(inVirtualEquipmentRest);
        when(inVirtualEquipmentRepository.findByDigitalServiceVersionUidAndId(inVirtualEquipmentRest.getDigitalServiceVersionUid(), inVirtualEquipmentRest.getId())).thenReturn(Optional.of(virtualEquipment));
        InVirtualEquipmentRest responseRest = inVirtualEquipmentService.updateInVirtualEquipment(inVirtualEquipmentRest.getDigitalServiceVersionUid(), inVirtualEquipmentRest.getId(), inVirtualEquipmentRest);
        assertNotNull(responseRest);

        when(inVirtualEquipmentRepository.findByInventoryIdAndId(inventoryId, id)).thenReturn(Optional.of(virtualEquipment));
        G4itRestException exception2 = assertThrows(G4itRestException.class, () ->
                inVirtualEquipmentService.updateInVirtualEquipment(inventoryId, id, inVirtualEquipmentRest));
        assertEquals("409", exception2.getCode());

        when(inVirtualEquipmentRepository.findByInventoryIdAndId(inventoryId, id)).thenReturn(Optional.empty());
        G4itRestException exception3 = assertThrows(G4itRestException.class, () ->
                inVirtualEquipmentService.updateInVirtualEquipment(inventoryId, id, inVirtualEquipmentRest));
        assertEquals("404", exception3.getCode());

        when(inVirtualEquipmentRepository.findByInventoryIdAndId(virtualEquipment.getInventoryId(), virtualEquipment.getId())).thenReturn(Optional.of(virtualEquipment));
        InVirtualEquipmentRest inventoryResponseRest = inVirtualEquipmentService.updateInVirtualEquipment(inVirtualEquipmentRest.getInventoryId(), inVirtualEquipmentRest.getId(), inVirtualEquipmentRest);
        assertNotNull(inventoryResponseRest);
    }

    @Test
    void deleteInVirtualEquipmentTest() {
        String digitalServiceUid = "service-123";
        Long id = 1L;
        Long inventoryId = 1L;
        InVirtualEquipmentRest inVirtualEquipmentRest = InVirtualEquipmentRest.builder().datacenterName("default").physicalEquipmentName("MyPE").name("MyVE").id(1L).allocationFactor(0.5).digitalServiceUid("dummyid").inventoryId(11L).durationHour(33.0).electricityConsumption(22.0).build();
        InVirtualEquipment virtualEquipment = InVirtualEquipment.builder().id(1L).name("MyVE").physicalEquipmentName("MyPE")
                .allocationFactor(0.5).datacenterName("default").digitalServiceUid("dummyid").durationHour(33.0).electricityConsumption(22.0).infrastructureType("CLOUD_SERVERS").quantity(12.0).sizeDiskGb(234.0).
                sizeMemoryGb(3.0).vcpuCoreNumber(2.0).workload(33.0).location("France").build();

        when(inVirtualEquipmentRepository.findByDigitalServiceVersionUidAndId(digitalServiceUid, id)).thenReturn(Optional.empty());
        G4itRestException exception1 = assertThrows(G4itRestException.class, () ->
                inVirtualEquipmentService.deleteInVirtualEquipment(digitalServiceUid, id));
        assertEquals("404", exception1.getCode());

        when(inVirtualEquipmentRepository.findByInventoryIdAndId(inventoryId, id)).thenReturn(Optional.empty());
        G4itRestException exception2 = assertThrows(G4itRestException.class, () ->
                inVirtualEquipmentService.deleteInVirtualEquipment(inventoryId, id));
        assertEquals("404", exception2.getCode());

        when(inVirtualEquipmentRepository.findByDigitalServiceVersionUidAndId(digitalServiceUid, inVirtualEquipmentRest.getId())).thenReturn(Optional.of(virtualEquipment));
        inVirtualEquipmentService.deleteInVirtualEquipment(digitalServiceUid, id);
        when(inVirtualEquipmentRepository.findByInventoryIdAndId(inventoryId, inVirtualEquipmentRest.getId())).thenReturn(Optional.of(virtualEquipment));
        inVirtualEquipmentService.deleteInVirtualEquipment(inventoryId, id);
    }

    @Test
    void getByDigitalServiceReturnsVirtualEquipmentListWhenDigitalServiceExists() {
        String digitalServiceUid = "service-123";
        List<InVirtualEquipment> virtualEquipments = List.of(new InVirtualEquipment());
        List<InVirtualEquipmentRest> virtualEquipmentRests = List.of(new InVirtualEquipmentRest());

        when(inVirtualEquipmentRepository.findByDigitalServiceVersionUidOrderByName(digitalServiceUid)).thenReturn(virtualEquipments);
        when(inVirtualEquipmentMapper.toRest(virtualEquipments)).thenReturn(virtualEquipmentRests);

        List<InVirtualEquipmentRest> result = inVirtualEquipmentService.getByDigitalServiceVersion(digitalServiceUid);

        assertEquals(virtualEquipmentRests, result);
        verify(inVirtualEquipmentRepository).findByDigitalServiceVersionUidOrderByName(digitalServiceUid);
        verify(inVirtualEquipmentMapper).toRest(virtualEquipments);
    }

    @Test
    void getByDigitalServiceAndId() {
        String digitalServiceUid = "service-123";
        Long id = 1L;

        when(inVirtualEquipmentRepository.findByDigitalServiceVersionUidAndId(digitalServiceUid, id)).thenReturn(Optional.empty());

        G4itRestException exception1 = assertThrows(G4itRestException.class, () ->
                inVirtualEquipmentService.getByDigitalServiceVersionAndId(digitalServiceUid, id));

        assertEquals("404", exception1.getCode());
        verify(inVirtualEquipmentRepository).findByDigitalServiceVersionUidAndId(digitalServiceUid, id);

        InVirtualEquipment virtualEquipment = InVirtualEquipment.builder().id(1L).name("MyVE").physicalEquipmentName("MyPE").digitalServiceVersionUid("uid")
                .allocationFactor(0.5).datacenterName("default").digitalServiceUid("dummyid").durationHour(33.0).electricityConsumption(22.0).infrastructureType("CLOUD_SERVERS").quantity(12.0).sizeDiskGb(234.0).
                sizeMemoryGb(3.0).vcpuCoreNumber(2.0).workload(33.0).location("France").build();
        InVirtualEquipmentRest inVirtualEquipmentRest = InVirtualEquipmentRest.builder().datacenterName("default").physicalEquipmentName("MyPE").name("MyVE").id(1L).allocationFactor(0.5).digitalServiceUid("dummyid").durationHour(33.0).electricityConsumption(22.0).build();

        when(inVirtualEquipmentRepository.findByDigitalServiceVersionUidAndId(digitalServiceUid, id)).thenReturn(Optional.of(virtualEquipment));
        G4itRestException exception2 = assertThrows(G4itRestException.class, () ->
                inVirtualEquipmentService.getByDigitalServiceVersionAndId(digitalServiceUid, id));

        assertEquals("409", exception2.getCode());
        when(inVirtualEquipmentRepository.findByDigitalServiceVersionUidAndId(virtualEquipment.getDigitalServiceVersionUid(), virtualEquipment.getId())).thenReturn(Optional.of(virtualEquipment));
        when(inVirtualEquipmentMapper.toRest(virtualEquipment)).thenReturn(inVirtualEquipmentRest);
        InVirtualEquipmentRest response = inVirtualEquipmentService.getByDigitalServiceVersionAndId(virtualEquipment.getDigitalServiceVersionUid(), virtualEquipment.getId());
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
    void createInVirtualEquipmentDigitalServiceThrowsExceptionWhenDigitalServiceDoesNotExist() {
        String digitalServiceUid = "service-123";
        InVirtualEquipmentRest inVirtualEquipmentRest = new InVirtualEquipmentRest();

        when(digitalServiceVersionRepository.findById(digitalServiceUid)).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inVirtualEquipmentService.createInVirtualEquipmentDigitalServiceVersion(digitalServiceUid, inVirtualEquipmentRest));

        assertEquals("404", exception.getCode());
        verify(digitalServiceVersionRepository).findById(digitalServiceUid);
    }

    @Test
    void deleteInVirtualEquipmentThrowsExceptionWhenVirtualEquipmentNotFound() {
        String digitalServiceUid = "service-123";
        Long id = 1L;

        when(inVirtualEquipmentRepository.findByDigitalServiceVersionUidAndId(digitalServiceUid, id)).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inVirtualEquipmentService.deleteInVirtualEquipment(digitalServiceUid, id));

        assertEquals("404", exception.getCode());
        verify(inVirtualEquipmentRepository).findByDigitalServiceVersionUidAndId(digitalServiceUid, id);
    }

    @Test
    void updateOrDeleteInVirtualEquipmentsHandlesEmptyRepositoryListWhenNoExistingVirtualEquipments() {
        String digitalServiceUid = "service-123";
        Long physicalEqpId = 1L;
        String physicalEqpName = "Physical Equipment 1";

        InPhysicalEquipment physicalEquipment = new InPhysicalEquipment();
        physicalEquipment.setName(physicalEqpName);

        InVirtualEquipmentRest inputEquipment = new InVirtualEquipmentRest();
        inputEquipment.setId(1L);
        List<InVirtualEquipmentRest> inVirtualEquipmentList = List.of(inputEquipment);

        when(inPhysicalEquipmentRepository.findById(physicalEqpId)).thenReturn(Optional.of(physicalEquipment));
        when(inVirtualEquipmentRepository.findByDigitalServiceVersionUidAndPhysicalEquipmentName(digitalServiceUid, physicalEqpName)).thenReturn(List.of());
        when(inVirtualEquipmentRepository.findByDigitalServiceVersionUidAndId(digitalServiceUid, inputEquipment.getId())).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inVirtualEquipmentService.updateOrDeleteInVirtualEquipments(digitalServiceUid, physicalEqpId, inVirtualEquipmentList));

        assertEquals("404", exception.getCode());
        verify(inPhysicalEquipmentRepository).findById(physicalEqpId);
    }


    @Test
    void updateOrDeleteInVirtualEquipmentsThrowsExceptionWhenPhysicalEquipmentIdIsNull() {
        String digitalServiceUid = "service-123";
        Long physicalEqpId = null;
        List<InVirtualEquipmentRest> inVirtualEquipmentList = List.of();

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inVirtualEquipmentService.updateOrDeleteInVirtualEquipments(digitalServiceUid, physicalEqpId, inVirtualEquipmentList));

        assertEquals("404", exception.getCode());
        assertEquals("The digitalService id provided: service-123 has no physical equipment with id: null", exception.getMessage());
    }

    @Test
    void updateOrDeleteInVirtualEquipmentsThrowsExceptionWhenInputListContainsNullElement() {
        String digitalServiceUid = "service-123";
        Long physicalEqpId = 1L;
        List<InVirtualEquipmentRest> inVirtualEquipmentList = List.of(new InVirtualEquipmentRest());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inVirtualEquipmentService.updateOrDeleteInVirtualEquipments(digitalServiceUid, physicalEqpId, inVirtualEquipmentList));

        assertEquals("404", exception.getCode());
        assertEquals("The digitalService id provided: service-123 has no physical equipment with id: 1", exception.getMessage());
    }

    @Test
    void updateOrDeleteInVirtualEquipmentsThrowsExceptionWhenInputListHasDuplicateIds() {
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

    @Test
    void createInVirtualEquipmentThrows400WhenCountryInvalid() {
        String digitalServiceId = "ds-1";
        DigitalService ds = DigitalService.builder().uid("ds-uid").build();
        DigitalServiceVersion dsv = DigitalServiceVersion.builder().digitalService(ds).build();

        InVirtualEquipmentRest rest = InVirtualEquipmentRest.builder().name("ve").location("InvalidCountry").build();

        when(digitalServiceVersionRepository.findById(digitalServiceId)).thenReturn(Optional.of(dsv));
        when(commonValidationUtil.validateboaviztaCountry("InvalidCountry")).thenReturn(false);

        G4itRestException ex = assertThrows(G4itRestException.class,
                () -> inVirtualEquipmentService.createInVirtualEquipmentDigitalServiceVersion(digitalServiceId, rest));
        assertEquals("400", ex.getCode());
        verify(digitalServiceVersionRepository).findById(digitalServiceId);
        verify(commonValidationUtil).validateboaviztaCountry("InvalidCountry");
        verify(inVirtualEquipmentRepository, never()).save(any());
    }

    @Test
    void createInVirtualEquipmentSucceedsWhenCountryValid() {
        String digitalServiceId = "ds-2";
        DigitalService ds = DigitalService.builder().uid("ds-uid-2").build();
        DigitalServiceVersion dsv = DigitalServiceVersion.builder().digitalService(ds).build();

        InVirtualEquipmentRest rest = InVirtualEquipmentRest.builder().name("ve2").location("France").build();
        InVirtualEquipment entity = new InVirtualEquipment();

        when(digitalServiceVersionRepository.findById(digitalServiceId)).thenReturn(Optional.of(dsv));
        when(commonValidationUtil.validateboaviztaCountry("France")).thenReturn(true);
        when(inVirtualEquipmentMapper.toEntity(rest)).thenReturn(entity);
        when(inVirtualEquipmentMapper.toRest(entity)).thenReturn(rest);

        InVirtualEquipmentRest result = inVirtualEquipmentService.createInVirtualEquipmentDigitalServiceVersion(digitalServiceId, rest);

        assertNotNull(result);
        // The service sets these fields on the entity produced by the mapper
        assertNull(entity.getId());
        assertEquals(digitalServiceId, entity.getDigitalServiceVersionUid());
        assertEquals(ds.getUid(), entity.getDigitalServiceUid());
        assertNotNull(entity.getCreationDate());
        assertNotNull(entity.getLastUpdateDate());
        verify(inVirtualEquipmentRepository).save(entity);
        verify(inVirtualEquipmentMapper).toEntity(rest);
        verify(inVirtualEquipmentMapper).toRest(entity);
    }

    @Test
    void shouldThrow400WhenLocationValidationFails() {
        String dsvUid = "dsv-2";
        Long id = 7L;
        InVirtualEquipment stored = new InVirtualEquipment();
        stored.setId(id);
        stored.setDigitalServiceVersionUid(dsvUid);

        InVirtualEquipmentRest updateRest = new InVirtualEquipmentRest();
        updateRest.setLocation("UnknownCountry");

        when(inVirtualEquipmentRepository.findByDigitalServiceVersionUidAndId(dsvUid, id)).thenReturn(Optional.of(stored));
        when(commonValidationUtil.validateboaviztaCountry("UnknownCountry")).thenReturn(false);

        G4itRestException ex = assertThrows(G4itRestException.class,
                () -> inVirtualEquipmentService.updateInVirtualEquipment(dsvUid, id, updateRest));
        assertEquals("400", ex.getCode());
        verify(inVirtualEquipmentRepository).findByDigitalServiceVersionUidAndId(dsvUid, id);
        verify(commonValidationUtil).validateboaviztaCountry("UnknownCountry");
        verifyNoInteractions(inVirtualEquipmentMapper);
    }

    @Test
    void shouldUpdateAndReturnRestWhenInputIsValid() {
        String dsvUid = "dsv-3";
        Long id = 3L;
        InVirtualEquipment stored = new InVirtualEquipment();
        stored.setId(id);
        stored.setDigitalServiceVersionUid(dsvUid);
        stored.setLocation("France");

        InVirtualEquipmentRest updateRest = new InVirtualEquipmentRest();
        updateRest.setLocation("France");
        updateRest.setName("UpdatedName");

        InVirtualEquipment updatesEntity = new InVirtualEquipment();
        InVirtualEquipmentRest returnedRest = new InVirtualEquipmentRest();
        returnedRest.setName("UpdatedName");

        when(inVirtualEquipmentRepository.findByDigitalServiceVersionUidAndId(dsvUid, id)).thenReturn(Optional.of(stored));
        when(commonValidationUtil.validateboaviztaCountry("France")).thenReturn(true);
        when(inVirtualEquipmentMapper.toEntity(updateRest)).thenReturn(updatesEntity);
        // mapper.merge is void - just ensure it is called
        when(inVirtualEquipmentMapper.toRest(stored)).thenReturn(returnedRest);

        InVirtualEquipmentRest result = inVirtualEquipmentService.updateInVirtualEquipment(dsvUid, id, updateRest);

        assertNotNull(result);
        assertEquals(returnedRest, result);
        verify(inVirtualEquipmentRepository).findByDigitalServiceVersionUidAndId(dsvUid, id);
        verify(commonValidationUtil).validateboaviztaCountry("France");
        verify(inVirtualEquipmentMapper).toEntity(updateRest);
        verify(inVirtualEquipmentMapper).merge(stored, updatesEntity);
        verify(inVirtualEquipmentRepository).save(stored);
        verify(inVirtualEquipmentMapper).toRest(stored);
    }

}
