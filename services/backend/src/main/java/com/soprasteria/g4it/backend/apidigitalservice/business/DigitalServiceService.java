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
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.business.RoleService;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.modeldb.UserOrganization;
import com.soprasteria.g4it.backend.apiuser.repository.SubscriberRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserOrganizationRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Digital-Service service.
 */
@Service
@Slf4j
public class DigitalServiceService {

    private static final String DEFAULT_NAME_PREFIX = "Digital Service";
    @Autowired
    private DigitalServiceRepository digitalServiceRepository;
    @Autowired
    private DigitalServiceReferentialService digitalServiceReferentialService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DigitalServiceMapper digitalServiceMapper;
    @Autowired
    private RoleService roleService;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private UserOrganizationRepository userOrganizationRepository;
    @Autowired
    private SubscriberRepository subscriberRepository;
    @Autowired
    private InDatacenterRepository inDatacenterRepository;
    @Autowired
    private InPhysicalEquipmentRepository inPhysicalEquipmentRepository;
    @Autowired
    private InVirtualEquipmentRepository inVirtualEquipmentRepository;
    @Autowired
    private InAiParameterRepository inAiParameterRepository;
    @Autowired
    private InAiInfrastructureRepository inAiInfrastructureRepository;
    @Value("${batch.local.working.folder.base.path:}")
    private String localWorkingPath;

    /**
     * Create a new digital service.
     *
     * @param organizationId the linked organization's id.
     * @param userId         the userId.
     * @param isAi AI service if true
     * @return the business object corresponding on the digital service created.
     */
    public DigitalServiceBO createDigitalService(final Long organizationId, final long userId, final Boolean isAi) {
        // Get the linked organization.
        final Organization linkedOrganization = organizationService.getOrganizationById(organizationId);

        // Get last index to create digital service.
        final List<DigitalService> orgDigitalServices = digitalServiceRepository.findByOrganization(linkedOrganization);
        final Integer lastDigitalServiceDefaultNumber = orgDigitalServices
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
                .isAi(isAi)
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
     * @param isAi  AI service if true
     * @return the digital service list.
     */
    public List<DigitalServiceBO> getDigitalServices(final Long organizationId, final Boolean isAi) {
        final Organization linkedOrganization = organizationService.getOrganizationById(organizationId);
        List<DigitalService> filterDigitalService = digitalServiceRepository.findByOrganization(linkedOrganization).stream().filter(ds -> ds.isAi() == isAi)
                .toList();
        return digitalServiceMapper.toBusinessObject(filterDigitalService);
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
        inAiParameterRepository.deleteByDigitalServiceUid(digitalServiceUid);
        inAiInfrastructureRepository.deleteByDigitalServiceUid(digitalServiceUid);
        digitalServiceRepository.deleteById(digitalServiceUid);
    }

    public void updateLastUpdateDate(final String digitalServiceUid) {
        digitalServiceRepository.updateLastUpdateDate(LocalDateTime.now(), digitalServiceUid);
    }

    /**
     * Update a digital service if has write access or
     * update enableDataInconsistency
     *
     * @param digitalService the business object containing data to update.
     * @param user           the user entity
     * @return the updated digital service
     */
    public DigitalServiceBO updateDigitalService(final DigitalServiceBO digitalService, final String subscriber, final Long organizationId, final UserBO user) {

        // Check if digital service exist.
        final DigitalService digitalServiceToUpdate = getDigitalServiceEntity(digitalService.getUid());

        // Check if digital service was updated.
        final DigitalServiceBO digitalServiceToUpdateBO = digitalServiceMapper.toFullBusinessObject(digitalServiceToUpdate);
        if (digitalService.equals(digitalServiceToUpdateBO)) {
            return digitalServiceToUpdateBO;
        }

        boolean changeDataInconsistency = !Objects.equals(digitalService.getEnableDataInconsistency(),
                digitalServiceToUpdate.isEnableDataInconsistency()
        );
        Long userId = user.getId();
        boolean isAdmin = roleService.hasAdminRightOnSubscriberOrOrganization
                (user, subscriberRepository.findByName(subscriber).get().getId(), organizationId);
        if (!isAdmin) {
            UserOrganization userOrganization = userOrganizationRepository.findByOrganizationIdAndUserId(organizationId, userId).orElseThrow();

            boolean hasWriteAccess = userOrganization.getRoles().stream().anyMatch(role -> "DIGITAL_SERVICE_WRITE".equals(role.getName()));

            if (!(changeDataInconsistency || hasWriteAccess)) {
                throw new G4itRestException("403", "Not authorized");
            }
        }
        // Merge digital service.
        digitalServiceMapper.mergeEntity(digitalServiceToUpdate, digitalService, digitalServiceReferentialService, User.builder().id(user.getId()).build());

        // Save the updated digital service.
        return digitalServiceMapper.toFullBusinessObject(digitalServiceRepository.save(digitalServiceToUpdate));
    }

    /**
     * Get a digital service.
     *
     * @param digitalServiceUid the digital service id.
     * @return the business object.
     */
    public DigitalServiceBO getDigitalService(final String digitalServiceUid) {
        return digitalServiceMapper.toFullBusinessObject(getDigitalServiceEntity(digitalServiceUid));
    }

    private DigitalService getDigitalServiceEntity(final String digitalServiceUid) {
        return digitalServiceRepository.findById(digitalServiceUid)
                .orElseThrow(() -> new G4itRestException("404", String.format("Digital Service %s not found.", digitalServiceUid)));
    }
    /**
     * Returns true if the inventory exists and linked to subscriber, organizationId and inventoryId
     *
     * @param subscriberName subscriberName
     * @param organizationId organizationId
     * @param digitalServiceUid    digitalServiceUid
     */
    @Cacheable("digitalServiceExists")
    public boolean digitalServiceExists(final String subscriberName, final Long organizationId, final String digitalServiceUid) {
        final Organization linkedOrganization = organizationService.getOrganizationById(organizationId);
        if (!Objects.equals(subscriberName, linkedOrganization.getSubscriber().getName())) {
            return false;
        }
        return digitalServiceRepository.findByOrganizationAndUid(linkedOrganization, digitalServiceUid).isPresent();
    }
}