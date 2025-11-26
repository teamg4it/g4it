/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apidigitalservice.business;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.management.relation.Role;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.soprasteria.g4it.backend.apiaiinfra.repository.InAiInfrastructureRepository;
import com.soprasteria.g4it.backend.apidigitalservice.mapper.DigitalServiceVersionMapper;
import com.soprasteria.g4it.backend.apidigitalservice.model.DigitalServiceVersionBO;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceSharedLink;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceVersion;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceVersionStatus;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceLinkRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceVersionRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InApplicationRepository;
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
import com.soprasteria.g4it.backend.server.gen.api.dto.DigitalServiceVersionsListRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InDigitalServiceVersionRest;

@ExtendWith(MockitoExtension.class)
class DigitalServiceVersionServiceTest {

    private static final Long WORKSPACE_ID = 1L;
    private static final Long ORGANIZATION_ID = 1L;
    private static final String DIGITAL_SERVICE_UID = "80651485-3f8b-49dd-a7be-753e4fe1fd36";
    private static final String DIGITAL_SERVICE_VERSION_UID = "90651485-3f8b-49dd-a7be-753e4fe1fd36";
    private static final String ORGANIZATION = "organization";
    private static final String WORKSPACE_NAME = "workspace";
    private static final long USER_ID = 1;
    private static final Boolean IS_AI = false;
    private static final String DIGITAL_SERVICE_NAME = "My DS";
    private static final String VERSION_NAME = "version 1";
    private static final String VERSION_TYPE = "draft";

    @Mock
    private DigitalServiceRepository digitalServiceRepository;
    @Mock
    private DigitalServiceVersionRepository digitalServiceVersionRepository;
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
    private DigitalServiceVersionMapper digitalServiceVersionMapper;
    @Mock
    private DigitalServiceReferentialService digitalServiceReferentialService;
    @Mock
    private DigitalServiceLinkRepository digitalServiceLinkRepo;
    @Mock
    private InVirtualEquipmentRepository inVirtualEquipmentRepository;
    @Mock
    private InApplicationRepository inApplicationRepository;
    @Mock
    private InPhysicalEquipmentRepository inPhysicalEquipmentRepository;
    @Mock
    private InDatacenterRepository inDatacenterRepository;
    @Mock
    private InAiParameterRepository inAiParameterRepository;
    @Mock
    private InAiInfrastructureRepository inAiInfrastructureRepository;
    @InjectMocks
    private DigitalServiceVersionService digitalServiceVersionService;

    @Test
    void shouldCreateNewDigitalServiceVersion_first() {

        final Workspace linkedWorkspace = Workspace.builder().name(WORKSPACE_NAME).build();

        final User user = User.builder().id(USER_ID).build();
        final DigitalServiceVersionBO expectedBo = DigitalServiceVersionBO.builder().build();
        final String expectedName = "Digital Service 1";
        final List<DigitalService> existingDigitalService = new ArrayList<>();

        final DigitalService digitalServiceToSave = DigitalService.builder().uid(DIGITAL_SERVICE_UID).workspace(linkedWorkspace).user(user).name(expectedName).build();
        final DigitalServiceVersion digitalServiceVersionToSave = DigitalServiceVersion.builder().digitalService(digitalServiceToSave).build();
        when(workspaceService.getWorkspaceById(WORKSPACE_ID)).thenReturn(linkedWorkspace);
        when(digitalServiceRepository.save(any())).thenReturn(digitalServiceToSave);
        when(digitalServiceVersionRepository.save(any())).thenReturn(digitalServiceVersionToSave);
        when(digitalServiceVersionMapper.toBusinessObject(digitalServiceVersionToSave, digitalServiceToSave)).thenReturn(expectedBo);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        InDigitalServiceVersionRest inDigitalServiceVersionRest = mock(InDigitalServiceVersionRest.class);


        final DigitalServiceVersionBO result = digitalServiceVersionService.createDigitalServiceVersion(WORKSPACE_ID, USER_ID, inDigitalServiceVersionRest);

        assertThat(result).isEqualTo(expectedBo);

        verify(workspaceService, times(1)).getWorkspaceById(WORKSPACE_ID);
        verify(digitalServiceRepository, times(1)).save(any());
        verify(digitalServiceVersionRepository, times(1)).save(any());
        verify(digitalServiceVersionMapper, times(1)).toBusinessObject(digitalServiceVersionToSave, digitalServiceToSave);
        verify(userRepository, times(1)).findById(USER_ID);
    }

    @Test
    void shouldCreateNewDigitalService_withExistingDigitalService() {

        final User user = User.builder().id(USER_ID).build();
        final Workspace linkedWorkspace = Workspace.builder().name(WORKSPACE_NAME).build();

        final DigitalServiceVersionBO expectedBo = DigitalServiceVersionBO.builder().build();
        final String expectedName = "Digital Service 2";
        final List<DigitalService> existingDigitalService = List.of(DigitalService.builder().name("Digital Service 1").build(), DigitalService.builder().name("My Digital Service").build());

        final DigitalService digitalServiceToSave = DigitalService.builder().workspace(linkedWorkspace).user(user).name(expectedName).build();
        final DigitalServiceVersion digitalServiceVersionToSave = DigitalServiceVersion.builder().digitalService(digitalServiceToSave).build();
        when(workspaceService.getWorkspaceById(WORKSPACE_ID)).thenReturn(linkedWorkspace);
        when(digitalServiceRepository.save(any())).thenReturn(digitalServiceToSave);
        when(digitalServiceVersionRepository.save(any())).thenReturn(digitalServiceVersionToSave);
        when(digitalServiceVersionMapper.toBusinessObject(digitalServiceVersionToSave, digitalServiceToSave)).thenReturn(expectedBo);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        InDigitalServiceVersionRest inDigitalServiceVersionRest = mock(InDigitalServiceVersionRest.class);
        final DigitalServiceVersionBO result = digitalServiceVersionService.createDigitalServiceVersion(WORKSPACE_ID, USER_ID, inDigitalServiceVersionRest);

        assertThat(result).isEqualTo(expectedBo);

        verify(workspaceService, times(1)).getWorkspaceById(WORKSPACE_ID);
        verify(digitalServiceRepository, times(1)).save(any());
        verify(digitalServiceVersionMapper, times(1)).toBusinessObject(digitalServiceVersionToSave, digitalServiceToSave);
        verify(userRepository, times(1)).findById(USER_ID);
    }

