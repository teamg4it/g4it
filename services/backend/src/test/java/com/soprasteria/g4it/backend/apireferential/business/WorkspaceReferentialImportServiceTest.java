package com.soprasteria.g4it.backend.apireferential.business;

import com.soprasteria.g4it.backend.apireferential.mapper.ReferentialMapper;
import com.soprasteria.g4it.backend.apireferential.modeldb.*;
import com.soprasteria.g4it.backend.apireferential.persistence.WorkspaceReferentialPersistenceService;
import com.soprasteria.g4it.backend.apireferential.repository.ItemImpactRepository;
import com.soprasteria.g4it.backend.exception.BadRequestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceReferentialImportServiceTest {

    @InjectMocks
    private WorkspaceReferentialImportService service;

    @Mock
    private ReferentialImportService referentialImportService;

    @Mock
    private WorkspaceReferentialPersistenceService workspacePersistenceService;

    @Mock
    private ItemImpactRepository itemImpactRepository;

    @Mock
    private ReferentialMapper referentialMapper;

    @Mock
    private MultipartFile file;

    // =========================
    // HELPERS
    // =========================

    private ImportReportRest emptyReport() {
        return ImportReportRest.builder()
                .errors(List.of())
                .build();
    }

    // =========================
    // ITEM TYPE
    // =========================

    @Test
    void importReferentialCSV_itemType_success() {
        ItemTypeRest rest = new ItemTypeRest();
        rest.setType("A");

        ItemTypeParseResult result = ItemTypeParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemTypeCsv(file)).thenReturn(result);
        when(referentialMapper.toItemTypeEntity(any())).thenReturn(List.of(new ItemType()));

        ImportReportRest response = service.importReferentialCSV(1L, "itemType", file);

        assertNotNull(response);
        verify(workspacePersistenceService).syncItemTypes(eq(1L), any());
    }

    @Test
    void importReferentialCSV_itemType_duplicate() {
        ItemTypeRest r1 = new ItemTypeRest();
        r1.setType("A");

        ItemTypeRest r2 = new ItemTypeRest();
        r2.setType("A");

        ItemTypeParseResult result = ItemTypeParseResult.builder()
                .data(List.of(r1, r2))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemTypeCsv(file)).thenReturn(result);

        assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(1L, "itemType", file));
    }

    @Test
    void importReferentialCSV_itemType_nullKey() {
        ItemTypeRest rest = new ItemTypeRest();
        rest.setType(null);

        ItemTypeParseResult result = ItemTypeParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemTypeCsv(file)).thenReturn(result);

        assertThrows(IllegalArgumentException.class,
                () -> service.importReferentialCSV(1L, "itemType", file));
    }

    // =========================
    // MATCHING ITEM
    // =========================

    @Test
    void importReferentialCSV_matchingItem_success() {
        MatchingItemRest rest = new MatchingItemRest();
        rest.setItemSource("A");
        rest.setRefItemTarget("valid");

        MatchingItemParseResult result = MatchingItemParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseMatchingItemCsv(file)).thenReturn(result);

        when(itemImpactRepository.findByWorkspaceIdOrWorkspaceIdIsNull(any()))
                .thenReturn(List.of(ItemImpact.builder().name("valid").build()));

        when(referentialMapper.toMatchingEntity(any()))
                .thenReturn(List.of(new MatchingItem()));

        ImportReportRest response = service.importReferentialCSV(1L, "matchingItem", file);

        assertNotNull(response);
        verify(workspacePersistenceService).syncMatchingItems(eq(1L), any());
    }

    @Test
    void importReferentialCSV_matchingItem_invalidTarget() {
        MatchingItemRest rest = new MatchingItemRest();
        rest.setItemSource("A");
        rest.setRefItemTarget("invalid");

        MatchingItemParseResult result = MatchingItemParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseMatchingItemCsv(file)).thenReturn(result);

        when(itemImpactRepository.findByWorkspaceIdOrWorkspaceIdIsNull(any()))
                .thenReturn(List.of(ItemImpact.builder().name("valid").build()));

        assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(1L, "matchingItem", file));
    }

    @Test
    void importReferentialCSV_matchingItem_duplicate() {
        MatchingItemRest r1 = new MatchingItemRest();
        r1.setItemSource("A");

        MatchingItemRest r2 = new MatchingItemRest();
        r2.setItemSource("A");

        MatchingItemParseResult result = MatchingItemParseResult.builder()
                .data(List.of(r1, r2))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseMatchingItemCsv(file)).thenReturn(result);

        assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(1L, "matchingItem", file));
    }

    // =========================
    // ITEM IMPACT
    // =========================

    @Test
    void importReferentialCSV_itemImpact_success() {
        ItemImpactRest rest = new ItemImpactRest();
        rest.setName("A");
        rest.setLifecycleStep("L");
        rest.setCriterion("C");

        ItemImpactParseResult result = ItemImpactParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemImpactCsv(file)).thenReturn(result);

        when(referentialMapper.toItemImpactEntity(any()))
                .thenReturn(List.of(new ItemImpact()));

        ImportReportRest response = service.importReferentialCSV(1L, "itemImpact", file);

        assertNotNull(response);
        verify(workspacePersistenceService).syncItemImpacts(eq(1L), any());
    }

    @Test
    void importReferentialCSV_itemImpact_duplicate() {
        ItemImpactRest r1 = new ItemImpactRest();
        r1.setName("A");
        r1.setLifecycleStep("L");
        r1.setCriterion("C");

        ItemImpactRest r2 = new ItemImpactRest();
        r2.setName("A");
        r2.setLifecycleStep("L");
        r2.setCriterion("C");

        ItemImpactParseResult result = ItemImpactParseResult.builder()
                .data(List.of(r1, r2))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemImpactCsv(file)).thenReturn(result);

        assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(1L, "itemImpact", file));
    }

    // =========================
    // GENERIC CASES
    // =========================

    @Test
    void importReferentialCSV_emptyFile() {
        when(file.isEmpty()).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(1L, "itemType", file));
    }

    @Test
    void importReferentialCSV_nullWorkspace() {
        assertThrows(IllegalArgumentException.class,
                () -> service.importReferentialCSV(null, "itemType", file));
    }

    @Test
    void importReferentialCSV_invalidType() {
        when(file.isEmpty()).thenReturn(false);

        assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(1L, "invalid", file));
    }

    @Test
    void importReferentialCSV_parseErrors_returnEarly() {
        ImportReportRest report = ImportReportRest.builder()
                .errors(List.of("error"))
                .build();

        ItemTypeParseResult result = ItemTypeParseResult.builder()
                .report(report)
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemTypeCsv(file)).thenReturn(result);

        ImportReportRest response = service.importReferentialCSV(1L, "itemType", file);

        assertEquals(report, response);
        verify(workspacePersistenceService, never()).syncItemTypes(any(), any());
    }
}