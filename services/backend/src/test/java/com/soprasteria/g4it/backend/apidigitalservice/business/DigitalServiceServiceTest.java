/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apidigitalservice.business;

import com.soprasteria.g4it.backend.apiaiinfra.repository.InAiInfrastructureRepository;
import com.soprasteria.g4it.backend.apidigitalservice.mapper.DigitalServiceMapper;
import com.soprasteria.g4it.backend.apidigitalservice.model.DigitalServiceBO;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceSharedLink;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceVersion;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceLinkRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceVersionRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InDatacenterRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiparameterai.repository.InAiParameterRepository;
import com.soprasteria.g4it.backend.apiuser.business.RoleService;
import com.soprasteria.g4it.backend.apiuser.business.WorkspaceService;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.*;
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserWorkspaceRepository;
import com.soprasteria.g4it.backend.common.model.NoteBO;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.DigitalServiceShareRest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DigitalServiceServiceTest {

    private static final Long WORKSPACE_ID = 1L;
    private static final Long ORGANIZATION_ID = 1L;
    private static final String DIGITAL_SERVICE_UID = "80651485-3f8b-49dd-a7be-753e4fe1fd36";
    private static final String ORGANIZATION = "organization";
    private static final String WORKSPACE_NAME = "workspace";
    private static final long USER_ID = 1;
    private static final Boolean IS_AI = false;

    @Mock
    private DigitalServiceRepository digitalServiceRepository;
    @Mock
    private WorkspaceService workspaceService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleService roleService;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private UserWorkspaceRepository userWorkspaceRepository;
    @Mock
    private DigitalServiceMapper digitalServiceMapper;
    @Mock
    private DigitalServiceReferentialService digitalServiceReferentialService;
    @Mock
    private DigitalServiceLinkRepository digitalServiceLinkRepo;
    @Mock
    private InVirtualEquipmentRepository inVirtualEquipmentRepository;
    @Mock
    private InPhysicalEquipmentRepository inPhysicalEquipmentRepository;
    @Mock
    private InDatacenterRepository inDatacenterRepository;
    @Mock
    private InAiParameterRepository inAiParameterRepository;
    @Mock
    private InAiInfrastructureRepository inAiInfrastructureRepository;
    @InjectMocks
    private DigitalServiceService digitalServiceService;
    @Mock
    private DigitalServiceVersionRepository digitalServiceVersionRepository;


    @Test
    void shouldListDigitalService() {

        final Workspace linkedWorkspace = Workspace.builder().name(WORKSPACE_NAME).build();

        User creator = User.builder().id(1L).firstName("first").lastName("last").build();

        DigitalService digitalService = DigitalService.builder().name("name").isAi(IS_AI).user(creator).build();
        DigitalServiceVersion digitalServiceVersion = DigitalServiceVersion.builder().uid("uid").digitalService(digitalService).build();
        final DigitalServiceBO digitalServiceBo = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).build();

        when(digitalServiceMapper.toBusinessObject(any(DigitalService.class)))
                .thenReturn(digitalServiceBo);

        when(workspaceService.getWorkspaceById(WORKSPACE_ID)).thenReturn(linkedWorkspace);
        when(digitalServiceRepository.findByWorkspace(linkedWorkspace)).thenReturn(List.of(digitalService));
        when(digitalServiceVersionRepository.findActiveDigitalServiceVersion(anyList())).thenReturn(List.of(digitalServiceVersion));

        List<DigitalServiceBO> result = digitalServiceService.getDigitalServices(WORKSPACE_ID, IS_AI);
        assertThat(result).isEqualTo(List.of(digitalServiceBo));

        verify(digitalServiceRepository, times(1)).findByWorkspace(linkedWorkspace);
        verify(workspaceService, times(1)).getWorkspaceById(WORKSPACE_ID);
        verify(digitalServiceMapper).toBusinessObject(any(DigitalService.class));

    }


    @Test
    void digitalServiceExists_WhenOrganizationMatchesAndServiceExists_ReturnsTrue() {


        final Workspace workspace = Workspace.builder().name(WORKSPACE_NAME)
                .organization(Organization.builder().name(ORGANIZATION).build()).build();

        final DigitalService digitalService = DigitalService.builder().uid(DIGITAL_SERVICE_UID).build();

        when(workspaceService.getWorkspaceById(WORKSPACE_ID)).thenReturn(workspace);
        when(digitalServiceRepository.findByWorkspaceAndUid(workspace, DIGITAL_SERVICE_UID))
                .thenReturn(Optional.of(digitalService));

        boolean result = digitalServiceService.digitalServiceExists(
                ORGANIZATION, WORKSPACE_ID, DIGITAL_SERVICE_UID);

        assertTrue(result);
        verify(digitalServiceRepository).findByWorkspaceAndUid(workspace, DIGITAL_SERVICE_UID);
    }

    @Test
    void digitalServiceExists_WhenOrganizationMismatch_ReturnsFalse() {

        final Workspace workspace = Workspace.builder().name(WORKSPACE_NAME)
                .organization(Organization.builder().name("Organization2").build()).build();

        when(workspaceService.getWorkspaceById(WORKSPACE_ID)).thenReturn(workspace);

        boolean result = digitalServiceService.digitalServiceExists(
                ORGANIZATION, WORKSPACE_ID, DIGITAL_SERVICE_UID);

        assertFalse(result);
        verify(digitalServiceRepository, never()).findByWorkspaceAndUid(any(), any());
    }

    @Test
    void digitalServiceExists_WhenOrganizationMatchesAndServiceNotExist_ReturnsFalse() {

        final Workspace workspace = Workspace.builder().name(WORKSPACE_NAME)
                .organization(Organization.builder().name(ORGANIZATION).build()).build();

        when(workspaceService.getWorkspaceById(WORKSPACE_ID)).thenReturn(workspace);
        when(digitalServiceRepository.findByWorkspaceAndUid(workspace, DIGITAL_SERVICE_UID))
                .thenReturn(Optional.empty());

        boolean result = digitalServiceService.digitalServiceExists(
                ORGANIZATION, WORKSPACE_ID, DIGITAL_SERVICE_UID);

        assertFalse(result);
        verify(digitalServiceRepository).findByWorkspaceAndUid(workspace, DIGITAL_SERVICE_UID);
    }

    @Test
    void deleteDigitalService_shouldCallAllRepositoriesWithCorrectUid() {
        // Arrange
        String digitalServiceUid = "ds-123";

        // Act
        digitalServiceService.deleteDigitalService(digitalServiceUid); // appelle ta méthode

        // Assert : on vérifie que tous les repositories ont bien été appelés
        verify(inVirtualEquipmentRepository).deleteByDigitalServiceUid(digitalServiceUid);
        verify(inPhysicalEquipmentRepository).deleteByDigitalServiceUid(digitalServiceUid);
        verify(inDatacenterRepository).deleteByDigitalServiceUid(digitalServiceUid);
        verify(inAiParameterRepository).deleteByDigitalServiceUid(digitalServiceUid);
        verify(inAiInfrastructureRepository).deleteByDigitalServiceUid(digitalServiceUid);
        verify(digitalServiceRepository).deleteById(digitalServiceUid);
    }


}