    @Test
    void shouldCreateNewDigitalService_withAI() {

        final User user = User.builder().id(USER_ID).build();
        final Workspace linkedWorkspace = Workspace.builder().name(WORKSPACE_NAME).build();
        final DigitalServiceVersionBO expectedBo = DigitalServiceVersionBO.builder().build();
        final String expectedName = "Digital Service 1 AI";
        final List<DigitalService> existingDigitalService = new ArrayList<>();

        final DigitalService digitalServiceToSave = DigitalService.builder().workspace(linkedWorkspace).user(user).name(expectedName).isAi(true).build();
        final DigitalServiceVersion digitalServiceVersionToSave = DigitalServiceVersion.builder().digitalService(digitalServiceToSave).build();
        when(workspaceService.getWorkspaceById(WORKSPACE_ID)).thenReturn(linkedWorkspace);
        when(digitalServiceRepository.save(any())).thenReturn(digitalServiceToSave);
        when(digitalServiceVersionRepository.save(any())).thenReturn(digitalServiceVersionToSave);
        when(digitalServiceVersionMapper.toBusinessObject(digitalServiceVersionToSave, digitalServiceToSave)).thenReturn(expectedBo);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        InDigitalServiceVersionRest inDigitalServiceVersionRest = mock(InDigitalServiceVersionRest.class);
        when(inDigitalServiceVersionRest.getIsAi()).thenReturn(true);
        final DigitalServiceVersionBO result = digitalServiceVersionService.createDigitalServiceVersion(WORKSPACE_ID, USER_ID, inDigitalServiceVersionRest);

        assertThat(result).isEqualTo(expectedBo);

        verify(workspaceService, times(1)).getWorkspaceById(WORKSPACE_ID);
        verify(digitalServiceRepository, times(1)).save(any());
        verify(digitalServiceVersionMapper, times(1)).toBusinessObject(digitalServiceVersionToSave, digitalServiceToSave);
        verify(userRepository, times(1)).findById(USER_ID);
    }

    @Test
    void deleteDigitalService_shouldCallAllRepositoriesWithCorrectUid() {
        // Arrange
        String digitalServiceUid = "ds-123";

        // Act
        digitalServiceVersionService.deleteDigitalServiceVersion(digitalServiceUid); // appelle ta méthode

        // Assert : on vérifie que tous les repositories ont bien été appelés
        verify(inVirtualEquipmentRepository).deleteByDigitalServiceVersionUid(digitalServiceUid);
        verify(inPhysicalEquipmentRepository).deleteByDigitalServiceVersionUid(digitalServiceUid);
        verify(inDatacenterRepository).deleteByDigitalServiceVersionUid(digitalServiceUid);
        verify(inAiParameterRepository).deleteByDigitalServiceVersionUid(digitalServiceUid);
        verify(inAiInfrastructureRepository).deleteByDigitalServiceVersionUid(digitalServiceUid);
        verify(digitalServiceVersionRepository).deleteById(digitalServiceUid);
    }

    @Test
    void shouldUpdateDigitalService() {
        final UserBO userBO = UserBO.builder().id(USER_ID).build();
        final User user = User.builder().id(USER_ID).build();
        final UserWorkspace userWorkspace = UserWorkspace.builder().id(1).roles(
                List.of(Role.builder().name("ROLE_DIGITAL_SERVICE_WRITE").build())).build();

        final DigitalServiceVersionBO inputDigitalServiceVersionBO = DigitalServiceVersionBO.builder().uid(DIGITAL_SERVICE_VERSION_UID).name("name").build();
        final DigitalServiceVersionBO digitalServiceVersionBO = DigitalServiceVersionBO.builder().uid(DIGITAL_SERVICE_VERSION_UID).enableDataInconsistency(false).build();
        final DigitalService digitalService = DigitalService.builder().uid(DIGITAL_SERVICE_UID).enableDataInconsistency(false).build();
        final DigitalServiceVersion digitalServiceVersion = DigitalServiceVersion.builder().uid(DIGITAL_SERVICE_VERSION_UID).digitalService(digitalService).build();
        final DigitalServiceVersion digitalServiceVersionUpdated = DigitalServiceVersion.builder().uid(DIGITAL_SERVICE_VERSION_UID).digitalService(digitalService).description("name").build();
        Organization organizationObj = Organization.builder().id(ORGANIZATION_ID).name(ORGANIZATION).build();

        when(organizationRepository.findByName(ORGANIZATION)).thenReturn(Optional.of(organizationObj));
        when(roleService.hasAdminRightOnOrganizationOrWorkspace(userBO, ORGANIZATION_ID, WORKSPACE_ID))
                .thenReturn(false);
        // No change in dataInconsistency
        when(userWorkspaceRepository.findByWorkspaceIdAndUserId(WORKSPACE_ID, USER_ID))
                .thenReturn(Optional.of(userWorkspace));
        when(digitalServiceVersionRepository.findById(digitalServiceVersion.getUid())).thenReturn(Optional.of(digitalServiceVersion));
        doNothing().when(digitalServiceVersionMapper)
                .mergeEntity(digitalServiceVersion, inputDigitalServiceVersionBO, digitalServiceReferentialService, user);
        when(digitalServiceVersionRepository.save(digitalServiceVersion)).thenReturn(digitalServiceVersionUpdated);
        when(digitalServiceVersionMapper.toFullBusinessObject(digitalServiceVersionUpdated)).thenReturn(inputDigitalServiceVersionBO);

        final DigitalServiceVersionBO result = digitalServiceVersionService
                .updateDigitalServiceVersion(inputDigitalServiceVersionBO, ORGANIZATION, WORKSPACE_ID, userBO);

        assertThat(result).isEqualTo(inputDigitalServiceVersionBO);
        verify(digitalServiceVersionRepository, times(1)).findById(digitalServiceVersionBO.getUid());
        verify(digitalServiceVersionMapper, times(1)).toFullBusinessObject(digitalServiceVersionUpdated);
        verify(digitalServiceVersionMapper, times(1)).mergeEntity(digitalServiceVersion, inputDigitalServiceVersionBO, digitalServiceReferentialService, user);
        verify(digitalServiceVersionRepository, times(1)).save(digitalServiceVersion);
        verify(userWorkspaceRepository, times(1)).findByWorkspaceIdAndUserId(WORKSPACE_ID, USER_ID);
        verify(organizationRepository, times(1)).findByName(ORGANIZATION);
        verify(roleService, times(1)).hasAdminRightOnOrganizationOrWorkspace(userBO, ORGANIZATION_ID, WORKSPACE_ID);
    }


