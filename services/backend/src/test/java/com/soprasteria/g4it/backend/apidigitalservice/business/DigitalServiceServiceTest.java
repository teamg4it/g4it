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
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InDatacenterRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiparameterai.repository.InAiParameterRepository;
import com.soprasteria.g4it.backend.apiuser.business.RoleService;
import com.soprasteria.g4it.backend.apiuser.business.WorkspaceService;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.*;
import com.soprasteria.g4it.backend.apiuser.repository.SubscriberRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserOrganizationRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserRepository;
import com.soprasteria.g4it.backend.common.model.NoteBO;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DigitalServiceServiceTest {

    private static final Long ORGANIZATION_ID = 1L;
    private static final Long SUBSCRIBER_ID = 1L;
    private static final String DIGITAL_SERVICE_UID = "80651485-3f8b-49dd-a7be-753e4fe1fd36";
    private static final String SUBSCRIBER = "subscriber";
    private static final String ORG_NAME = "organization";
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
    private SubscriberRepository subscriberRepository;
    @Mock
    private UserOrganizationRepository userOrganizationRepository;
    @Mock
    private DigitalServiceMapper digitalServiceMapper;
    @Mock
    private DigitalServiceReferentialService digitalServiceReferentialService;
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

        final String organizationName = "test";
        final Workspace linkedWorkspace = Workspace.builder().name(organizationName).build();
        final User user = User.builder().id(USER_ID).build();
        final DigitalServiceBO expectedBo = DigitalServiceBO.builder().build();
        final String expectedName = "Digital Service 1";
        final List<DigitalService> existingDigitalService = new ArrayList<>();

        final DigitalService digitalServiceToSave = DigitalService.builder().workspace(linkedWorkspace).user(user).name(expectedName).build();
        when(digitalServiceRepository.findByWorkspaceAndIsAi(linkedWorkspace, false)).thenReturn(existingDigitalService);
        when(workspaceService.getOrganizationById(ORGANIZATION_ID)).thenReturn(linkedWorkspace);
        when(digitalServiceRepository.save(any())).thenReturn(digitalServiceToSave);
        when(digitalServiceMapper.toBusinessObject(digitalServiceToSave)).thenReturn(expectedBo);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        final DigitalServiceBO result = digitalServiceService.createDigitalService(ORGANIZATION_ID, USER_ID, IS_AI);

        assertThat(result).isEqualTo(expectedBo);

        verify(workspaceService, times(1)).getOrganizationById(ORGANIZATION_ID);
        verify(digitalServiceRepository, times(1)).findByWorkspaceAndIsAi(linkedWorkspace, false);
        verify(digitalServiceRepository, times(1)).save(any());
        verify(digitalServiceMapper, times(1)).toBusinessObject(digitalServiceToSave);
        verify(userRepository, times(1)).findById(USER_ID);
    }

    @Test
    void shouldCreateNewDigitalService_withExistingDigitalService() {

        final User user = User.builder().id(USER_ID).build();
        final Workspace linkedWorkspace = Workspace.builder().name(organizationName).build();
        final DigitalServiceBO expectedBo = DigitalServiceBO.builder().build();
        final String expectedName = "Digital Service 2";
        final List<DigitalService> existingDigitalService = List.of(DigitalService.builder().name("Digital Service 1").build(), DigitalService.builder().name("My Digital Service").build());

        final DigitalService digitalServiceToSave = DigitalService.builder().workspace(linkedWorkspace).user(user).name(expectedName).build();
        when(digitalServiceRepository.findByWorkspaceAndIsAi(linkedWorkspace, false)).thenReturn(existingDigitalService);
        when(workspaceService.getOrganizationById(ORGANIZATION_ID)).thenReturn(linkedWorkspace);
        when(digitalServiceRepository.save(any())).thenReturn(digitalServiceToSave);
        when(digitalServiceMapper.toBusinessObject(digitalServiceToSave)).thenReturn(expectedBo);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        final DigitalServiceBO result = digitalServiceService.createDigitalService(ORGANIZATION_ID, USER_ID, IS_AI);

        assertThat(result).isEqualTo(expectedBo);

        verify(workspaceService, times(1)).getOrganizationById(ORGANIZATION_ID);
        verify(digitalServiceRepository, times(1)).findByWorkspaceAndIsAi(linkedWorkspace, false);
        verify(digitalServiceRepository, times(1)).save(any());
        verify(digitalServiceMapper, times(1)).toBusinessObject(digitalServiceToSave);
        verify(userRepository, times(1)).findById(USER_ID);
    }

    @Test
    void shouldCreateNewDigitalService_withAI() {

        final User user = User.builder().id(USER_ID).build();
        final Organization linkedOrganization = Organization.builder().name(ORG_NAME).build();
        final DigitalServiceBO expectedBo = DigitalServiceBO.builder().build();
        final String expectedName = "Digital Service 1 AI";
        final List<DigitalService> existingDigitalService = new ArrayList<>();

        final DigitalService digitalServiceToSave = DigitalService.builder().organization(linkedOrganization).user(user).name(expectedName).isAi(true).build();
        when(digitalServiceRepository.findByOrganizationAndIsAi(linkedOrganization, true)).thenReturn(existingDigitalService);
        when(organizationService.getOrganizationById(ORGANIZATION_ID)).thenReturn(linkedOrganization);
        when(digitalServiceRepository.save(any())).thenReturn(digitalServiceToSave);
        when(digitalServiceMapper.toBusinessObject(digitalServiceToSave)).thenReturn(expectedBo);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        final DigitalServiceBO result = digitalServiceService.createDigitalService(ORGANIZATION_ID, USER_ID, true);

        assertThat(result).isEqualTo(expectedBo);

        verify(organizationService, times(1)).getOrganizationById(ORGANIZATION_ID);
        verify(digitalServiceRepository, times(1)).findByOrganizationAndIsAi(linkedOrganization, true);
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
        final String organizationName = "test";
        final Workspace linkedWorkspace = Workspace.builder().name(organizationName).build();
        User creator = User.builder().id(1L).firstName("first").lastName("last").build();

        DigitalService digitalService = DigitalService.builder().name("name").isAi(IS_AI).user(creator).build();
        final DigitalServiceBO digitalServiceBo = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).build();

        when(digitalServiceMapper.toBusinessObject(anyList())).thenReturn(List.of(digitalServiceBo));

        when(workspaceService.getOrganizationById(ORGANIZATION_ID)).thenReturn(linkedWorkspace);
        when(digitalServiceRepository.findByWorkspace(linkedWorkspace)).thenReturn(List.of(digitalService));

        List<DigitalServiceBO> result = digitalServiceService.getDigitalServices(ORGANIZATION_ID, IS_AI);
        assertThat(result).isEqualTo(List.of(digitalServiceBo));

        verify(digitalServiceRepository, times(1)).findByWorkspace(linkedWorkspace);
        verify(workspaceService, times(1)).getOrganizationById(ORGANIZATION_ID);
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
        Subscriber subscriberObj = Subscriber.builder().id(SUBSCRIBER_ID).name(SUBSCRIBER).build();

        when(subscriberRepository.findByName(SUBSCRIBER)).thenReturn(Optional.of(subscriberObj));
        when(roleService.hasAdminRightOnSubscriberOrOrganization(userBO, SUBSCRIBER_ID, ORGANIZATION_ID))
                .thenReturn(false);
        // No change in dataInconsistency
        when(userOrganizationRepository.findByWorkspaceIdAndUserId(ORGANIZATION_ID, USER_ID))
                .thenReturn(Optional.of(userWorkspace));
        when(digitalServiceRepository.findById(digitalService.getUid())).thenReturn(Optional.of(digitalService));
        when(digitalServiceMapper.toFullBusinessObject(digitalService)).thenReturn(digitalServiceBO);
        doNothing().when(digitalServiceMapper)
                .mergeEntity(digitalService, inputDigitalServiceBO, digitalServiceReferentialService, user);
        when(digitalServiceRepository.save(digitalService)).thenReturn(digitalServiceUpdated);
        when(digitalServiceMapper.toFullBusinessObject(digitalServiceUpdated)).thenReturn(inputDigitalServiceBO);

        final DigitalServiceBO result = digitalServiceService
                .updateDigitalService(inputDigitalServiceBO, SUBSCRIBER, ORGANIZATION_ID, userBO);

        assertThat(result).isEqualTo(inputDigitalServiceBO);
        verify(digitalServiceRepository, times(1)).findById(digitalServiceBO.getUid());
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalService);
        verify(digitalServiceMapper, times(1)).mergeEntity(digitalService, inputDigitalServiceBO, digitalServiceReferentialService, user);
        verify(digitalServiceRepository, times(1)).save(digitalService);
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalServiceUpdated);
        verify(userOrganizationRepository, times(1)).findByWorkspaceIdAndUserId(ORGANIZATION_ID, USER_ID);
        verify(subscriberRepository, times(1)).findByName(SUBSCRIBER);
        verify(roleService, times(1)).hasAdminRightOnSubscriberOrOrganization(userBO, SUBSCRIBER_ID, ORGANIZATION_ID);
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
        Subscriber subscriberObj = Subscriber.builder().id(SUBSCRIBER_ID).name(SUBSCRIBER).build();

        when(subscriberRepository.findByName(SUBSCRIBER)).thenReturn(Optional.of(subscriberObj));
        when(roleService.hasAdminRightOnSubscriberOrOrganization(userBO, SUBSCRIBER_ID, ORGANIZATION_ID))
                .thenReturn(false);
        when(userOrganizationRepository.findByWorkspaceIdAndUserId(ORGANIZATION_ID, USER_ID))
                .thenReturn(Optional.of(userWorkspace));
        when(digitalServiceRepository.findById(digitalService.getUid())).thenReturn(Optional.of(digitalService));
        when(digitalServiceMapper.toFullBusinessObject(digitalService)).thenReturn(digitalServiceBO);
        doNothing().when(digitalServiceMapper).mergeEntity(digitalService, inputDigitalServiceBO, digitalServiceReferentialService, user);
        when(digitalServiceRepository.save(digitalService)).thenReturn(digitalServiceUpdated);
        when(digitalServiceMapper.toFullBusinessObject(digitalServiceUpdated)).thenReturn(inputDigitalServiceBO);

        final DigitalServiceBO result = digitalServiceService.updateDigitalService(inputDigitalServiceBO, SUBSCRIBER, ORGANIZATION_ID, userBO);

        assertThat(result).isEqualTo(inputDigitalServiceBO);
        verify(digitalServiceRepository, times(1)).findById(digitalServiceBO.getUid());
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalService);
        verify(digitalServiceMapper, times(1)).mergeEntity(digitalService, inputDigitalServiceBO, digitalServiceReferentialService, user);
        verify(digitalServiceRepository, times(1)).save(digitalService);
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalServiceUpdated);
        verify(userOrganizationRepository, times(1)).findByWorkspaceIdAndUserId(ORGANIZATION_ID, USER_ID);
        verify(subscriberRepository, times(1)).findByName(SUBSCRIBER);
        verify(roleService, times(1)).hasAdminRightOnSubscriberOrOrganization(userBO, SUBSCRIBER_ID, ORGANIZATION_ID);
    }

    @Test
    void whenNoChange_thenDigitalServiceEntityNotChange() {
        final DigitalServiceBO digitalServiceBO = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).build();
        final DigitalService digitalService = DigitalService.builder().uid(DIGITAL_SERVICE_UID).build();

        when(digitalServiceRepository.findById(digitalService.getUid())).thenReturn(Optional.of(digitalService));
        when(digitalServiceMapper.toFullBusinessObject(digitalService)).thenReturn(digitalServiceBO);

        final DigitalServiceBO result = digitalServiceService.updateDigitalService(digitalServiceBO, SUBSCRIBER, ORGANIZATION_ID, null);

        assertThat(result).isEqualTo(digitalServiceBO);
        verify(digitalServiceRepository, times(1)).findById(digitalServiceBO.getUid());
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalService);
    }


    @Test
    void whenUpdateNotExistDigitalService_thenThrow() {
        when(digitalServiceRepository.findById(DIGITAL_SERVICE_UID)).thenReturn(Optional.empty());

        final DigitalServiceBO bo = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).build();
        assertThatThrownBy(() -> digitalServiceService.updateDigitalService(bo, SUBSCRIBER, ORGANIZATION_ID, null))
                .hasMessageContaining("Digital Service " + DIGITAL_SERVICE_UID + " not found.")
                .isInstanceOf(G4itRestException.class);

        verify(digitalServiceRepository, times(1)).findById(DIGITAL_SERVICE_UID);
        verifyNoInteractions(digitalServiceMapper);
        verifyNoInteractions(userOrganizationRepository);
    }

    @Test
    void shouldUpdateWhenUser_IsAdmin() {
        final UserBO userBO = UserBO.builder().id(USER_ID).build();
        final DigitalServiceBO inputDigitalServiceBO = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).name("name").build();
        final DigitalService digitalService = DigitalService.builder().uid(DIGITAL_SERVICE_UID).build();
        final DigitalService digitalServiceUpdated = DigitalService.builder().uid(DIGITAL_SERVICE_UID).name("name").build();
        final DigitalServiceBO digitalServiceBO = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).build();
        Subscriber subscriberObj = Subscriber.builder().id(SUBSCRIBER_ID).name(SUBSCRIBER).build();

        when(subscriberRepository.findByName(SUBSCRIBER)).thenReturn(Optional.of(subscriberObj));
        when(roleService.hasAdminRightOnSubscriberOrOrganization(userBO, SUBSCRIBER_ID, ORGANIZATION_ID))
                .thenReturn(true);   // user is admin

        when(digitalServiceRepository.findById(digitalService.getUid())).thenReturn(Optional.of(digitalService));
        when(digitalServiceMapper.toFullBusinessObject(digitalService)).thenReturn(digitalServiceBO);
        doNothing().when(digitalServiceMapper).mergeEntity(eq(digitalService), eq(inputDigitalServiceBO), eq(digitalServiceReferentialService), any());
        when(digitalServiceRepository.save(digitalService)).thenReturn(digitalServiceUpdated);
        when(digitalServiceMapper.toFullBusinessObject(digitalServiceUpdated)).thenReturn(inputDigitalServiceBO);

        final DigitalServiceBO result = digitalServiceService.updateDigitalService(inputDigitalServiceBO, SUBSCRIBER, ORGANIZATION_ID, userBO);

        assertThat(result).isEqualTo(inputDigitalServiceBO);

        verify(subscriberRepository, times(1)).findByName(SUBSCRIBER);
        verify(roleService, times(1)).hasAdminRightOnSubscriberOrOrganization(userBO, SUBSCRIBER_ID, ORGANIZATION_ID);

        // Key point: should NOT check user org roles since admin
        verify(userOrganizationRepository, never()).findByWorkspaceIdAndUserId(anyLong(), anyLong());
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
        Subscriber subscriberObj = Subscriber.builder().id(SUBSCRIBER_ID).name(SUBSCRIBER).build();

        when(subscriberRepository.findByName(SUBSCRIBER)).thenReturn(Optional.of(subscriberObj));
        when(roleService.hasAdminRightOnSubscriberOrOrganization(userBO, SUBSCRIBER_ID, ORGANIZATION_ID))
                .thenReturn(false);

        when(digitalServiceRepository.findById(digitalService.getUid())).thenReturn(Optional.of(digitalService));
        when(digitalServiceMapper.toFullBusinessObject(digitalService)).thenReturn(DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).enableDataInconsistency(false).build());
        when(userOrganizationRepository.findByWorkspaceIdAndUserId(ORGANIZATION_ID, USER_ID))
                .thenReturn(Optional.of(userOrg));

        assertThatThrownBy(() -> digitalServiceService.updateDigitalService(digitalServiceBO, SUBSCRIBER, ORGANIZATION_ID, userBO))
                .hasMessageContaining("Not authorized")
                .isInstanceOf(G4itRestException.class);

        verify(userOrganizationRepository, times(1)).findByWorkspaceIdAndUserId(ORGANIZATION_ID, USER_ID);
        verify(subscriberRepository, times(1)).findByName(SUBSCRIBER);
        verify(roleService, times(1)).hasAdminRightOnSubscriberOrOrganization(userBO, SUBSCRIBER_ID, ORGANIZATION_ID);
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
        Subscriber subscriberObj = Subscriber.builder().id(SUBSCRIBER_ID).name(SUBSCRIBER).build();

        when(subscriberRepository.findByName(SUBSCRIBER)).thenReturn(Optional.of(subscriberObj));
        when(roleService.hasAdminRightOnSubscriberOrOrganization(userBO, SUBSCRIBER_ID, ORGANIZATION_ID))
                .thenReturn(false);

        when(digitalServiceRepository.findById(digitalService.getUid())).thenReturn(Optional.of(digitalService));
        when(digitalServiceMapper.toFullBusinessObject(digitalService)).thenReturn(
                DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).enableDataInconsistency(false).build());
        when(userOrganizationRepository.findByWorkspaceIdAndUserId(ORGANIZATION_ID, USER_ID))
                .thenReturn(Optional.of(userOrg));
        // Data inconsistency value is different
        doNothing().when(digitalServiceMapper).mergeEntity(digitalService, digitalServiceBO, digitalServiceReferentialService, user);
        when(digitalServiceRepository.save(digitalService)).thenReturn(digitalServiceUpdated);
        when(digitalServiceMapper.toFullBusinessObject(digitalServiceUpdated)).thenReturn(digitalServiceBO);

        final DigitalServiceBO result = digitalServiceService.updateDigitalService(digitalServiceBO, SUBSCRIBER, ORGANIZATION_ID, userBO);

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
        Subscriber subscriberObj = Subscriber.builder().id(SUBSCRIBER_ID).name(SUBSCRIBER).build();
        final UserWorkspace userWorkspace = UserWorkspace.builder().id(1)
                .roles(List.of(Role.builder().name("ROLE_DIGITAL_SERVICE_WRITE").build())).build();

        when(subscriberRepository.findByName(SUBSCRIBER)).thenReturn(Optional.of(subscriberObj));
        when(roleService.hasAdminRightOnSubscriberOrOrganization(userBO, SUBSCRIBER_ID, ORGANIZATION_ID)).thenReturn(false);
        when(userOrganizationRepository.findByWorkspaceIdAndUserId(ORGANIZATION_ID, USER_ID)).thenReturn(Optional.of(userWorkspace));
        when(digitalServiceRepository.findById(digitalService.getUid())).thenReturn(Optional.of(digitalService));
        when(digitalServiceMapper.toFullBusinessObject(digitalService)).thenReturn(digitalServiceBO);
        doNothing().when(digitalServiceMapper).mergeEntity(digitalService, inputDigitalServiceBO, digitalServiceReferentialService, user);
        when(digitalServiceRepository.save(digitalService)).thenReturn(digitalServiceUpdated);
        when(digitalServiceMapper.toFullBusinessObject(digitalServiceUpdated)).thenReturn(inputDigitalServiceBO);

        final DigitalServiceBO result = digitalServiceService.updateDigitalService(inputDigitalServiceBO, SUBSCRIBER, ORGANIZATION_ID, userBO);

        assertThat(result).isEqualTo(inputDigitalServiceBO);

        verify(subscriberRepository, times(1)).findByName(SUBSCRIBER);
        verify(roleService, times(1)).hasAdminRightOnSubscriberOrOrganization(userBO, SUBSCRIBER_ID, ORGANIZATION_ID);
        verify(userOrganizationRepository, times(1)).findByWorkspaceIdAndUserId(ORGANIZATION_ID, USER_ID);
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
    void digitalServiceExists_WhenSubscriberMatchesAndServiceExists_ReturnsTrue() {

        final Workspace workspace = Workspace.builder().name("test")
                .subscriber(Subscriber.builder().name(SUBSCRIBER).build()).build();
        final DigitalService digitalService = DigitalService.builder().uid(DIGITAL_SERVICE_UID).build();

        when(workspaceService.getOrganizationById(ORGANIZATION_ID)).thenReturn(workspace);
        when(digitalServiceRepository.findByWorkspaceAndUid(workspace, DIGITAL_SERVICE_UID))
                .thenReturn(Optional.of(digitalService));

        boolean result = digitalServiceService.digitalServiceExists(
                SUBSCRIBER, ORGANIZATION_ID, DIGITAL_SERVICE_UID);

        assertTrue(result);
        verify(digitalServiceRepository).findByWorkspaceAndUid(workspace, DIGITAL_SERVICE_UID);
    }

    @Test
    void digitalServiceExists_WhenSubscriberMismatch_ReturnsFalse() {

        final Workspace workspace = Workspace.builder().name("test")
                .subscriber(Subscriber.builder().name("Subscriber2").build()).build();

        when(workspaceService.getOrganizationById(ORGANIZATION_ID)).thenReturn(workspace);

        boolean result = digitalServiceService.digitalServiceExists(
                SUBSCRIBER, ORGANIZATION_ID, DIGITAL_SERVICE_UID);

        assertFalse(result);
        verify(digitalServiceRepository, never()).findByWorkspaceAndUid(any(), any());
    }

    @Test
    void digitalServiceExists_WhenSubscriberMatchesAndServiceNotExist_ReturnsFalse() {

        final Workspace workspace = Workspace.builder().name("test")
                .subscriber(Subscriber.builder().name(SUBSCRIBER).build()).build();
        when(workspaceService.getOrganizationById(ORGANIZATION_ID)).thenReturn(workspace);
        when(digitalServiceRepository.findByWorkspaceAndUid(workspace, DIGITAL_SERVICE_UID))
                .thenReturn(Optional.empty());

        boolean result = digitalServiceService.digitalServiceExists(
                SUBSCRIBER, ORGANIZATION_ID, DIGITAL_SERVICE_UID);

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

}