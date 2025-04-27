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
    void getByDigitalService_returnsDatacenters_whenDigitalServiceExists() {
        String digitalServiceUid = "service-123";
        List<InDatacenter> datacenters = List.of(new InDatacenter());
        List<InDatacenterRest> datacenterRests = List.of(new InDatacenterRest());

        when(inDatacenterRepository.findByDigitalServiceUid(digitalServiceUid)).thenReturn(datacenters);
        when(inDatacenterMapper.toRest(datacenters)).thenReturn(datacenterRests);

        List<InDatacenterRest> result = inDatacenterService.getByDigitalService(digitalServiceUid);

        assertEquals(datacenterRests, result);
        verify(inDatacenterRepository).findByDigitalServiceUid(digitalServiceUid);
        verify(inDatacenterMapper).toRest(datacenters);
    }

    @Test
    void getByDigitalServiceAndId_throwsException_whenDatacenterNotFound() {
        String digitalServiceUid = "service-123";
        Long id = 1L;

        when(inDatacenterRepository.findByDigitalServiceUidAndId(digitalServiceUid, id)).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inDatacenterService.getByDigitalServiceAndId(digitalServiceUid, id));

        assertEquals("404", exception.getCode());
        verify(inDatacenterRepository).findByDigitalServiceUidAndId(digitalServiceUid, id);
    }

    @Test
    void createInDatacenterDigitalService_throwsException_whenDigitalServiceDoesNotExist() {
        String digitalServiceUid = "service-123";
        InDatacenterRest inDatacenterRest = new InDatacenterRest();

        when(digitalServiceRepository.findById(digitalServiceUid)).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inDatacenterService.createInDatacenterDigitalService(digitalServiceUid, inDatacenterRest));

        assertEquals("404", exception.getCode());
        verify(digitalServiceRepository).findById(digitalServiceUid);
    }

    @Test
    void updateInDatacenter_throwsException_whenDatacenterNotFound() {
        String digitalServiceUid = "service-123";
        Long id = 1L;
        InDatacenterRest inDatacenterUpdateRest = new InDatacenterRest();

        when(inDatacenterRepository.findByDigitalServiceUidAndId(digitalServiceUid, id)).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inDatacenterService.updateInDatacenter(digitalServiceUid, id, inDatacenterUpdateRest));

        assertEquals("404", exception.getCode());
        verify(inDatacenterRepository).findByDigitalServiceUidAndId(digitalServiceUid, id);
    }

    @Test
    void deleteInDatacenter_deletesDatacenter_whenIdExists() {
        Long id = 1L;

        doNothing().when(inDatacenterRepository).deleteById(id);

        inDatacenterService.deleteInDatacenter(id);

        verify(inDatacenterRepository).deleteById(id);
    }
}