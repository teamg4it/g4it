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
import com.soprasteria.g4it.backend.apidigitalservice.mapper.DigitalServiceVersionMapper;
import com.soprasteria.g4it.backend.apidigitalservice.model.DigitalServiceBO;
import com.soprasteria.g4it.backend.apidigitalservice.model.DigitalServiceVersionBO;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceSharedLink;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceVersion;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceVersionStatus;
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
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.modeldb.UserWorkspace;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserWorkspaceRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.DigitalServiceShareRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.DigitalServiceVersionsListRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InDigitalServiceVersionRest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

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
    private DigitalServiceVersionMapper digitalServiceVersionMapper;
    @Autowired
    private RoleService roleService;
    @Autowired
    private WorkspaceService workspaceService;
    @Autowired
    private UserWorkspaceRepository userWorkspaceRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
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
    @Autowired
    private DigitalServiceLinkRepository digitalServiceLinkRepository;
    @Value("${batch.local.working.folder.base.path:}")
    private String localWorkingPath;
    @Autowired
    private DigitalServiceVersionRepository digitalServiceVersionRepository;


    /**
     * Create a new digital service.
     *
     * @param workspaceId the linked workspace id.
     * @param userId      the userId.
     * @param isAi        AI service if true
     * @return the business object corresponding on the digital service created.
     */
    public DigitalServiceBO createDigitalService(final Long workspaceId, final long userId, final Boolean isAi) {
        // Get the linked workspace.
        final Workspace linkedWorkspace = workspaceService.getWorkspaceById(workspaceId);

        // Get last index to create digital service.

        String regex = "^" + DEFAULT_NAME_PREFIX + " (\\d+)" + (isAi ? " AI" : "") + "$";

        final List<DigitalService> orgDigitalServices = digitalServiceRepository.findByWorkspaceAndIsAi(linkedWorkspace, isAi);
        final Integer lastDigitalServiceDefaultNumber = orgDigitalServices
                .stream()
                .map(DigitalService::getName)
                .filter(name -> name.matches(regex))
                .map(name -> name.replace(DEFAULT_NAME_PREFIX + " ", "")
                        .replace(isAi ? " AI" : "", "").trim())
                .map(Integer::valueOf)
                .max(Comparator.naturalOrder())
                .orElse(0);

        // Get the linked user.
        final User user = userRepository.findById(userId).orElseThrow();

        // Save the digital service with +1 on index name.
        final LocalDateTime now = LocalDateTime.now();

        String dsName = DEFAULT_NAME_PREFIX + " " + (lastDigitalServiceDefaultNumber + 1) + (isAi ? " AI" : "");

        final DigitalService digitalServiceToSave = DigitalService
                .builder()
                .name(dsName)
                .user(user)
                .workspace(linkedWorkspace)
                .isAi(isAi)
                .creationDate(now)
                .lastUpdateDate(now)
                .build();
        final DigitalService digitalServiceSaved = digitalServiceRepository.save(digitalServiceToSave);

        // Return the business object.
        return digitalServiceMapper.toBusinessObject(digitalServiceSaved);
    }

    /**
     * Create a new digital service version.
     *
     * @param workspaceId                 the linked workspace id.
     * @param userId                      the userId.
     * @param inDigitalServiceVersionRest the digital service version request data
     * @return the business object corresponding to the digital service created.
     */
    public DigitalServiceVersionBO createDigitalServiceVersion(final Long workspaceId,
                                                               final long userId,
                                                               final InDigitalServiceVersionRest inDigitalServiceVersionRest) {
        // Get the linked workspace
        final Workspace linkedWorkspace = workspaceService.getWorkspaceById(workspaceId);

        // Get the linked user
        final User user = userRepository.findById(userId).orElseThrow();

        final LocalDateTime now = LocalDateTime.now();

        // Step 1: Create the Digital Service
        final DigitalService digitalService = DigitalService.builder()
                .name(inDigitalServiceVersionRest.getDsName())
                .user(user)
                .workspace(linkedWorkspace)
                .isAi(inDigitalServiceVersionRest.getIsAI())
                .creationDate(now)
                .lastUpdateDate(now)
                .build();

        final DigitalService savedDigitalService = digitalServiceRepository.save(digitalService);

        // Step 2: Create the Digital Service Version
        final DigitalServiceVersion digitalServiceVersion = DigitalServiceVersion.builder()
                .description(inDigitalServiceVersionRest.getVersionName())
                .digitalService(DigitalService.builder().uid(savedDigitalService.getUid()).build())
                .versionType(DigitalServiceVersionStatus.DRAFT.name()) // Initial version type
                .createdBy(savedDigitalService.getUser().getId())
                .creationDate(savedDigitalService.getCreationDate())
                .lastUpdateDate(savedDigitalService.getLastUpdateDate())
                .lastCalculationDate(savedDigitalService.getLastCalculationDate())
                .build();

        final DigitalServiceVersion savedDigitalServiceVersion = digitalServiceVersionRepository.save(digitalServiceVersion);

        // Return the business object
        return digitalServiceVersionMapper.toBusinessObject(savedDigitalServiceVersion, savedDigitalService);
    }


    /**
     * Get the digital service list linked to a user.
     *
     * @param workspaceId the workspace ID.
     * @param isAi        AI service if true
     * @return the digital service list.
     */
    public List<DigitalServiceBO> getDigitalServices(final Long workspaceId, final Boolean isAi) {
        final Workspace linkedWorkspace = workspaceService.getWorkspaceById(workspaceId);
        List<DigitalService> filterDigitalService = digitalServiceRepository.findByWorkspace(linkedWorkspace).stream().filter(ds -> ds.isAi() == isAi)
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
     * Update a digital service if user has write access or
     * update enableDataInconsistency
     *
     * @param digitalService   the business object containing data to update.
     * @param organizationName the organization name
     * @param workspaceId      the workspace Id
     * @param user             the user entity
     * @return the updated digital service
     */
    public DigitalServiceBO updateDigitalService(final DigitalServiceBO digitalService, final String organizationName,
                                                 final Long workspaceId, final UserBO user) {

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
        boolean isAdmin = roleService.hasAdminRightOnOrganizationOrWorkspace
                (user, organizationRepository.findByName(organizationName).get().getId(), workspaceId);
        if (!isAdmin) {
            UserWorkspace userWorkspace = userWorkspaceRepository.findByWorkspaceIdAndUserId(workspaceId, userId).orElseThrow();

            boolean hasWriteAccess = userWorkspace.getRoles().stream().anyMatch(role -> "ROLE_DIGITAL_SERVICE_WRITE".equals(role.getName()));

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
     * Generate the link to share the digital service
     *
     * @param organization      the client organization name.
     * @param workspaceId       the linked workspace id.
     * @param digitalServiceUid the digital service id.
     * @return the url.
     */
    public DigitalServiceShareRest shareDigitalService(final String organization, final Long workspaceId,
                                                       final String digitalServiceUid, final UserBO userBO,
                                                       final Boolean extendLink) {
        DigitalService digitalService = digitalServiceRepository.findById(digitalServiceUid).orElseThrow(() ->
                new G4itRestException("404", String.format("Digital service %s not found in %s/%d", digitalServiceUid, organization, workspaceId))
        );

        // Get the linked user.
        final User user = userRepository.findById(userBO.getId()).orElseThrow();

        List<DigitalServiceSharedLink> digitalServiceLinkList = digitalServiceLinkRepository.findByDigitalService(digitalService);

        DigitalServiceSharedLink digitalServiceActiveLink = digitalServiceLinkList.stream()
                .filter(DigitalServiceSharedLink::isActive)
                .findFirst()
                .orElse(null);

        LocalDateTime expiryDate = LocalDateTime.now()
                .plusDays(60)
                .withHour(23)
                .withMinute(59)
                .withSecond(0)
                .withNano(0);
        if (digitalServiceActiveLink != null) {
            // Update expiry date to 60 days from now.
            if (Boolean.TRUE.equals(extendLink)) {
                digitalServiceActiveLink.setExpiryDate(expiryDate);
                digitalServiceLinkRepository.save(digitalServiceActiveLink);
            }
            return DigitalServiceShareRest.builder().url(String.format("/shared/%s/ds/%s",
                            digitalServiceActiveLink.getUid(), digitalServiceUid))
                    .expiryDate(digitalServiceActiveLink.getExpiryDate())
                    .build();


        } else {
            // Create a new shared link
            DigitalServiceSharedLink linkToCreate = DigitalServiceSharedLink.builder()
                    .digitalService(digitalService)
                    .createdBy(user)
                    .isActive(true)
                    .creationDate(LocalDateTime.now())
                    .expiryDate(LocalDateTime.now().plusDays(60))
                    .build();

            DigitalServiceSharedLink savedLink = digitalServiceLinkRepository.save(linkToCreate);

            return DigitalServiceShareRest.builder()
                    .url(String.format("/shared/%s/ds/%s", savedLink.getUid(), digitalServiceUid))
                    .expiryDate(savedLink.getExpiryDate())
                    .build();
        }
    }


    /**
     * Get a digital service.
     *
     * @param digitalServiceUid the digital service id.
     * @return the business object.
     */
    public DigitalServiceBO getDigitalService(final String digitalServiceUid) {
        DigitalServiceBO digitalServiceBO = digitalServiceMapper.toFullBusinessObject(getDigitalServiceEntity(digitalServiceUid));

        //check shared link presence
        boolean isShared = digitalServiceLinkRepository.existsByDigitalService_UidAndIsActiveTrue(digitalServiceUid);

        digitalServiceBO.setIsShared(isShared);
        return digitalServiceBO;
    }

    private DigitalService getDigitalServiceEntity(final String digitalServiceUid) {
        return digitalServiceRepository.findById(digitalServiceUid)
                .orElseThrow(() -> new G4itRestException("404", String.format("Digital Service %s not found.", digitalServiceUid)));
    }


    /**
     * Returns true if the digital service exists and linked to organization, workspaceId
     *
     * @param organizationName  organizationName
     * @param workspaceId       workspaceId
     * @param digitalServiceUid digitalServiceUid
     */
    @Cacheable("digitalServiceExists")
    public boolean digitalServiceExists(final String organizationName, final Long workspaceId, final String digitalServiceUid) {
        final Workspace linkedWorkspace = workspaceService.getWorkspaceById(workspaceId);
        if (!Objects.equals(organizationName, linkedWorkspace.getOrganization().getName())) {
            return false;
        }
        return digitalServiceRepository.findByWorkspaceAndUid(linkedWorkspace, digitalServiceUid).isPresent();
    }


    public Boolean validateDigitalServiceSharedLink(String digitalServiceUid,
                                                    String shareId) {
        return digitalServiceLinkRepository.validateLink(shareId, digitalServiceUid).isPresent();


    }


    public List<DigitalServiceVersionsListRest> getDigitalServiceVersions(String digitalServiceVersionUid, Boolean isAi) {
        // get digitalServiceUid from the version
        final Optional<DigitalServiceVersion> digitalServiceVersion = digitalServiceVersionRepository.findById(digitalServiceVersionUid);
        if (digitalServiceVersion.isEmpty()) {
            return List.of();
        }
        String digitalServiceUid = digitalServiceVersion.get().getDigitalService().getUid();

        // fetch all versions for the digitalServiceUid
        List<DigitalServiceVersion> versions = digitalServiceVersionRepository.findByDigitalServiceUid(digitalServiceUid);

        // Step 3: Map to DigitalServiceVersionsListRest
        return versions.stream()
                .map(v -> DigitalServiceVersionsListRest.builder()
                        .versionName(v.getDescription())
                        .versionType(v.getVersionType())
                        .digitalServiceVersionUid(v.getUid())
                        .digitalServiceUid(digitalServiceUid)
                        .build()).collect(toList());

    }
}