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
import com.soprasteria.g4it.backend.server.gen.api.dto.DigitalServiceVersionsListRest;
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
    private static final String DIGITAL_SERVICE_NAME = "My DS";
    private static final String VERSION_NAME = "version 1";
    private static final String VERSION_TYPE = "draft";
    private static final String DIGITAL_SERVICE_VERSION_UID = "308f8869-d802-4669-bff9-df78daafd252";

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

    @Test
    void shouldCreateNewDigitalService_first() {

        final Workspace linkedWorkspace = Workspace.builder().name(WORKSPACE_NAME).build();

        final User user = User.builder().id(USER_ID).build();
        final DigitalServiceBO expectedBo = DigitalServiceBO.builder().build();
        final String expectedName = "Digital Service 1";
        final List<DigitalService> existingDigitalService = new ArrayList<>();

        final DigitalService digitalServiceToSave = DigitalService.builder().workspace(linkedWorkspace).user(user).name(expectedName).build();
        when(digitalServiceRepository.findByWorkspaceAndIsAi(linkedWorkspace, false)).thenReturn(existingDigitalService);
        when(workspaceService.getWorkspaceById(WORKSPACE_ID)).thenReturn(linkedWorkspace);
        when(digitalServiceRepository.save(any())).thenReturn(digitalServiceToSave);
        when(digitalServiceMapper.toBusinessObject(digitalServiceToSave)).thenReturn(expectedBo);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        final DigitalServiceBO result = digitalServiceService.createDigitalService(WORKSPACE_ID, USER_ID, IS_AI);

        assertThat(result).isEqualTo(expectedBo);

        verify(workspaceService, times(1)).getWorkspaceById(WORKSPACE_ID);
        verify(digitalServiceRepository, times(1)).findByWorkspaceAndIsAi(linkedWorkspace, false);
        verify(digitalServiceRepository, times(1)).save(any());
        verify(digitalServiceMapper, times(1)).toBusinessObject(digitalServiceToSave);
        verify(userRepository, times(1)).findById(USER_ID);
    }

    @Test
    void shouldCreateNewDigitalService_withExistingDigitalService() {

        final User user = User.builder().id(USER_ID).build();
        final Workspace linkedWorkspace = Workspace.builder().name(WORKSPACE_NAME).build();

        final DigitalServiceBO expectedBo = DigitalServiceBO.builder().build();
        final String expectedName = "Digital Service 2";
        final List<DigitalService> existingDigitalService = List.of(DigitalService.builder().name("Digital Service 1").build(), DigitalService.builder().name("My Digital Service").build());

        final DigitalService digitalServiceToSave = DigitalService.builder().workspace(linkedWorkspace).user(user).name(expectedName).build();
        when(digitalServiceRepository.findByWorkspaceAndIsAi(linkedWorkspace, false)).thenReturn(existingDigitalService);
        when(workspaceService.getWorkspaceById(WORKSPACE_ID)).thenReturn(linkedWorkspace);
        when(digitalServiceRepository.save(any())).thenReturn(digitalServiceToSave);
        when(digitalServiceMapper.toBusinessObject(digitalServiceToSave)).thenReturn(expectedBo);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        final DigitalServiceBO result = digitalServiceService.createDigitalService(WORKSPACE_ID, USER_ID, IS_AI);

        assertThat(result).isEqualTo(expectedBo);

        verify(workspaceService, times(1)).getWorkspaceById(WORKSPACE_ID);
        verify(digitalServiceRepository, times(1)).findByWorkspaceAndIsAi(linkedWorkspace, false);
        verify(digitalServiceRepository, times(1)).save(any());
        verify(digitalServiceMapper, times(1)).toBusinessObject(digitalServiceToSave);
        verify(userRepository, times(1)).findById(USER_ID);
    }

    @Test
    void shouldCreateNewDigitalService_withAI() {

        final User user = User.builder().id(USER_ID).build();
        final Workspace linkedWorkspace = Workspace.builder().name(WORKSPACE_NAME).build();
        final DigitalServiceBO expectedBo = DigitalServiceBO.builder().build();
        final String expectedName = "Digital Service 1 AI";
        final List<DigitalService> existingDigitalService = new ArrayList<>();

        final DigitalService digitalServiceToSave = DigitalService.builder().workspace(linkedWorkspace).user(user).name(expectedName).isAi(true).build();
        when(digitalServiceRepository.findByWorkspaceAndIsAi(linkedWorkspace, true)).thenReturn(existingDigitalService);
        when(workspaceService.getWorkspaceById(WORKSPACE_ID)).thenReturn(linkedWorkspace);
        when(digitalServiceRepository.save(any())).thenReturn(digitalServiceToSave);
        when(digitalServiceMapper.toBusinessObject(digitalServiceToSave)).thenReturn(expectedBo);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        final DigitalServiceBO result = digitalServiceService.createDigitalService(WORKSPACE_ID, USER_ID, true);

        assertThat(result).isEqualTo(expectedBo);

        verify(workspaceService, times(1)).getWorkspaceById(WORKSPACE_ID);
        verify(digitalServiceRepository, times(1)).findByWorkspaceAndIsAi(linkedWorkspace, true);
        verify(digitalServiceRepository, times(1)).save(any());
        verify(digitalServiceMapper, times(1)).toBusinessObject(digitalServiceToSave);
        verify(userRepository, times(1)).findById(USER_ID);
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

    @Test
    void shouldListDigitalService() {

        final Workspace linkedWorkspace = Workspace.builder().name(WORKSPACE_NAME).build();

        User creator = User.builder().id(1L).firstName("first").lastName("last").build();

        DigitalService digitalService = DigitalService.builder().name("name").isAi(IS_AI).user(creator).build();
        final DigitalServiceBO digitalServiceBo = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).build();

        when(digitalServiceMapper.toBusinessObject(anyList())).thenReturn(List.of(digitalServiceBo));

        when(workspaceService.getWorkspaceById(WORKSPACE_ID)).thenReturn(linkedWorkspace);
        when(digitalServiceRepository.findByWorkspace(linkedWorkspace)).thenReturn(List.of(digitalService));

        List<DigitalServiceBO> result = digitalServiceService.getDigitalServices(WORKSPACE_ID, IS_AI);
        assertThat(result).isEqualTo(List.of(digitalServiceBo));

        verify(digitalServiceRepository, times(1)).findByWorkspace(linkedWorkspace);
        verify(workspaceService, times(1)).getWorkspaceById(WORKSPACE_ID);
        verify(digitalServiceMapper, times(1)).toBusinessObject(anyList());

    }

    @Test
    void shouldUpdateDigitalService() {
        final UserBO userBO = UserBO.builder().id(USER_ID).build();
        final User user = User.builder().id(USER_ID).build();
        final UserWorkspace userWorkspace = UserWorkspace.builder().id(1).roles(
                List.of(Role.builder().name("ROLE_DIGITAL_SERVICE_WRITE").build())).build();

        final DigitalServiceBO inputDigitalServiceBO = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).name("name").build();
        final DigitalServiceBO digitalServiceBO = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).build();
        final DigitalService digitalService = DigitalService.builder().uid(DIGITAL_SERVICE_UID).build();
        final DigitalService digitalServiceUpdated = DigitalService.builder().uid(DIGITAL_SERVICE_UID).name("name").build();
        Organization organizationObj = Organization.builder().id(ORGANIZATION_ID).name(ORGANIZATION).build();

        when(organizationRepository.findByName(ORGANIZATION)).thenReturn(Optional.of(organizationObj));
        when(roleService.hasAdminRightOnOrganizationOrWorkspace(userBO, ORGANIZATION_ID, WORKSPACE_ID))
                .thenReturn(false);
        // No change in dataInconsistency
        when(userWorkspaceRepository.findByWorkspaceIdAndUserId(WORKSPACE_ID, USER_ID))
                .thenReturn(Optional.of(userWorkspace));
        when(digitalServiceRepository.findById(digitalService.getUid())).thenReturn(Optional.of(digitalService));
        when(digitalServiceMapper.toFullBusinessObject(digitalService)).thenReturn(digitalServiceBO);
        doNothing().when(digitalServiceMapper)
                .mergeEntity(digitalService, inputDigitalServiceBO, digitalServiceReferentialService, user);
        when(digitalServiceRepository.save(digitalService)).thenReturn(digitalServiceUpdated);
        when(digitalServiceMapper.toFullBusinessObject(digitalServiceUpdated)).thenReturn(inputDigitalServiceBO);

        final DigitalServiceBO result = digitalServiceService
                .updateDigitalService(inputDigitalServiceBO, ORGANIZATION, WORKSPACE_ID, userBO);

        assertThat(result).isEqualTo(inputDigitalServiceBO);
        verify(digitalServiceRepository, times(1)).findById(digitalServiceBO.getUid());
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalService);
        verify(digitalServiceMapper, times(1)).mergeEntity(digitalService, inputDigitalServiceBO, digitalServiceReferentialService, user);
        verify(digitalServiceRepository, times(1)).save(digitalService);
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalServiceUpdated);
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

        final DigitalServiceBO inputDigitalServiceBO = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).name("name").build();
        final DigitalService digitalService = DigitalService.builder().uid(DIGITAL_SERVICE_UID).build();
        final DigitalServiceBO digitalServiceBO = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).build();
        final DigitalService digitalServiceUpdated = DigitalService.builder().uid(DIGITAL_SERVICE_UID).name("name").build();
        Organization organizationObj = Organization.builder().id(ORGANIZATION_ID).name(ORGANIZATION).build();

        when(organizationRepository.findByName(ORGANIZATION)).thenReturn(Optional.of(organizationObj));
        when(roleService.hasAdminRightOnOrganizationOrWorkspace(userBO, ORGANIZATION_ID, WORKSPACE_ID))
                .thenReturn(false);
        when(userWorkspaceRepository.findByWorkspaceIdAndUserId(WORKSPACE_ID, USER_ID))
                .thenReturn(Optional.of(userWorkspace));
        when(digitalServiceRepository.findById(digitalService.getUid())).thenReturn(Optional.of(digitalService));
        when(digitalServiceMapper.toFullBusinessObject(digitalService)).thenReturn(digitalServiceBO);
        doNothing().when(digitalServiceMapper).mergeEntity(digitalService, inputDigitalServiceBO, digitalServiceReferentialService, user);
        when(digitalServiceRepository.save(digitalService)).thenReturn(digitalServiceUpdated);
        when(digitalServiceMapper.toFullBusinessObject(digitalServiceUpdated)).thenReturn(inputDigitalServiceBO);

        final DigitalServiceBO result = digitalServiceService.updateDigitalService(inputDigitalServiceBO, ORGANIZATION, WORKSPACE_ID, userBO);

        assertThat(result).isEqualTo(inputDigitalServiceBO);
        verify(digitalServiceRepository, times(1)).findById(digitalServiceBO.getUid());
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalService);
        verify(digitalServiceMapper, times(1)).mergeEntity(digitalService, inputDigitalServiceBO, digitalServiceReferentialService, user);
        verify(digitalServiceRepository, times(1)).save(digitalService);
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalServiceUpdated);
        verify(userWorkspaceRepository, times(1)).findByWorkspaceIdAndUserId(WORKSPACE_ID, USER_ID);
        verify(organizationRepository, times(1)).findByName(ORGANIZATION);
        verify(roleService, times(1)).hasAdminRightOnOrganizationOrWorkspace(userBO, ORGANIZATION_ID, WORKSPACE_ID);
    }

    @Test
    void whenNoChange_thenDigitalServiceEntityNotChange() {
        final DigitalServiceBO digitalServiceBO = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).build();
        final DigitalService digitalService = DigitalService.builder().uid(DIGITAL_SERVICE_UID).build();

        when(digitalServiceRepository.findById(digitalService.getUid())).thenReturn(Optional.of(digitalService));
        when(digitalServiceMapper.toFullBusinessObject(digitalService)).thenReturn(digitalServiceBO);

        final DigitalServiceBO result = digitalServiceService.updateDigitalService(digitalServiceBO, ORGANIZATION, WORKSPACE_ID, null);

        assertThat(result).isEqualTo(digitalServiceBO);
        verify(digitalServiceRepository, times(1)).findById(digitalServiceBO.getUid());
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalService);
    }


    @Test
    void whenUpdateNotExistDigitalService_thenThrow() {
        when(digitalServiceRepository.findById(DIGITAL_SERVICE_UID)).thenReturn(Optional.empty());

        final DigitalServiceBO bo = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).build();
        assertThatThrownBy(() -> digitalServiceService.updateDigitalService(bo, ORGANIZATION, WORKSPACE_ID, null))
                .hasMessageContaining("Digital Service " + DIGITAL_SERVICE_UID + " not found.")
                .isInstanceOf(G4itRestException.class);

        verify(digitalServiceRepository, times(1)).findById(DIGITAL_SERVICE_UID);
        verifyNoInteractions(digitalServiceMapper);
        verifyNoInteractions(userWorkspaceRepository);
    }

    @Test
    void shouldUpdateWhenUser_IsAdmin() {
        final UserBO userBO = UserBO.builder().id(USER_ID).build();
        final DigitalServiceBO inputDigitalServiceBO = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).name("name").build();
        final DigitalService digitalService = DigitalService.builder().uid(DIGITAL_SERVICE_UID).build();
        final DigitalService digitalServiceUpdated = DigitalService.builder().uid(DIGITAL_SERVICE_UID).name("name").build();
        final DigitalServiceBO digitalServiceBO = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).build();
        Organization organizationObj = Organization.builder().id(ORGANIZATION_ID).name(ORGANIZATION).build();

        when(organizationRepository.findByName(ORGANIZATION)).thenReturn(Optional.of(organizationObj));
        when(roleService.hasAdminRightOnOrganizationOrWorkspace(userBO, ORGANIZATION_ID, WORKSPACE_ID))
                .thenReturn(true);   // user is admin

        when(digitalServiceRepository.findById(digitalService.getUid())).thenReturn(Optional.of(digitalService));
        when(digitalServiceMapper.toFullBusinessObject(digitalService)).thenReturn(digitalServiceBO);
        doNothing().when(digitalServiceMapper).mergeEntity(eq(digitalService), eq(inputDigitalServiceBO), eq(digitalServiceReferentialService), any());
        when(digitalServiceRepository.save(digitalService)).thenReturn(digitalServiceUpdated);
        when(digitalServiceMapper.toFullBusinessObject(digitalServiceUpdated)).thenReturn(inputDigitalServiceBO);

        final DigitalServiceBO result = digitalServiceService.updateDigitalService(inputDigitalServiceBO, ORGANIZATION, WORKSPACE_ID, userBO);

        assertThat(result).isEqualTo(inputDigitalServiceBO);

        verify(organizationRepository, times(1)).findByName(ORGANIZATION);
        verify(roleService, times(1)).hasAdminRightOnOrganizationOrWorkspace(userBO, ORGANIZATION_ID, WORKSPACE_ID);

        // Key point: should NOT check user org roles since admin
        verify(userWorkspaceRepository, never()).findByWorkspaceIdAndUserId(anyLong(), anyLong());
        verify(digitalServiceRepository, times(1)).findById(digitalService.getUid());
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalService);
        verify(digitalServiceMapper, times(1)).mergeEntity(eq(digitalService), eq(inputDigitalServiceBO), eq(digitalServiceReferentialService), any());
        verify(digitalServiceRepository, times(1)).save(digitalService);
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalServiceUpdated);
    }

    @Test
    void shouldThrowIfNotAuthorized_NoRoleAndNoInconsistencyChange() {
        final UserBO userBO = UserBO.builder().id(USER_ID).build();
        final DigitalServiceBO digitalServiceBO = DigitalServiceBO.builder()
                .uid(DIGITAL_SERVICE_UID).note(NoteBO.builder().content("note").build()).enableDataInconsistency(false).build();
        final DigitalService digitalService = DigitalService.builder()
                .uid(DIGITAL_SERVICE_UID).enableDataInconsistency(false).build();

        UserWorkspace userOrg = new UserWorkspace();
        userOrg.setRoles(List.of(Role.builder().name("READ").build()));
        Organization organizationObj = Organization.builder().id(ORGANIZATION_ID).name(ORGANIZATION).build();

        when(organizationRepository.findByName(ORGANIZATION)).thenReturn(Optional.of(organizationObj));
        when(roleService.hasAdminRightOnOrganizationOrWorkspace(userBO, ORGANIZATION_ID, WORKSPACE_ID))
                .thenReturn(false);

        when(digitalServiceRepository.findById(digitalService.getUid())).thenReturn(Optional.of(digitalService));
        when(digitalServiceMapper.toFullBusinessObject(digitalService)).thenReturn(DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).enableDataInconsistency(false).build());
        when(userWorkspaceRepository.findByWorkspaceIdAndUserId(WORKSPACE_ID, USER_ID))
                .thenReturn(Optional.of(userOrg));

        assertThatThrownBy(() -> digitalServiceService.updateDigitalService(digitalServiceBO, ORGANIZATION, WORKSPACE_ID, userBO))
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

        final DigitalServiceBO digitalServiceBO = DigitalServiceBO.builder()
                .uid(DIGITAL_SERVICE_UID).enableDataInconsistency(true).build();
        final DigitalService digitalService = DigitalService.builder()
                .uid(DIGITAL_SERVICE_UID).enableDataInconsistency(false).build();
        final DigitalService digitalServiceUpdated = DigitalService.builder()
                .uid(DIGITAL_SERVICE_UID).enableDataInconsistency(true).build();

        UserWorkspace userOrg = new UserWorkspace();
        userOrg.setRoles(List.of(Role.builder().name("READ").build()));
        Organization organizationObj = Organization.builder().id(ORGANIZATION_ID).name(ORGANIZATION).build();

        when(organizationRepository.findByName(ORGANIZATION)).thenReturn(Optional.of(organizationObj));
        when(roleService.hasAdminRightOnOrganizationOrWorkspace(userBO, ORGANIZATION_ID, WORKSPACE_ID))
                .thenReturn(false);

        when(digitalServiceRepository.findById(digitalService.getUid())).thenReturn(Optional.of(digitalService));
        when(digitalServiceMapper.toFullBusinessObject(digitalService)).thenReturn(
                DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).enableDataInconsistency(false).build());
        when(userWorkspaceRepository.findByWorkspaceIdAndUserId(WORKSPACE_ID, USER_ID))
                .thenReturn(Optional.of(userOrg));
        // Data inconsistency value is different
        doNothing().when(digitalServiceMapper).mergeEntity(digitalService, digitalServiceBO, digitalServiceReferentialService, user);
        when(digitalServiceRepository.save(digitalService)).thenReturn(digitalServiceUpdated);
        when(digitalServiceMapper.toFullBusinessObject(digitalServiceUpdated)).thenReturn(digitalServiceBO);

        final DigitalServiceBO result = digitalServiceService.updateDigitalService(digitalServiceBO, ORGANIZATION, WORKSPACE_ID, userBO);

        assertThat(result).isEqualTo(digitalServiceBO);
    }

    @Test
    void shouldUpdateWhenUserHas_WriteAccessAnd_NoDataInconsistencyChange() {
        final UserBO userBO = UserBO.builder().id(USER_ID).build();
        final User user = User.builder().id(USER_ID).build();

        final DigitalServiceBO inputDigitalServiceBO = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).enableDataInconsistency(false).name("service").build();
        final DigitalService digitalService = DigitalService.builder().uid(DIGITAL_SERVICE_UID).enableDataInconsistency(false).build();
        final DigitalService digitalServiceUpdated = DigitalService.builder().uid(DIGITAL_SERVICE_UID).enableDataInconsistency(false).name("service").build();
        final DigitalServiceBO digitalServiceBO = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).enableDataInconsistency(false).build();
        Organization organizationObj = Organization.builder().id(ORGANIZATION_ID).name(ORGANIZATION).build();
        final UserWorkspace userWorkspace = UserWorkspace.builder().id(1)
                .roles(List.of(Role.builder().name("ROLE_DIGITAL_SERVICE_WRITE").build())).build();

        when(organizationRepository.findByName(ORGANIZATION)).thenReturn(Optional.of(organizationObj));
        when(roleService.hasAdminRightOnOrganizationOrWorkspace(userBO, ORGANIZATION_ID, WORKSPACE_ID)).thenReturn(false);
        when(userWorkspaceRepository.findByWorkspaceIdAndUserId(WORKSPACE_ID, USER_ID)).thenReturn(Optional.of(userWorkspace));
        when(digitalServiceRepository.findById(digitalService.getUid())).thenReturn(Optional.of(digitalService));
        when(digitalServiceMapper.toFullBusinessObject(digitalService)).thenReturn(digitalServiceBO);
        doNothing().when(digitalServiceMapper).mergeEntity(digitalService, inputDigitalServiceBO, digitalServiceReferentialService, user);
        when(digitalServiceRepository.save(digitalService)).thenReturn(digitalServiceUpdated);
        when(digitalServiceMapper.toFullBusinessObject(digitalServiceUpdated)).thenReturn(inputDigitalServiceBO);

        final DigitalServiceBO result = digitalServiceService.updateDigitalService(inputDigitalServiceBO, ORGANIZATION, WORKSPACE_ID, userBO);

        assertThat(result).isEqualTo(inputDigitalServiceBO);

        verify(organizationRepository, times(1)).findByName(ORGANIZATION);
        verify(roleService, times(1)).hasAdminRightOnOrganizationOrWorkspace(userBO, ORGANIZATION_ID, WORKSPACE_ID);
        verify(userWorkspaceRepository, times(1)).findByWorkspaceIdAndUserId(WORKSPACE_ID, USER_ID);
        verify(digitalServiceRepository, times(1)).findById(digitalService.getUid());
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalService);
        verify(digitalServiceMapper, times(1)).mergeEntity(digitalService, inputDigitalServiceBO, digitalServiceReferentialService, user);
        verify(digitalServiceRepository, times(1)).save(digitalService);
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalServiceUpdated);
    }

    @Test
    void shouldGetDigitalService() {
        User user = User.builder().id(1L).firstName("first").lastName("last").build();

        final DigitalService digitalService = DigitalService.builder().user(user).build();
        when(digitalServiceRepository.findById(DIGITAL_SERVICE_UID)).thenReturn(Optional.of(digitalService));
        final DigitalServiceBO digitalServiceBo = DigitalServiceBO.builder().build();
        when(digitalServiceMapper.toFullBusinessObject(digitalService)).thenReturn(digitalServiceBo);

        final DigitalServiceBO result = digitalServiceService.getDigitalService(DIGITAL_SERVICE_UID);

        assertThat(result).isEqualTo(digitalServiceBo);

        verify(digitalServiceRepository, times(1)).findById(DIGITAL_SERVICE_UID);
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalService);
    }

    @Test
    void whenGetNotExistDigitalService_thenThrow() {

        when(digitalServiceRepository.findById(DIGITAL_SERVICE_UID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> digitalServiceService.getDigitalService(DIGITAL_SERVICE_UID))
                .hasMessageContaining("Digital Service 80651485-3f8b-49dd-a7be-753e4fe1fd36 not found.")
                .isInstanceOf(G4itRestException.class);

        verify(digitalServiceRepository, times(1)).findById(DIGITAL_SERVICE_UID);
        verifyNoInteractions(digitalServiceMapper);
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
    void shouldUpdateLastUpdateDate() {

        digitalServiceService.updateLastUpdateDate(DIGITAL_SERVICE_UID);
        verify(digitalServiceRepository, times(1))
                .updateLastUpdateDate(
                        argThat(date -> date.isAfter(LocalDateTime.now().minusSeconds(1)) && date.isBefore(LocalDateTime.now().plusSeconds(1))),
                        eq(DIGITAL_SERVICE_UID)
                );

    }

    @Test
    void shareDigitalService_existingLink_updatesExpiryAndReturnsRest() {
        DigitalService digitalService = new DigitalService();
        digitalService.setUid(DIGITAL_SERVICE_UID);

        final User user = User.builder().id(USER_ID).build();
        final UserBO userBO = UserBO.builder().id(USER_ID).build();

        List<DigitalServiceSharedLink> digitalServiceSharedLinks = new ArrayList<>();
        DigitalServiceSharedLink existingLink = DigitalServiceSharedLink.builder()
                .uid("linkUid")
                .digitalService(digitalService)
                .expiryDate(LocalDateTime.now().minusDays(1))
                .isActive(true)
                .build();

        digitalServiceSharedLinks.add(existingLink);
        when(digitalServiceRepository.findById(DIGITAL_SERVICE_UID)).thenReturn(Optional.of(digitalService));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(digitalServiceLinkRepo.findByDigitalService(digitalService)).thenReturn(digitalServiceSharedLinks);
        when(digitalServiceLinkRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DigitalServiceShareRest result = digitalServiceService.shareDigitalService(ORGANIZATION, WORKSPACE_ID, DIGITAL_SERVICE_UID, userBO, true);

        assertNotNull(result);
        assertTrue(result.getUrl().contains(DIGITAL_SERVICE_UID));
        assertTrue(result.getUrl().contains(existingLink.getUid()));
        verify(digitalServiceLinkRepo).save(existingLink);

        assertTrue(result.getExpiryDate().isAfter(LocalDateTime.now().plusDays(59)));
    }

    @Test
    void shareDigitalService_noExistingLink_createsNewLinkAndReturnsRest() {
        DigitalService digitalService = new DigitalService();
        digitalService.setUid(DIGITAL_SERVICE_UID);

        final User user = User.builder().id(USER_ID).build();
        final UserBO userBO = UserBO.builder().id(USER_ID).build();

        DigitalServiceSharedLink newLink = DigitalServiceSharedLink.builder()
                .uid("newUid123")
                .expiryDate(LocalDateTime.now().plusDays(30))
                .build();

        when(digitalServiceRepository.findById(DIGITAL_SERVICE_UID)).thenReturn(Optional.of(digitalService));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(digitalServiceLinkRepo.findByDigitalService(digitalService)).thenReturn(Collections.emptyList());
        when(digitalServiceLinkRepo.save(any())).thenReturn(newLink);

        DigitalServiceShareRest result = digitalServiceService.shareDigitalService(ORGANIZATION, WORKSPACE_ID, DIGITAL_SERVICE_UID, userBO, true);

        assertNotNull(result);
        assertTrue(result.getUrl().contains(DIGITAL_SERVICE_UID));
        assertTrue(result.getUrl().contains(newLink.getUid()));
        assertEquals(newLink.getExpiryDate(), result.getExpiryDate());
        verify(digitalServiceLinkRepo).save(any());
    }

    @Test
    void shareDigitalService_digitalServiceNotFound_throwsException() {
        final UserBO userBO = UserBO.builder().id(USER_ID).build();

        when(digitalServiceRepository.findById(DIGITAL_SERVICE_UID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> digitalServiceService.shareDigitalService(ORGANIZATION, WORKSPACE_ID, DIGITAL_SERVICE_UID, userBO, true))
                .hasMessageContaining("Digital service " + DIGITAL_SERVICE_UID +
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
                digitalServiceService.getDigitalServiceVersions(DIGITAL_SERVICE_VERSION_UID, false);

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
                digitalServiceService.getDigitalServiceVersions(DIGITAL_SERVICE_VERSION_UID, false);

        assertNotNull(result);
        assertTrue(result.isEmpty(), "If version not found, should return empty list");
    }


}