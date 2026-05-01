/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.scheduler;

import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceService;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceVersion;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceVersionRepository;
import com.soprasteria.g4it.backend.apiinventory.business.InventoryDeleteService;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.modeldb.UserWorkspace;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.apiuser.repository.UserRoleWorkspaceRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserWorkspaceRepository;
import com.soprasteria.g4it.backend.apiuser.repository.WorkspaceRepository;
import com.soprasteria.g4it.backend.common.utils.AzureEmailService;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.ObjectUtils;
import com.soprasteria.g4it.backend.common.utils.WorkspaceStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Slf4j
public class DataDeletionService {

    @Value("${g4it.data.retention.day}")
    private Integer dataRetentiondDay;

    @Value("${g4it.data.retention.first-reminder-day:30}")
    private Integer firstReminderDay;

    @Value("${g4it.data.retention.second-reminder-day:2}")
    private Integer secondReminderDay;

    @Value("${g4it.data.retention.email.link.digital-service:https://int.saas-g4it.com/organizations/{organization}/workspaces/{workspaceId}/digital-service-version/{digitalServiceVersionUid}/footprint/resources?renew=true}")
    private String digitalServiceLink;

    @Value("${g4it.data.retention.email.link.inventory:https://int.saas-g4it.com/organizations/{organization}/workspaces/{workspaceId}/inventories?inventoryId={inventoryId}&renew=true}")
    private String inventoryLink;

    @Value("${g4it.data.retention.email.link.eco-mind-ai:https://int.saas-g4it.com/organizations/{organization}/workspaces/{workspaceId}/eco-mind-ai/{digitalServiceVersionUid}/footprint/dashboard?renew=true}")
    private String ecoMindAiLink;

    private final WorkspaceRepository workspaceRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryDeleteService inventoryDeleteService;
    private final DigitalServiceService digitalServiceService;
    private final DigitalServiceRepository digitalServiceRepository;
    private final AzureEmailService azureEmailService;
    private final MessageSource messageSource;
    private final DigitalServiceVersionRepository digitalServiceVersionRepository;
    private final UserWorkspaceRepository userWorkspaceRepository;
    private final UserRoleWorkspaceRepository userRoleWorkspaceRepository;

    @Autowired
    public DataDeletionService(
            WorkspaceRepository workspaceRepository,
            InventoryRepository inventoryRepository,
            InventoryDeleteService inventoryDeleteService,
            DigitalServiceService digitalServiceService,
            DigitalServiceRepository digitalServiceRepository,
            AzureEmailService azureEmailService,
            MessageSource messageSource, DigitalServiceVersionRepository digitalServiceVersionRepository, UserWorkspaceRepository userWorkspaceRepository, UserRoleWorkspaceRepository userRoleWorkspaceRepository
    ) {
        this.workspaceRepository = workspaceRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryDeleteService = inventoryDeleteService;
        this.digitalServiceService = digitalServiceService;
        this.digitalServiceRepository = digitalServiceRepository;
        this.azureEmailService = azureEmailService;
        this.messageSource = messageSource;
        this.digitalServiceVersionRepository = digitalServiceVersionRepository;
        this.userWorkspaceRepository = userWorkspaceRepository;
        this.userRoleWorkspaceRepository = userRoleWorkspaceRepository;
    }

    /**
     * Execute the deletion
     * Get all organizations and workspaces
     * Execute the deletion for data in
     */
    public void executeDeletion() {
        final LocalDateTime now = LocalDateTime.now();
        int nbInventoriesDeleted = 0;
        int nbDigitalServicesDeleted = 0;
        final long start = System.currentTimeMillis();

        List<Workspace> workspaces = workspaceRepository.findAllByStatusIn(List.of(WorkspaceStatus.ACTIVE.name()));

        for (Workspace workspaceEntity : workspaces) {
            final String organization = workspaceEntity.getOrganization().getName();
            final Long workspaceId = workspaceEntity.getId();

            // workspace > organization > default
            final Integer retentionDay = Optional.ofNullable(workspaceEntity.getDataRetentionDay())
                    .orElse(Optional.ofNullable(workspaceEntity.getOrganization().getDataRetentionDay())
                            .orElse(dataRetentiondDay));
            nbInventoriesDeleted += handleInventoryDeletion(workspaceEntity, organization, workspaceId, retentionDay, now);
            nbDigitalServicesDeleted += handleDigitalServiceDeletion(workspaceEntity, organization, workspaceId, retentionDay, now);
        }

        log.info("Deletion of {} inventories and {} digital-services in database, execution time={} ms", nbInventoriesDeleted, nbDigitalServicesDeleted, System.currentTimeMillis() - start);
    }

