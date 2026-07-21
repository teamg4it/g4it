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
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.CsvUtils;
import com.soprasteria.g4it.backend.exception.AsyncTaskException;
import com.soprasteria.g4it.backend.server.gen.api.dto.InApplicationRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InVirtualEquipmentRest;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
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
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoadFileServiceTest {

    @Mock
    CsvFileMapperInfo csvFileMapperInfo;
    @Mock
    MessageSource messageSource;
    @SuppressWarnings("unused")
    @Mock
    LoadDatacenterService loadDatacenterService;
    @Mock
    LoadPhysicalEquipmentService loadPhysicalEquipmentService;
    @Mock
    LoadVirtualEquipmentService loadVirtualEquipmentService;
    @SuppressWarnings("unused")
    @Mock
    LoadApplicationService loadApplicationService;
    @Mock
    CsvToInMapper csvToInMapper;
    @SuppressWarnings("unused")
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
    private static final LocalDateTime referenceTime =
            LocalDateTime.of(2025, Month.JANUARY, 1, 12, 0);

    @BeforeEach
    void setUp() throws Exception {
        tempCsv = Files.createTempFile(tempDir, "converted-", ".csv");

        lenient().when(fileToLoad.getOriginalFileName()).thenReturn("original.csv");
        lenient().when(context.getInventoryId()).thenReturn(123L);
        lenient().when(context.getDatetime()).thenReturn(referenceTime);
        lenient().when(context.log()).thenReturn("CTX");
        lenient().when(context.getLocale()).thenReturn(Locale.ENGLISH);
        lenient().when(context.getFilesToLoad()).thenReturn(List.of(fileToLoad));
        lenient().when(fileToLoad.getFileType()).thenReturn(FileType.DATACENTER);

        Field f = LoadFileService.class.getDeclaredField("localWorkingFolder");
        f.setAccessible(true);
        f.set(loadFileService, tempDir.toString());

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
        assertEquals("Unknown header message", errors.getFirst());
        verify(messageSource).getMessage(eq("header.unknown"), any(Object[].class), eq(Locale.ENGLISH));
    }


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
        assertEquals("Missing headers", errors.getFirst());
        verify(messageSource).getMessage(eq("header.mandatory"), any(Object[].class), eq(Locale.ENGLISH));
    }

    @Test
    void mandatoryHeadersCheck_convertedFileNotFound_throwsAsyncTaskException() {
        when(fileToLoad.getConvertedFile()).thenReturn(Path.of("nonexistent-file.csv").toFile());
        when(context.getFilesToLoad()).thenReturn(List.of(fileToLoad));
        when(context.log()).thenReturn("LOG_CONTEXT");

        assertThrows(AsyncTaskException.class, () -> loadFileService.mandatoryHeadersCheck(context));
    }

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

    @Test
    void readPhysicalEquipments_emptyParser_executesSingleEmptyBatch() throws Exception {
        CSVParser parser = mockParserWithRows(0);
        when(loadPhysicalEquipmentService.execute(context, fileToLoad, 0, Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        List<LineError> errors = invokeReadPhysicalEquipments(parser);

        assertNotNull(errors);
        assertTrue(errors.isEmpty());
        verifyNoInteractions(csvToInMapper);
        verify(loadPhysicalEquipmentService).execute(context, fileToLoad, 0, Collections.emptyList());
    }

    @Test
    void readPhysicalEquipments_singleRecord_mapsAndLoadsSinglePage() throws Exception {
        InPhysicalEquipmentRest mapped = new InPhysicalEquipmentRest();

        CSVParser parser = mockParserWithRows(1);
        when(csvToInMapper.csvInPhysicalEquipmentToRest(any(), eq(123L), eq(null))).thenReturn(mapped);
        List<InPhysicalEquipmentRest> capturedBatch = new ArrayList<>();
        when(loadPhysicalEquipmentService.execute(eq(context), eq(fileToLoad), eq(0), anyList()))
                .thenAnswer(invocation -> {
                    capturedBatch.addAll(invocation.getArgument(3));
                    return Collections.emptyList();
                });

        List<LineError> errors = invokeReadPhysicalEquipments(parser);

        assertNotNull(errors);
        assertTrue(errors.isEmpty());
        verify(csvToInMapper, times(1)).csvInPhysicalEquipmentToRest(any(), eq(123L), eq(null));
        verify(loadPhysicalEquipmentService).execute(eq(context), eq(fileToLoad), eq(0), anyList());
        assertEquals(1, capturedBatch.size());
        assertSame(mapped, capturedBatch.getFirst());
    }

    @Test
    void readPhysicalEquipments_moreThanBatchSize_splitsPagesAndAggregatesErrors() throws Exception {
        int totalRows = Constants.BATCH_SIZE + 1;
        LineError page0Error = new LineError("physical.csv", 1, "page 0 error");
        LineError page1Error = new LineError("physical.csv", 2, "page 1 error");

        CSVParser parser = mockParserWithRows(totalRows);
        when(csvToInMapper.csvInPhysicalEquipmentToRest(any(), eq(123L), eq(null)))
                .thenAnswer(invocation -> new InPhysicalEquipmentRest());
        List<List<InPhysicalEquipmentRest>> allBatches = new ArrayList<>();
        when(loadPhysicalEquipmentService.execute(eq(context), eq(fileToLoad), anyInt(), anyList()))
                .thenAnswer(invocation -> {
                    allBatches.add(new ArrayList<>(invocation.getArgument(3)));
                    Integer pageNumber = invocation.getArgument(2);
                    return pageNumber == 0 ? List.of(page0Error) : List.of(page1Error);
                });

        List<LineError> errors = invokeReadPhysicalEquipments(parser);

        assertEquals(List.of(page0Error, page1Error), errors);
        verify(csvToInMapper, times(totalRows)).csvInPhysicalEquipmentToRest(any(), eq(123L), eq(null));
        verify(loadPhysicalEquipmentService, times(2)).execute(eq(context), eq(fileToLoad), anyInt(), anyList());
        assertEquals(2, allBatches.size());
        assertEquals(Constants.BATCH_SIZE, allBatches.get(0).size());
        assertEquals(1, allBatches.get(1).size());
        verify(loadPhysicalEquipmentService).execute(eq(context), eq(fileToLoad), eq(0), anyList());
        verify(loadPhysicalEquipmentService).execute(eq(context), eq(fileToLoad), eq(1), anyList());
    }

    @Test
    void readApplications_emptyParser_executesSingleEmptyBatch() throws Exception {
        CSVParser parser = mockParserWithRows(0);
        when(loadApplicationService.execute(context, fileToLoad, 0, Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        List<LineError> errors = invokeReadApplications(parser);

        assertNotNull(errors);
        assertTrue(errors.isEmpty());
        verify(loadApplicationService).execute(context, fileToLoad, 0, Collections.emptyList());
        verify(csvToInMapper, never()).csvInApplicationToRest(any(), anyLong());
    }

    @Test
    void readApplications_singleRecord_mapsAndLoadsSinglePage() throws Exception {
        InApplicationRest mapped = new InApplicationRest();

        CSVParser parser = mockParserWithRows(1);
        when(csvToInMapper.csvInApplicationToRest(any(), eq(123L))).thenReturn(mapped);
        List<InApplicationRest> capturedBatch = new ArrayList<>();
        when(loadApplicationService.execute(eq(context), eq(fileToLoad), eq(0), anyList()))
                .thenAnswer(invocation -> {
                    capturedBatch.addAll(invocation.getArgument(3));
                    return Collections.emptyList();
                });

        List<LineError> errors = invokeReadApplications(parser);

        assertNotNull(errors);
        assertTrue(errors.isEmpty());
        verify(csvToInMapper).csvInApplicationToRest(any(), eq(123L));
        verify(loadApplicationService).execute(eq(context), eq(fileToLoad), eq(0), anyList());
        assertEquals(1, capturedBatch.size());
        assertSame(mapped, capturedBatch.getFirst());
    }

    @Test
    void readApplications_moreThanBatchSize_splitsPagesAndAggregatesErrors() throws Exception {
        int totalRows = Constants.BATCH_SIZE + 1;
        LineError page0Error = new LineError("application.csv", 1, "page 0 error");
        LineError page1Error = new LineError("application.csv", 2, "page 1 error");

        CSVParser parser = mockParserWithRows(totalRows);
        when(csvToInMapper.csvInApplicationToRest(any(), eq(123L)))
                .thenAnswer(invocation -> new InApplicationRest());
        List<List<InApplicationRest>> allBatches = new ArrayList<>();
        when(loadApplicationService.execute(eq(context), eq(fileToLoad), anyInt(), anyList()))
                .thenAnswer(invocation -> {
                    allBatches.add(new ArrayList<>(invocation.getArgument(3)));
                    Integer pageNumber = invocation.getArgument(2);
                    return pageNumber == 0 ? List.of(page0Error) : List.of(page1Error);
                });

        List<LineError> errors = invokeReadApplications(parser);

        assertEquals(List.of(page0Error, page1Error), errors);
        verify(csvToInMapper, times(totalRows)).csvInApplicationToRest(any(), eq(123L));
        verify(loadApplicationService, times(2)).execute(eq(context), eq(fileToLoad), anyInt(), anyList());
        assertEquals(2, allBatches.size());
        assertEquals(Constants.BATCH_SIZE, allBatches.getFirst().size());
        assertEquals(1, allBatches.get(1).size());
        verify(loadApplicationService).execute(eq(context), eq(fileToLoad), eq(0), anyList());
        verify(loadApplicationService).execute(eq(context), eq(fileToLoad), eq(1), anyList());
    }

    private List<LineError> invokeReadPhysicalEquipments(CSVParser parser) throws Exception {
        Method method = LoadFileService.class.getDeclaredMethod("readPhysicalEquipments", Context.class, FileToLoad.class, CSVParser.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<LineError> result = (List<LineError>) method.invoke(loadFileService, context, fileToLoad, parser);
        return result;
    }

    private List<LineError> invokeReadApplications(CSVParser parser) throws Exception {
        Method method = LoadFileService.class.getDeclaredMethod("readApplications", Context.class, FileToLoad.class, CSVParser.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<LineError> result = (List<LineError>) method.invoke(loadFileService, context, fileToLoad, parser);
        return result;
    }

    private CSVParser mockParserWithRows(int rowCount) {
        CSVParser parser = mock(CSVParser.class);
        List<CSVRecord> records = new ArrayList<>(rowCount);
        for (int i = 0; i < rowCount; i++) {
            records.add(mock(CSVRecord.class));
        }
        when(parser.iterator()).thenReturn(records.iterator());
        return parser;
    }
}