    @Test
    void shouldUpdateDigitalService_withRemovedTerminalAndNetwork() {
        final UserBO userBO = UserBO.builder().id(USER_ID).build();
        final User user = User.builder().id(USER_ID).build();
        final UserWorkspace userWorkspace = UserWorkspace.builder().id(1).roles(
                List.of(Role.builder().name("ROLE_DIGITAL_SERVICE_WRITE").build())).build();

        final DigitalServiceVersionBO inputDigitalServiceBO = DigitalServiceVersionBO.builder().uid(DIGITAL_SERVICE_VERSION_UID).name("name").enableDataInconsistency(false).build();
        final DigitalService digitalService = DigitalService.builder().uid(DIGITAL_SERVICE_VERSION_UID).enableDataInconsistency(false).build();
        final DigitalServiceVersion digitalServiceVersion = DigitalServiceVersion.builder().uid(DIGITAL_SERVICE_VERSION_UID).digitalService(digitalService).build();
        final DigitalServiceVersionBO digitalServiceBO = DigitalServiceVersionBO.builder().uid(DIGITAL_SERVICE_VERSION_UID).build();
        final DigitalServiceVersion digitalServiceUpdated = DigitalServiceVersion.builder().uid(DIGITAL_SERVICE_VERSION_UID).description("name").build();
        Organization organizationObj = Organization.builder().id(ORGANIZATION_ID).name(ORGANIZATION).build();

        when(organizationRepository.findByName(ORGANIZATION)).thenReturn(Optional.of(organizationObj));
        when(roleService.hasAdminRightOnOrganizationOrWorkspace(userBO, ORGANIZATION_ID, WORKSPACE_ID))
                .thenReturn(false);
        when(userWorkspaceRepository.findByWorkspaceIdAndUserId(WORKSPACE_ID, USER_ID))
                .thenReturn(Optional.of(userWorkspace));
        when(digitalServiceVersionRepository.findById(digitalServiceVersion.getUid())).thenReturn(Optional.of(digitalServiceVersion));
        doNothing().when(digitalServiceVersionMapper).mergeEntity(digitalServiceVersion, inputDigitalServiceBO, digitalServiceReferentialService, user);
        when(digitalServiceVersionRepository.save(digitalServiceVersion)).thenReturn(digitalServiceUpdated);
        when(digitalServiceVersionMapper.toFullBusinessObject(digitalServiceUpdated)).thenReturn(inputDigitalServiceBO);

        final DigitalServiceVersionBO result = digitalServiceVersionService.updateDigitalServiceVersion(inputDigitalServiceBO, ORGANIZATION, WORKSPACE_ID, userBO);

        assertThat(result).isEqualTo(inputDigitalServiceBO);
        verify(digitalServiceVersionRepository, times(1)).findById(digitalServiceBO.getUid());
        verify(digitalServiceVersionMapper, times(1)).mergeEntity(digitalServiceVersion, inputDigitalServiceBO, digitalServiceReferentialService, user);
        verify(digitalServiceVersionRepository, times(1)).save(digitalServiceVersion);
        verify(digitalServiceVersionMapper, times(1)).toFullBusinessObject(digitalServiceUpdated);
        verify(userWorkspaceRepository, times(1)).findByWorkspaceIdAndUserId(WORKSPACE_ID, USER_ID);
        verify(organizationRepository, times(1)).findByName(ORGANIZATION);
        verify(roleService, times(1)).hasAdminRightOnOrganizationOrWorkspace(userBO, ORGANIZATION_ID, WORKSPACE_ID);
    }

    @Test
    void whenNoChange_thenDigitalServiceEntityNotChange() {
        final DigitalServiceVersionBO digitalServiceVersionBO = DigitalServiceVersionBO.builder().uid(DIGITAL_SERVICE_VERSION_UID).enableDataInconsistency(false).build();
        final DigitalService digitalService = DigitalService.builder().uid(DIGITAL_SERVICE_UID).enableDataInconsistency(false).build();
        final DigitalServiceVersion digitalServiceVersion = DigitalServiceVersion.builder().digitalService(digitalService).uid(DIGITAL_SERVICE_VERSION_UID).build();

        when(digitalServiceVersionRepository.findById(DIGITAL_SERVICE_VERSION_UID)).thenReturn(Optional.of(digitalServiceVersion));
        when(digitalServiceVersionMapper.toBusinessObject(digitalServiceVersion)).thenReturn(digitalServiceVersionBO);

        final DigitalServiceVersionBO result = digitalServiceVersionService.updateDigitalServiceVersion(digitalServiceVersionBO, ORGANIZATION, WORKSPACE_ID, null);

        assertThat(result).isEqualTo(digitalServiceVersionBO);
        verify(digitalServiceVersionRepository, times(1)).findById(digitalServiceVersionBO.getUid());
        verify(digitalServiceVersionMapper, times(1)).toBusinessObject(digitalServiceVersion);
    }


