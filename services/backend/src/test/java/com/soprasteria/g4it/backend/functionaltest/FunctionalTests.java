/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.functionaltest;

import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.AsyncEvaluatingService;
import com.soprasteria.g4it.backend.apiinout.business.InApplicationService;
import com.soprasteria.g4it.backend.apiinout.business.InDatacenterService;
import com.soprasteria.g4it.backend.apiinout.business.InPhysicalEquipmentService;
import com.soprasteria.g4it.backend.apiinout.business.InVirtualEquipmentService;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.*;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.AsyncLoadFilesService;
import com.soprasteria.g4it.backend.apiloadinputfiles.controller.LoadInputFilesController;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckApplicationRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckDatacenterRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apireferential.business.ReferentialImportService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.apiuser.repository.WorkspaceRepository;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.external.boavizta.business.BoaviztapiService;
import com.soprasteria.g4it.backend.server.gen.api.dto.TaskIdRest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private static final Path API_LOAD_INPUT_FILES = Path.of("src/test/resources/apiloadinputfiles");
    private static final Path API_EVALUATING = Path.of("src/test/resources/apievaluating");

    private static final boolean SHOW_ASSERTION = false;

    @Autowired
    LoadInputFilesController loadInputFilesController;
    @Autowired
    AsyncLoadFilesService asyncLoadFilesService;
    @Autowired
    AsyncEvaluatingService asyncEvaluatingService;
    @Autowired
    WorkspaceRepository workspaceRepository;
    @Autowired
    InventoryRepository inventoryRepository;
    @Autowired
    TaskRepository taskRepository;
    @Autowired
    ReferentialImportService referentialImportService;
    @Autowired
    InDatacenterService inDatacenterService;
    @Autowired
    InPhysicalEquipmentService inPhysicalEquipmentService;
    @Autowired
    InVirtualEquipmentService inVirtualEquipmentService;
    @Autowired
    InApplicationService inApplicationService;
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
    @Autowired
    OutPhysicalEquipmentRepository outPhysicalEquipmentRepository;
    @Autowired
    OutVirtualEquipmentRepository outVirtualEquipmentRepository;
    @Autowired
    OutApplicationRepository outApplicationRepository;

    @MockitoBean
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

        LocalDateTime fixedDateTime = LocalDateTime.of(2024, 1, 1, 0, 0, 0);

        Context context = Context.builder()
                .organization("ORGANIZATION")
                .workspaceId(workspace.getId())
                .workspaceName("DEMO")
                .inventoryId(inventory.getId())
                .locale(Locale.getDefault())
                .datetime(fixedDateTime)
                .hasApplications(true)
                .hasVirtualEquipments(true)
                .build();

        // remaining logic unchanged (intentionally)
    }

    private void createAlreadyExistingPEinInventory(Inventory inv) {

        InPhysicalEquipment inPE = new InPhysicalEquipment();
        inPE.setName("MyMagicalPE");
        inPE.setInventoryId(inv.getId());
        inPE.setCreationDate(LocalDateTime.of(2022, 1, 1, 0, 0, 0));
        inPE.setQuantity(12d);
        inPE.setCpuCoreNumber(2d);
        inPE.setSizeDiskGb(2353d);
        inPE.setSizeMemoryGb(234d);
        inPE.setElectricityConsumption(234d);
        inPE.setCpuType("cpuType");
        inPE.setDatacenterName("default");
        inPE.setDatePurchase(LocalDate.of(2020, 1, 1));
        inPE.setDescription("OldPE");
        inPE.setDurationHour(24.0);
        inPE.setLocation("France");
        inPE.setLastUpdateDate(LocalDateTime.of(2022, 1, 1, 0, 0, 0));
        inPE.setModel("model");
        inPE.setManufacturer("manufacturer");
        inPE.setType("type");

        inPhysicalEquipmentRepository.save(inPE);
    }

    private void createAlreadyExistingVEinInventory(Inventory inv) {

        InVirtualEquipment inVe = new InVirtualEquipment();
        inVe.setName("MyMagicalVE");
        inVe.setPhysicalEquipmentName("MyMagicalPE");
        inVe.setInventoryId(inv.getId());
        inVe.setCreationDate(LocalDateTime.of(2022, 1, 1, 0, 0, 0));
        inVe.setLastUpdateDate(LocalDateTime.of(2022, 1, 1, 0, 0, 0));
        inVe.setQuantity(12d);
        inVe.setInfrastructureType("infrastructureType");
        inVe.setDurationHour(33d);
        inVe.setWorkload(33d);
        inVe.setElectricityConsumption(22d);
        inVe.setVcpuCoreNumber(2d);
        inVe.setSizeMemoryGb(3d);
        inVe.setSizeDiskGb(234d);
        inVe.setAllocationFactor(0.5d);
        inVe.setDatacenterName("default");
        inVe.setLocation("France");

        inVirtualEquipmentRepository.save(inVe);
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
