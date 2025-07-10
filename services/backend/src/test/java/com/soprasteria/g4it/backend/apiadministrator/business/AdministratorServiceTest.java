/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiadministrator.business;

import com.soprasteria.g4it.backend.TestUtils;
import com.soprasteria.g4it.backend.apiuser.business.SubscriberService;
import com.soprasteria.g4it.backend.apiuser.business.UserService;
import com.soprasteria.g4it.backend.apiuser.mapper.SubscriberRestMapper;
import com.soprasteria.g4it.backend.apiuser.model.SubscriberBO;
import com.soprasteria.g4it.backend.apiuser.model.UserSearchBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.*;
import com.soprasteria.g4it.backend.apiuser.repository.SubscriberRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserRepository;
import com.soprasteria.g4it.backend.common.utils.OrganizationStatus;
import com.soprasteria.g4it.backend.server.gen.api.dto.CriteriaRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.soprasteria.g4it.backend.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdministratorServiceTest {

    private final Organization organization = TestUtils.createOrganization();
    @Mock
    CacheManager cacheManager;
    private long organizationId;
    private long subscriberId;
    // Given global
    @InjectMocks
    private AdministratorService administratorService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AdministratorRoleService administratorRoleService;
    @Mock
    private SubscriberRepository subscriberRepository;
    @Mock
    private SubscriberService subscriberService;
    @Mock
    private SubscriberRestMapper subscriberRestMapper;
    @Mock
    private UserService userService;

    @BeforeEach
    void init() {
        subscriberId = organization.getSubscriber().getId();
        organizationId = organization.getId();
        Mockito.lenient().when(cacheManager.getCache(any())).thenReturn(Mockito.mock(Cache.class));
    }


    @Test
    void searchUserByName_withNoLinkedOrg() {
        String searchedUser = "stName";

        String authorizedDomains = "soprasteria.com,test.com";
        Subscriber subscriber = TestUtils.createSubscriber(subscriberId);
        subscriber.setAuthorizedDomains(authorizedDomains);
        doNothing().when(administratorRoleService).hasAdminRightOnSubscriberOrOrganization(any(), any(), any());

        when(subscriberRepository.findById(any())).thenReturn(Optional.of(subscriber));
        when(userRepository.findBySearchedName(eq(searchedUser), any())).thenReturn(
                List.of(User.builder().email("testName@soprasteria.com").firstName("test").lastName("Name").build()));

        List<UserSearchBO> searchedUsers = administratorService.searchUserByName(searchedUser, subscriberId, organization.getId(),
                TestUtils.createUserBOAdminSub());

        assertEquals(1, searchedUsers.size());
    }

    @Test
    void searchUserByName_withLinkedOrg() {
        String searchedUser = "test";
        String authorizedDomains = "soprasteria.com,test.com";
        Subscriber subscriber = TestUtils.createSubscriber(subscriberId);
        subscriber.setAuthorizedDomains(authorizedDomains);
        doNothing().when(administratorRoleService).hasAdminRightOnSubscriberOrOrganization(any(), any(), any());

        when(subscriberRepository.findById(any())).thenReturn(Optional.of(subscriber));
        when(userRepository.findBySearchedName(eq(searchedUser), any())).thenReturn(Collections.singletonList(User
                .builder().email("test@soprasteria.com")
                .userOrganizations(List.of(UserOrganization
                        .builder().defaultFlag(true).roles(List.of(Role.builder().name(ROLE).build()))
                        .organization(Organization.builder().id(organizationId).name(ORGANIZATION)
                                .status(OrganizationStatus.ACTIVE.name())
                                .subscriber(Subscriber.builder().id(2L).name(SUBSCRIBER).build()).build())
                        .build()))
                .userSubscribers(List.of())
                .build()));
        List<UserSearchBO> searchedUsers;

        searchedUsers = administratorService.searchUserByName(searchedUser, subscriberId, organizationId, TestUtils.createUserBOAdminSub());

        assertEquals(1, searchedUsers.size());
    }

    @Test
    void updateSubscriberCriteria() {
        // Arrange
        subscriberId = 1L;
        CriteriaRest criteriaRest = CriteriaRest.builder().criteria(List.of("New Criteria")).build();
        Subscriber subscriber = TestUtils.createSubscriber(subscriberId);
        subscriber.setCriteria(List.of("Old Criteria"));

        Subscriber updatedSubscriber = TestUtils.createSubscriber(subscriberId);
        updatedSubscriber.setCriteria(List.of("New Criteria"));
        SubscriberBO subscriberBO = SubscriberBO.builder().id(subscriberId)
                .name("SUBSCRIBER")
                .criteria(List.of("New Criteria")).build();
        doNothing().when(administratorRoleService).hasAdminRightsOnAnySubscriber(any());

        when(subscriberService.getSubscriptionById(subscriberId)).thenReturn(subscriber);
        when(subscriberRepository.save(any())).thenReturn(updatedSubscriber);
        when(subscriberRestMapper.toBusinessObject(updatedSubscriber)).thenReturn(subscriberBO);

        SubscriberBO result = administratorService.updateSubscriberCriteria(subscriberId, criteriaRest, createUserBOAdminSub());

        assertThat(result.getCriteria()).isEqualTo(List.of("New Criteria"));

        verify(subscriberRepository).save(updatedSubscriber);
        verify(subscriberRestMapper, times(1)).toBusinessObject(updatedSubscriber);
        verify(userService).clearUserAllCache();
    }

}
