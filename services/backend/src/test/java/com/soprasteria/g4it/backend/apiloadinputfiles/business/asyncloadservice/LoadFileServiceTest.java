/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice;

import com.soprasteria.g4it.backend.apiinout.modeldb.InApplication;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InApplicationRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadobject.LoadApplicationService;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadobject.LoadDatacenterService;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadobject.LoadPhysicalEquipmentService;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadobject.LoadVirtualEquipmentService;
import com.soprasteria.g4it.backend.apiloadinputfiles.mapper.CsvToInMapper;
import com.soprasteria.g4it.backend.common.filesystem.model.CsvFileMapperInfo;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.common.utils.CsvUtils;
import com.soprasteria.g4it.backend.exception.AsyncTaskException;
import com.soprasteria.g4it.backend.server.gen.api.dto.InDatacenterRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InVirtualEquipmentRest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LoadFileServiceTest {

    @Mock
    CsvFileMapperInfo csvFileMapperInfo;
    @Mock
    MessageSource messageSource;
    @Mock
    LoadDatacenterService loadDatacenterService;
    @Mock
    LoadPhysicalEquipmentService loadPhysicalEquipmentService;
    @Mock
    LoadVirtualEquipmentService loadVirtualEquipmentService;
    @Mock
    LoadApplicationService loadApplicationService;
    @Mock
    CsvToInMapper csvToInMapper;
    @Mock
    InventoryRepository inventoryRepository;
    @Mock
    InVirtualEquipmentRepository inVirtualEquipmentRepository;
    @Mock
    InApplicationRepository inApplicationRepository;

    @InjectMocks
    private LoadFileService loadFileService;

    @Mock
    private Context context;

    @Mock
    private FileToLoad fileToLoad;

    @TempDir
    Path tempDir;

    private Path tempCsv;

    @BeforeEach
    void setUp() throws Exception {
        tempCsv = Files.createTempFile(tempDir, "converted-", ".csv");

        // make common stubbings lenient to avoid UnnecessaryStubbingException
        lenient().when(fileToLoad.getOriginalFileName()).thenReturn("original.csv");
        lenient().when(context.getInventoryId()).thenReturn(123L);
        lenient().when(context.getDatetime()).thenReturn(LocalDateTime.now());
        lenient().when(context.log()).thenReturn("CTX");
        lenient().when(context.getLocale()).thenReturn(Locale.ENGLISH);
        lenient().when(context.getFilesToLoad()).thenReturn(List.of(fileToLoad));
        lenient().when(fileToLoad.getFileType()).thenReturn(FileType.DATACENTER);

        // set private localWorkingFolder to a temp folder so writeRejected writes there
        Field f = LoadFileService.class.getDeclaredField("localWorkingFolder");
        f.setAccessible(true);
        f.set(loadFileService, tempDir.toString());

        // ensure rejected root exists (initFolder is called in real app; tests ensure folder is present)
        Files.createDirectories(tempDir.resolve("rejected"));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (tempCsv != null) {
            Files.deleteIfExists(tempCsv);
        }
    }

    @Test
    void manageFile_convertedFileNotFound_throwsAsyncTaskException() {
        File missing = tempDir.resolve("does-not-exist.csv").toFile();
        when(fileToLoad.getConvertedFile()).thenReturn(missing);

        assertThrows(AsyncTaskException.class, () -> loadFileService.manageFile(context, fileToLoad));
    }

    @Test
    void manageFile_unknownHeader_returnsLocalizedError() throws Exception {
        String delim = CsvUtils.DELIMITER;
        Files.writeString(tempCsv, String.join(delim, List.of("id", "unknown")) + System.lineSeparator());

        when(fileToLoad.getConvertedFile()).thenReturn(tempCsv.toFile());
        when(fileToLoad.getFileType()).thenReturn(FileType.DATACENTER);

        when(csvFileMapperInfo.getHeaderFields(FileType.DATACENTER, false)).thenReturn(new HashSet<>(List.of("id")));
        when(messageSource.getMessage(eq("header.unknown"), any(Object[].class), eq(Locale.ENGLISH)))
                .thenReturn("Unknown header message");

        List<String> errors = loadFileService.manageFile(context, fileToLoad);

        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertEquals("Unknown header message", errors.get(0));
        verify(messageSource).getMessage(eq("header.unknown"), any(Object[].class), eq(Locale.ENGLISH));
    }

    /*@Test
    void manageFile_datacenter_withReadErrors_writesRejectedFile_and_returnsNoHeaderErrors() throws Exception {
        String delim = CsvUtils.DELIMITER;
        Files.writeString(tempCsv, String.join(delim, List.of("id", "name")) + System.lineSeparator() + "1" + delim + "A");

        when(fileToLoad.getConvertedFile()).thenReturn(tempCsv.toFile());
        when(fileToLoad.getFileType()).thenReturn(FileType.DATACENTER);

        when(csvFileMapperInfo.getHeaderFields(FileType.DATACENTER, false)).thenReturn(new HashSet<>(List.of("id", "name")));
        when(csvToInMapper.csvInDatacenterToRest(any(), anyLong(), any()))
                .thenReturn(new InDatacenterRest());

        LineError le = new LineError(2, "bad line");
        when(loadDatacenterService.execute(eq(context), eq(fileToLoad), anyInt(), anyList()))
                .thenReturn(Collections.singletonList(le));

        List<String> errors = loadFileService.manageFile(context, fileToLoad);

        assertNotNull(errors);
        assertTrue(errors.isEmpty());

        Path rejectedDir = tempDir.resolve("rejected").resolve(String.valueOf(context.getInventoryId()));
        assertTrue(Files.exists(rejectedDir), "rejected directory must exist");

        boolean rejectedFileExists = Files.list(rejectedDir)
                .anyMatch(p -> p.getFileName().toString().startsWith("rejected_") && p.getFileName().toString().endsWith(".csv"));
        assertTrue(rejectedFileExists, "a rejected CSV file should have been created");
    }*/

    @Test
    void manageFile_virtualEquipmentRouting_callsVirtualLoader() throws Exception {
        String delim = CsvUtils.DELIMITER;
        Files.writeString(tempCsv, String.join(delim, List.of("name", "physicalEquipmentName")) + System.lineSeparator() + "VE1" + delim + "PE1");

        when(fileToLoad.getConvertedFile()).thenReturn(tempCsv.toFile());
        when(fileToLoad.getFileType()).thenReturn(FileType.EQUIPEMENT_VIRTUEL);
        when(csvFileMapperInfo.getHeaderFields(FileType.EQUIPEMENT_VIRTUEL, false))
                .thenReturn(new HashSet<>(List.of("name", "physicalEquipmentName")));
        when(csvToInMapper.csvInVirtualEquipmentToRest(any(), anyLong(), any()))
                .thenReturn(new InVirtualEquipmentRest());
        when(loadVirtualEquipmentService.execute(eq(context), eq(fileToLoad), anyInt(), anyList()))
                .thenReturn(Collections.emptyList());

        List<String> errors = loadFileService.manageFile(context, fileToLoad);

        assertNotNull(errors);
        assertTrue(errors.isEmpty());
        verify(loadVirtualEquipmentService).execute(eq(context), eq(fileToLoad), anyInt(), anyList());
    }

    @Test
    void mandatoryHeadersCheck_allHeadersPresent_returnsEmptyList() throws Exception {
        String delim = CsvUtils.DELIMITER;
        Set<String> mandatory = new LinkedHashSet<>(List.of("id", "name", "location"));
        Files.writeString(tempCsv, String.join(delim, mandatory) + System.lineSeparator() + "1" + delim + "A" + delim + "L");

        when(fileToLoad.getConvertedFile()).thenReturn(tempCsv.toFile());
        when(fileToLoad.getFileType()).thenReturn(FileType.DATACENTER);
        when(csvFileMapperInfo.getHeaderFields(FileType.DATACENTER, true)).thenReturn(new HashSet<>(mandatory));
        when(context.getFilesToLoad()).thenReturn(List.of(fileToLoad));

        List<String> errors = loadFileService.mandatoryHeadersCheck(context);

        assertNotNull(errors);
        assertTrue(errors.isEmpty());
        verify(messageSource, never()).getMessage(any(), any(), any());
    }

    @Test
    void mandatoryHeadersCheck_missingHeader_returnsLocalizedMessage() throws Exception {
        String delim = CsvUtils.DELIMITER;
        List<String> present = List.of("id", "name");
        Set<String> mandatory = new LinkedHashSet<>(List.of("id", "name", "location"));
        Files.writeString(tempCsv, String.join(delim, present) + System.lineSeparator());

        when(fileToLoad.getConvertedFile()).thenReturn(tempCsv.toFile());
        when(fileToLoad.getFileType()).thenReturn(FileType.DATACENTER);
        when(csvFileMapperInfo.getHeaderFields(FileType.DATACENTER, true)).thenReturn(new HashSet<>(mandatory));
        when(context.getFilesToLoad()).thenReturn(List.of(fileToLoad));
        when(messageSource.getMessage(eq("header.mandatory"), any(Object[].class), eq(Locale.ENGLISH)))
                .thenReturn("Missing headers");

        List<String> errors = loadFileService.mandatoryHeadersCheck(context);

        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertEquals("Missing headers", errors.get(0));
        verify(messageSource).getMessage(eq("header.mandatory"), any(Object[].class), eq(Locale.ENGLISH));
    }

    @Test
    void mandatoryHeadersCheck_convertedFileNotFound_throwsAsyncTaskException() {
        when(fileToLoad.getConvertedFile()).thenReturn(Path.of("nonexistent-file.csv").toFile());
        when(context.getFilesToLoad()).thenReturn(List.of(fileToLoad));
        when(context.log()).thenReturn("LOG_CONTEXT");

        assertThrows(AsyncTaskException.class, () -> loadFileService.mandatoryHeadersCheck(context));
    }

   /* @Test
    void setInventoryCounts_updatesCountsAndSavesInventory() {
        Inventory inventory = new Inventory();
        when(inventoryRepository.findById(123L)).thenReturn(Optional.of(inventory));

        when(loadDatacenterService.getDatacenterCount(123L)).thenReturn(10);
        when(loadPhysicalEquipmentService.getPhysicalEquipmentCount(123L)).thenReturn(20);
        when(loadVirtualEquipmentService.getVirtualEquipmentCount(123L)).thenReturn(30);
        when(loadApplicationService.getApplicationCount(123L)).thenReturn(40);

        loadFileService.setInventoryCounts(123L);

        verify(inventoryRepository).save(inventory);
        assertEquals(10, inventory.getDataCenterCount());
        assertEquals(20, inventory.getPhysicalEquipmentCount());
        assertEquals(30, inventory.getVirtualEquipmentCount());
        assertEquals(40, inventory.getApplicationCount());
    }*/

    @Test
    void linkApplicationsToVirtualEquipments_linksAndSaves() {
        Long inventoryId = 55L;

        InVirtualEquipment ve = new InVirtualEquipment();
        ve.setName("VE1");
        ve.setPhysicalEquipmentName("PE1");

        InApplication app = new InApplication();
        app.setVirtualEquipmentName("VE1");
        app.setPhysicalEquipmentName(null);

        when(inVirtualEquipmentRepository.findByInventoryId(inventoryId)).thenReturn(List.of(ve));
        when(inApplicationRepository.findByInventoryIdAndPhysicalEquipmentNameIsNull(inventoryId)).thenReturn(List.of(app));

        loadFileService.linkApplicationsToVirtualEquipments(inventoryId);

        ArgumentCaptor<InApplication> captor = ArgumentCaptor.forClass(InApplication.class);
        verify(inApplicationRepository).save(captor.capture());

        InApplication saved = captor.getValue();
        assertEquals("PE1", saved.getPhysicalEquipmentName());
    }
}
