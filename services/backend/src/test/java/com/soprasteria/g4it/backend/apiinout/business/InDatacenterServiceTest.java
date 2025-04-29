/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiinout.business;

import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apiinout.mapper.InDatacenterMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.InDatacenter;
import com.soprasteria.g4it.backend.apiinout.repository.InDatacenterRepository;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.InDatacenterRest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InDatacenterServiceTest {

    @Mock
    private InDatacenterRepository inDatacenterRepository;

    @Mock
    private InDatacenterMapper inDatacenterMapper;

    @Mock
    private DigitalServiceRepository digitalServiceRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InDatacenterService inDatacenterService;

    @Test
    void getByInventory_returnsDatacenters_whenInventoryExists() {
        Long inventoryId = 1L;
        List<InDatacenter> datacenters = List.of(new InDatacenter());
        List<InDatacenterRest> datacenterRests = List.of(new InDatacenterRest());

        when(inDatacenterRepository.findByInventoryId(inventoryId)).thenReturn(datacenters);
        when(inDatacenterMapper.toRest(datacenters)).thenReturn(datacenterRests);

        List<InDatacenterRest> result = inDatacenterService.getByInventory(inventoryId);

        assertEquals(datacenterRests, result);
        verify(inDatacenterRepository).findByInventoryId(inventoryId);
        verify(inDatacenterMapper).toRest(datacenters);
    }

    @Test
    void getByInventoryAndId_throwsException_whenDatacenterNotFound() {
        Long inventoryId = 1L;
        Long id = 1L;

        when(inDatacenterRepository.findByInventoryIdAndId(inventoryId, id)).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inDatacenterService.getByInventoryAndId(inventoryId, id));

        assertEquals("404", exception.getCode());
        verify(inDatacenterRepository).findByInventoryIdAndId(inventoryId, id);
    }

    @Test
    void createInDatacenterInventory_throwsException_whenInventoryDoesNotExist() {
        Long inventoryId = 1L;
        InDatacenterRest inDatacenterRest = new InDatacenterRest();

        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inDatacenterService.createInDatacenterInventory(inventoryId, inDatacenterRest));

        assertEquals("404", exception.getCode());
        verify(inventoryRepository).findById(inventoryId);
    }

    @Test
    void updateInDatacenter_throwsException_whenInventoryDatacenterNotFound() {
        Long inventoryId = 1L;
        Long id = 1L;
        InDatacenterRest inDatacenterUpdateRest = new InDatacenterRest();

        when(inDatacenterRepository.findByInventoryIdAndId(inventoryId, id)).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inDatacenterService.updateInDatacenter(inventoryId, id, inDatacenterUpdateRest));

        assertEquals("404", exception.getCode());
        verify(inDatacenterRepository).findByInventoryIdAndId(inventoryId, id);
    }

    @Test
    void getByDigitalService_returnsEmptyList_whenNoDatacentersExist() {
        String digitalServiceUid = "nonexistent-uid";

        when(inDatacenterRepository.findByDigitalServiceUid(digitalServiceUid)).thenReturn(List.of());

        List<InDatacenterRest> result = inDatacenterService.getByDigitalService(digitalServiceUid);

        assertEquals(0, result.size());
        verify(inDatacenterRepository).findByDigitalServiceUid(digitalServiceUid);
        verify(inDatacenterMapper).toRest(List.of());
    }

    @Test
    void getByDigitalServiceAndId_throwsException_whenDigitalServiceUidMismatch() {
        String digitalServiceUid = "uid1";
        Long id = 1L;
        InDatacenter inDatacenter = new InDatacenter();
        inDatacenter.setDigitalServiceUid("uid2");

        when(inDatacenterRepository.findByDigitalServiceUidAndId(digitalServiceUid, id)).thenReturn(Optional.of(inDatacenter));

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inDatacenterService.getByDigitalServiceAndId(digitalServiceUid, id));

        assertEquals("409", exception.getCode());
        verify(inDatacenterRepository).findByDigitalServiceUidAndId(digitalServiceUid, id);
    }

    @Test
    void createInDatacenterDigitalService_throwsException_whenDigitalServiceDoesNotExist() {
        String digitalServiceUid = "nonexistent-uid";
        InDatacenterRest inDatacenterRest = new InDatacenterRest();

        when(digitalServiceRepository.findById(digitalServiceUid)).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inDatacenterService.createInDatacenterDigitalService(digitalServiceUid, inDatacenterRest));

        assertEquals("404", exception.getCode());
        verify(digitalServiceRepository).findById(digitalServiceUid);
    }

    @Test
    void updateInDatacenter_throwsException_whenDigitalServiceUidMismatch() {
        String digitalServiceUid = "uid1";
        Long id = 1L;
        InDatacenterRest inDatacenterUpdateRest = new InDatacenterRest();
        InDatacenter inDatacenter = new InDatacenter();
        inDatacenter.setDigitalServiceUid("uid2");

        when(inDatacenterRepository.findByDigitalServiceUidAndId(digitalServiceUid, id)).thenReturn(Optional.of(inDatacenter));

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inDatacenterService.updateInDatacenter(digitalServiceUid, id, inDatacenterUpdateRest));

        assertEquals("409", exception.getCode());
        verify(inDatacenterRepository).findByDigitalServiceUidAndId(digitalServiceUid, id);
    }

    @Test
    void deleteInDatacenter_doesNotThrowException_whenDatacenterExists() {
        Long id = 1L;

        doNothing().when(inDatacenterRepository).deleteById(id);

        inDatacenterService.deleteInDatacenter(id);

        verify(inDatacenterRepository).deleteById(id);
    }

    
}