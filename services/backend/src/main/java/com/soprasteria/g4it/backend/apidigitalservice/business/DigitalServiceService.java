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
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceLink;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceShared;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceLinkRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceSharedRepository;
import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.ExportService;
import com.soprasteria.g4it.backend.apiindicator.business.IndicatorService;
import com.soprasteria.g4it.backend.apiinout.repository.InDatacenterRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.OutVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.model.UserInfoBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.repository.UserRepository;
import com.soprasteria.g4it.backend.common.criteria.CriteriaService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileMapperInfo;
import com.soprasteria.g4it.backend.common.task.business.TaskService;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Digital-Service service.
 */
@Service
@Slf4j
public class DigitalServiceService {

    private static final String DEFAULT_NAME_PREFIX = "Digital Service";
    @Autowired
    private DigitalServiceSharedRepository digitalServiceSharedRepository;
    @Autowired
    private DigitalServiceRepository digitalServiceRepository;
    @Autowired
    private DigitalServiceReferentialService digitalServiceReferentialService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DigitalServiceLinkRepository digitalServiceLinkRepository;
    @Autowired
    private DigitalServiceMapper digitalServiceMapper;

    @Autowired
    private IndicatorService indicatorService;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private CriteriaService criteriaService;
    @Autowired
    private FileMapperInfo fileInfo;
    @Autowired
    private InDatacenterRepository inDatacenterRepository;
    @Autowired
    private InPhysicalEquipmentRepository inPhysicalEquipmentRepository;
    @Autowired
    private InVirtualEquipmentRepository inVirtualEquipmentRepository;
    @Value("${batch.local.working.folder.base.path:}")
    private String localWorkingPath;
    @Autowired
    private TaskService taskService;

    @Autowired
    private OutVirtualEquipmentRepository outVirtualEquipmentRepository;
    @Autowired
    private ExportService exportService;

    /**
     * Create a new digital service.
     *
     * @param organizationId the linked organization's id.
     * @param userId         the userId.
     * @return the business object corresponding on the digital service created.
     */
    public DigitalServiceBO createDigitalService(final Long organizationId, final long userId) {
        // Get the linked organization.
        final Organization linkedOrganization = organizationService.getOrganizationById(organizationId);

        // Get last index to create digital service.
        final List<DigitalService> userDigitalServices = digitalServiceRepository.findByOrganizationAndUserId(linkedOrganization, userId);
        final Integer lastDigitalServiceDefaultNumber = userDigitalServices
                .stream()
                .map(DigitalService::getName)
                .filter(name -> name.matches("^" + DEFAULT_NAME_PREFIX + " \\d+$"))
                .map(name -> name.replace(DEFAULT_NAME_PREFIX + " ", ""))
                .map(Integer::valueOf)
                .max(Comparator.naturalOrder()).orElse(0);

        // Get the linked user.
        final User user = userRepository.findById(userId).orElseThrow();

        // Save the digital service with +1 on index name.
        final LocalDateTime now = LocalDateTime.now();
        final DigitalService digitalServiceToSave = DigitalService
                .builder()
                .name(DEFAULT_NAME_PREFIX + " " + (lastDigitalServiceDefaultNumber + 1))
                .user(user)
                .organization(linkedOrganization)
                .creationDate(now)
                .lastUpdateDate(now)
                .build();
        final DigitalService digitalServiceSaved = digitalServiceRepository.save(digitalServiceToSave);

        // Return the business object.
        return digitalServiceMapper.toBusinessObject(digitalServiceSaved);
    }

