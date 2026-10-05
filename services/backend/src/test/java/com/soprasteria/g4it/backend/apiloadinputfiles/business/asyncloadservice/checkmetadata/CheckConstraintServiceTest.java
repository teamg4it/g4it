package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkmetadata;

import com.soprasteria.g4it.backend.apiloadinputfiles.dto.CoherenceParentDTO;
import com.soprasteria.g4it.backend.apiloadinputfiles.dto.DuplicateEquipmentDTO;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckApplicationRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckDatacenterRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.common.model.LineError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CheckConstraintServiceTest {

    @InjectMocks
    private CheckConstraintService service;

    @Mock
    private CheckDatacenterRepository datacenterRepo;

    @Mock
    private CheckPhysicalEquipmentRepository physicalRepo;

    @Mock
    private CheckVirtualEquipmentRepository virtualRepo;

    @Mock
    private CheckApplicationRepository applicationRepo;

    @Mock
    private MessageSource messageSource;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    // -------------------------------------------------------------
    // 1️⃣ Test: checkUnicity() — duplicate virtual equipment
    // -------------------------------------------------------------
    @Test
    void testCheckUnicity_duplicateVirtualEquipment() {

        DuplicateEquipmentDTO duplicate = mock(DuplicateEquipmentDTO.class);
        when(duplicate.getFilenameLineInfo()).thenReturn("file1:1,file2:3");
        when(duplicate.getEquipmentName()).thenReturn("Veqp1");

        when(virtualRepo.findDuplicateDigitalServiceVirtualEqp(10L))
                .thenReturn(List.of(duplicate));

        when(physicalRepo.findDuplicatePhysicalEquipments(10L)).thenReturn(List.of());
        when(datacenterRepo.findDuplicateDatacenters(10L)).thenReturn(List.of());
        when(applicationRepo.findDuplicateApplications(10L)).thenReturn(List.of());

        Map<String, Map<Integer, List<LineError>>> result =
                service.checkUnicity(10L, true);

        assertNotNull(result.get("file1"));
        assertEquals("Veqp1", result.get("file1").get(1).get(0).equipementName().orElse(null));

        assertEquals("Veqp1", result.get("file2").get(3).get(0).equipementName().orElse(null));
    }

    // -------------------------------------------------------------
    // 2️⃣ Test: checkUnicity() — only incoherent physical equipment
    // -------------------------------------------------------------
    @Test
    void testCheckUnicity_incoherentPhysicalEquipment() {

        // No duplicates
        when(virtualRepo.findDuplicateDigitalServiceVirtualEqp(1L)).thenReturn(List.of());
        when(physicalRepo.findDuplicatePhysicalEquipments(1L)).thenReturn(List.of());
        when(datacenterRepo.findDuplicateDatacenters(1L)).thenReturn(List.of());
        when(applicationRepo.findDuplicateApplications(1L)).thenReturn(List.of());

        Map<String, Map<Integer, List<LineError>>> result =
                service.checkUnicity(1L, true);

        // Since NO duplicates → map MUST be empty
        assertTrue(result.isEmpty(), "Unicity map must be empty");
    }
    // -------------------------------------------------------------
    // 5️⃣ Test: application coherence
    // -------------------------------------------------------------
    @Test
    void testCheckCoherence_application() {

        CoherenceParentDTO dto = mock(CoherenceParentDTO.class);
        when(dto.getFilename()).thenReturn("fileApp");
        when(dto.getLineNb()).thenReturn(7);
        when(dto.getParentEquipmentName()).thenReturn("VirtualEqp1");

        when(applicationRepo.findIncoherentApplications(anyLong(), anyLong()))
                .thenReturn(List.of(dto));

        when(messageSource.getMessage(eq("equipementvirtuel.should.exist"), any(), any()))
                .thenReturn("Parent Virtual missing");

        Map<String, Map<Integer, List<LineError>>> coherence =
                service.checkCoherence(3L, 99L, null, new HashMap<>());

        LineError error = coherence.get("fileApp").get(7).get(0);
        assertEquals("Parent Virtual missing", error.error());
    }
}
