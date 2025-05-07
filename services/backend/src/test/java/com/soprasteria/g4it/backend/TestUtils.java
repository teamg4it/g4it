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
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.soprasteria.g4it.backend.apidigitalservice.model.DigitalServiceBO;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiuser.model.OrganizationBO;
import com.soprasteria.g4it.backend.apiuser.model.SubscriberBO;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.*;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.task.model.TaskStatus;
import com.soprasteria.g4it.backend.common.task.model.TaskType;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.OrganizationStatus;
import com.soprasteria.g4it.backend.server.gen.api.dto.LinkUserRoleRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.OrganizationUpsertRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.UserRoleRest;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class TestUtils {

    public static String SUBSCRIBER = "SUBSCRIBER";
    public static String ORGANIZATION = "ORGANIZATION";
    public static Long ORGANIZATION_ID = 1L;
    public static String EMAIL = "user.test@unitaire";
    public static String ROLE = "ROLE";
    public static ObjectMapper mapper = new ObjectMapper()
            .configure(SerializationFeature.INDENT_OUTPUT, true)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
            .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .registerModule(new JavaTimeModule()
                    .addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    .addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
            );

    public static UserBO createUserBO(final List<String> userRoles) {
        return UserBO.builder()
                .id(1)
                .email(EMAIL)
                .subscribers(List.of(SubscriberBO.builder()
                        .name(SUBSCRIBER)
                        .organizations(List.of(OrganizationBO.builder()
                                .name(ORGANIZATION)
                                .status(OrganizationStatus.ACTIVE.name())
                                .roles(userRoles)
                                .build()))
                        .build()))
                .build();
    }

    public static Subscriber createSubscriber() {
        return Subscriber.builder().id(1L).name(SUBSCRIBER).build();
    }

    public static Subscriber createSubscriber(Long SubscriberId) {
        return Subscriber.builder().id(SubscriberId).name(SUBSCRIBER).build();
    }

    public static Organization createOrganization() {
        return Organization.builder()
                .id(1)
                .name(ORGANIZATION)
                .status(OrganizationStatus.ACTIVE.name())
                .subscriber(Subscriber.builder()
                        .id(1)
                        .name(SUBSCRIBER)
                        .build())
                .build();
    }

    public static Organization createOrganizationWithStatus(String status) {
        return Organization.builder()
                .name(ORGANIZATION)
                .status(status)
                .deletionDate(LocalDateTime.now())
                .subscriber(Subscriber.builder()
                        .name(SUBSCRIBER)
                        .build())
                .build();
    }

    public static Organization createOrganization(Long subId, Long orgId, String status, LocalDateTime deletionDate) {
        return Organization.builder()
                .id(orgId)
                .name(ORGANIZATION)
                .status(status)
                .deletionDate(deletionDate)
                .subscriber(Subscriber.builder()
                        .id(subId)
                        .name(SUBSCRIBER)
                        .build())
                .build();
    }

    public static Organization createToBeDeletedOrganization(String status, LocalDateTime deletionDate) {
        return Organization.builder()
                .id(ORGANIZATION_ID)
                .name(ORGANIZATION)
                .status(OrganizationStatus.TO_BE_DELETED.name())
                .deletionDate(deletionDate)
                .subscriber(Subscriber.builder()
                        .name(SUBSCRIBER)
                        .build())
                .build();
    }


    public static DigitalService createDigitalService() {
        final String digitalServiceUid = "80651485-3f8b-49dd-a7be-753e4fe1fd36";
        final DigitalService digitalService = DigitalService.builder().uid(digitalServiceUid).name("name").lastUpdateDate(LocalDateTime.now()).build();
        return digitalService;
    }

    public static DigitalServiceBO createDigitalServiceBO() {
        final String digitalServiceUid = "80651485-3f8b-49dd-a7be-753e4fe1fd36";
        return DigitalServiceBO.builder().uid(digitalServiceUid).name("name").lastUpdateDate(LocalDateTime.now()).build();
    }

    public static User createUserWithRoleOnSub(Long subscriberId, List<Role> roles) {
        return User.builder().email(EMAIL)
                .userSubscribers(List.of(UserSubscriber.builder().defaultFlag(true).roles(roles).subscriber(Subscriber.builder().name(SUBSCRIBER).build()).build()))
                .userOrganizations(List.of(UserOrganization
                        .builder().defaultFlag(true).roles(List.of(Role.builder().name("ROLE_INVENTORY_READ").build())).organization(Organization.builder().name(ORGANIZATION).status(OrganizationStatus.ACTIVE.name())
                                .subscriber(Subscriber.builder().id(subscriberId).name(SUBSCRIBER).build()).build()).build())).build();
    }

    public static UserBO createUserBOAdminSub() {
        return UserBO.builder().email(EMAIL)
                .subscribers(List.of(
                        SubscriberBO.builder()
                                .id(1L)
                                .roles(List.of())
                                .build(),
                        SubscriberBO.builder()
                                .id(2L)
                                .roles(List.of(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR))
                                .build()))
                .build();
    }

    public static UserBO createUserBONoRole() {
        return UserBO.builder().email(EMAIL)
                .subscribers(List.of(SubscriberBO.builder()
                        .id(1L)
                        .roles(List.of())
                        .build()))
                .build();
    }


    public static UserOrganization createUserOrganization(Long organizationId, List<Role> roles, String status) {
        return UserOrganization
                .builder().defaultFlag(true).roles(roles).organization(createOrganizationWithStatus(status)).build();
    }

    public static UserOrganization createUserOrganization(Long subId, Long orgId, List<Role> roles, String status, LocalDateTime deletionDate, List<UserRoleOrganization> userRoleOrganization) {
        return UserOrganization
                .builder().defaultFlag(true).roles(roles).organization(createOrganization(subId, orgId, status, deletionDate)).userRoleOrganization(userRoleOrganization).build();
    }

    public static UserSubscriber createUserSubscriber(Long subscriberId, List<Role> roles, List<UserRoleSubscriber> userRoleSubscriberList) {
        return UserSubscriber
                .builder().defaultFlag(true).roles(roles).subscriber(createSubscriber(subscriberId)).userRoleSubscriber(userRoleSubscriberList).build();
    }

    public static OrganizationUpsertRest createOrganizationUpsert(Long subscriberId, String orgName
            , String orgStatus, Long dataRetentionDays) {

        com.soprasteria.g4it.backend.server.gen.api.dto.OrganizationStatus status = null;
        if (orgStatus != null)
            status = com.soprasteria.g4it.backend.server.gen.api.dto.OrganizationStatus.valueOf(orgStatus);

        return OrganizationUpsertRest.builder().subscriberId(subscriberId).name(orgName).status(status)
                .dataRetentionDays(dataRetentionDays).build();
    }

    public static OrganizationUpsertRest createOrganizationUpsert(Long subscriberId, String orgName
            , String orgStatus, String criteriaDs, String criteriaIs) {

        com.soprasteria.g4it.backend.server.gen.api.dto.OrganizationStatus status = null;
        if (orgStatus != null)
            status = com.soprasteria.g4it.backend.server.gen.api.dto.OrganizationStatus.valueOf(orgStatus);

        return OrganizationUpsertRest.builder().subscriberId(subscriberId)
                .name(orgName)
                .criteriaDs(List.of(criteriaDs))
                .criteriaIs(List.of(criteriaIs)).status(status)
                .build();
    }

    public static UserRoleSubscriber createUserRoleSubscriber(Role role) {
        return UserRoleSubscriber.builder().roles(role).build();
    }

    public static UserRoleOrganization createUserRoleOrganization(Role role) {
        return UserRoleOrganization.builder().roles(role).build();
    }

    public static LinkUserRoleRest createLinkUserRoleRest(Long organizationId, List<UserRoleRest> userRoleRest) {

        return LinkUserRoleRest.builder().organizationId(organizationId).users(userRoleRest).build();
    }

    public static UserRoleRest createUserRoleRest(Long userId, List<String> roles) {
        return UserRoleRest.builder().userId(userId).roles(roles).build();
    }

    public static User createUserWithAdminRoleOnOrg() {
        long organizationId = 1L;
        List<Role> organizationAdminRole = List.of(Role.builder().name(Constants.ROLE_ORGANIZATION_ADMINISTRATOR).build());
        return User.builder().email(EMAIL)
                .userOrganizations(List.of(UserOrganization
                        .builder().defaultFlag(true)
                        .roles(organizationAdminRole).organization(Organization.builder().id(organizationId).name(ORGANIZATION).status(OrganizationStatus.ACTIVE.name())
                                .build()).build())).build();


    }

    public static UserOrganization createUserOrganization(Long organizationId, Long userId) {
        return UserOrganization.builder()
                .id(1L)
                .organization(Organization.builder().id(organizationId).name(ORGANIZATION).build())
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

}