    /**
     * Get the digital service list linked to a user.
     *
     * @param organizationId the organization's id.
     * @param userId         the userId.
     * @return the digital service list.
     */
    public List<DigitalServiceBO> getDigitalServices(final Long organizationId, final long userId) {
        final Organization linkedOrganization = organizationService.getOrganizationById(organizationId);

        // Retrieve digital services created by the user
        List<DigitalService> digitalServices = digitalServiceRepository.findByOrganizationAndUserId(linkedOrganization, userId);

        // Retrieve shared digital services for the user
        List<DigitalService> sharedDigitalServices = digitalServiceSharedRepository.findByOrganizationAndUserId(linkedOrganization, userId)
                .stream()
                .map(DigitalServiceShared::getDigitalService)
                .toList();

        final List<DigitalService> combinedDigitalServices = Stream.concat(digitalServices.stream(), sharedDigitalServices.stream())
                .toList();
        List<DigitalServiceBO> allDigitalServicesBO = digitalServiceMapper.toBusinessObject(combinedDigitalServices);


        return allDigitalServicesBO.stream().peek(digitalServiceBO -> {
            User user = getDigitalServiceEntity(digitalServiceBO.getUid()).getUser();
            //set creator info
            digitalServiceBO.setCreator(UserInfoBO.builder().id(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName()).build());

            List<DigitalServiceShared> shared = digitalServiceSharedRepository.findByDigitalServiceUid(digitalServiceBO.getUid());
            List<UserInfoBO> members = null;
            if (shared != null) {
                members = shared.stream().map(
                        sharedDigitalService -> {
                            User userEntity = sharedDigitalService.getUser();
                            return UserInfoBO.builder().id(userEntity.getId())
                                    .firstName(userEntity.getFirstName())
                                    .lastName(userEntity.getLastName())
                                    .build();
                        }).collect(Collectors.toList());
            }
            //set member info
            digitalServiceBO.setMembers(members);

        }).toList();
    }

    /**
     * Delete a digital service.
     *
     * @param digitalServiceUid the digital service UID.
     */
    public void deleteDigitalService(final String digitalServiceUid) {
        inVirtualEquipmentRepository.deleteByDigitalServiceUid(digitalServiceUid);
        inPhysicalEquipmentRepository.deleteByDigitalServiceUid(digitalServiceUid);
        inDatacenterRepository.deleteByDigitalServiceUid(digitalServiceUid);
        digitalServiceRepository.deleteById(digitalServiceUid);
    }

    public void updateLastUpdateDate(final String digitalServiceUid) {
        digitalServiceRepository.updateLastUpdateDate(LocalDateTime.now(), digitalServiceUid);
    }

    /**
     * Update a digital service.
     *
     * @param digitalService the business object containing data to update.
     * @param user           the user entity
     * @return the updated digital service.
     */
    public DigitalServiceBO updateDigitalService(final DigitalServiceBO digitalService, final UserBO user) {

        // Check if digital service exist.
        final DigitalService digitalServiceToUpdate = getDigitalServiceEntity(digitalService.getUid());

        // Check if digital service was updated.
        final DigitalServiceBO digitalServiceToUpdateBO = digitalServiceMapper.toFullBusinessObject(digitalServiceToUpdate);
        if (digitalService.equals(digitalServiceToUpdateBO)) {
            return digitalServiceToUpdateBO;
        }

        // Merge digital service.
        digitalServiceMapper.mergeEntity(digitalServiceToUpdate, digitalService, digitalServiceReferentialService, User.builder().id(user.getId()).build());

        // Save the updated digital service.
        DigitalServiceBO updateDigitalServiceBO = digitalServiceMapper.toFullBusinessObject(digitalServiceRepository.save(digitalServiceToUpdate));
        updateDigitalServiceBO.setCreator(digitalService.getCreator());
        updateDigitalServiceBO.setMembers(digitalService.getMembers());
        return updateDigitalServiceBO;
    }

    /**
     * Get a digital service.
     *
     * @param digitalServiceUid the digital service id.
     * @return the business object.
     */
    public DigitalServiceBO getDigitalService(final String digitalServiceUid) {
        DigitalService digitalServiceEntity = getDigitalServiceEntity(digitalServiceUid);

        DigitalServiceBO digitalServiceBO = digitalServiceMapper.toFullBusinessObject(digitalServiceEntity);

        // Set the creator information
        User user = digitalServiceEntity.getUser();
        digitalServiceBO.setCreator(UserInfoBO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build());

        List<DigitalServiceShared> sharedDigitalServices = digitalServiceSharedRepository.findByDigitalServiceUid(digitalServiceUid);

        List<UserInfoBO> members = sharedDigitalServices.stream()
                .map(sharedDigitalService -> {
                    User sharedUser = sharedDigitalService.getUser();
                    return UserInfoBO.builder()
                            .id(sharedUser.getId())
                            .firstName(sharedUser.getFirstName())
                            .lastName(sharedUser.getLastName())
                            .build();
                })
                .collect(Collectors.toList());
        // Set the members' information
        digitalServiceBO.setMembers(members);

        return digitalServiceBO;
    }


