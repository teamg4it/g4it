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
import com.soprasteria.g4it.backend.server.gen.api.dto.InDigitalServiceVersionRest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
     * Get the digital service list linked to a user.
     *
     * @param workspaceId the workspace ID.
     * @param isAi        AI service if true
     * @return the digital service list.
     */
    public List<DigitalServiceBO> getDigitalServices(final Long workspaceId, final Boolean isAi) {
        final Workspace linkedWorkspace = workspaceService.getWorkspaceById(workspaceId);
        List<DigitalService> filterDigitalService = digitalServiceRepository.findByWorkspace(linkedWorkspace).stream()
                .filter(ds -> ds.isAi() == isAi)
                .toList();
        // Step 1: Extract DS UIDs
        List<String> dsUids = filterDigitalService.stream()
                .map(DigitalService::getUid)
                .toList();

        // Step 2: Fetch all active DSVs in ONE query
        List<DigitalServiceVersion> activeDSVersions =
                digitalServiceVersionRepository.findActiveDigitalServiceVersion(dsUids);

        // Step 3: Convert to map (dsUid → activeDsvUid)
        Map<String, String> activeDsvMap = activeDSVersions.stream()
                .collect(Collectors.toMap(
                        dsv -> dsv.getDigitalService().getUid(),
                        DigitalServiceVersion::getUid
                ));

        return filterDigitalService.stream().map(ds -> {
            DigitalServiceBO bo = digitalServiceMapper.toBusinessObject(ds);
            bo.setActiveDsvUid(activeDsvMap.get(ds.getUid())); // add the dsv uid
            return bo;
        }).toList();
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

}