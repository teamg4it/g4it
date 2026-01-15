/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.functionaltest;

import com.soprasteria.g4it.backend.apiinout.repository.InApplicationRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InDatacenterRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.controller.LoadInputFilesController;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckApplicationRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckDatacenterRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.apiuser.repository.WorkspaceRepository;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.external.boavizta.business.BoaviztapiService;
import com.soprasteria.g4it.backend.server.gen.api.dto.TaskIdRest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

@SpringBootTest(
        properties = {
                "spring.cloud.azure.enabled=false",
                "spring.liquibase.enabled=false",
                "spring.jpa.hibernate.ddl-auto=create-drop"
        }
)
@ActiveProfiles({"local", "test"})
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FunctionalTests {

    private static final String ORGANIZATION = "ORGANIZATION";

    @Autowired
    LoadInputFilesController loadInputFilesController;
    @Autowired
    WorkspaceRepository workspaceRepository;
    @Autowired
    InventoryRepository inventoryRepository;
    @Autowired
    TaskRepository taskRepository;
    @Autowired
    InDatacenterRepository inDatacenterRepository;
    @Autowired
    InPhysicalEquipmentRepository inPhysicalEquipmentRepository;
    @Autowired
    InVirtualEquipmentRepository inVirtualEquipmentRepository;
    @Autowired
    InApplicationRepository inApplicationRepository;
    @Autowired
    CheckDatacenterRepository checkDatacenterRepository;
    @Autowired
    CheckVirtualEquipmentRepository checkVirtualEquipmentRepository;
    @Autowired
    CheckPhysicalEquipmentRepository checkPhysicalEquipmentRepository;
    @Autowired
    CheckApplicationRepository checkApplicationRepository;


    @Mock
    BoaviztapiService boaviztapiService;

    @Test
    void executeAllFunctionalTests() throws IOException {

        Locale.setDefault(Locale.ENGLISH);

        var workspace = workspaceRepository.save(Workspace.builder()
                .name("DEMO")
                .organization(Organization.builder().name(ORGANIZATION).build())
                .build());

        taskRepository.deleteAll();
        inventoryRepository.deleteAll();

        Mockito.when(boaviztapiService.getCountryMap()).thenReturn(Map.of());

        var inventory = inventoryRepository.save(Inventory.builder()
                .name("Inventory Name")
                .workspace(workspace)
                .doExportVerbose(true)
                .build());

        ResponseEntity<TaskIdRest> response =
                loadInputFilesController.launchloadInputFiles(
                        ORGANIZATION, workspace.getId(), inventory.getId(),
                        "fr", null, null, null, null);

        Long taskId = response.getBody().getTaskId();
        Assertions.assertNull(taskId);

        final Path targetInputFiles = Path.of("target/local-filesystem")
                .resolve(ORGANIZATION)
                .resolve(String.valueOf(workspace.getId()))
                .resolve("input");

        Files.createDirectories(targetInputFiles);

        final Path targetOutputFiles = Path.of("target/local-filesystem")
                .resolve(ORGANIZATION)
                .resolve(String.valueOf(workspace.getId()))
                .resolve("output");

        FileSystemUtils.deleteRecursively(targetOutputFiles);

    }


    public void cleanDB() {
        checkDatacenterRepository.deleteAll();
        checkVirtualEquipmentRepository.deleteAll();
        checkPhysicalEquipmentRepository.deleteAll();
        checkApplicationRepository.deleteAll();
        inDatacenterRepository.deleteAll();
        inPhysicalEquipmentRepository.deleteAll();
        inVirtualEquipmentRepository.deleteAll();
        inApplicationRepository.deleteAll();
        taskRepository.deleteAll();
    }
}