    private DigitalService getDigitalServiceEntity(final String digitalServiceUid) {
        return digitalServiceRepository.findById(digitalServiceUid)
                .orElseThrow(() -> new G4itRestException("404", String.format("Digital Service %s not found.", digitalServiceUid)));
    }

    /**
     * Generate the link to share the digital service
     *
     * @param subscriber        the client subscriber name.
     * @param organizationId    the linked organization's id.
     * @param digitalServiceUid the digital service id.
     * @return the url.
     */
    public String shareDigitalService(final String subscriber, final Long organizationId,
                                      final String digitalServiceUid) {
        DigitalService digitalService = digitalServiceRepository.findById(digitalServiceUid).orElseThrow(() ->
                new G4itRestException("404", String.format("Digital service %s not found in %s/%d", digitalServiceUid, subscriber, organizationId))
        );

        DigitalServiceLink linkToCreate = DigitalServiceLink.builder()
                .digitalService(digitalService)
                .expirationDate(LocalDateTime.now().plusDays(1))
                .build();
        String uid = digitalServiceLinkRepository.save(linkToCreate).getUid();
        return String.format("/subscribers/%s/organizations/%d/digital-services/%s/share/%s",
                subscriber, organizationId, digitalServiceUid, uid);
    }

    /**
     * Associate the digital service to the user accessing the shared link
     *
     * @param subscriber        the client subscriber name.
     * @param organizationId    the linked organization's id.
     * @param digitalServiceUid the digital service id.
     * @param sharedUid         the unique id of url shared
     * @param userId            userId of the user accessing the link
     */
    public void linkDigitalServiceToUser(final String subscriber, final Long organizationId,
                                         final String digitalServiceUid, final String sharedUid, final long userId) {
        // Check if digital service exists
        DigitalService digitalService = digitalServiceRepository.findById(digitalServiceUid).orElseThrow(() ->
                new G4itRestException("404", String.format("Digital service %s not found in %s/%d", digitalServiceUid, subscriber, organizationId))
        );

        // Validate if the shared url is not expired
        Optional<DigitalServiceLink> sharedLink = digitalServiceLinkRepository.findById(sharedUid);
        if (sharedLink.isEmpty()) {
            throw new G4itRestException("410", String.format("The shared url for Digital service %s/%s/%d has expired.", digitalServiceUid, subscriber, organizationId));
        }

        User user = userRepository.findById(userId).orElseThrow();

        // if the current user owns or has already accessed the digital service then return
        if (digitalServiceRepository.existsByUidAndUserId(digitalServiceUid, userId) ||
                digitalServiceSharedRepository.existsByDigitalServiceUidAndUserId(digitalServiceUid, userId)) {
            return;
        }

        DigitalServiceShared newDigitalServiceShared = DigitalServiceShared.builder().digitalService(digitalService)
                .user(user).organization(organizationService.getOrganizationById(organizationId))
                .build();
        digitalServiceSharedRepository.save(newDigitalServiceShared);

    }

    /**
     * Unlink the shared digital service from user
     *
     * @param digitalServiceUid the shared digital service's uid
     * @param userId            the user id
     */
    public void unlinkSharedDigitalService(final String digitalServiceUid, final long userId) {

        Optional<DigitalServiceShared> optDigitalServiceShared = digitalServiceSharedRepository.findByDigitalServiceUidAndUserId(digitalServiceUid, userId);
        optDigitalServiceShared.ifPresent(digitalServiceShared -> digitalServiceSharedRepository.delete(digitalServiceShared));
    }

}
