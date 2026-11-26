// This file has been created (totally or partially) with the assistance of artificial intelligence tools.
// All content has been generated under the direct supervision of a named individual,
// and under the AI.Backbone Orchestrator Compliance framework

package com.soprasteria.g4it.backend.apiindicator.controller;

import com.soprasteria.g4it.backend.apifiles.business.FileSystemService;
import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.ExportService;
import com.soprasteria.g4it.backend.apievaluating.mapper.ImpactToCsvRecord;
import com.soprasteria.g4it.backend.apiinout.mapper.InputToCsvRecord;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.PhysicalEquipmentEvaluationTrace;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiuser.business.AuthService;
import com.soprasteria.g4it.backend.apiuser.business.RoleService;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.Role;
import com.soprasteria.g4it.backend.apiuser.modeldb.UserWorkspace;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserWorkspaceRepository;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.filesystem.business.FileStorage;
import com.soprasteria.g4it.backend.common.filesystem.business.FileSystem;
import com.soprasteria.g4it.backend.common.filesystem.business.local.LocalFileService;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.mapstruct.factory.Mappers;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementPhysique;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryIndicatorExportControllerTest {
    @Mock
    private TaskRepository tasks;
    @Mock
    private FileSystemService files;
    @Mock
    private AuthService auth;
    @Mock
    private RoleService roles;
    @Mock
    private OrganizationRepository organizations;
    @Mock
    private UserWorkspaceRepository userWorkspaces;

    @InjectMocks
    private InventoryIndicatorController controller;

    private final UserBO user = UserBO.builder().id(5L).build();

    @BeforeEach
    void setUp() {
        when(tasks.findByInventoryAndLastCreationDate(any(Inventory.class)))
                .thenReturn(Optional.of(Task.builder().id(42L).build()));
        when(auth.getUser()).thenReturn(user);
        when(organizations.findByName("synthetic-org"))
                .thenReturn(Optional.of(Organization.builder().id(3L).build()));
    }

    @Test
    void authorizedWriterDownloadsTheLatestInventoryTaskExport() throws Exception {
        when(userWorkspaces.findByWorkspaceIdAndUserId(7L, 5L)).thenReturn(Optional.of(
                UserWorkspace.builder().workspace(Workspace.builder().name("workspace").build())
                        .roles(List.of(Role.builder().name("ROLE_INVENTORY_WRITE").build())).build()));
        byte[] expected = "synthetic zip content".getBytes(StandardCharsets.UTF_8);
        when(files.downloadFile("synthetic-org", 7L, FileFolder.EXPORT, "42.zip"))
                .thenReturn(new ByteArrayInputStream(expected));

        var response = controller.getIndicatorsExportResult("synthetic-org", 7L, 9L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertArrayEquals(expected, response.getBody().getInputStream().readAllBytes());
        verify(tasks).findByInventoryAndLastCreationDate(argThat(inventory -> inventory.getId().equals(9L)));
        verify(files).downloadFile("synthetic-org", 7L, FileFolder.EXPORT, "42.zip");
    }

    @Test
    void userWithoutInventoryWriteCannotDownloadTheExport() {
        when(userWorkspaces.findByWorkspaceIdAndUserId(7L, 5L)).thenReturn(Optional.of(
                UserWorkspace.builder().workspace(Workspace.builder().name("workspace").build())
                        .roles(List.of(Role.builder().name("ROLE_INVENTORY_READ").build())).build()));

        var error = assertThrows(G4itRestException.class,
                () -> controller.getIndicatorsExportResult("synthetic-org", 7L, 9L));

        assertEquals("403", error.getCode());
        verifyNoInteractions(files);
    }

    @Test
    void administratorCanDownloadWithoutWorkspaceMembership() throws Exception {
        when(roles.hasAdminRightOnOrganizationOrWorkspace(user, 3L, 7L)).thenReturn(true);
        when(files.downloadFile("synthetic-org", 7L, FileFolder.EXPORT, "42.zip"))
                .thenReturn(new ByteArrayInputStream(new byte[0]));

        assertEquals(HttpStatus.OK, controller.getIndicatorsExportResult("synthetic-org", 7L, 9L).getStatusCode());
        verifyNoInteractions(userWorkspaces);
    }

    @Test
    void downloadedTaskZipRetainsSourceAndEvaluatedUsage(@TempDir Path tempDir) throws Exception {
        when(roles.hasAdminRightOnOrganizationOrWorkspace(user, 3L, 7L)).thenReturn(true);
        var storage = mock(FileStorage.class);
        var fileSystem = mock(FileSystem.class);
        when(fileSystem.mount("synthetic-org", "7")).thenReturn(storage);
        var exportService = new ExportService();
        ReflectionTestUtils.setField(exportService, "localWorkingFolder", tempDir.toString());
        ReflectionTestUtils.setField(exportService, "localFileService", new LocalFileService());
        ReflectionTestUtils.setField(exportService, "fileSystem", fileSystem);
        var equipment = InPhysicalEquipment.builder().id(12L).name("synthetic-device")
                .quantity(2.5d).electricityConsumption(125d).usageContext("COPE")
                .usageFraction("0.9000").build();
        var trace = PhysicalEquipmentEvaluationTrace.builder().taskId(42L).sourceEquipmentId(12L)
                .itemCategory("DEVICES").usageContext("BYOD").effectiveFraction(0.57d).build();
        var context = Context.builder().workspaceId(7L).datetime(LocalDateTime.of(2026, 11, 24, 12, 0)).build();
        var impact = ImpactEquipementPhysique.builder().critere("CLIMATE_CHANGE").etapeACV("USING")
                .statutIndicateur("OK").trace("").typeEquipement("laptop").unite("kg CO2 eq").build();
        var source = Mappers.getMapper(InputToCsvRecord.class).toCsv(equipment, null);
        var evaluated = Mappers.getMapper(ImpactToCsvRecord.class)
                .toCsv(context, 42L, "synthetic-inventory", equipment, impact, 1d, true, trace);
        Path exportPath = exportService.createExportDirectory(42L);
        try (var sourcePrinter = new CSVPrinter(Files.newBufferedWriter(exportPath.resolve("physical_equipment.csv")),
                CSVFormat.DEFAULT);
             var evaluatedPrinter = new CSVPrinter(Files.newBufferedWriter(exportPath.resolve("ind_physical_equipment.csv")),
                     CSVFormat.DEFAULT)) {
            sourcePrinter.printRecord(source);
            evaluatedPrinter.printRecord(evaluated);
        }
        var uploaded = new AtomicReference<byte[]>();
        doAnswer(invocation -> {
            uploaded.set(Files.readAllBytes(Path.of(invocation.getArgument(0, String.class))));
            return null;
        }).when(storage).upload(anyString(), eq(FileFolder.EXPORT), eq("42.zip"));
        exportService.uploadExportZip(42L, "synthetic-org", "7");
        assertNotNull(uploaded.get());
        when(files.downloadFile("synthetic-org", 7L, FileFolder.EXPORT, "42.zip"))
                .thenReturn(new ByteArrayInputStream(uploaded.get()));

        var response = controller.getIndicatorsExportResult("synthetic-org", 7L, 9L);

        assertNotNull(response.getBody());
        try (var zip = new ZipInputStream(response.getBody().getInputStream())) {
            boolean sourceFound = false;
            boolean evaluatedFound = false;
            for (var entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                var records = CSVFormat.DEFAULT.parse(
                        new java.io.StringReader(new String(zip.readAllBytes(), StandardCharsets.UTF_8))).getRecords();
                if (entry.getName().equals("physical_equipment.csv")) {
                    assertEquals("COPE", records.getFirst().get(20));
                    assertEquals("0.9000", records.getFirst().get(21));
                    sourceFound = true;
                } else if (entry.getName().equals("ind_physical_equipment.csv")) {
                    assertEquals("BYOD", records.getFirst().get(26));
                    assertEquals("0.57", records.getFirst().get(27));
                    evaluatedFound = true;
                }
            }
            assertTrue(sourceFound);
            assertTrue(evaluatedFound);
        }
    }
}