    @Test
    void whenUpdateNotExistDigitalService_thenThrow() {
        when(digitalServiceVersionRepository.findById(DIGITAL_SERVICE_VERSION_UID)).thenReturn(Optional.empty());

        final DigitalServiceVersionBO bo = DigitalServiceVersionBO.builder().uid(DIGITAL_SERVICE_VERSION_UID).build();
        assertThatThrownBy(() -> digitalServiceVersionService.updateDigitalServiceVersion(bo, ORGANIZATION, WORKSPACE_ID, null))
                .hasMessageContaining("Digital Service " + DIGITAL_SERVICE_VERSION_UID + " not found.")
                .isInstanceOf(G4itRestException.class);

        verify(digitalServiceVersionRepository, times(1)).findById(DIGITAL_SERVICE_VERSION_UID);
        verifyNoInteractions(digitalServiceVersionMapper);
        verifyNoInteractions(userWorkspaceRepository);
    }

    @Test
    void shouldUpdateWhenUser_IsAdmin() {
        final UserBO userBO = UserBO.builder().id(USER_ID).build();
        final DigitalServiceVersionBO inputDigitalServiceVersionBO = DigitalServiceVersionBO.builder().uid(DIGITAL_SERVICE_VERSION_UID).name("name").build();
        final DigitalService digitalService = DigitalService.builder().uid(DIGITAL_SERVICE_UID).name("name").enableDataInconsistency(false).build();
        final DigitalServiceVersion digitalServiceVersion = DigitalServiceVersion.builder().uid(DIGITAL_SERVICE_VERSION_UID).digitalService(digitalService).build();
        final DigitalServiceVersion digitalServiceVersionUpdated = DigitalServiceVersion.builder().uid(DIGITAL_SERVICE_VERSION_UID).description("name").digitalService(digitalService).build();
        final DigitalServiceVersionBO digitalServiceVersionBO = DigitalServiceVersionBO.builder().uid(DIGITAL_SERVICE_VERSION_UID).build();
        Organization organizationObj = Organization.builder().id(ORGANIZATION_ID).name(ORGANIZATION).build();

        when(organizationRepository.findByName(ORGANIZATION)).thenReturn(Optional.of(organizationObj));
        when(roleService.hasAdminRightOnOrganizationOrWorkspace(userBO, ORGANIZATION_ID, WORKSPACE_ID))
                .thenReturn(true);   // user is admin

        when(digitalServiceVersionRepository.findById(digitalServiceVersion.getUid())).thenReturn(Optional.of(digitalServiceVersion));
        when(digitalServiceVersionMapper.toBusinessObject(digitalServiceVersion)).thenReturn(digitalServiceVersionBO);
        doNothing().when(digitalServiceVersionMapper).mergeEntity(eq(digitalServiceVersion), eq(inputDigitalServiceVersionBO), eq(digitalServiceReferentialService), any());
        when(digitalServiceVersionRepository.save(digitalServiceVersion)).thenReturn(digitalServiceVersionUpdated);
        when(digitalServiceVersionMapper.toFullBusinessObject(digitalServiceVersionUpdated)).thenReturn(inputDigitalServiceVersionBO);

        final DigitalServiceVersionBO result = digitalServiceVersionService.updateDigitalServiceVersion(inputDigitalServiceVersionBO, ORGANIZATION, WORKSPACE_ID, userBO);

        assertThat(result).isEqualTo(inputDigitalServiceVersionBO);

        verify(organizationRepository, times(1)).findByName(ORGANIZATION);
        verify(roleService, times(1)).hasAdminRightOnOrganizationOrWorkspace(userBO, ORGANIZATION_ID, WORKSPACE_ID);

        // Key point: should NOT check user org roles since admin
        verify(userWorkspaceRepository, never()).findByWorkspaceIdAndUserId(anyLong(), anyLong());
        verify(digitalServiceVersionRepository, times(1)).findById(digitalServiceVersion.getUid());
        verify(digitalServiceVersionMapper, times(1)).toBusinessObject(digitalServiceVersion);
        verify(digitalServiceVersionMapper, times(1)).mergeEntity(eq(digitalServiceVersion), eq(inputDigitalServiceVersionBO), eq(digitalServiceReferentialService), any());
        verify(digitalServiceVersionRepository, times(1)).save(digitalServiceVersion);
        verify(digitalServiceVersionMapper, times(1)).toFullBusinessObject(digitalServiceVersionUpdated);
    }

    @Test
    void shouldThrowIfNotAuthorized_NoRoleAndNoInconsistencyChange() {
        final UserBO userBO = UserBO.builder().id(USER_ID).build();
        final DigitalServiceVersionBO digitalServiceVersionBO = DigitalServiceVersionBO.builder()
                .uid(DIGITAL_SERVICE_VERSION_UID).note(NoteBO.builder().content("note").build()).enableDataInconsistency(false).build();
        final DigitalService digitalService = DigitalService.builder()
                .uid(DIGITAL_SERVICE_UID).enableDataInconsistency(false).build();
        final DigitalServiceVersion digitalServiceVersion = DigitalServiceVersion.builder()
                .uid(DIGITAL_SERVICE_VERSION_UID).digitalService(digitalService).build();

        UserWorkspace userOrg = new UserWorkspace();
        userOrg.setRoles(List.of(Role.builder().name("READ").build()));
        Organization organizationObj = Organization.builder().id(ORGANIZATION_ID).name(ORGANIZATION).build();

        when(organizationRepository.findByName(ORGANIZATION)).thenReturn(Optional.of(organizationObj));
        when(roleService.hasAdminRightOnOrganizationOrWorkspace(userBO, ORGANIZATION_ID, WORKSPACE_ID))
                .thenReturn(false);

        when(digitalServiceVersionRepository.findById(digitalServiceVersion.getUid())).thenReturn(Optional.of(digitalServiceVersion));
        when(userWorkspaceRepository.findByWorkspaceIdAndUserId(WORKSPACE_ID, USER_ID))
                .thenReturn(Optional.of(userOrg));

        assertThatThrownBy(() -> digitalServiceVersionService.updateDigitalServiceVersion(digitalServiceVersionBO, ORGANIZATION, WORKSPACE_ID, userBO))
                .hasMessageContaining("Not authorized")
                .isInstanceOf(G4itRestException.class);

        verify(userWorkspaceRepository, times(1)).findByWorkspaceIdAndUserId(WORKSPACE_ID, USER_ID);
        verify(organizationRepository, times(1)).findByName(ORGANIZATION);
        verify(roleService, times(1)).hasAdminRightOnOrganizationOrWorkspace(userBO, ORGANIZATION_ID, WORKSPACE_ID);
    }

