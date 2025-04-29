/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.functionaltest;

import com.soprasteria.g4it.backend.TestUtils;
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
import com.soprasteria.g4it.backend.apiloadinputfiles.modeldb.CheckApplication;
import com.soprasteria.g4it.backend.apiloadinputfiles.modeldb.CheckPhysicalEquipment;
import com.soprasteria.g4it.backend.apiloadinputfiles.modeldb.CheckVirtualEquipment;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckApplicationRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckDatacenterRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apireferential.business.ReferentialImportService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.Subscriber;
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import com.soprasteria.g4it.backend.apiuser.repository.SubscriberRepository;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.task.model.TaskType;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.Constants;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@SpringBootTest
@ActiveProfiles({"local", "test"})
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FunctionalTests {
    private static final String SUBSCRIBER = "SUBSCRIBER";
    private static final Path apiloadinputfiles = Path.of("src/test/resources/apiloadinputfiles");
    private static final Path apievaluating = Path.of("src/test/resources/apievaluating");
    // Set to true if you want Assertions on each fail
    // please commit with  SHOW_ASSERTION = false;
    private static final boolean SHOW_ASSERTION = false;
    @Autowired
    LoadInputFilesController loadInputFilesController;
    @Autowired
    AsyncLoadFilesService asyncLoadFilesService;
    @Autowired
    AsyncEvaluatingService asyncEvaluatingService;
    @Autowired
    SubscriberRepository subscriberRepository;
    @Autowired
    OrganizationRepository organizationRepository;
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
    @MockBean
    BoaviztapiService boaviztapiService;

    /**
     * Execute all test cases located in :
     * - src/test/resources/apiloadinputfiles
     * - src/test/resources/apievaluating
     */
    @Test
    void executeAllFunctionalTests() throws IOException {
        Locale.setDefault(Locale.ENGLISH);

        var organization = organizationRepository.save(Organization.builder()
                .name("DEMO")
                .subscriber(Subscriber.builder().name(SUBSCRIBER).build())
                .build());

        taskRepository.deleteAll();
        inventoryRepository.deleteAll();

        Mockito.when(boaviztapiService.getCountryMap()).thenReturn(Map.of());

        var inventory = inventoryRepository.save(Inventory.builder()
                .name("Inventory Name")
                .organization(organization)
                .doExportVerbose(true)
                .build());

        // case with no input data
        ResponseEntity<TaskIdRest> response = loadInputFilesController.launchloadInputFiles(SUBSCRIBER, organization.getId(), inventory.getId(), "fr", null, null, null, null);
        Assertions.assertNull(response.getBody().getTaskId());

        final Path targetInputFiles = Path.of("target/local-filesystem").resolve(SUBSCRIBER).resolve(String.valueOf(organization.getId())).resolve("input");
        Files.createDirectories(targetInputFiles);

        final Path targetOutputFiles = Path.of("target/local-filesystem").resolve(SUBSCRIBER).resolve(String.valueOf(organization.getId())).resolve("output");
        FileSystemUtils.deleteRecursively(targetOutputFiles);

        LocalDateTime fixedDateTime = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        Context context = Context.builder()
                .subscriber("SUBSCRIBER")
                .organizationId(organization.getId())
                .organizationName("DEMO")
                .inventoryId(inventory.getId())
                .locale(Locale.getDefault())
                .datetime(fixedDateTime)
                .hasApplications(true).hasVirtualEquipments(true)
                .build();

        /*
         * LOAD INPUT FILES FUNCTIONAL TESTS
         */
        boolean allOk = true;
        for (File testFolder : Arrays.stream(Objects.requireNonNull(apiloadinputfiles.toFile().listFiles())).sorted().toList()) {

            // PREPARE
            var testCase = testFolder.getName();

            // clean tables
            checkDatacenterRepository.deleteAll();
            checkVirtualEquipmentRepository.deleteAll();
            checkPhysicalEquipmentRepository.deleteAll();
            checkApplicationRepository.deleteAll();
            inDatacenterRepository.deleteAll();
            inPhysicalEquipmentRepository.deleteAll();
            inVirtualEquipmentRepository.deleteAll();
            inApplicationRepository.deleteAll();
            taskRepository.deleteAll();

            //Load already existing PE in inventory
            InPhysicalEquipment oldPhysicalEquipment = createAlreadyExistingPEinInventory(inventory, organization.getId());
            InVirtualEquipment oldVE = createAlreadyExistingVEinInventory(inventory, organization.getId());

            // copy files in work
            File inputFolder = apiloadinputfiles.resolve(testCase).resolve("input").toFile();
            List<String> filenames = new ArrayList<>();

            for (File f : Objects.requireNonNull(inputFolder.listFiles())) {
                String targetFilename = f.getName().replace(".csv", ".csv_" + UUID.randomUUID() + ".csv");
                Files.copy(f.toPath(), targetInputFiles.resolve(targetFilename));
                filenames.add(targetFilename);
            }

            File refFolder = apiloadinputfiles.resolve(testCase).resolve("ref").toFile();
            for (File f : Objects.requireNonNull(refFolder.listFiles())) {
                MultipartFile multipartFile = new MockMultipartFile("file", f.getName(), "text/csv", Files.readAllBytes(f.toPath()));
                referentialImportService.importReferentialCSV(f.getName().replace(".csv", ""), multipartFile, null);
            }

            // EXECUTE LOADING
            var task = taskRepository.save(TestUtils.createTask(context, filenames, TaskType.LOADING, null, inventory));
            asyncLoadFilesService.execute(context, task);

            //TODO to delete this debug code
            //TEST checks
            List<CheckVirtualEquipment> cve = checkVirtualEquipmentRepository.findAll();
            List<CheckPhysicalEquipment> pec = checkPhysicalEquipmentRepository.findAll();
            List<CheckApplication> apc = checkApplicationRepository.findAll();

            // ASSERT
            Path outputPath = apiloadinputfiles.resolve(testCase).resolve("output");

            for (File file : Objects.requireNonNull(outputPath.toFile().listFiles())) {
                var actual = switch (file.getName()) {
                    case "1-datacenters.json" ->
                            TestUtils.toJson(inDatacenterService.getByInventory(context.getInventoryId()), "inventoryId", "taskId", "id", "creationDate", "lastUpdateDate");
                    case "2-physical_equipments.json" ->
                            TestUtils.toJson(inPhysicalEquipmentService.getByInventory(context.getInventoryId()), "inventoryId", "taskId", "id", "creationDate", "lastUpdateDate");
                    case "3-virtual_equipments.json" ->
                            TestUtils.toJson(inVirtualEquipmentService.getByInventory(context.getInventoryId()), "inventoryId", "taskId", "id", "creationDate", "lastUpdateDate");
                    case "4-applications.json" ->
                            TestUtils.toJson(inApplicationService.getByInventory(context.getInventoryId()), "inventoryId", "taskId", "id", "creationDate", "lastUpdateDate");
                    default -> "[]";
                };
                var expected = TestUtils.formatJson(Files.readString(outputPath.resolve(file.getName())), "inventoryId", "taskId", "id");

                if (actual.equals(expected)) {
                    log.info("*{}* - OK Assert file {}", testCase, "output/" + file.getName());
                } else {
                    allOk = false;
                    log.error("*{}* - KO: Assert file {}", testCase, "output/" + file.getName());
                    System.out.println("ACTUAL: ");
                    System.out.println(actual);
                    System.out.println("EXPECTED: ");
                    System.out.println(expected);
                }
            }

            Path zipDirPath = Path.of("target/local-filesystem").resolve("SUBSCRIBER").resolve(String.valueOf(organization.getId())).resolve("output").resolve(String.valueOf(task.getId()));
            Path zipPath = zipDirPath.resolve(Constants.REJECTED_FILES_ZIP);

            if (Files.exists(zipPath)) {
                ZipUtil.unpack(zipPath.toFile(), zipDirPath.resolve("out").toFile());
                File[] rejectFiles = zipDirPath.resolve("out").toFile().listFiles();
                for (File file : rejectFiles) {
                    var actual = Files.readString(file.toPath()).replaceAll("\r\n", "\n");
                    var expected = Files.readString(apiloadinputfiles.resolve(testCase).resolve("rejects").resolve(file.getName())).replaceAll("\r\n", "\n");
                    if (actual.equals(expected)) {
                        log.info("*{}* - OK Assert file {}", testCase, "rejects/" + file.getName());
                    } else {
                        allOk = false;
                        log.error("*{}* - KO: Assert file {}", testCase, "rejects/" + file.getName());
                        System.out.println("ACTUAL: ");
                        System.out.println(actual);
                        System.out.println("EXPECTED: ");
                        System.out.println(expected);
                        if (SHOW_ASSERTION) Assertions.assertEquals(expected, actual);
                    }
                }
            } else {
                log.info("*{}* does not have rejects", testCase);
            }

            FileSystemUtils.deleteRecursively(zipPath);
        }

        if (!allOk) {
            Assertions.fail("LoadInputFiles - At least one test case has fail, please check the logs in the console");
        }


        /*
         * EVALUATING FUNCTIONAL TESTS
         */
        final Path targetExportFiles = Path.of("target/local-filesystem").resolve(SUBSCRIBER).resolve(String.valueOf(organization.getId())).resolve("export");
        FileSystemUtils.deleteRecursively(targetExportFiles);

        for (File testFolder : Arrays.stream(Objects.requireNonNull(apievaluating.toFile().listFiles())).sorted().toList()) {
            // PREPARE
            var testCase = testFolder.getName();

            // clean tables
            inDatacenterRepository.deleteAll();
            inPhysicalEquipmentRepository.deleteAll();
            inVirtualEquipmentRepository.deleteAll();
            inApplicationRepository.deleteAll();
            taskRepository.deleteAll();

            // copy files in work
            File inputFolder = apievaluating.resolve(testCase).resolve("input").toFile();
            List<String> filenames = new ArrayList<>();

            for (File f : Objects.requireNonNull(inputFolder.listFiles())) {
                String targetFilename = f.getName().replace(".csv", ".csv_" + UUID.randomUUID() + ".csv");
                Files.copy(f.toPath(), targetInputFiles.resolve(targetFilename));
                filenames.add(targetFilename);
            }

            File refFolder = apievaluating.resolve(testCase).resolve("ref").toFile();
            for (File f : Objects.requireNonNull(refFolder.listFiles())) {
                MultipartFile multipartFile = new MockMultipartFile("file", f.getName(), "text/csv", Files.readAllBytes(f.toPath()));
                referentialImportService.importReferentialCSV(f.getName().replace(".csv", ""), multipartFile, null);
            }

            // EXECUTE LOADING
            var taskLoading = taskRepository.save(TestUtils.createTask(context, filenames, TaskType.LOADING, null, inventory));
            asyncLoadFilesService.execute(context, taskLoading);

            // EXECUTE EVALUATING
            var taskEvaluating = taskRepository.save(TestUtils.createTask(context, null, TaskType.EVALUATING, List.of("CLIMATE_CHANGE"), inventory));
            asyncEvaluatingService.execute(context, taskEvaluating);

            // ASSERT
            Path outputPath = apievaluating.resolve(testCase).resolve("output");

            for (File file : Objects.requireNonNull(outputPath.toFile().listFiles())) {
                var actual = switch (file.getName()) {
                    case "1-out-physical-equipments.json" ->
                            TestUtils.toJson(outPhysicalEquipmentRepository.findAll(), "inventoryId", "taskId", "id");
                    case "2-out-virtual-equipments.json" ->
                            TestUtils.toJson(outVirtualEquipmentRepository.findAll(Sort.by(Sort.Direction.ASC, "unitImpact")), "inventoryId", "taskId", "id");
                    case "3-out-applications.json" ->
                            TestUtils.toJson(outApplicationRepository.findAll(Sort.by(Sort.Direction.ASC, "unitImpact")), "inventoryId", "taskId", "id");
                    default -> "[]";
                };
                var expected = TestUtils.formatJson(Files.readString(outputPath.resolve(file.getName())), "inventoryId", "taskId", "id");

                if (actual.equals(expected)) {
                    log.info("*{}* - OK Assert file {}", testCase, "output/" + file.getName());
                } else {
                    allOk = false;
                    log.error("*{}* - KO: Assert file {}", testCase, "output/" + file.getName());
                    System.out.println("ACTUAL: ");
                    System.out.println(actual);
                    System.out.println("EXPECTED: ");
                    System.out.println(expected);
                }
            }

            Path zipDirPath = Path.of("target/local-filesystem").resolve("SUBSCRIBER").resolve(String.valueOf(organization.getId())).resolve("export");
            Path zipPath = zipDirPath.resolve(taskEvaluating.getId() + Constants.ZIP);

            if (Files.exists(zipPath)) {
                ZipUtil.unpack(zipPath.toFile(), zipDirPath.resolve("out").toFile());
                File[] rejectFiles = zipDirPath.resolve("out").toFile().listFiles();
                for (File file : rejectFiles) {
                    var actual = Files.readString(file.toPath()).replaceAll("\r\n", "\n");
                    var expected = Files.readString(apievaluating.resolve(testCase).resolve("export").resolve(file.getName())).replaceAll("\r\n", "\n");
                    if (actual.equals(expected)) {
                        log.info("*{}* - OK Assert file {}", testCase, "export/" + file.getName());
                    } else {
                        allOk = false;
                        log.error("*{}* - KO: Assert file {}", testCase, "export/" + file.getName());
                        System.out.println("ACTUAL: ");
                        System.out.println(actual);
                        System.out.println("EXPECTED: ");
                        System.out.println(expected);
                        if (SHOW_ASSERTION) Assertions.assertEquals(expected, actual);
                    }
                }
            } else {
                log.info("*{}* does not have exports", testCase);
            }
        }

        if (!allOk) {
            Assertions.fail("Evaluating - At least one test case has fail, please check the logs in the console");
        }

        log.info("Virtual equipements {}", checkVirtualEquipmentRepository.findAll());

    }

    private InPhysicalEquipment createAlreadyExistingPEinInventory(Inventory inv, Long OrganizationId) {

        InPhysicalEquipment inPE = new InPhysicalEquipment();
        inPE.setName("MyMagicalPE");
        inPE.setInventoryId(inv.getId());
        inPE.setCreationDate(LocalDateTime.of(2022, 1, 1, 0, 0, 0));
        inPE.setId(-1L);
        inPE.setQuantity(12d);
        inPE.setCpuCoreNumber(2d);
        inPE.setSizeDiskGb(2353d);
        inPE.setSizeMemoryGb(234d);
        inPE.setElectricityConsumption(234d);
        inPE.setCpuType("cpuType");
        inPE.setCpuCoreNumber(2d);
        inPE.setDatacenterName("default");
        inPE.setDatePurchase(LocalDate.of(2020, 1, 1));
        inPE.setDescription("OldPE");
        inPE.setDurationHour(24.0);
        inPE.setLocation("France");
        inPE.setLastUpdateDate(LocalDateTime.of(2022, 1, 1, 0, 0, 0));

        inPE.setModel("model");
        inPE.setManufacturer("manufacturer");
        inPE.setType("type");

        return inPhysicalEquipmentRepository.save(inPE);
    }

    private InVirtualEquipment createAlreadyExistingVEinInventory(Inventory inv, Long OrganizationId) {


        InVirtualEquipment inVe = new InVirtualEquipment();

        inVe.setId(-1l);
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
        inVe.setInfrastructureType("infrastructureType");

        return inVirtualEquipmentRepository.save(inVe);


    }


}
