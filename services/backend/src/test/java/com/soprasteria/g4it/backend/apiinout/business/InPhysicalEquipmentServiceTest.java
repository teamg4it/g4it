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
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceVersionRepository;
import com.soprasteria.g4it.backend.apiinout.mapper.InPhysicalEquipmentMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.common.utils.CommonValidationUtil;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InPhysicalEquipmentServiceTest {

    @Mock
    private InPhysicalEquipmentRepository inPhysicalEquipmentRepository;

    @Mock
    private InPhysicalEquipmentMapper inPhysicalEquipmentMapper;

    @Mock
    private DigitalServiceVersionRepository digitalServiceVersionRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InPhysicalEquipmentService inPhysicalEquipmentService;

    @Mock
    private CommonValidationUtil commonValidationUtil;


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

        when(inPhysicalEquipmentRepository.findByDigitalServiceVersionUidOrderByName(digitalServiceUid)).thenReturn(List.of());

        List<InPhysicalEquipmentRest> result = inPhysicalEquipmentService.getByDigitalServiceVersion(digitalServiceUid);

        assertEquals(0, result.size());
        verify(inPhysicalEquipmentRepository).findByDigitalServiceVersionUidOrderByName(digitalServiceUid);
    }

    @Test
    void getByDigitalServiceAndId() {
        String digitalServiceUid = "test-exception";
        Long id = 1L;
        InPhysicalEquipment inPhysicalEquipment = InPhysicalEquipment.builder().digitalServiceVersionUid("dummy_id").id(31L).build();
        G4itRestException exception1 = assertThrows(G4itRestException.class, () ->
                inPhysicalEquipmentService.getByDigitalServiceVersionAndId(digitalServiceUid, id));

        assertEquals("404", exception1.getCode());
        when(inPhysicalEquipmentRepository.findByDigitalServiceVersionUidAndId(digitalServiceUid, id)).thenReturn(Optional.of(inPhysicalEquipment));
        G4itRestException exception2 = assertThrows(G4itRestException.class, () ->
                inPhysicalEquipmentService.getByDigitalServiceVersionAndId(digitalServiceUid, id));

        assertEquals("409", exception2.getCode());
        when(inPhysicalEquipmentRepository.findByDigitalServiceVersionUidAndId(inPhysicalEquipment.getDigitalServiceVersionUid(), inPhysicalEquipment.getId())).thenReturn(Optional.of(inPhysicalEquipment));
        when(inPhysicalEquipmentMapper.toRest(Mockito.any(InPhysicalEquipment.class))).thenReturn(new InPhysicalEquipmentRest());
        InPhysicalEquipmentRest response = inPhysicalEquipmentService.getByDigitalServiceVersionAndId(inPhysicalEquipment.getDigitalServiceVersionUid(), inPhysicalEquipment.getId());
        assertNotNull(response);
    }

    @Test
    void createInPhysicalEquipmentDigitalService_throwsException_whenDigitalServiceDoesNotExist() {
        String digitalServiceUid = "service-123";
        InPhysicalEquipmentRest equipmentRest = new InPhysicalEquipmentRest();

        when(digitalServiceVersionRepository.findById(digitalServiceUid)).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inPhysicalEquipmentService.createInPhysicalEquipmentDigitalServiceVersion(digitalServiceUid, equipmentRest));

        assertEquals("404", exception.getCode());
        assertEquals("the digital service version of uid : service-123, doesn't exist", exception.getMessage());
        verify(digitalServiceVersionRepository).findById(digitalServiceUid);
    }

    @Test
    void updateInPhysicalEquipment_throwsException_whenDigitalServiceUidMismatch() {
        String digitalServiceUid = "service-123";
        Long id = 1L;
        InPhysicalEquipmentRest equipmentUpdateRest = new InPhysicalEquipmentRest();
        InPhysicalEquipment existingEquipment = new InPhysicalEquipment();
        existingEquipment.setDigitalServiceVersionUid("service-456");

        when(inPhysicalEquipmentRepository.findByDigitalServiceVersionUidAndId(digitalServiceUid, id)).thenReturn(Optional.of(existingEquipment));

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inPhysicalEquipmentService.updateInPhysicalEquipment(digitalServiceUid, id, equipmentUpdateRest));

        assertEquals("409", exception.getCode());
        assertEquals("the digital service version uid provided: service-123 is not compatible with the digital uid : service-456 linked to this physical equipment id: 1", exception.getMessage());
        verify(inPhysicalEquipmentRepository).findByDigitalServiceVersionUidAndId(digitalServiceUid, id);
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
        InPhysicalEquipmentRest inVirtualEquipmentRest = InPhysicalEquipmentRest.builder().datacenterName("default").name("MyPE").name("MyVE").id(1L).digitalServiceUid("dummyid").electricityConsumption(22.0).build();
        InPhysicalEquipment inVirtualEquipment = InPhysicalEquipment.builder().id(1L).name("MyVE").name("MyPE")
                .datacenterName("default").digitalServiceUid("dummyid").durationHour(33.0).electricityConsumption(22.0).sizeDiskGb(234.0).
                sizeMemoryGb(3.0).build();

        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.empty());
        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inPhysicalEquipmentService.createInPhysicalEquipmentInventory(inventoryId, inVirtualEquipmentRest));
        assertEquals("404", exception.getCode());
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(inventory));
        when(inPhysicalEquipmentMapper.toEntity(inVirtualEquipmentRest)).thenReturn(inVirtualEquipment);
        when(inPhysicalEquipmentMapper.toRest(inVirtualEquipment)).thenReturn(inVirtualEquipmentRest);
        InPhysicalEquipmentRest responseRest = inPhysicalEquipmentService.createInPhysicalEquipmentInventory(inventoryId, inVirtualEquipmentRest);
        assertNotNull(responseRest);
    }


    @Test
    void createInPhysicalEquipmentDigitalServiceTest() {
        String digitalServiceId = "dummy_id";
        var organization = Workspace.builder()
                .name("DEMO")
                .organization(Organization.builder().name("SUBSCRIBER").build())
                .build();
        var digitalService = DigitalService.builder()
                .name("DS_Name")
                .workspace(organization)
                .build();

        var digitalServiceVersion = DigitalServiceVersion.builder()
                .description("DS_Name_1")
                .digitalService(digitalService)
                .build();

        InPhysicalEquipmentRest inVirtualEquipmentRest = InPhysicalEquipmentRest.builder().datacenterName("default").name("MyPE").name("MyVE").id(1L).digitalServiceUid("dummyid").electricityConsumption(22.0).build();
        InPhysicalEquipment inVirtualEquipment = InPhysicalEquipment.builder().id(1L).name("MyVE").name("MyPE")
                .datacenterName("default").digitalServiceUid("dummyid").durationHour(33.0).electricityConsumption(22.0).quantity(12.0).sizeDiskGb(234.0).
                sizeMemoryGb(3.0).location("France").build();

        when(digitalServiceVersionRepository.findById(digitalServiceId)).thenReturn(Optional.empty());
        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inPhysicalEquipmentService.createInPhysicalEquipmentDigitalServiceVersion(digitalServiceId, inVirtualEquipmentRest));
        assertEquals("404", exception.getCode());
        when(digitalServiceVersionRepository.findById(digitalServiceId)).thenReturn(Optional.of(digitalServiceVersion));
        when(inPhysicalEquipmentMapper.toEntity(inVirtualEquipmentRest)).thenReturn(inVirtualEquipment);
        when(inPhysicalEquipmentMapper.toRest(inVirtualEquipment)).thenReturn(inVirtualEquipmentRest);
        InPhysicalEquipmentRest responseRest = inPhysicalEquipmentService.createInPhysicalEquipmentDigitalServiceVersion(digitalServiceId, inVirtualEquipmentRest);
        assertNotNull(responseRest);
    }

    @Test
    void createInPhysicalEquipmentDigitalServiceVersion_throwsBadRequest_whenLocationInvalid() {
        String digitalServiceId = "dummy_id";
        InPhysicalEquipmentRest inVirtualEquipmentRest = InPhysicalEquipmentRest.builder()
                .datacenterName("default")
                .name("MyPE")
                .id(1L)
                .location("InvalidCountry")
                .build();

        var digitalServiceVersion = com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceVersion.builder()
                .digitalService(com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService.builder().build())
                .build();

        when(digitalServiceVersionRepository.findById(digitalServiceId)).thenReturn(Optional.of(digitalServiceVersion));
        when(commonValidationUtil.validateCountry("InvalidCountry")).thenReturn(false);

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inPhysicalEquipmentService.createInPhysicalEquipmentDigitalServiceVersion(digitalServiceId, inVirtualEquipmentRest));

        assertEquals("400", exception.getCode());
        verify(digitalServiceVersionRepository).findById(digitalServiceId);
        verify(commonValidationUtil).validateCountry("InvalidCountry");
    }

    @Test
    void createInPhysicalEquipmentDigitalServiceVersion_allowsNullLocation_andCreates() {
        String digitalServiceId = "dummy_id";
        InPhysicalEquipmentRest inVirtualEquipmentRest = InPhysicalEquipmentRest.builder()
                .datacenterName("default")
                .name("MyPE")
                .id(1L)
                .location(null)
                .build();

        var digitalService = com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService.builder().build();
        var digitalServiceVersion = com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceVersion.builder()
                .digitalService(digitalService)
                .build();

        InPhysicalEquipment inVirtualEquipment = InPhysicalEquipment.builder()
                .id(1L)
                .name("MyPE")
                .datacenterName("default")
                .build();

        when(digitalServiceVersionRepository.findById(digitalServiceId)).thenReturn(Optional.of(digitalServiceVersion));
        when(inPhysicalEquipmentMapper.toEntity(inVirtualEquipmentRest)).thenReturn(inVirtualEquipment);
        when(inPhysicalEquipmentMapper.toRest(inVirtualEquipment)).thenReturn(inVirtualEquipmentRest);

        InPhysicalEquipmentRest response = inPhysicalEquipmentService.createInPhysicalEquipmentDigitalServiceVersion(digitalServiceId, inVirtualEquipmentRest);

        assertNotNull(response);
        verify(digitalServiceVersionRepository).findById(digitalServiceId);
        verify(commonValidationUtil, never()).validateCountry(anyString());
        verify(inPhysicalEquipmentRepository).save(inVirtualEquipment);
        verify(inPhysicalEquipmentMapper).toRest(inVirtualEquipment);
    }

    @Test
    void updateInPhysicalEquipment_throwsBadRequest_whenLocationInvalid() {
        String digitalServiceUid = "service-123";
        Long id = 1L;
        InPhysicalEquipment existing = InPhysicalEquipment.builder()
                .id(id)
                .digitalServiceVersionUid(digitalServiceUid)
                .build();

        InPhysicalEquipmentRest updateRest = InPhysicalEquipmentRest.builder()
                .location("InvalidCountry")
                .build();

        when(inPhysicalEquipmentRepository.findByDigitalServiceVersionUidAndId(digitalServiceUid, id))
                .thenReturn(Optional.of(existing));
        when(commonValidationUtil.validateCountry("InvalidCountry")).thenReturn(false);

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inPhysicalEquipmentService.updateInPhysicalEquipment(digitalServiceUid, id, updateRest));

        assertEquals("400", exception.getCode());
        assertTrue(exception.getMessage().contains("Selected Country : InvalidCountry"));
        verify(commonValidationUtil).validateCountry("InvalidCountry");
        verify(inPhysicalEquipmentRepository).findByDigitalServiceVersionUidAndId(digitalServiceUid, id);
    }

    @Test
    void updateInPhysicalEquipment_updatesSuccessfully() {
        String digitalServiceUid = "service-123";
        Long id = 1L;
        InPhysicalEquipment existing = InPhysicalEquipment.builder()
                .id(id)
                .digitalServiceVersionUid(digitalServiceUid)
                .name("oldName")
                .build();

        InPhysicalEquipmentRest updateRest = InPhysicalEquipmentRest.builder()
                .name("newName")
                .location(null) // skip validation
                .build();

        InPhysicalEquipment updates = InPhysicalEquipment.builder()
                .name("newName")
                .build();

        when(inPhysicalEquipmentRepository.findByDigitalServiceVersionUidAndId(digitalServiceUid, id))
                .thenReturn(Optional.of(existing));
        when(inPhysicalEquipmentMapper.toEntity(updateRest)).thenReturn(updates);
        when(inPhysicalEquipmentMapper.toRest(existing)).thenReturn(updateRest);

        InPhysicalEquipmentRest response = inPhysicalEquipmentService.updateInPhysicalEquipment(digitalServiceUid, id, updateRest);

        assertNotNull(response);
        verify(inPhysicalEquipmentRepository).findByDigitalServiceVersionUidAndId(digitalServiceUid, id);
        verify(inPhysicalEquipmentMapper).toEntity(updateRest);
        verify(inPhysicalEquipmentMapper).merge(existing, updates);
        verify(inPhysicalEquipmentRepository).save(existing);
        verify(inPhysicalEquipmentMapper).toRest(existing);
    }

}
