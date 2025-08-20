/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.business;

import com.soprasteria.g4it.backend.TestUtils;
import com.soprasteria.g4it.backend.apiuser.mapper.OrganizationMapperImpl;
import com.soprasteria.g4it.backend.apiuser.model.OrganizationBO;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.Role;
import com.soprasteria.g4it.backend.apiuser.modeldb.Subscriber;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserOrganizationRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserRoleOrganizationRepository;
import com.soprasteria.g4it.backend.common.filesystem.business.FileStorage;
import com.soprasteria.g4it.backend.common.filesystem.business.FileSystem;
import com.soprasteria.g4it.backend.common.filesystem.business.LocalFileSystem;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.OrganizationStatus;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.WorkspaceUpdateRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @InjectMocks
    private OrganizationService organizationService;

    public static final List<String> ORGANIZATION_ACTIVE_STATUS = List.of(
            OrganizationStatus.ACTIVE.name(),
            OrganizationStatus.TO_BE_DELETED.name()
    );
    private static final String LOCAL_FILESYSTEM_PATH = "target/local-filestorage-test/";
    public static final Long SUBSCRIBER_ID = 1L;
    public static final Long ORGANIZATION_ID = 1L;
    @Mock
    private final FileSystem fileSystem = new LocalFileSystem(LOCAL_FILESYSTEM_PATH);
    @Mock
    private final FileStorage storage = fileSystem.mount("local", "G4IT");
    @Mock
    OrganizationRepository organizationRepository;
    @Mock
    UserOrganizationRepository userOrganizationRepository;
    @Mock
    UserRoleOrganizationRepository userRoleOrganizationRepository;
    @Mock
    RoleService roleService;
    @Mock
    SubscriberService subscriberService;
    @Mock
    CacheManager cacheManager;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(organizationService, "organizationMapper", new OrganizationMapperImpl());
        Mockito.lenient().when(cacheManager.getCache(any())).thenReturn(Mockito.mock(Cache.class));
    }


    @Test
    void updateOrganization_setStatustoToBeDeleted() {
        Long subscriberId = 1L;
        long organizationId = 1L;
        long dataRetentionDay = 7L;
        LocalDateTime now = LocalDateTime.now();
        String organizationName = "ORGANIZATION";
        String currentStatus = OrganizationStatus.ACTIVE.name();
        String updatedStatus = OrganizationStatus.TO_BE_DELETED.name();
        List<Role> subscriberAdminRole = List.of(Role.builder().name(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR).build());

        User user = TestUtils.createUserWithRoleOnSub(subscriberId, subscriberAdminRole);
        Optional<Organization> organizationEntity = Optional.of(Organization.builder().id(organizationId).name(organizationName).status(currentStatus)
                .deletionDate(now.plusDays(dataRetentionDay))
                .subscriber(Subscriber.builder().id(SUBSCRIBER_ID).build())
                .build());
        WorkspaceUpdateRest organizationUpsertRest = TestUtils.createOrganizationUpsert(SUBSCRIBER_ID, organizationName
                , updatedStatus, dataRetentionDay);

        when(organizationRepository.findByIdAndSubscriberIdAndStatusIn(ORGANIZATION_ID, SUBSCRIBER_ID, Constants.ORGANIZATION_ACTIVE_OR_DELETED_STATUS)).thenReturn(organizationEntity);

        OrganizationBO orgBO = organizationService.updateOrganization(ORGANIZATION_ID, organizationUpsertRest, user.getId());

        verify(organizationRepository, times(1)).findByIdAndSubscriberIdAndStatusIn(ORGANIZATION_ID, SUBSCRIBER_ID, ORGANIZATION_ACTIVE_STATUS);
        verify(organizationRepository, times(1)).save(any());
        assertEquals(updatedStatus, orgBO.getStatus());
    }

    @Test
    void updateOrganization_setStatustoToActive() {
        Long subscriberId = 1L;
        long organizationId = 1L;
        long dataRetentionDay = 7L;
        String organizationName = "ORGANIZATION";
        String currentStatus = OrganizationStatus.TO_BE_DELETED.name();
        String updatedStatus = OrganizationStatus.ACTIVE.name();
        List<Role> subscriberAdminRole = List.of(Role.builder().name(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR).build());

        User user = TestUtils.createUserWithRoleOnSub(subscriberId, subscriberAdminRole);
        Optional<Organization> organizationEntity = Optional.of(Organization.builder().name(organizationName).id(organizationId).status(currentStatus)
                .deletionDate(null)
                .subscriber(Subscriber.builder().id(SUBSCRIBER_ID).build())
                .build());
        WorkspaceUpdateRest organizationUpsertRest = TestUtils.createOrganizationUpsert(SUBSCRIBER_ID, organizationName
                , updatedStatus, dataRetentionDay);

        when(organizationRepository.findByIdAndSubscriberIdAndStatusIn(ORGANIZATION_ID, SUBSCRIBER_ID, Constants.ORGANIZATION_ACTIVE_OR_DELETED_STATUS)).thenReturn(organizationEntity);

        OrganizationBO orgBO = organizationService.updateOrganization(ORGANIZATION_ID, organizationUpsertRest, user.getId());

        verify(organizationRepository, times(1)).findByIdAndSubscriberIdAndStatusIn(ORGANIZATION_ID, SUBSCRIBER_ID, ORGANIZATION_ACTIVE_STATUS);
        verify(organizationRepository, times(1)).save(any());
        assertEquals(updatedStatus, orgBO.getStatus());
        assertNull(orgBO.getDeletionDate());
    }

    @Test
    void updateOrganization_updateOrgNameWithAlreadyExist() {
        Long subscriberId = 1L;
        long organizationId = 1L;
        long dataRetentionDay = 7L;
        String organizationName = "ORGANIZATION";
        String organizationUpdatedName = "ORGANIZATION_UPDATED";
        List<Role> subscriberAdminRole = List.of(Role.builder().name(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR).build());
        User user = TestUtils.createUserWithRoleOnSub(subscriberId, subscriberAdminRole);

        WorkspaceUpdateRest organizationUpsertRest = TestUtils.createOrganizationUpsert(SUBSCRIBER_ID, organizationUpdatedName
                , OrganizationStatus.ACTIVE.name(), dataRetentionDay);

        Optional<Organization> organizationEntity = Optional.of(Organization.builder().name(organizationName).id(organizationId).status(OrganizationStatus.ACTIVE.name())
                .deletionDate(null)
                .subscriber(Subscriber.builder().id(SUBSCRIBER_ID).build())
                .build());

        Optional<Organization> organizationEntityWithSameName = Optional.of(Organization.builder().name(organizationUpdatedName).id(2L).status(OrganizationStatus.ACTIVE.name())
                .deletionDate(null)
                .subscriber(Subscriber.builder().id(SUBSCRIBER_ID).build())
                .build());

        when(organizationRepository.findByIdAndSubscriberIdAndStatusIn(ORGANIZATION_ID, SUBSCRIBER_ID, ORGANIZATION_ACTIVE_STATUS)).thenReturn(organizationEntity);
        when(organizationRepository.findBySubscriberIdAndName(SUBSCRIBER_ID, organizationUpdatedName)).thenReturn(organizationEntityWithSameName);

        assertThatThrownBy(() -> organizationService.updateOrganization(organizationId, organizationUpsertRest, user.getId()))
                .isInstanceOf(G4itRestException.class)
                .hasMessageContaining("organization 'ORGANIZATION_UPDATED' already exists in subscriber '1'");


    }

    @Test
    void updateOrganization_updateCriteria() {
        Long subscriberId = 1L;
        long organizationId = 1L;
        String organizationName = "ORGANIZATION";
        List<Role> subscriberAdminRole = List.of(Role.builder().name(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR).build());
        User user = TestUtils.createUserWithRoleOnSub(subscriberId, subscriberAdminRole);

        WorkspaceUpdateRest organizationUpsertRest = TestUtils.createOrganizationUpsert(SUBSCRIBER_ID, organizationName
                , OrganizationStatus.ACTIVE.name(), "criteriaDs", "criteriaIs");

        Optional<Organization> organizationEntity = Optional.of(Organization.builder().name(organizationName).id(organizationId).status(OrganizationStatus.ACTIVE.name())
                .subscriber(Subscriber.builder().id(SUBSCRIBER_ID).build())
                .build());

        when(organizationRepository.findByIdAndSubscriberIdAndStatusIn(ORGANIZATION_ID, SUBSCRIBER_ID, Constants.ORGANIZATION_ACTIVE_OR_DELETED_STATUS)).thenReturn(organizationEntity);

        OrganizationBO orgBO = organizationService.updateOrganization(ORGANIZATION_ID, organizationUpsertRest, user.getId());

        verify(organizationRepository, times(1)).findByIdAndSubscriberIdAndStatusIn(ORGANIZATION_ID, SUBSCRIBER_ID, ORGANIZATION_ACTIVE_STATUS);
        verify(organizationRepository, times(1)).save(any());
        assertEquals(List.of("criteriaDs"), orgBO.getCriteriaDs());
        assertEquals(List.of("criteriaIs"), orgBO.getCriteriaIs());

    }

    @Test
    void createOrganization() {
        String organizationName = "ORGANIZATION";
        UserBO user = TestUtils.createUserBOAdminSub();
        Subscriber subscriber = Subscriber.builder().name("SUBSCRIBER").id(1L).build();

        WorkspaceUpdateRest organizationUpsertRest = TestUtils.createOrganizationUpsert(SUBSCRIBER_ID, organizationName
                , null, 0L);


        OrganizationBO orgBO = organizationService.createOrganization(organizationUpsertRest, user, subscriber.getId());

        verify(organizationRepository, times(1)).findBySubscriberIdAndName(SUBSCRIBER_ID, organizationName);

        assertEquals(organizationUpsertRest.getName(), orgBO.getName());
        assertEquals(OrganizationStatus.ACTIVE.name(), orgBO.getStatus());
    }

    @Test
    void createOrganization_WithAlreadyExistName() {
        Subscriber subscriber = Subscriber.builder().name("SUBSCRIBER").id(1L).build();
        long organizationId = 1L;
        String organizationName = "ORGANIZATION";
        String status = OrganizationStatus.ACTIVE.name();
        UserBO user = TestUtils.createUserBOAdminSub();

        WorkspaceUpdateRest organizationUpsertRest = TestUtils.createOrganizationUpsert(SUBSCRIBER_ID, organizationName
                , null, 0L);
        Optional<Organization> organizationEntity = Optional.of(Organization.builder().name(organizationName).id(organizationId).status(status)
                .deletionDate(null)
                .subscriber(Subscriber.builder().id(SUBSCRIBER_ID).build())
                .build());

        when(organizationRepository.findBySubscriberIdAndName(SUBSCRIBER_ID, organizationName)).thenReturn(organizationEntity);
        assertThatThrownBy(() -> organizationService.createOrganization(organizationUpsertRest, user, subscriber.getId()))
                .isInstanceOf(G4itRestException.class)
                .hasMessageContaining("organization 'ORGANIZATION' already exists in subscriber '1'");

    }

}