    private void sendRetentionReminderEmail(String recipientEmail, String itemName, String expirationDate, Integer retentionDay, String emailLink) {
        // Send email notification
        log.info("Sending retention reminder email to {} for item {} expiring on {}", recipientEmail, itemName, expirationDate);
        String[] args = new String[]{itemName, expirationDate, String.valueOf(retentionDay),emailLink};
        String emailContentEn = messageSource.getMessage("email.body", args, Locale.ENGLISH);
        String emailContentFr = messageSource.getMessage("email.body", args, Locale.FRANCE);
        String emailSubjectEn = messageSource.getMessage("email.subject", new String[]{}, Locale.ENGLISH);
        String emailSubjectFr = messageSource.getMessage("email.subject", new String[]{}, Locale.FRANCE);
        // Combine both English and French subjects with a separator
        String combinedSubject = emailSubjectEn + " / " + emailSubjectFr;
        // Combine both English and French content with custom formatting
        if (emailContentFr.contains("{0}") && emailContentFr.contains("{1}") && emailContentFr.contains("{2}") && emailContentFr.contains("{3}")) {
            emailContentFr = emailContentFr.replace("{0}", itemName)
                    .replace("{1}", expirationDate)
                    .replace("{2}", String.valueOf(retentionDay))
                    .replace("{3}", emailLink);
        }
        String combinedBody = emailContentEn + "\n\n" + emailContentFr;
        azureEmailService.sendEmail(recipientEmail, combinedSubject, combinedBody);
    }

    private int handleInventoryDeletion(Workspace workspaceEntity, String organization, Long workspaceId, Integer retentionDay, LocalDateTime now) {
        return inventoryRepository.findByWorkspace(workspaceEntity).stream()
                .mapToInt(inventory -> {
                    long daysSinceLastUpdate = java.time.Duration.between(
                            inventory.getLastUpdateDate(), now
                    ).toDays();
                    if ((daysSinceLastUpdate == retentionDay - firstReminderDay) || (daysSinceLastUpdate == retentionDay - secondReminderDay)) {
                        String expirationDate = ObjectUtils.getExpiryDate(inventory.getLastUpdateDate(), retentionDay);
                        String emailLink = inventoryLink.replace("{organization}", organization)
                                .replace("{workspaceId}", String.valueOf(workspaceId))
                                .replace("{inventoryId}", String.valueOf(inventory.getId()));
                        for (User user : getWorkspaceUsers(workspaceEntity,Constants.ROLE_INVENTORY_WRITE)) {
                            sendRetentionReminderEmail(
                                    user.getEmail(),
                                    inventory.getName(),
                                    expirationDate,
                                    retentionDay, emailLink
                            );
                        }
                        return 0;
                    } else if (now.minusDays(retentionDay).isAfter(inventory.getLastUpdateDate())) {
                        inventoryDeleteService.deleteInventory(organization, workspaceId, inventory.getId());
                        return 1;
                    }
                    return 0;
                })
                .sum();
    }

    private int handleDigitalServiceDeletion(Workspace workspaceEntity, String organization, Long workspaceId, Integer retentionDay, LocalDateTime now) {
        return digitalServiceRepository.findByWorkspace(workspaceEntity).stream()
                .mapToInt(digitalServiceBO -> {
                    long daysSinceLastUpdate = java.time.Duration.between(
                            digitalServiceBO.getLastUpdateDate(), now
                    ).toDays();
                    if ((daysSinceLastUpdate == retentionDay - firstReminderDay) || (daysSinceLastUpdate == retentionDay - secondReminderDay)) {
                        String expirationDate = ObjectUtils.getExpiryDate(digitalServiceBO.getLastUpdateDate(), retentionDay);
                        List<DigitalServiceVersion> digitalServiceVersionList= digitalServiceVersionRepository.findByDigitalServiceUid(digitalServiceBO.getUid());
                        String emailLink;
                        String roleName ;
                        if (digitalServiceBO.isAi()) {
                            emailLink = ecoMindAiLink;
                            roleName = Constants.ROLE_ECO_MIND_AI_WRITE;
                        }else {
                            emailLink = digitalServiceLink;
                            roleName = Constants.ROLE_DIGITAL_SERVICE_WRITE;
                        }
                        emailLink= emailLink.replace("{organization}", organization)
                                .replace("{workspaceId}", String.valueOf(workspaceId))
                                .replace("{digitalServiceVersionUid}", digitalServiceVersionList.getFirst().getUid());
                        for (User user : getWorkspaceUsers(workspaceEntity,roleName)) {
                            sendRetentionReminderEmail(
                                    user.getEmail(),
                                    digitalServiceBO.getName(),
                                    expirationDate,
                                    retentionDay, emailLink
                            );
                        }
                        return 0;
                    } else if (now.minusDays(retentionDay).isAfter(digitalServiceBO.getLastUpdateDate())) {
                        digitalServiceService.deleteDigitalService(digitalServiceBO.getUid());
                        return 1;
                    }
                    return 0;
                })
                .sum();
    }

    private List<User> getWorkspaceUsers(Workspace workspace, String roleName) {
        return userWorkspaceRepository.findByWorkspace(workspace)
                .stream().filter(userWorkspace -> userRoleWorkspaceRepository.findByUserWorkspaces(userWorkspace).parallelStream().anyMatch(userRoleWorkspace ->
                        userRoleWorkspace.getRoles().getName().equals(roleName) || Constants.ROLE_WORKSPACE_ADMINISTRATOR.equals(userRoleWorkspace.getRoles().getName()))).map(UserWorkspace::getUser).toList();

    }

}
