/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.soprasteria.g4it.backend.apidigitalservice.model.DigitalServiceBO;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiuser.model.OrganizationBO;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.model.WorkspaceBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.*;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.task.model.TaskStatus;
import com.soprasteria.g4it.backend.common.task.model.TaskType;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.WorkspaceStatus;
import com.soprasteria.g4it.backend.server.gen.api.dto.LinkUserRoleRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.UserRoleRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.WorkspaceUpsertRest;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class TestUtils {

    public static final String ORGANIZATION = "ORGANIZATION";
    public static final String WORKSPACE = "WORKSPACE";
    public static final Long WORKSPACE_ID = 1L;
    public static final String EMAIL = "user.test@unitaire";
    public static final String ROLE = "ROLE";
    public static ObjectMapper mapper = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .addModule(new JavaTimeModule()
                    .addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    .addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
            .build();

    public static UserBO createUserBO(final List<String> userRoles) {
        return UserBO.builder()
                .id(1)
                .email(EMAIL)
                .organizations(List.of(OrganizationBO.builder()
                        .name(ORGANIZATION)
                        .workspaces(List.of(WorkspaceBO.builder()
                                .name(WORKSPACE)
                                .status(WorkspaceStatus.ACTIVE.name())
                                .roles(userRoles)
                                .build()))
                        .build()))
                .build();
    }

    public static Organization createOrganization() {
        return Organization.builder().id(1L).name(ORGANIZATION).build();
    }

    public static Organization createOrganization(Long organizationId) {
        return Organization.builder().id(organizationId).name(ORGANIZATION).build();
    }

    public static Workspace createWorkspace() {
        return Workspace.builder()
                .id(1)
                .name(WORKSPACE)
                .status(WorkspaceStatus.ACTIVE.name())
                .organization(Organization.builder()
                        .id(1)
                        .name(ORGANIZATION)
                        .build())
                .build();
    }

    public static Workspace createOrganizationWithStatus(String status) {
        return Workspace.builder()
                .name(WORKSPACE)
                .status(status)
                .deletionDate(LocalDateTime.now())
                .organization(Organization.builder()
                        .name(ORGANIZATION)
                        .build())
                .build();
    }

    public static Workspace createOrganization(Long subId, Long orgId, String status, LocalDateTime deletionDate) {
        return Workspace.builder()
                .id(orgId)
                .name(WORKSPACE)
                .status(status)
                .deletionDate(deletionDate)
                .organization(Organization.builder()
                        .id(subId)
                        .name(ORGANIZATION)
                        .build())
                .build();
    }

    public static Workspace createToBeDeletedOrganization(LocalDateTime deletionDate) {
        return Workspace.builder()
                .id(WORKSPACE_ID)
                .name(WORKSPACE)
                .status(WorkspaceStatus.TO_BE_DELETED.name())
                .deletionDate(deletionDate)
                .organization(Organization.builder()
                        .name(ORGANIZATION)
                        .build())
                .build();
    }

    public static DigitalService createDigitalService() {
        final String digitalServiceUid = "80651485-3f8b-49dd-a7be-753e4fe1fd36";
        return DigitalService.builder().uid(digitalServiceUid).name("name").lastUpdateDate(LocalDateTime.now()).build();
    }

    public static DigitalServiceBO createDigitalServiceBO() {
        final String digitalServiceUid = "80651485-3f8b-49dd-a7be-753e4fe1fd36";
        return DigitalServiceBO.builder().uid(digitalServiceUid).name("name").lastUpdateDate(LocalDateTime.now()).build();
    }

    public static User createUserWithRoleOnOrg(Long organizationId, List<Role> roles) {
        return User.builder().email(EMAIL)
                .userOrganizations(List.of(UserOrganization.builder().defaultFlag(true).roles(roles).organization(Organization.builder().name(ORGANIZATION).build()).build()))
                .userWorkspaces(List.of(UserWorkspace
                        .builder().defaultFlag(true).roles(List.of(Role.builder().name("ROLE_INVENTORY_READ").build())).workspace(Workspace.builder().name(WORKSPACE).status(WorkspaceStatus.ACTIVE.name())
                                .organization(Organization.builder().id(organizationId).name(ORGANIZATION).build()).build()).build())).build();
    }

    public static UserBO createUserBOAdminSub() {
        return UserBO.builder().email(EMAIL)
                .organizations(List.of(
                        OrganizationBO.builder()
                                .id(1L)
                                .roles(List.of())
                                .build(),
                        OrganizationBO.builder()
                                .id(2L)
                                .roles(List.of(Constants.ROLE_ORGANIZATION_ADMINISTRATOR))
                                .build()))
                .build();
    }

    public static UserBO createAuthorizedUserAdminSub() {
        return UserBO.builder().email(EMAIL)
                .organizations(List.of(
                        OrganizationBO.builder()
                                .id(1L)
                                .authorizedDomains("unitaire")
                                .build()))
                .build();
    }

    public static UserBO createUserBONoRole() {
        return UserBO.builder().email(EMAIL)
                .organizations(List.of(OrganizationBO.builder()
                        .id(1L)
                        .workspaces(List.of(WorkspaceBO.builder()
                                .name(WORKSPACE)
                                .status(WorkspaceStatus.ACTIVE.name())
                                .roles(List.of(Constants.ROLE_INVENTORY_WRITE))
                                .build()))
                        .roles(List.of())
                        .build()))
                .build();
    }


    public static UserWorkspace createUserWorkspace(List<Role> roles, String status) {
        return UserWorkspace
                .builder().defaultFlag(true).roles(roles).workspace(createOrganizationWithStatus(status)).build();
    }

    public static UserWorkspace createUserWorkspace(Long subId, Long orgId, List<Role> roles, String status, LocalDateTime deletionDate, List<UserRoleWorkspace> userRoleWorkspace) {
        return UserWorkspace
                .builder().defaultFlag(true).roles(roles).workspace(createOrganization(subId, orgId, status, deletionDate)).userRoleWorkspace(userRoleWorkspace).build();
    }

    public static UserOrganization createUserOrganization(Long organizationId, List<Role> roles, List<UserRoleOrganization> userRoleOrganizationList) {
        return UserOrganization
                .builder().defaultFlag(true).roles(roles).organization(createOrganization(organizationId)).userRoleOrganization(userRoleOrganizationList).build();
    }

    public static WorkspaceUpsertRest createOrganizationUpsert(Long organizationId, String orgName
            , String orgStatus, Long dataRetentionDays) {

        com.soprasteria.g4it.backend.server.gen.api.dto.WorkspaceStatus status = null;
        if (orgStatus != null)
            status = com.soprasteria.g4it.backend.server.gen.api.dto.WorkspaceStatus.valueOf(orgStatus);

        return WorkspaceUpsertRest.builder().organizationId(organizationId).name(orgName).status(status)
                .dataRetentionDays(dataRetentionDays).build();
    }

    public static WorkspaceUpsertRest createOrganizationUpsert(Long organizationId, String orgName
            , String orgStatus, String criteriaDs, String criteriaIs) {

        com.soprasteria.g4it.backend.server.gen.api.dto.WorkspaceStatus status = null;
        if (orgStatus != null)
            status = com.soprasteria.g4it.backend.server.gen.api.dto.WorkspaceStatus.valueOf(orgStatus);

        return WorkspaceUpsertRest.builder().organizationId(organizationId)
                .name(orgName)
                .criteriaDs(List.of(criteriaDs))
                .criteriaIs(List.of(criteriaIs)).status(status)
                .build();
    }

    public static UserRoleOrganization createUserRoleOrganization(Role role) {
        return UserRoleOrganization.builder().roles(role).build();
    }

    public static UserRoleWorkspace createUserRoleWorkspace(Role role) {
        return UserRoleWorkspace.builder().roles(role).build();
    }

    public static LinkUserRoleRest createLinkUserRoleRest(Long organizationId, List<UserRoleRest> userRoleRest) {

        return LinkUserRoleRest.builder().workspaceId(organizationId).users(userRoleRest).build();
    }

    public static UserRoleRest createUserRoleRest(Long userId, List<String> roles) {
        return UserRoleRest.builder().userId(userId).roles(roles).build();
    }

    public static User createUserWithAdminRoleOnOrg() {
        long organizationId = 1L;
        List<Role> organizationAdminRole = List.of(Role.builder().name(Constants.ROLE_WORKSPACE_ADMINISTRATOR).build());
        return User.builder().email(EMAIL)
                .userWorkspaces(List.of(UserWorkspace
                        .builder().defaultFlag(true)
                        .roles(organizationAdminRole).workspace(Workspace.builder().id(organizationId).name(WORKSPACE).status(WorkspaceStatus.ACTIVE.name())
                                .build()).build())).build();


    }

    public static UserWorkspace createUserWorkspace(Long organizationId, Long userId) {
        return UserWorkspace.builder()
                .id(1L)
                .workspace(Workspace.builder().id(organizationId).name(WORKSPACE).build())
                .user(User.builder().id(userId).email(EMAIL).build())
                .defaultFlag(true)
                .build();
    }

    public static String toJson(Object object, String... ignoreFields) {
        try {
            return Arrays.stream(mapper.writeValueAsString(object).split("\n"))
                    .filter(line -> {
                        for (String field : ignoreFields) {
                            if (line.contains("\"" + field + "\"")) {
                                return false;
                            }
                        }
                        return true;
                    }).collect(Collectors.joining("\n"));
        } catch (JsonProcessingException e) {
            log.error("Cannot parse object: {}", object, e);
            return "{}";
        }
    }

    public static String formatJson(String json, String... ignoreFields) {
        try {
            return Arrays.stream(mapper.writeValueAsString(mapper.readValue(json, Object.class)).split("\n"))
                    .filter(line -> {
                        for (String field : ignoreFields) {
                            if (line.contains("\"" + field + "\"")) {
                                return false;
                            }
                        }
                        return true;
                    }).collect(Collectors.joining("\n"));
        } catch (JsonProcessingException e) {
            log.error("Cannot parse object: {}", json, e);
            return json;
        }
    }

    public static Task createTask(Context context, List<String> filenames, TaskType type, List<String> criteria, Inventory inventory) {
        return Task.builder()
                .creationDate(context.getDatetime())
                .details(new ArrayList<>())
                .lastUpdateDate(context.getDatetime())
                .progressPercentage("0%")
                .status(TaskStatus.TO_START.toString())
                .type(type.toString())
                .inventory(inventory)
                .filenames(filenames)
                .criteria(criteria)
                .build();
    }
    public static UserBO createSuperAdminUserBO() {
        return UserBO.builder()
                .email(Constants.SUPER_ADMIN_EMAIL)
                .organizations(Collections.emptyList())
                .build();
    }

//    public static UserBO createUserBONoRole() {
//        return UserBO.builder()
//                .email(EMAIL)
//                .organizations(List.of(
//                        OrganizationBO.builder()
//                                .id(1L)
//                                .roles(Collections.emptyList())
//                                .workspaces(Collections.emptyList())
//                                .authorizedDomains("")
//                                .build()))
//                .build();
//    }

    public static UserBO createWorkspaceAdminUserBO(Long workspaceId) {
        return UserBO.builder()
                .email(EMAIL)
                .organizations(List.of(
                        OrganizationBO.builder()
                                .id(workspaceId)
                                .roles(Collections.emptyList())
                                .workspaces(List.of(
                                        WorkspaceBO.builder()
                                                .id(workspaceId)
                                                .name(WORKSPACE)
                                                .status(WorkspaceStatus.ACTIVE.name())
                                                .roles(List.of(Constants.ROLE_WORKSPACE_ADMINISTRATOR))
                                                .build()))
                                .build()))
                .build();
    }

    public static UserBO createUserBOWithOrgAdminRole(Long organizationId) {
        return UserBO.builder()
                .email(EMAIL)
                .organizations(List.of(
                        OrganizationBO.builder()
                                .id(organizationId)
                                .roles(List.of(Constants.ROLE_ORGANIZATION_ADMINISTRATOR))
                                .workspaces(Collections.emptyList())
                                .build()))
                .build();
    }

    public static UserBO createUserBOWithWorkspaceAndRoles(Long workspaceId, List<String> roles) {
        return UserBO.builder()
                .email(EMAIL)
                .organizations(List.of(
                        OrganizationBO.builder()
                                .id(workspaceId)
                                .roles(Collections.emptyList())
                                .workspaces(List.of(
                                        WorkspaceBO.builder()
                                                .id(workspaceId)
                                                .name(WORKSPACE)
                                                .status(WorkspaceStatus.ACTIVE.name())
                                                .roles(roles)
                                                .build()))
                                .build()))
                .build();
    }

    public static UserBO createUserBOWithAuthorizedDomains(Long organizationId, String domainsCsv, String emailDomain) {
        return UserBO.builder()
                .email("test@" + emailDomain)
                .organizations(List.of(
                        OrganizationBO.builder()
                                .id(organizationId)
                                .roles(Collections.emptyList())
                                .workspaces(Collections.emptyList())
                                .authorizedDomains(domainsCsv)
                                .build()))
                .build();
    }

}
