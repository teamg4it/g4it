/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiadministratoractions.business;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apievaluating.business.EvaluatingService;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.server.gen.api.dto.AllEvaluationStatusRest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.CallableStatement;
import java.sql.SQLWarning;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AdministratorActionsService {

    @Autowired
    EvaluatingService evaluatingService;

    @Autowired
    DigitalServiceRepository digitalServiceRepository;

    @Autowired
    OrganizationRepository organizationRepository;

    @Autowired
    InPhysicalEquipmentRepository physicalEquipmentRepository;

    @Autowired
    TaskRepository taskRepository;
    @Autowired
    RoleManagementService roleManagementService;
    private final JdbcTemplate jdbcTemplate;

    public AdministratorActionsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public AllEvaluationStatusRest evaluateAllDigitalServices() {
        List<DigitalService> digitalServices = digitalServiceRepository.findAll();
        int count = 0;
        final LocalDateTime now = LocalDateTime.now();
        boolean runEvaluation = true;
        int skipEvaluationCount = 0;
        int countMissingNetworkEquipment = 0;
        log.info("Digital-service count before evaluation- {}", digitalServices.size());
        for (DigitalService digitalService : digitalServices) {
            //evaluating
            String digitalServiceUid = digitalService.getUid();
            Organization organization = organizationRepository.findById(digitalService.getOrganization().getId()).orElseThrow();
            Long organizationId = organization.getId();
            String subscriber = organization.getSubscriber().getName();

            List<InPhysicalEquipment> physicalEquipments = physicalEquipmentRepository.findByDigitalServiceUid(digitalServiceUid);
            if (!physicalEquipments.isEmpty()) {
                Optional<Task> tasks = taskRepository.findByDigitalService(digitalService);
                if (tasks.isPresent()) {
                    Task task = tasks.get();
                    int day = task.getLastUpdateDate().getDayOfMonth();
                    int month = task.getLastUpdateDate().getMonthValue();
                    int year = task.getLastUpdateDate().getYear();
                    if (now.getDayOfMonth() == day && now.getMonthValue() == month && now.getYear() == year && "COMPLETED".equals(task.getStatus())) {
                        runEvaluation = false;
                        skipEvaluationCount++;
                    } else {
                        runEvaluation = true;
                    }
                }
                List<InPhysicalEquipment> networkEquipments = physicalEquipments.stream().filter(equipment -> "Network".equals(equipment.getType())).toList();
                if (networkEquipments.isEmpty()) {
                    countMissingNetworkEquipment++;
                    log.info("Network equipment not found - {}", digitalServiceUid);
                }
                if (!networkEquipments.isEmpty() && runEvaluation) {
                    count++;
                    log.info("Digital-service re-evaluation and count- {}: {}", digitalServiceUid, count);
                    evaluatingService.evaluatingDigitalService(subscriber, organizationId, digitalServiceUid);
                }
            }
        }
        log.info("Missing Network equipment count - {}", countMissingNetworkEquipment);
        log.info("re-evaluation count- {}", count);
        log.info("skip-evaluation count- {}", skipEvaluationCount);
        return AllEvaluationStatusRest.builder().response("success").build();
    }

    /**
     * Remove write access on Digital Service for all the
     * non-admin users on DEMO organization
     */
    public AllEvaluationStatusRest executeWriteRoleCleanupOnDemoOrg() {
        //call procedure to remove write access
        return roleManagementService.executeRoleCleanup();
    }


    /**
     * Rename the randomly named terminals
     */
    public void renameTerminals() {
        try {
            log.info("START-- renaming the terminals");

            jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
                // Call the procedure
                try (CallableStatement cs = connection.prepareCall("CALL rename_randomly_generated_terminal_name()")) {
                    cs.execute();

                    //  Log NOTICE messages
                    SQLWarning warning = cs.getWarnings();
                    while (warning != null) {
                        log.info("Warning NOTICE: {}", warning.getMessage());
                        warning = warning.getNextWarning();
                    }
                }
                return null;
            });
            log.info("COMPLETED-- terminals renamed");
        } catch (DataAccessException ex) {
            log.error("Failed to rename terminals: {}", ex.getMessage());
        }
    }

    /**
     * Rename the randomly named networks
     */
    public AllEvaluationStatusRest renameNetworks() {
        try {
            log.info("START-- renaming the networks");

            jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
                // Call the procedure
                try (CallableStatement cs = connection.prepareCall("CALL rename_randomly_generated_network_name()")) {
                    cs.execute();

                    //  Log NOTICE messages
                    SQLWarning warning = cs.getWarnings();
                    while (warning != null) {
                        log.info("Warning NOTICE: {}", warning.getMessage());
                        warning = warning.getNextWarning();
                    }
                }
                return null;
            });

            log.info("COMPLETED-- networks renamed");
            return AllEvaluationStatusRest.builder().response("success").build();
        } catch (DataAccessException ex) {
            log.error("Failed to rename networks: {}", ex.getMessage());
            return AllEvaluationStatusRest.builder().response("error").build();
        }
    }
}