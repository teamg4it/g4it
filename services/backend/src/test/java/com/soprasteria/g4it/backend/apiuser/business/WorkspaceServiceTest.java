/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.business;

import com.soprasteria.g4it.backend.TestUtils;
import com.soprasteria.g4it.backend.apiuser.mapper.WorkspaceMapperImpl;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.model.WorkspaceBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.Role;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.apiuser.repository.UserRoleWorkspaceRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserWorkspaceRepository;
import com.soprasteria.g4it.backend.apiuser.repository.WorkspaceRepository;
import com.soprasteria.g4it.backend.common.filesystem.business.FileStorage;
import com.soprasteria.g4it.backend.common.filesystem.business.FileSystem;
import com.soprasteria.g4it.backend.common.filesystem.business.LocalFileSystem;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.WorkspaceStatus;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.WorkspaceUpsertRest;
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
class WorkspaceServiceTest {

    @InjectMocks
    private WorkspaceService workspaceService;

    public static final List<String> ORGANIZATION_ACTIVE_STATUS = List.of(
            WorkspaceStatus.ACTIVE.name(),
            WorkspaceStatus.TO_BE_DELETED.name()
    );
    private static final String LOCAL_FILESYSTEM_PATH = "target/local-filestorage-test/";
    public static final Long ORGANIZATION_ID = 1L;
    public static final Long WORKSPACE_ID = 1L;
    @Mock
    private final FileSystem fileSystem = new LocalFileSystem(LOCAL_FILESYSTEM_PATH);
    @Mock
    private final FileStorage storage = fileSystem.mount("local", "G4IT");
    @Mock
    WorkspaceRepository workspaceRepository;
    @Mock
    UserWorkspaceRepository userWorkspaceRepository;
    @Mock
    UserRoleWorkspaceRepository userRoleWorkspaceRepository;
    @Mock
    RoleService roleService;
    @Mock
    OrganizationService organizationService;
    @Mock
    CacheManager cacheManager;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(workspaceService, "workspaceMapper", new WorkspaceMapperImpl());
        Mockito.lenient().when(cacheManager.getCache(any())).thenReturn(Mockito.mock(Cache.class));
    }


    @Test
    void updateWorkspace_setStatustoToBeDeleted() {
        Long organizationId = 1L;
        long workspaceId = 1L;
        long dataRetentionDay = 7L;
        LocalDateTime now = LocalDateTime.now();
        String workspaceName = "WORKSPACE";
        String currentStatus = WorkspaceStatus.ACTIVE.name();
        String updatedStatus = WorkspaceStatus.TO_BE_DELETED.name();
        List<Role> organizationAdminRole = List.of(Role.builder().name(Constants.ROLE_ORGANIZATION_ADMINISTRATOR).build());

        User user = TestUtils.createUserWithRoleOnSub(organizationId, organizationAdminRole);
        Optional<Workspace> workspaceEntity = Optional.of(Workspace.builder().id(workspaceId).name(workspaceName).status(currentStatus)
                .deletionDate(now.plusDays(dataRetentionDay))
                .organization(Organization.builder().id(ORGANIZATION_ID).build())
                .build());
        WorkspaceUpsertRest organizationUpsertRest = TestUtils.createOrganizationUpsert(ORGANIZATION_ID, workspaceName
                , updatedStatus, dataRetentionDay);

        when(workspaceRepository.findByIdAndOrganizationIdAndStatusIn(WORKSPACE_ID, ORGANIZATION_ID, Constants.WORKSPACE_ACTIVE_OR_DELETED_STATUS)).thenReturn(workspaceEntity);

        WorkspaceBO orgBO = workspaceService.updateWorkspace(WORKSPACE_ID, organizationUpsertRest, user.getId());

        verify(workspaceRepository, times(1)).findByIdAndOrganizationIdAndStatusIn(WORKSPACE_ID, ORGANIZATION_ID, ORGANIZATION_ACTIVE_STATUS);
        verify(workspaceRepository, times(1)).save(any());
        assertEquals(updatedStatus, orgBO.getStatus());
    }

    @Test
    void updateWorkspace_setStatustoToActive() {
        Long organizationId = 1L;
        long workspaceId = 1L;
        long dataRetentionDay = 7L;
        String workspaceName = "WORKSPACE";
        String currentStatus = WorkspaceStatus.TO_BE_DELETED.name();
        String updatedStatus = WorkspaceStatus.ACTIVE.name();
        List<Role> organizationAdminRole = List.of(Role.builder().name(Constants.ROLE_ORGANIZATION_ADMINISTRATOR).build());

        User user = TestUtils.createUserWithRoleOnSub(organizationId, organizationAdminRole);
        Optional<Workspace> workspaceEntity = Optional.of(Workspace.builder().name(workspaceName).id(workspaceId).status(currentStatus)
                .deletionDate(null)
                .organization(Organization.builder().id(ORGANIZATION_ID).build())
                .build());
        WorkspaceUpsertRest organizationUpsertRest = TestUtils.createOrganizationUpsert(ORGANIZATION_ID, workspaceName
                , updatedStatus, dataRetentionDay);

        when(workspaceRepository.findByIdAndOrganizationIdAndStatusIn(WORKSPACE_ID, ORGANIZATION_ID, Constants.WORKSPACE_ACTIVE_OR_DELETED_STATUS)).thenReturn(workspaceEntity);

        WorkspaceBO orgBO = workspaceService.updateWorkspace(WORKSPACE_ID, organizationUpsertRest, user.getId());

        verify(workspaceRepository, times(1)).findByIdAndOrganizationIdAndStatusIn(WORKSPACE_ID, ORGANIZATION_ID, ORGANIZATION_ACTIVE_STATUS);
        verify(workspaceRepository, times(1)).save(any());
        assertEquals(updatedStatus, orgBO.getStatus());
        assertNull(orgBO.getDeletionDate());
    }

    @Test
    void updateOrganization_updateOrgNameWithAlreadyExist() {
        Long organizationId = 1L;
        long workspaceId = 1L;
        long dataRetentionDay = 7L;
        String workspaceName = "WORKSPACE";
        String workspaceUpdatedName = "WORKSPACE_UPDATED";
        List<Role> organizationAdminRole = List.of(Role.builder().name(Constants.ROLE_ORGANIZATION_ADMINISTRATOR).build());
        User user = TestUtils.createUserWithRoleOnSub(organizationId, organizationAdminRole);

        WorkspaceUpsertRest organizationUpsertRest = TestUtils.createOrganizationUpsert(ORGANIZATION_ID, workspaceUpdatedName
                , WorkspaceStatus.ACTIVE.name(), dataRetentionDay);

        Optional<Workspace> workspaceEntity = Optional.of(Workspace.builder().name(workspaceName).id(workspaceId).status(WorkspaceStatus.ACTIVE.name())
                .deletionDate(null)
                .organization(Organization.builder().id(ORGANIZATION_ID).build())
                .build());

        Optional<Workspace> organizationEntityWithSameName = Optional.of(Workspace.builder().name(workspaceUpdatedName).id(2L).status(WorkspaceStatus.ACTIVE.name())
                .deletionDate(null)
                .organization(Organization.builder().id(ORGANIZATION_ID).build())
                .build());

        when(workspaceRepository.findByIdAndOrganizationIdAndStatusIn(WORKSPACE_ID, ORGANIZATION_ID, ORGANIZATION_ACTIVE_STATUS)).thenReturn(workspaceEntity);
        when(workspaceRepository.findByOrganizationIdAndName(ORGANIZATION_ID, workspaceUpdatedName)).thenReturn(organizationEntityWithSameName);

        assertThatThrownBy(() -> workspaceService.updateWorkspace(workspaceId, organizationUpsertRest, user.getId()))
                .isInstanceOf(G4itRestException.class)
                .hasMessageContaining("workspace 'WORKSPACE_UPDATED' already exists in organization '1'");


    }

    @Test
    void updateOrganization_updateCriteria() {
        Long organizationId = 1L;
        long workspaceId = 1L;
        String workspaceName = "WORKSPACE";
        List<Role> organizationAdminRole = List.of(Role.builder().name(Constants.ROLE_ORGANIZATION_ADMINISTRATOR).build());
        User user = TestUtils.createUserWithRoleOnSub(organizationId, organizationAdminRole);

        WorkspaceUpsertRest organizationUpsertRest = TestUtils.createOrganizationUpsert(ORGANIZATION_ID, workspaceName
                , WorkspaceStatus.ACTIVE.name(), "criteriaDs", "criteriaIs");

        Optional<Workspace> workspaceEntity = Optional.of(Workspace.builder().name(workspaceName).id(workspaceId).status(WorkspaceStatus.ACTIVE.name())
                .organization(Organization.builder().id(ORGANIZATION_ID).build())
                .build());

        when(workspaceRepository.findByIdAndOrganizationIdAndStatusIn(WORKSPACE_ID, ORGANIZATION_ID, Constants.WORKSPACE_ACTIVE_OR_DELETED_STATUS)).thenReturn(workspaceEntity);

        WorkspaceBO orgBO = workspaceService.updateWorkspace(WORKSPACE_ID, organizationUpsertRest, user.getId());

        verify(workspaceRepository, times(1)).findByIdAndOrganizationIdAndStatusIn(WORKSPACE_ID, ORGANIZATION_ID, ORGANIZATION_ACTIVE_STATUS);
        verify(workspaceRepository, times(1)).save(any());
        assertEquals(List.of("criteriaDs"), orgBO.getCriteriaDs());
        assertEquals(List.of("criteriaIs"), orgBO.getCriteriaIs());

    }

    @Test
    void createWorkspace() {
        String workspaceName = "WORKSPACE";
        UserBO user = TestUtils.createUserBOAdminSub();
        Organization organization = Organization.builder().name("ORGANIZATION").id(1L).build();

        WorkspaceUpsertRest organizationUpsertRest = TestUtils.createOrganizationUpsert(ORGANIZATION_ID, workspaceName
                , null, 0L);


        WorkspaceBO orgBO = workspaceService.createWorkspace(organizationUpsertRest, user, organization.getId());

        verify(workspaceRepository, times(1)).findByOrganizationIdAndName(ORGANIZATION_ID, workspaceName);

        assertEquals(organizationUpsertRest.getName(), orgBO.getName());
        assertEquals(WorkspaceStatus.ACTIVE.name(), orgBO.getStatus());
    }

    @Test
    void createWorkspace_WithAlreadyExistName() {
        Organization organization = Organization.builder().name("ORGANIZATION").id(1L).build();
        long workspaceId = 1L;
        String workspaceName = "WORKSPACE";
        String status = WorkspaceStatus.ACTIVE.name();
        UserBO user = TestUtils.createUserBOAdminSub();

        WorkspaceUpsertRest organizationUpsertRest = TestUtils.createOrganizationUpsert(ORGANIZATION_ID, workspaceName
                , null, 0L);
        Optional<Workspace> workspaceEntity = Optional.of(Workspace.builder().name(workspaceName).id(workspaceId).status(status)
                .deletionDate(null)
                .organization(Organization.builder().id(ORGANIZATION_ID).build())
                .build());

        when(workspaceRepository.findByOrganizationIdAndName(ORGANIZATION_ID, workspaceName)).thenReturn(workspaceEntity);
        assertThatThrownBy(() -> workspaceService.createWorkspace(organizationUpsertRest, user, organization.getId()))
                .isInstanceOf(G4itRestException.class)
                .hasMessageContaining("workspace 'WORKSPACE' already exists in organization '1'");

    }

}
