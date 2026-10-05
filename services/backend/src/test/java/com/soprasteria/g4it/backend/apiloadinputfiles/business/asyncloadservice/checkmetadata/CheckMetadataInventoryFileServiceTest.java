/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkmetadata;

import com.soprasteria.g4it.backend.common.model.LineError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckMetadataInventoryFileServiceTest {

    @Mock
    private CheckConstraintService checkConstraintService;

    @InjectMocks
    private CheckMetadataInventoryFileService checkMetadataInventoryFileService;

    private Long taskId;
    private Long inventoryId;
    private String digitalServiceVersionUid;

    @BeforeEach
    void setUp() {
        taskId = 1L;
        inventoryId = 100L;
        digitalServiceVersionUid = "ds-version-123";
    }

    @Test
    void checkMetadataInventoryFile_withEmptyResults_shouldReturnEmptyMap() {
        // Arrange
        when(checkConstraintService.checkUnicity(taskId, true))
                .thenReturn(new HashMap<>());
        when(checkConstraintService.checkCoherence(taskId, inventoryId, digitalServiceVersionUid, new HashMap<>()))
                .thenReturn(new HashMap<>());

        // Act
        Map<String, Map<Integer, List<LineError>>> result =
                checkMetadataInventoryFileService.checkMetadataInventoryFile(taskId, inventoryId, digitalServiceVersionUid);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(checkConstraintService, times(1)).checkUnicity(taskId, true);
        verify(checkConstraintService, times(1)).checkCoherence(taskId, inventoryId, digitalServiceVersionUid, new HashMap<>());
    }

    @Test
    void checkMetadataInventoryFile_withDuplicatesOnly_shouldReturnDuplicatesMap() {
        // Arrange
        Map<String, Map<Integer, List<LineError>>> duplicatesMap = createMockDuplicatesMap();
        Map<String, Map<Integer, List<LineError>>> emptyCoherenceMap = new HashMap<>();

        when(checkConstraintService.checkUnicity(taskId, true))
                .thenReturn(duplicatesMap);
        when(checkConstraintService.checkCoherence(taskId, inventoryId, digitalServiceVersionUid, duplicatesMap))
                .thenReturn(emptyCoherenceMap);

        // Act
        Map<String, Map<Integer, List<LineError>>> result =
                checkMetadataInventoryFileService.checkMetadataInventoryFile(taskId, inventoryId, digitalServiceVersionUid);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("inventory.csv"));
        assertEquals(1, result.get("inventory.csv").size());
        assertEquals(1, result.get("inventory.csv").get(2).size());
    }

    @Test
    void checkMetadataInventoryFile_withCoherenceErrorsOnly_shouldReturnCoherenceMap() {
        // Arrange
        Map<String, Map<Integer, List<LineError>>> emptyDuplicatesMap = new HashMap<>();
        Map<String, Map<Integer, List<LineError>>> coherenceMap = createMockCoherenceMap();

        when(checkConstraintService.checkUnicity(taskId, true))
                .thenReturn(emptyDuplicatesMap);
        when(checkConstraintService.checkCoherence(taskId, inventoryId, digitalServiceVersionUid, emptyDuplicatesMap))
                .thenReturn(coherenceMap);

        // Act
        Map<String, Map<Integer, List<LineError>>> result =
                checkMetadataInventoryFileService.checkMetadataInventoryFile(taskId, inventoryId, digitalServiceVersionUid);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("entities.csv"));
        assertEquals(1, result.get("entities.csv").size());
        assertEquals(1, result.get("entities.csv").get(5).size());
    }

    @Test
    void checkMetadataInventoryFile_withDuplicatesAndCoherenceErrors_shouldMergeMaps() {
        // Arrange
        Map<String, Map<Integer, List<LineError>>> duplicatesMap = createMockDuplicatesMap();
        Map<String, Map<Integer, List<LineError>>> coherenceMap = createMockCoherenceMap();

        when(checkConstraintService.checkUnicity(taskId, true))
                .thenReturn(duplicatesMap);
        when(checkConstraintService.checkCoherence(taskId, inventoryId, digitalServiceVersionUid, duplicatesMap))
                .thenReturn(coherenceMap);

        // Act
        Map<String, Map<Integer, List<LineError>>> result =
                checkMetadataInventoryFileService.checkMetadataInventoryFile(taskId, inventoryId, digitalServiceVersionUid);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size()); // Two different files
        assertTrue(result.containsKey("inventory.csv"));
        assertTrue(result.containsKey("entities.csv"));
    }

    @Test
    void checkMetadataInventoryFile_withSameFileAndLineNumber_shouldCombineErrors() {
        // Arrange - Both duplicates and coherence errors for same file and line
        Map<String, Map<Integer, List<LineError>>> duplicatesMap = new HashMap<>();
        LineError duplicateError = new LineError("inventory.csv", 3, "Duplicate entry found");
        Map<Integer, List<LineError>> duplicateLines = new HashMap<>();
        duplicateLines.put(3, Arrays.asList(duplicateError));
        duplicatesMap.put("inventory.csv", duplicateLines);

        Map<String, Map<Integer, List<LineError>>> coherenceMap = new HashMap<>();
        LineError coherenceError = new LineError("inventory.csv", 3, "Data inconsistency detected");
        Map<Integer, List<LineError>> coherenceLines = new HashMap<>();
        coherenceLines.put(3, Arrays.asList(coherenceError));
        coherenceMap.put("inventory.csv", coherenceLines);

        when(checkConstraintService.checkUnicity(taskId, true))
                .thenReturn(duplicatesMap);
        when(checkConstraintService.checkCoherence(taskId, inventoryId, digitalServiceVersionUid, duplicatesMap))
                .thenReturn(coherenceMap);

        // Act
        Map<String, Map<Integer, List<LineError>>> result =
                checkMetadataInventoryFileService.checkMetadataInventoryFile(taskId, inventoryId, digitalServiceVersionUid);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get("inventory.csv").size());
        List<LineError> combinedErrors = result.get("inventory.csv").get(3);
        assertEquals(2, combinedErrors.size());
        assertEquals("Duplicate entry found", combinedErrors.get(0).error());
        assertEquals("Data inconsistency detected", combinedErrors.get(1).error());
    }

    @Test
    void checkMetadataInventoryFile_withMultipleErrors_shouldHandleCorrectly() {
        // Arrange
        Map<String, Map<Integer, List<LineError>>> duplicatesMap = new HashMap<>();
        LineError error1 = new LineError("inventory.csv", 2, "Error 1");
        LineError error2 = new LineError("inventory.csv", 2, "Error 2");
        Map<Integer, List<LineError>> duplicateLines = new HashMap<>();
        duplicateLines.put(2, Arrays.asList(error1, error2));
        duplicatesMap.put("inventory.csv", duplicateLines);

        Map<String, Map<Integer, List<LineError>>> coherenceMap = new HashMap<>();
        LineError error3 = new LineError("inventory.csv", 2, "Error 3");
        Map<Integer, List<LineError>> coherenceLines = new HashMap<>();
        coherenceLines.put(2, Arrays.asList(error3));
        coherenceMap.put("inventory.csv", coherenceLines);

        when(checkConstraintService.checkUnicity(taskId, true))
                .thenReturn(duplicatesMap);
        when(checkConstraintService.checkCoherence(taskId, inventoryId, digitalServiceVersionUid, duplicatesMap))
                .thenReturn(coherenceMap);

        // Act
        Map<String, Map<Integer, List<LineError>>> result =
                checkMetadataInventoryFileService.checkMetadataInventoryFile(taskId, inventoryId, digitalServiceVersionUid);

        // Assert
        assertNotNull(result);
        List<LineError> combinedErrors = result.get("inventory.csv").get(2);
        assertEquals(3, combinedErrors.size());
    }

    @Test
    void checkMetadataInventoryFile_withDigitalServiceVersionUidNull_shouldCallCheckUniciityWithFalse() {
        // Arrange
        when(checkConstraintService.checkUnicity(taskId, false))
                .thenReturn(new HashMap<>());
        when(checkConstraintService.checkCoherence(taskId, inventoryId, null, new HashMap<>()))
                .thenReturn(new HashMap<>());

        // Act
        Map<String, Map<Integer, List<LineError>>> result =
                checkMetadataInventoryFileService.checkMetadataInventoryFile(taskId, inventoryId, null);

        // Assert
        assertNotNull(result);
        verify(checkConstraintService, times(1)).checkUnicity(taskId, false);
        verify(checkConstraintService, times(1)).checkCoherence(taskId, inventoryId, null, new HashMap<>());
    }

    @Test
    void checkMetadataInventoryFile_withMultipleFilesAndLines_shouldMergeCorrectly() {
        // Arrange
        Map<String, Map<Integer, List<LineError>>> duplicatesMap = new HashMap<>();

        // File 1: inventory.csv
        LineError inv1Error = new LineError("inventory.csv", 1, "Duplicate");
        Map<Integer, List<LineError>> invLines = new HashMap<>();
        invLines.put(1, Arrays.asList(inv1Error));
        duplicatesMap.put("inventory.csv", invLines);

        // File 2: entities.csv
        LineError ent2Error = new LineError("entities.csv", 2, "Duplicate");
        Map<Integer, List<LineError>> entLines = new HashMap<>();
        entLines.put(2, Arrays.asList(ent2Error));
        duplicatesMap.put("entities.csv", entLines);

        Map<String, Map<Integer, List<LineError>>> coherenceMap = new HashMap<>();

        // File 1: inventory.csv additional line
        LineError inv3Error = new LineError("inventory.csv", 3, "Coherence issue");
        Map<Integer, List<LineError>> cohInvLines = new HashMap<>();
        cohInvLines.put(3, Arrays.asList(inv3Error));
        coherenceMap.put("inventory.csv", cohInvLines);

        // File 3: measures.csv
        LineError meas1Error = new LineError("measures.csv", 1, "Coherence issue");
        Map<Integer, List<LineError>> measLines = new HashMap<>();
        measLines.put(1, Arrays.asList(meas1Error));
        coherenceMap.put("measures.csv", measLines);

        when(checkConstraintService.checkUnicity(taskId, true))
                .thenReturn(duplicatesMap);
        when(checkConstraintService.checkCoherence(taskId, inventoryId, digitalServiceVersionUid, duplicatesMap))
                .thenReturn(coherenceMap);

        // Act
        Map<String, Map<Integer, List<LineError>>> result =
                checkMetadataInventoryFileService.checkMetadataInventoryFile(taskId, inventoryId, digitalServiceVersionUid);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size()); // Three files total
        assertTrue(result.containsKey("inventory.csv"));
        assertTrue(result.containsKey("entities.csv"));
        assertTrue(result.containsKey("measures.csv"));

        // inventory.csv should have 2 line numbers (1 and 3)
        assertEquals(2, result.get("inventory.csv").size());
        assertEquals(1, result.get("inventory.csv").get(1).size());
        assertEquals(1, result.get("inventory.csv").get(3).size());
    }

    @Test
    void checkMetadataInventoryFile_shouldCallBothCheckMethods() {
        // Arrange
        when(checkConstraintService.checkUnicity(taskId, true))
                .thenReturn(new HashMap<>());
        when(checkConstraintService.checkCoherence(taskId, inventoryId, digitalServiceVersionUid, new HashMap<>()))
                .thenReturn(new HashMap<>());

        // Act
        checkMetadataInventoryFileService.checkMetadataInventoryFile(taskId, inventoryId, digitalServiceVersionUid);

        // Assert
        verify(checkConstraintService, times(1)).checkUnicity(taskId, true);
        verify(checkConstraintService, times(1)).checkCoherence(taskId, inventoryId, digitalServiceVersionUid, new HashMap<>());
        verifyNoMoreInteractions(checkConstraintService);
    }

    @Test
    void checkMetadataInventoryFile_withComplexScenario_shouldPreserveDuplicatesBeforeCoherence() {
        // Arrange - Verify that duplicates map is passed to checkCoherence before merging
        Map<String, Map<Integer, List<LineError>>> duplicatesMap = createMockDuplicatesMap();
        Map<String, Map<Integer, List<LineError>>> coherenceMap = createMockCoherenceMap();

        when(checkConstraintService.checkUnicity(taskId, true))
                .thenReturn(duplicatesMap);
        when(checkConstraintService.checkCoherence(taskId, inventoryId, digitalServiceVersionUid, duplicatesMap))
                .thenReturn(coherenceMap);

        // Act
        Map<String, Map<Integer, List<LineError>>> result =
                checkMetadataInventoryFileService.checkMetadataInventoryFile(taskId, inventoryId, digitalServiceVersionUid);

        // Assert
        verify(checkConstraintService).checkCoherence(taskId, inventoryId, digitalServiceVersionUid, duplicatesMap);
        assertNotNull(result);
    }

    /**
     * Helper method to create a mock duplicates map
     */
    private Map<String, Map<Integer, List<LineError>>> createMockDuplicatesMap() {
        Map<String, Map<Integer, List<LineError>>> duplicatesMap = new HashMap<>();
        LineError error = new LineError("inventory.csv", 2, "Duplicate entry found");
        Map<Integer, List<LineError>> lines = new HashMap<>();
        lines.put(2, Arrays.asList(error));
        duplicatesMap.put("inventory.csv", lines);
        return duplicatesMap;
    }

    /**
     * Helper method to create a mock coherence map
     */
    private Map<String, Map<Integer, List<LineError>>> createMockCoherenceMap() {
        Map<String, Map<Integer, List<LineError>>> coherenceMap = new HashMap<>();
        LineError error = new LineError("entities.csv", 5, "Data inconsistency detected");
        Map<Integer, List<LineError>> lines = new HashMap<>();
        lines.put(5, Arrays.asList(error));
        coherenceMap.put("entities.csv", lines);
        return coherenceMap;
    }
}