    @Test
    void shouldAllowDataInconsistencyChangeEvenWithout_WriteRole() {
        final UserBO userBO = UserBO.builder().id(USER_ID).build();
        final User user = User.builder().id(USER_ID).build();

        final DigitalServiceVersionBO digitalServiceVersionBO = DigitalServiceVersionBO.builder()
                .uid(DIGITAL_SERVICE_VERSION_UID).enableDataInconsistency(true).build();
        final DigitalService digitalService = DigitalService.builder()
                .uid(DIGITAL_SERVICE_UID).enableDataInconsistency(false).build();
        final DigitalServiceVersion digitalServiceVersion = DigitalServiceVersion.builder()
                .uid(DIGITAL_SERVICE_VERSION_UID).digitalService(digitalService).build();
        final DigitalService digitalServiceUpdated = DigitalService.builder()
                .uid(DIGITAL_SERVICE_UID).enableDataInconsistency(true).build();
        final DigitalServiceVersion digitalServiceVersionUpdated = DigitalServiceVersion.builder()
                .uid(DIGITAL_SERVICE_VERSION_UID).digitalService(digitalService).build();

        UserWorkspace userOrg = new UserWorkspace();
        userOrg.setRoles(List.of(Role.builder().name("READ").build()));
        Organization organizationObj = Organization.builder().id(ORGANIZATION_ID).name(ORGANIZATION).build();

        when(organizationRepository.findByName(ORGANIZATION)).thenReturn(Optional.of(organizationObj));
        when(roleService.hasAdminRightOnOrganizationOrWorkspace(userBO, ORGANIZATION_ID, WORKSPACE_ID))
                .thenReturn(false);

        when(digitalServiceVersionRepository.findById(digitalServiceVersion.getUid())).thenReturn(Optional.of(digitalServiceVersion));
        when(digitalServiceVersionMapper.toFullBusinessObject(digitalServiceVersion)).thenReturn(
                DigitalServiceVersionBO.builder().uid(DIGITAL_SERVICE_VERSION_UID).enableDataInconsistency(false).build());
        when(userWorkspaceRepository.findByWorkspaceIdAndUserId(WORKSPACE_ID, USER_ID))
                .thenReturn(Optional.of(userOrg));
        // Data inconsistency value is different
        doNothing().when(digitalServiceVersionMapper).mergeEntity(digitalServiceVersion, digitalServiceVersionBO, digitalServiceReferentialService, user);
        when(digitalServiceVersionRepository.save(digitalServiceVersion)).thenReturn(digitalServiceVersionUpdated);
        when(digitalServiceVersionMapper.toFullBusinessObject(digitalServiceVersionUpdated)).thenReturn(digitalServiceVersionBO);

        final DigitalServiceVersionBO result = digitalServiceVersionService.updateDigitalServiceVersion(digitalServiceVersionBO, ORGANIZATION, WORKSPACE_ID, userBO);

        assertThat(result).isEqualTo(digitalServiceVersionBO);
    }

    @Test
    void shouldUpdateWhenUserHas_WriteAccessAnd_NoDataInconsistencyChange() {
        final UserBO userBO = UserBO.builder().id(USER_ID).build();
        final User user = User.builder().id(USER_ID).build();

        final DigitalServiceVersionBO inputDigitalServiceVersionBO = DigitalServiceVersionBO.builder().uid(DIGITAL_SERVICE_VERSION_UID).enableDataInconsistency(false).name("service").build();
        final DigitalService digitalService = DigitalService.builder().uid(DIGITAL_SERVICE_UID).enableDataInconsistency(false).build();
        final DigitalServiceVersion digitalServiceVersion = DigitalServiceVersion.builder().uid(DIGITAL_SERVICE_VERSION_UID).digitalService(digitalService).build();
        final DigitalService digitalServiceUpdated = DigitalService.builder().uid(DIGITAL_SERVICE_UID).enableDataInconsistency(false).name("service").build();
        final DigitalServiceVersion digitalServiceVersionUpdated = DigitalServiceVersion.builder().uid(DIGITAL_SERVICE_VERSION_UID).digitalService(digitalServiceUpdated).description("service").build();
        Organization organizationObj = Organization.builder().id(ORGANIZATION_ID).name(ORGANIZATION).build();
        final UserWorkspace userWorkspace = UserWorkspace.builder().id(1)
                .roles(List.of(Role.builder().name("ROLE_DIGITAL_SERVICE_WRITE").build())).build();

        when(organizationRepository.findByName(ORGANIZATION)).thenReturn(Optional.of(organizationObj));
        when(roleService.hasAdminRightOnOrganizationOrWorkspace(userBO, ORGANIZATION_ID, WORKSPACE_ID)).thenReturn(false);
        when(userWorkspaceRepository.findByWorkspaceIdAndUserId(WORKSPACE_ID, USER_ID)).thenReturn(Optional.of(userWorkspace));
        when(digitalServiceVersionRepository.findById(digitalServiceVersion.getUid())).thenReturn(Optional.of(digitalServiceVersion));
        doNothing().when(digitalServiceVersionMapper).mergeEntity(digitalServiceVersion, inputDigitalServiceVersionBO, digitalServiceReferentialService, user);
        when(digitalServiceVersionRepository.save(digitalServiceVersion)).thenReturn(digitalServiceVersionUpdated);
        when(digitalServiceVersionMapper.toFullBusinessObject(digitalServiceVersionUpdated)).thenReturn(inputDigitalServiceVersionBO);

        final DigitalServiceVersionBO result = digitalServiceVersionService.updateDigitalServiceVersion(inputDigitalServiceVersionBO, ORGANIZATION, WORKSPACE_ID, userBO);

        assertThat(result).isEqualTo(inputDigitalServiceVersionBO);

        verify(organizationRepository, times(1)).findByName(ORGANIZATION);
        verify(roleService, times(1)).hasAdminRightOnOrganizationOrWorkspace(userBO, ORGANIZATION_ID, WORKSPACE_ID);
        verify(userWorkspaceRepository, times(1)).findByWorkspaceIdAndUserId(WORKSPACE_ID, USER_ID);
        verify(digitalServiceVersionRepository, times(1)).findById(digitalServiceVersion.getUid());
        verify(digitalServiceVersionMapper, times(1)).mergeEntity(digitalServiceVersion, inputDigitalServiceVersionBO, digitalServiceReferentialService, user);
        verify(digitalServiceVersionRepository, times(1)).save(digitalServiceVersion);
        verify(digitalServiceVersionMapper, times(1)).toFullBusinessObject(digitalServiceVersionUpdated);
    }

