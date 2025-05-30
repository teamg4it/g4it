/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apidigitalservice.business;

import com.soprasteria.g4it.backend.apidigitalservice.mapper.DigitalServiceMapper;
import com.soprasteria.g4it.backend.apidigitalservice.model.DigitalServiceBO;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apiindicator.business.IndicatorService;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.Subscriber;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.repository.SubscriberRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserRepository;
import com.soprasteria.g4it.backend.common.criteria.CriteriaService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileMapperInfo;
import com.soprasteria.g4it.backend.common.task.business.TaskService;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DigitalServiceServiceTest {

    final static Long ORGANIZATION_ID = 1L;
    final static String DIGITAL_SERVICE_UID = "80651485-3f8b-49dd-a7be-753e4fe1fd36";
    final static String SUBSCRIBER = "subscriber";
    final static long User_ID = 1;

    final static List<String> criteriaList = List.of("ionising-radiation", "climate-change");

    @Mock
    private DigitalServiceRepository digitalServiceRepository;
    @Mock
    private OrganizationService organizationService;
    @Mock
    private CriteriaService criteriaService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private TaskService taskService;
    @Mock
    private SubscriberRepository subscriberRepository;
    @Mock
    private DigitalServiceMapper digitalServiceMapper;
    @Mock
    private DigitalServiceReferentialService digitalServiceReferentialService;
    @Mock
    private IndicatorService indicatorService;
    @Mock
    private FileMapperInfo fileInfo;
    @Mock
    private InVirtualEquipmentRepository inVirtualEquipmentRepository;
    @InjectMocks
    private DigitalServiceService digitalServiceService;

    @Test
    void shouldCreateNewDigitalService_first() {

        final String organizationName = "test";
        final Organization linkedOrganization = Organization.builder().name(organizationName).build();
        final User user = User.builder().id(User_ID).build();
        final DigitalServiceBO expectedBo = DigitalServiceBO.builder().build();
        final String expectedName = "Digital Service 1";
        final List<DigitalService> existingDigitalService = new ArrayList<>();

        final DigitalService digitalServiceToSave = DigitalService.builder().organization(linkedOrganization).user(user).name(expectedName).build();
        when(digitalServiceRepository.findByOrganization(linkedOrganization)).thenReturn(existingDigitalService);
        when(organizationService.getOrganizationById(ORGANIZATION_ID)).thenReturn(linkedOrganization);
        when(digitalServiceRepository.save(any())).thenReturn(digitalServiceToSave);
        when(digitalServiceMapper.toBusinessObject(digitalServiceToSave)).thenReturn(expectedBo);
        when(userRepository.findById(User_ID)).thenReturn(Optional.of(user));

        final DigitalServiceBO result = digitalServiceService.createDigitalService(ORGANIZATION_ID, User_ID);

        assertThat(result).isEqualTo(expectedBo);

        verify(organizationService, times(1)).getOrganizationById(ORGANIZATION_ID);
        verify(digitalServiceRepository, times(1)).findByOrganization(linkedOrganization);
        verify(digitalServiceRepository, times(1)).save(any());
        verify(digitalServiceMapper, times(1)).toBusinessObject(digitalServiceToSave);
        verify(userRepository, times(1)).findById(User_ID);
    }

    @Test
    void shouldCreateNewDigitalService_withExistingDigitalService() {
        final String organizationName = "test";
        final User user = User.builder().id(User_ID).build();
        final Organization linkedOrganization = Organization.builder().name(organizationName).build();
        final DigitalServiceBO expectedBo = DigitalServiceBO.builder().build();
        final String expectedName = "Digital Service 2";
        final List<DigitalService> existingDigitalService = List.of(DigitalService.builder().name("Digital Service 1").build(), DigitalService.builder().name("My Digital Service").build());

        final DigitalService digitalServiceToSave = DigitalService.builder().organization(linkedOrganization).user(user).name(expectedName).build();
        when(digitalServiceRepository.findByOrganization(linkedOrganization)).thenReturn(existingDigitalService);
        when(organizationService.getOrganizationById(ORGANIZATION_ID)).thenReturn(linkedOrganization);
        when(digitalServiceRepository.save(any())).thenReturn(digitalServiceToSave);
        when(digitalServiceMapper.toBusinessObject(digitalServiceToSave)).thenReturn(expectedBo);
        when(userRepository.findById(User_ID)).thenReturn(Optional.of(user));

        final DigitalServiceBO result = digitalServiceService.createDigitalService(ORGANIZATION_ID, User_ID);

        assertThat(result).isEqualTo(expectedBo);

        verify(organizationService, times(1)).getOrganizationById(ORGANIZATION_ID);
        verify(digitalServiceRepository, times(1)).findByOrganization(linkedOrganization);
        verify(digitalServiceRepository, times(1)).save(any());
        verify(digitalServiceMapper, times(1)).toBusinessObject(digitalServiceToSave);
        verify(userRepository, times(1)).findById(User_ID);
    }

    @Test
    void shouldListDigitalService() {
        final String organizationName = "test";
        final Organization linkedOrganization = Organization.builder().name(organizationName).build();
        User creator = User.builder().id(1L).firstName("first").lastName("last").build();

        DigitalService digitalService = DigitalService.builder().name("name").user(creator).build();
        final DigitalServiceBO digitalServiceBo = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).build();

        when(digitalServiceMapper.toBusinessObject(anyList())).thenReturn(List.of(digitalServiceBo));

        when(organizationService.getOrganizationById(ORGANIZATION_ID)).thenReturn(linkedOrganization);
        when(digitalServiceRepository.findByOrganization(linkedOrganization)).thenReturn(List.of(digitalService));

        List<DigitalServiceBO> result = digitalServiceService.getDigitalServices(ORGANIZATION_ID);
        assertThat(result).isEqualTo(List.of(digitalServiceBo));

        verify(digitalServiceRepository, times(1)).findByOrganization(linkedOrganization);
        verify(organizationService, times(1)).getOrganizationById(ORGANIZATION_ID);
        verify(digitalServiceMapper, times(1)).toBusinessObject(anyList());

    }

    @Test
    void shouldUpdateDigitalService() {
        final UserBO userBO = UserBO.builder().id(1).build();
        final User user = User.builder().id(1).build();

        final DigitalServiceBO inputDigitalServiceBO = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).name("name").build();
        final DigitalServiceBO digitalServiceBO = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).build();
        final DigitalService digitalService = DigitalService.builder().uid(DIGITAL_SERVICE_UID).build();
        final DigitalService digitalServiceUpdated = DigitalService.builder().uid(DIGITAL_SERVICE_UID).name("name").build();

        when(digitalServiceRepository.findById(digitalService.getUid())).thenReturn(Optional.of(digitalService));
        when(digitalServiceMapper.toFullBusinessObject(digitalService)).thenReturn(digitalServiceBO);
        doNothing().when(digitalServiceMapper).mergeEntity(digitalService, inputDigitalServiceBO, digitalServiceReferentialService, user);
        when(digitalServiceRepository.save(digitalService)).thenReturn(digitalServiceUpdated);
        when(digitalServiceMapper.toFullBusinessObject(digitalServiceUpdated)).thenReturn(inputDigitalServiceBO);

        final DigitalServiceBO result = digitalServiceService.updateDigitalService(inputDigitalServiceBO, userBO);

        assertThat(result).isEqualTo(inputDigitalServiceBO);
        verify(digitalServiceRepository, times(1)).findById(digitalServiceBO.getUid());
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalService);
        verify(digitalServiceMapper, times(1)).mergeEntity(digitalService, inputDigitalServiceBO, digitalServiceReferentialService, user);
        verify(digitalServiceRepository, times(1)).save(digitalService);
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalServiceUpdated);
    }

    @Test
    void shouldUpdateDigitalService_withRemovedTerminalAndNetwork() {
        final UserBO userBO = UserBO.builder().id(1).build();
        final User user = User.builder().id(1).build();

        final DigitalServiceBO inputDigitalServiceBO = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).name("name").build();
        final DigitalService digitalService = DigitalService.builder().uid(DIGITAL_SERVICE_UID).build();
        final DigitalServiceBO digitalServiceBO = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).build();
        final DigitalService digitalServiceUpdated = DigitalService.builder().uid(DIGITAL_SERVICE_UID).name("name").build();

        when(digitalServiceRepository.findById(digitalService.getUid())).thenReturn(Optional.of(digitalService));
        when(digitalServiceMapper.toFullBusinessObject(digitalService)).thenReturn(digitalServiceBO);
        doNothing().when(digitalServiceMapper).mergeEntity(digitalService, inputDigitalServiceBO, digitalServiceReferentialService, user);
        when(digitalServiceRepository.save(digitalService)).thenReturn(digitalServiceUpdated);
        when(digitalServiceMapper.toFullBusinessObject(digitalServiceUpdated)).thenReturn(inputDigitalServiceBO);

        final DigitalServiceBO result = digitalServiceService.updateDigitalService(inputDigitalServiceBO, userBO);

        assertThat(result).isEqualTo(inputDigitalServiceBO);
        verify(digitalServiceRepository, times(1)).findById(digitalServiceBO.getUid());
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalService);
        verify(digitalServiceMapper, times(1)).mergeEntity(digitalService, inputDigitalServiceBO, digitalServiceReferentialService, user);
        verify(digitalServiceRepository, times(1)).save(digitalService);
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalServiceUpdated);
    }

    @Test
    void whenNoChange_thenDigitalServiceEntityNotChange() {

        final DigitalServiceBO digitalServiceBO = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).build();
        final DigitalService digitalService = DigitalService.builder().uid(DIGITAL_SERVICE_UID).build();

        when(digitalServiceRepository.findById(digitalService.getUid())).thenReturn(Optional.of(digitalService));
        when(digitalServiceMapper.toFullBusinessObject(digitalService)).thenReturn(digitalServiceBO);

        final DigitalServiceBO result = digitalServiceService.updateDigitalService(digitalServiceBO, null);

        assertThat(result).isEqualTo(digitalServiceBO);
        verify(digitalServiceRepository, times(1)).findById(digitalServiceBO.getUid());
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalService);
    }

    @Test
    void whenUpdateNotExistDigitalService_thenThrow() {

        when(digitalServiceRepository.findById(DIGITAL_SERVICE_UID)).thenReturn(Optional.empty());

        final DigitalServiceBO bo = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).build();
        assertThatThrownBy(() -> digitalServiceService.updateDigitalService(bo, null))
                .hasMessageContaining("Digital Service 80651485-3f8b-49dd-a7be-753e4fe1fd36 not found.")
                .isInstanceOf(G4itRestException.class);

        verify(digitalServiceRepository, times(1)).findById(DIGITAL_SERVICE_UID);
        verifyNoInteractions(digitalServiceMapper);
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

        final Organization organization = Organization.builder().name("test")
                .subscriber(Subscriber.builder().name(SUBSCRIBER).build()).build();
        final DigitalService digitalService = DigitalService.builder().uid(DIGITAL_SERVICE_UID).build();

        when(organizationService.getOrganizationById(ORGANIZATION_ID)).thenReturn(organization);
        when(digitalServiceRepository.findByOrganizationAndUid(organization, DIGITAL_SERVICE_UID))
                .thenReturn(Optional.of(digitalService));

        boolean result = digitalServiceService.digitalServiceExists(
                SUBSCRIBER, ORGANIZATION_ID, DIGITAL_SERVICE_UID);

        assertTrue(result);
        verify(digitalServiceRepository).findByOrganizationAndUid(organization, DIGITAL_SERVICE_UID);
    }

    @Test
    void digitalServiceExists_WhenSubscriberMismatch_ReturnsFalse() {

        final Organization organization = Organization.builder().name("test")
                .subscriber(Subscriber.builder().name("Subscriber2").build()).build();

        when(organizationService.getOrganizationById(ORGANIZATION_ID)).thenReturn(organization);

        boolean result = digitalServiceService.digitalServiceExists(
                SUBSCRIBER, ORGANIZATION_ID, DIGITAL_SERVICE_UID);

        assertFalse(result);
        verify(digitalServiceRepository, never()).findByOrganizationAndUid(any(), any());
    }
    @Test
    void digitalServiceExists_WhenSubscriberMatchesAndServiceNotExist_ReturnsFalse() {

        final Organization organization = Organization.builder().name("test")
                .subscriber(Subscriber.builder().name(SUBSCRIBER).build()).build();
        when(organizationService.getOrganizationById(ORGANIZATION_ID)).thenReturn(organization);
        when(digitalServiceRepository.findByOrganizationAndUid(organization, DIGITAL_SERVICE_UID))
                .thenReturn(Optional.empty());

        boolean result = digitalServiceService.digitalServiceExists(
                SUBSCRIBER, ORGANIZATION_ID, DIGITAL_SERVICE_UID);

        assertFalse(result);
        verify(digitalServiceRepository).findByOrganizationAndUid(organization, DIGITAL_SERVICE_UID);
    }

}
