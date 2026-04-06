/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadmetadata;

import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadmetadata.loaders.LoadApplicationMetadataService;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadmetadata.loaders.LoadDatacenterMetadataService;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadmetadata.loaders.LoadPhysicalEquipmentMetadataService;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadmetadata.loaders.LoadVirtualEquipmentMetadataService;
import com.soprasteria.g4it.backend.apiloadinputfiles.mapper.CsvToInMapper;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.exception.AsyncTaskException;
import com.soprasteria.g4it.backend.server.gen.api.dto.InApplicationRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InDatacenterRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InVirtualEquipmentRest;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoadMetadataServiceTest {

    @InjectMocks
    private LoadMetadataService loadMetadataService;

    @Mock
    private CsvToInMapper csvToInMapper;
    @Mock
    private LoadDatacenterMetadataService loadDatacenterMetadataService;
    @Mock
    private LoadVirtualEquipmentMetadataService loadVirtualEquipmentMetadataService;
    @Mock
    private LoadPhysicalEquipmentMetadataService loadPhysicalEquipmentMetadataService;
    @Mock
    private LoadApplicationMetadataService loadApplicationMetadataService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadMetadataFile_Datacenter(@TempDir java.nio.file.Path tempDir) throws Exception {
        // Arrange
        File tempFile = tempDir.resolve("test.csv").toFile();
        try (PrintWriter out = new PrintWriter(tempFile)) {
            out.println("header1,header2");
            out.println("value1,value2");
        }
        FileToLoad fileToLoad = mock(FileToLoad.class);
        when(fileToLoad.getFilename()).thenReturn("test.csv");
        when(fileToLoad.getFileType()).thenReturn(FileType.DATACENTER);
        when(fileToLoad.getConvertedFile()).thenReturn(tempFile);

        Context context = mock(Context.class);
        when(context.log()).thenReturn("context-log");
        when(context.getInventoryId()).thenReturn(1L);
        when(context.getDigitalServiceVersionUid()).thenReturn("v1");

        InDatacenterRest mappedObj = new InDatacenterRest();
        when(csvToInMapper.csvInDatacenterToRest(any(), anyLong(), anyString())).thenReturn(mappedObj);

        doNothing().when(loadDatacenterMetadataService).execute(any(), any(), anyInt(), anyList());

        // Act & Assert
        assertDoesNotThrow(() -> loadMetadataService.loadMetadataFile(fileToLoad, context));
        verify(loadDatacenterMetadataService, atLeastOnce()).execute(any(), any(), anyInt(), anyList());
    }

    @Test
    void testLoadMetadataFile_PhysicalEquipment(@TempDir java.nio.file.Path tempDir) throws Exception {
        // Arrange
        File tempFile = tempDir.resolve("test.csv").toFile();
        try (PrintWriter out = new PrintWriter(tempFile)) {
            out.println("header1,header2");
            out.println("value1,value2");
        }
        FileToLoad fileToLoad = mock(FileToLoad.class);
        when(fileToLoad.getFilename()).thenReturn("test.csv");
        when(fileToLoad.getFileType()).thenReturn(FileType.EQUIPEMENT_PHYSIQUE);
        when(fileToLoad.getConvertedFile()).thenReturn(tempFile);

        Context context = mock(Context.class);
        when(context.log()).thenReturn("context-log");
        when(context.getInventoryId()).thenReturn(1L);
        when(context.getDigitalServiceVersionUid()).thenReturn("v1");

        when(csvToInMapper.csvInPhysicalEquipmentToRest(any(), anyLong(), anyString())).thenReturn(new InPhysicalEquipmentRest());
        doNothing().when(loadPhysicalEquipmentMetadataService).execute(any(), any(), anyInt(), anyList());

        // Act & Assert
        assertDoesNotThrow(() -> loadMetadataService.loadMetadataFile(fileToLoad, context));
        verify(loadPhysicalEquipmentMetadataService, atLeastOnce()).execute(any(), any(), anyInt(), anyList());
    }

    @Test
    void testLoadMetadataFile_VirtualEquipment(@TempDir java.nio.file.Path tempDir) throws Exception {
        // Arrange
        File tempFile = tempDir.resolve("test.csv").toFile();
        try (PrintWriter out = new PrintWriter(tempFile)) {
            out.println("header1,header2");
            out.println("value1,value2");
        }
        FileToLoad fileToLoad = mock(FileToLoad.class);
        when(fileToLoad.getFilename()).thenReturn("test.csv");
        when(fileToLoad.getFileType()).thenReturn(FileType.EQUIPEMENT_VIRTUEL);
        when(fileToLoad.getConvertedFile()).thenReturn(tempFile);

        Context context = mock(Context.class);
        when(context.log()).thenReturn("context-log");
        when(context.getInventoryId()).thenReturn(1L);
        when(context.getDigitalServiceVersionUid()).thenReturn("v1");

        when(csvToInMapper.csvInVirtualEquipmentToRest(any(), anyLong(), anyString())).thenReturn(new InVirtualEquipmentRest());
        doNothing().when(loadVirtualEquipmentMetadataService).execute(any(), any(), anyInt(), anyList());

        // Act & Assert
        assertDoesNotThrow(() -> loadMetadataService.loadMetadataFile(fileToLoad, context));
        verify(loadVirtualEquipmentMetadataService, atLeastOnce()).execute(any(), any(), anyInt(), anyList());
    }

    @Test
    void testLoadMetadataFile_Application(@TempDir java.nio.file.Path tempDir) throws Exception {
        // Arrange
        File tempFile = tempDir.resolve("test.csv").toFile();
        try (PrintWriter out = new PrintWriter(tempFile)) {
            out.println("header1,header2");
            out.println("value1,value2");
        }
        FileToLoad fileToLoad = mock(FileToLoad.class);
        when(fileToLoad.getFilename()).thenReturn("test.csv");
        when(fileToLoad.getFileType()).thenReturn(FileType.APPLICATION);
        when(fileToLoad.getConvertedFile()).thenReturn(tempFile);

        Context context = mock(Context.class);
        when(context.log()).thenReturn("context-log");
        when(context.getInventoryId()).thenReturn(1L);
        when(context.getDigitalServiceVersionUid()).thenReturn("v1");

        when(csvToInMapper.csvInApplicationToRest(any(), anyLong())).thenReturn(new InApplicationRest());
        doNothing().when(loadApplicationMetadataService).execute(any(), any(), anyInt(), anyList());

        // Act & Assert
        assertDoesNotThrow(() -> loadMetadataService.loadMetadataFile(fileToLoad, context));
        verify(loadApplicationMetadataService, atLeastOnce()).execute(any(), any(), anyInt(), anyList());
    }

    @Test
    void testLoadMetadataFile_IOException() {
        FileToLoad fileToLoad = mock(FileToLoad.class);
        when(fileToLoad.getFilename()).thenReturn("test.csv");
        when(fileToLoad.getFileType()).thenReturn(FileType.DATACENTER);
        when(fileToLoad.getConvertedFile()).thenReturn(new File("nonexistent.csv"));
        when(fileToLoad.getOriginalFileName()).thenReturn("test.csv");

        Context context = mock(Context.class);
        when(context.log()).thenReturn("context-log");

        assertThrows(AsyncTaskException.class, () -> loadMetadataService.loadMetadataFile(fileToLoad, context));
    }

    @Test
    void testMapCsvToInMetadataObject_Datacenter() throws Exception {
        CSVRecord csvRecord = mock(CSVRecord.class);
        when(csvToInMapper.csvInDatacenterToRest(any(), anyLong(), anyString())).thenReturn(new InDatacenterRest());

        var method = LoadMetadataService.class.getDeclaredMethod("mapCsvToInMetadataObject", CSVRecord.class, Long.class, String.class, FileType.class);
        method.setAccessible(true);
        Object result = method.invoke(loadMetadataService, csvRecord, 1L, "v1", FileType.DATACENTER);
        assertNotNull(result);
    }

    @Test
    void testMapCsvToInMetadataObject_PhysicalEquipment() throws Exception {
        CSVRecord csvRecord = mock(CSVRecord.class);
        when(csvToInMapper.csvInPhysicalEquipmentToRest(any(), anyLong(), anyString())).thenReturn(new InPhysicalEquipmentRest());
        var method = LoadMetadataService.class.getDeclaredMethod("mapCsvToInMetadataObject", CSVRecord.class, Long.class, String.class, FileType.class);
        method.setAccessible(true);
        Object result = method.invoke(loadMetadataService, csvRecord, 1L, "v1", FileType.EQUIPEMENT_PHYSIQUE);
        assertNotNull(result);
    }

    @Test
    void testMapCsvToInMetadataObject_VirtualEquipment() throws Exception {
        CSVRecord csvRecord = mock(CSVRecord.class);
        when(csvToInMapper.csvInVirtualEquipmentToRest(any(), anyLong(), anyString())).thenReturn(new InVirtualEquipmentRest());
        var method = LoadMetadataService.class.getDeclaredMethod("mapCsvToInMetadataObject", CSVRecord.class, Long.class, String.class, FileType.class);
        method.setAccessible(true);
        Object result = method.invoke(loadMetadataService, csvRecord, 1L, "v1", FileType.EQUIPEMENT_VIRTUEL);
        assertNotNull(result);
    }

    @Test
    void testMapCsvToInMetadataObject_Application() throws Exception {
        CSVRecord csvRecord = mock(CSVRecord.class);
        when(csvToInMapper.csvInApplicationToRest(any(), anyLong())).thenReturn(new InApplicationRest());
        var method = LoadMetadataService.class.getDeclaredMethod("mapCsvToInMetadataObject", CSVRecord.class, Long.class, String.class, FileType.class);
        method.setAccessible(true);
        Object result = method.invoke(loadMetadataService, csvRecord, 1L, "v1", FileType.APPLICATION);
        assertNotNull(result);
    }

    @Test
    void testRetrieveMetadataLoaderService_AllTypes() throws Exception {
        var method = LoadMetadataService.class.getDeclaredMethod("retrieveMetadataLoaderService", FileType.class);
        method.setAccessible(true);
        assertEquals(loadDatacenterMetadataService, method.invoke(loadMetadataService, FileType.DATACENTER));
        assertEquals(loadPhysicalEquipmentMetadataService, method.invoke(loadMetadataService, FileType.EQUIPEMENT_PHYSIQUE));
        assertEquals(loadVirtualEquipmentMetadataService, method.invoke(loadMetadataService, FileType.EQUIPEMENT_VIRTUEL));
        assertEquals(loadApplicationMetadataService, method.invoke(loadMetadataService, FileType.APPLICATION));
    }

    @Test
    void testRetrieveMetadataLoaderService_UnexpectedType() throws Exception {
        var method = LoadMetadataService.class.getDeclaredMethod("retrieveMetadataLoaderService", FileType.class);
        method.setAccessible(true);
        Exception exception = assertThrows(InvocationTargetException.class, () -> method.invoke(loadMetadataService, FileType.UNKNOWN));
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertTrue(exception.getCause().getMessage().contains("Unexpected value"));
    }
}
