/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiinout.business;


import com.soprasteria.g4it.backend.apiinout.mapper.InApplicationMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.InApplication;
import com.soprasteria.g4it.backend.apiinout.repository.InApplicationRepository;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.InApplicationRest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InApplicationServiceTest {

    @Mock
    private InApplicationRepository inApplicationRepository;

    @Mock
    private InApplicationMapper inApplicationMapper;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InApplicationService inApplicationService;

    @Test
    void getByInventory_returnsApplications_whenInventoryExists() {
        Long inventoryId = 1L;
        List<InApplication> applications = List.of(new InApplication());
        List<InApplicationRest> applicationRests = List.of(new InApplicationRest());

        when(inApplicationRepository.findByInventoryId(inventoryId)).thenReturn(applications);
        when(inApplicationMapper.toRest(applications)).thenReturn(applicationRests);

        List<InApplicationRest> result = inApplicationService.getByInventory(inventoryId);

        assertEquals(applicationRests, result);
        verify(inApplicationRepository).findByInventoryId(inventoryId);
        verify(inApplicationMapper).toRest(applications);
    }

    @Test
    void getByInventoryAndId_throwsException_whenApplicationNotFound() {
        Long inventoryId = 1L;
        Long applicationId = 2L;

        when(inApplicationRepository.findByInventoryIdAndId(inventoryId, applicationId)).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inApplicationService.getByInventoryAndId(inventoryId, applicationId));

        assertEquals("404", exception.getCode());
        assertTrue(exception.getMessage().contains("has no application with id"));
        verify(inApplicationRepository).findByInventoryIdAndId(inventoryId, applicationId);
    }

    @Test
    void createInApplicationInventory_throwsException_whenInventoryDoesNotExist() {
        Long inventoryId = 1L;
        InApplicationRest inApplicationRest = new InApplicationRest();

        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inApplicationService.createInApplicationInventory(inventoryId, inApplicationRest));

        assertEquals("404", exception.getCode());
        assertTrue(exception.getMessage().contains("doesn't exist"));
        verify(inventoryRepository).findById(inventoryId);
    }

    @Test
    void updateInApplication_throwsException_whenInventoryIdMismatch() {
        Long inventoryId = 1L;
        Long applicationId = 2L;
        InApplicationRest inApplicationUpdateRest = new InApplicationRest();
        InApplication existingApplication = new InApplication();
        existingApplication.setInventoryId(3L);

        when(inApplicationRepository.findByInventoryIdAndId(inventoryId, applicationId)).thenReturn(Optional.of(existingApplication));

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inApplicationService.updateInApplication(inventoryId, applicationId, inApplicationUpdateRest));

        assertEquals("409", exception.getCode());
        assertTrue(exception.getMessage().contains("is not compatible with the inventory id"));
        verify(inApplicationRepository).findByInventoryIdAndId(inventoryId, applicationId);
    }

    @Test
    void deleteInApplication_deletesApplication_whenIdExists() {
        Long applicationId = 1L;

        doNothing().when(inApplicationRepository).deleteById(applicationId);

        inApplicationService.deleteInApplication(applicationId);

        verify(inApplicationRepository).deleteById(applicationId);
    }
}