    @Test
    void shouldGetDigitalService() {
        User user = User.builder().id(1L).firstName("first").lastName("last").build();

        final DigitalService digitalService = DigitalService.builder().user(user).build();
        final DigitalServiceVersion digitalServiceVersion = DigitalServiceVersion.builder().digitalService(digitalService).build();
        when(digitalServiceVersionRepository.findById(DIGITAL_SERVICE_VERSION_UID)).thenReturn(Optional.of(digitalServiceVersion));
        final DigitalServiceVersionBO digitalServiceVersionBo = DigitalServiceVersionBO.builder().build();
        when(digitalServiceVersionMapper.toFullBusinessObject(digitalServiceVersion)).thenReturn(digitalServiceVersionBo);

        final DigitalServiceVersionBO result = digitalServiceVersionService.getDigitalServiceVersion(DIGITAL_SERVICE_VERSION_UID);

        assertThat(result).isEqualTo(digitalServiceVersionBo);

        verify(digitalServiceVersionRepository, times(1)).findById(DIGITAL_SERVICE_VERSION_UID);
        verify(digitalServiceVersionMapper, times(1)).toFullBusinessObject(digitalServiceVersion);
    }

    @Test
    void whenGetNotExistDigitalService_thenThrow() {

        when(digitalServiceVersionRepository.findById(DIGITAL_SERVICE_VERSION_UID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> digitalServiceVersionService.getDigitalServiceVersion(DIGITAL_SERVICE_VERSION_UID))
                .hasMessageContaining("Digital Service 90651485-3f8b-49dd-a7be-753e4fe1fd36 not found.")
                .isInstanceOf(G4itRestException.class);

        verify(digitalServiceVersionRepository, times(1)).findById(DIGITAL_SERVICE_VERSION_UID);
        verifyNoInteractions(digitalServiceVersionMapper);
    }

    @Test
    void digitalServiceExists_WhenOrganizationMatchesAndServiceExists_ReturnsTrue() {


        final Workspace workspace = Workspace.builder().name(WORKSPACE_NAME)
                .organization(Organization.builder().name(ORGANIZATION).build()).build();

        final DigitalService digitalService = DigitalService.builder().uid(DIGITAL_SERVICE_UID).build();

        when(workspaceService.getWorkspaceById(WORKSPACE_ID)).thenReturn(workspace);
        when(digitalServiceRepository.findByWorkspaceAndUid(workspace, DIGITAL_SERVICE_UID))
                .thenReturn(Optional.of(digitalService));

        boolean result = digitalServiceVersionService.digitalServiceExists(
                ORGANIZATION, WORKSPACE_ID, DIGITAL_SERVICE_UID);

        assertTrue(result);
        verify(digitalServiceRepository).findByWorkspaceAndUid(workspace, DIGITAL_SERVICE_UID);
    }

    @Test
    void digitalServiceExists_WhenOrganizationMismatch_ReturnsFalse() {

        final Workspace workspace = Workspace.builder().name(WORKSPACE_NAME)
                .organization(Organization.builder().name("Organization2").build()).build();

        when(workspaceService.getWorkspaceById(WORKSPACE_ID)).thenReturn(workspace);

        boolean result = digitalServiceVersionService.digitalServiceExists(
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

        boolean result = digitalServiceVersionService.digitalServiceExists(
                ORGANIZATION, WORKSPACE_ID, DIGITAL_SERVICE_UID);

        assertFalse(result);
        verify(digitalServiceRepository).findByWorkspaceAndUid(workspace, DIGITAL_SERVICE_UID);
    }

    @Test
    void shouldUpdateLastUpdateDate() {

        digitalServiceVersionService.updateLastUpdateDate(DIGITAL_SERVICE_UID);
        verify(digitalServiceVersionRepository, times(1))
                .updateLastUpdateDate(
                        argThat(date -> date.isAfter(LocalDateTime.now().minusSeconds(1)) && date.isBefore(LocalDateTime.now().plusSeconds(1))),
                        eq(DIGITAL_SERVICE_UID)
                );

    }

    @Test
    void shareDigitalService_existingLink_updatesExpiryAndReturnsRest() {
        DigitalServiceVersion digitalServiceVersion = new DigitalServiceVersion();
        digitalServiceVersion.setUid(DIGITAL_SERVICE_VERSION_UID);

        final User user = User.builder().id(USER_ID).build();
        final UserBO userBO = UserBO.builder().id(USER_ID).build();

        List<DigitalServiceSharedLink> digitalServiceSharedLinks = new ArrayList<>();
        DigitalServiceSharedLink existingLink = DigitalServiceSharedLink.builder()
                .uid("linkUid")
                .digitalServiceVersion(digitalServiceVersion)
                .expiryDate(LocalDateTime.now().minusDays(1))
                .isActive(true)
                .build();

        digitalServiceSharedLinks.add(existingLink);
        when(digitalServiceVersionRepository.findById(DIGITAL_SERVICE_VERSION_UID)).thenReturn(Optional.of(digitalServiceVersion));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(digitalServiceLinkRepo.findByDigitalServiceVersion(digitalServiceVersion)).thenReturn(digitalServiceSharedLinks);
        when(digitalServiceLinkRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DigitalServiceShareRest result = digitalServiceVersionService.shareDigitalService(ORGANIZATION, WORKSPACE_ID, DIGITAL_SERVICE_VERSION_UID, userBO, true);

        assertNotNull(result);
        assertTrue(result.getUrl().contains(DIGITAL_SERVICE_VERSION_UID));
        assertTrue(result.getUrl().contains(existingLink.getUid()));
        verify(digitalServiceLinkRepo).save(existingLink);

        assertTrue(result.getExpiryDate().isAfter(LocalDateTime.now().plusDays(59)));
    }

    @Test
    void shareDigitalService_noExistingLink_createsNewLinkAndReturnsRest() {
        DigitalServiceVersion digitalServiceVersion = new DigitalServiceVersion();
        digitalServiceVersion.setUid(DIGITAL_SERVICE_UID);

        final User user = User.builder().id(USER_ID).build();
        final UserBO userBO = UserBO.builder().id(USER_ID).build();

        DigitalServiceSharedLink newLink = DigitalServiceSharedLink.builder()
                .uid("newUid123")
                .expiryDate(LocalDateTime.now().plusDays(30))
                .build();

        when(digitalServiceVersionRepository.findById(DIGITAL_SERVICE_VERSION_UID)).thenReturn(Optional.of(digitalServiceVersion));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(digitalServiceLinkRepo.findByDigitalServiceVersion(digitalServiceVersion)).thenReturn(Collections.emptyList());
        when(digitalServiceLinkRepo.save(any())).thenReturn(newLink);

        DigitalServiceShareRest result = digitalServiceVersionService.shareDigitalService(ORGANIZATION, WORKSPACE_ID, DIGITAL_SERVICE_VERSION_UID, userBO, true);

        assertNotNull(result);
        assertTrue(result.getUrl().contains(DIGITAL_SERVICE_VERSION_UID));
        assertTrue(result.getUrl().contains(newLink.getUid()));
        assertEquals(newLink.getExpiryDate(), result.getExpiryDate());
        verify(digitalServiceLinkRepo).save(any());
    }

    @Test
    void shareDigitalService_digitalServiceNotFound_throwsException() {
        final UserBO userBO = UserBO.builder().id(USER_ID).build();

        when(digitalServiceVersionRepository.findById(DIGITAL_SERVICE_VERSION_UID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> digitalServiceVersionService.shareDigitalService(ORGANIZATION, WORKSPACE_ID, DIGITAL_SERVICE_VERSION_UID, userBO, true))
                .hasMessageContaining("Digital service " + DIGITAL_SERVICE_VERSION_UID +
                        " not found in " + ORGANIZATION + "/" + WORKSPACE_ID)
                .isInstanceOf(G4itRestException.class);

    }

    @Test
    void testGetDigitalServiceVersions_success() {


        DigitalService digitalService = DigitalService.builder()
                .uid(DIGITAL_SERVICE_UID)
                .build();

        DigitalServiceVersion versionFound = DigitalServiceVersion.builder()
                .uid(DIGITAL_SERVICE_VERSION_UID)
                .description("Version A")
                .versionType("draft")
                .digitalService(digitalService)
                .build();

        DigitalServiceVersion v1 = DigitalServiceVersion.builder()
                .uid("v1")
                .description("Version A")
                .versionType("draft")
                .digitalService(digitalService)
                .build();

        DigitalServiceVersion v2 = DigitalServiceVersion.builder()
                .uid("v2")
                .description("Version B")
                .versionType("active")
                .digitalService(digitalService)
                .build();

        when(digitalServiceVersionRepository.findById(DIGITAL_SERVICE_VERSION_UID))
                .thenReturn(Optional.of(versionFound));

        when(digitalServiceVersionRepository.findByDigitalServiceUid(DIGITAL_SERVICE_UID))
                .thenReturn(List.of(v1, v2));


        List<DigitalServiceVersionsListRest> result =
                digitalServiceVersionService.getDigitalServiceVersions(DIGITAL_SERVICE_VERSION_UID);

        assertNotNull(result);
        assertEquals(2, result.size(), "Should return all versions belonging to the digital service");

        // Validate first version
        assertEquals("Version A", result.get(0).getVersionName());
        assertEquals("draft", result.get(0).getVersionType());
        assertEquals("v1", result.get(0).getDigitalServiceVersionUid());
        assertEquals(DIGITAL_SERVICE_UID, result.get(0).getDigitalServiceUid());

        // Validate second version
        assertEquals("Version B", result.get(1).getVersionName());
        assertEquals("active", result.get(1).getVersionType());
        assertEquals("v2", result.get(1).getDigitalServiceVersionUid());
        assertEquals(DIGITAL_SERVICE_UID, result.get(1).getDigitalServiceUid());
    }

    @Test
    void testGetDigitalServiceVersions_versionNotFound_returnsEmptyList() {


        when(digitalServiceVersionRepository.findById(DIGITAL_SERVICE_VERSION_UID))
                .thenReturn(Optional.empty());

        List<DigitalServiceVersionsListRest> result =
                digitalServiceVersionService.getDigitalServiceVersions(DIGITAL_SERVICE_VERSION_UID);

        assertNotNull(result);
        assertTrue(result.isEmpty(), "If version not found, should return empty list");
    void shouldCreateDigitalServiceVersion_first() {

        final Workspace linkedWorkspace = Workspace.builder().id(WORKSPACE_ID).build();
        final User user = User.builder().id(USER_ID).build();

        InDigitalServiceVersionRest inDigitalServiceVersionRest = InDigitalServiceVersionRest.builder()
                .dsName(DIGITAL_SERVICE_NAME)
                .versionName(VERSION_NAME)
                .isAi(IS_AI)
                .build();
        DigitalService digitalServiceSaved = DigitalService.builder()
                .uid(DIGITAL_SERVICE_UID)
                .name(inDigitalServiceVersionRest.getDsName())
                .user(user)
                .workspace(linkedWorkspace)
                .isAi(inDigitalServiceVersionRest.getIsAi())
                .build();

        LocalDateTime now = LocalDateTime.now();

        final DigitalServiceVersion digitalServiceVersion = DigitalServiceVersion.builder()
                .uid(DIGITAL_SERVICE_VERSION_UID)
                .description(inDigitalServiceVersionRest.getVersionName())
                .digitalService(DigitalService.builder().uid(digitalServiceSaved.getUid()).build())
                .versionType(DigitalServiceVersionStatus.DRAFT.name()) // Initial version type
                .createdBy(digitalServiceSaved.getUser().getId())
                .creationDate(now)
                .lastUpdateDate(now)
                .lastCalculationDate(now)
                .build();


        DigitalServiceVersionBO expectedBO = DigitalServiceVersionBO.builder()
                .uid(DIGITAL_SERVICE_VERSION_UID)
                .description(VERSION_NAME)
                .versionType(VERSION_TYPE)
                .build();

        when(workspaceService.getWorkspaceById(WORKSPACE_ID)).thenReturn(linkedWorkspace);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(digitalServiceRepository.save(any())).thenReturn(digitalServiceSaved);
        when(digitalServiceVersionRepository.save(any())).thenReturn(digitalServiceVersion);
        when(digitalServiceVersionMapper.toBusinessObject(digitalServiceVersion, digitalServiceSaved)).thenReturn(expectedBO);

        final DigitalServiceVersionBO result = digitalServiceVersionService.createDigitalServiceVersion(WORKSPACE_ID, USER_ID, inDigitalServiceVersionRest);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedBO);

        verify(workspaceService, times(1)).getWorkspaceById(WORKSPACE_ID);
        verify(userRepository, times(1)).findById(USER_ID);
        verify(digitalServiceRepository, times(1)).save(any());
        verify(digitalServiceVersionRepository, times(1)).save(any());
        verify(digitalServiceVersionMapper, times(1)).toBusinessObject(digitalServiceVersion, digitalServiceSaved);

    }

    @Test
    void shouldDuplicateDigitalServiceVersion() {

        // --- Input ---
        final String originalUid = DIGITAL_SERVICE_VERSION_UID;
        final UUID newUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");
        final String newUid = newUuid.toString();

        // --- Mock original version ---
        DigitalServiceVersion originalVersion = DigitalServiceVersion.builder()
                .uid(originalUid)
                .description(VERSION_NAME)
                .versionType(DigitalServiceVersionStatus.DRAFT.name())
                .createdBy(USER_ID)
                .build();

        // --- Mock duplicated version ---
        DigitalServiceVersion duplicatedVersion = DigitalServiceVersion.builder()
                .uid(newUid)
                .description(VERSION_NAME + " (1)")
                .versionType(DigitalServiceVersionStatus.DRAFT.name())
                .createdBy(USER_ID)
                .build();

        // --- Expected business object ---
        DigitalServiceVersionBO expectedBO = DigitalServiceVersionBO.builder()
                .uid(newUid)
                .description(VERSION_NAME + " (1)")
                .versionType(DigitalServiceVersionStatus.DRAFT.name())
                .build();

        // --- Mock repository behaviors ---
        when(digitalServiceVersionRepository.findById(originalUid)).thenReturn(Optional.of(originalVersion));

        // Mock UUID generation
        try (MockedStatic<UUID> mockedUuid = Mockito.mockStatic(UUID.class)) {

            mockedUuid.when(UUID::randomUUID).thenReturn(newUuid);

            // Stub repository calls for the duplicated version
            when(digitalServiceVersionRepository.findById(newUid)).thenReturn(Optional.of(duplicatedVersion));
            when(digitalServiceVersionMapper.toBusinessObject(duplicatedVersion)).thenReturn(expectedBO);

            // --- Execute method under test ---
            DigitalServiceVersionBO result = digitalServiceVersionService.duplicateDigitalServiceVersion(originalUid);

            // --- Validate ---
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(expectedBO);

            // --- Verify interactions ---
            verify(digitalServiceVersionRepository).findById(originalUid);
            verify(digitalServiceVersionRepository).duplicateVersionRecord(originalUid, newUid);

            verify(inPhysicalEquipmentRepository).copyForVersion(originalUid, newUid);
            verify(inVirtualEquipmentRepository).copyForVersion(originalUid, newUid);
            verify(inDatacenterRepository).copyForVersion(originalUid, newUid);
            verify(inApplicationRepository).copyForVersion(originalUid, newUid);
            verify(inAiInfrastructureRepository).copyForVersion(originalUid, newUid);
            verify(inAiParameterRepository).copyForVersion(originalUid, newUid);

            verify(digitalServiceVersionRepository).findById(newUid);
            verify(digitalServiceVersionMapper).toBusinessObject(duplicatedVersion);
        }
    }


}