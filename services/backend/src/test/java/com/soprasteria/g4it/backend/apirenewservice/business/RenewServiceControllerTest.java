/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apirenewservice.business;

import com.soprasteria.g4it.backend.apirenewservice.controller.RenewServiceController;
import com.soprasteria.g4it.backend.server.gen.api.dto.RenewResponseRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.RenewRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.RenewUpdateRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RenewServiceControllerTest {

    @Mock
    private RenewService renewService;

    @InjectMocks
    private RenewServiceController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetRenewDetailsInventory() {
        Long workspace = 1L;
        Long inventoryId = 2L;
        String organization = "org";
        RenewRest mockRest = RenewRest.builder().build();
        when(renewService.getRenewDetailsInventory(workspace, inventoryId)).thenReturn(mockRest);

        ResponseEntity<RenewRest> response = controller.getRenewDetailsInventory(organization, workspace, inventoryId);
        assertEquals(mockRest, response.getBody());
        assertEquals(200, response.getStatusCode().value());
        verify(renewService).getRenewDetailsInventory(workspace, inventoryId);
    }

    @Test
    void testGetRenewDetailsDigitalService() {
        Long workspace = 1L;
        String digitalServiceVersionUid = "uid";
        String organization = "org";
        RenewRest mockRest = RenewRest.builder().build();
        when(renewService.getRenewDetailsDigitalService(workspace, digitalServiceVersionUid)).thenReturn(mockRest);

        ResponseEntity<RenewRest> response = controller.getRenewDetailsDigitalService(organization, workspace, digitalServiceVersionUid);
        assertEquals(mockRest, response.getBody());
        assertEquals(200, response.getStatusCode().value());
        verify(renewService).getRenewDetailsDigitalService(workspace, digitalServiceVersionUid);
    }

    @Test
    void testRenewDigitalService() {
        Long workspace = 1L;
        String digitalServiceVersionUid = "uid";
        String organization = "org";
        RenewUpdateRest renewUpdateRest = RenewUpdateRest.builder().build();
        RenewResponseRest mockResponse = RenewResponseRest.builder().build();
        when(renewService.renewDigitalService(workspace, renewUpdateRest)).thenReturn(mockResponse);

        ResponseEntity<RenewResponseRest> response = controller.renewDigitalService(organization, workspace, digitalServiceVersionUid, renewUpdateRest);
        assertEquals(mockResponse, response.getBody());
        assertEquals(200, response.getStatusCode().value());
        verify(renewService).renewDigitalService(workspace, renewUpdateRest);
    }

    @Test
    void testRenewInventoryService() {
        Long workspace = 1L;
        Long inventoryId = 2L;
        String organization = "org";
        RenewUpdateRest renewUpdateRest = RenewUpdateRest.builder().build();
        RenewResponseRest mockResponse = RenewResponseRest.builder().build();
        when(renewService.renewInventoryService(workspace, inventoryId, renewUpdateRest)).thenReturn(mockResponse);

        ResponseEntity<RenewResponseRest> response = controller.renewInventoryService(organization, workspace, inventoryId, renewUpdateRest);
        assertEquals(mockResponse, response.getBody());
        assertEquals(200, response.getStatusCode().value());
        verify(renewService).renewInventoryService(workspace, inventoryId, renewUpdateRest);
    }
}
