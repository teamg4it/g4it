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
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

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

    private static final String ORG = "ORG"; // ✅ added

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
        rest.setCategory("Laptop");
        rest.setDefaultLifespan(5.0);
        rest.setIsServer(false);
        rest.setRefDefaultItem("Laptop");

        ItemTypeParseResult result = ItemTypeParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemTypeCsv(file)).thenReturn(result);
        when(referentialMapper.toItemTypeEntity(any())).thenReturn(List.of(new ItemType()));

        ImportReportRest response =
                service.importReferentialCSV(ORG, 1L, "itemType", file); // ✅ fixed

        assertNotNull(response);
        verify(workspacePersistenceService).syncItemTypes(eq(1L), any());
    }

    @Test
    void importReferentialCSV_itemType_duplicate() {
        ItemTypeRest rest1 = new ItemTypeRest();
        rest1.setType("A");
        rest1.setCategory("Laptop");
        rest1.setDefaultLifespan(5.0);
        rest1.setIsServer(false);
        rest1.setRefDefaultItem("Laptop");

        ItemTypeRest rest2 = new ItemTypeRest();
        rest2.setType("A");
        rest2.setCategory("Laptop");
        rest2.setDefaultLifespan(5.0);
        rest2.setIsServer(false);
        rest2.setRefDefaultItem("Laptop");

        ItemTypeParseResult result = ItemTypeParseResult.builder()
                .data(List.of(rest1, rest2))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemTypeCsv(file)).thenReturn(result);

        assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemType", file)); // ✅
    }

    @Test
    void importReferentialCSV_itemType_nullKey() {
        ItemTypeRest rest = new ItemTypeRest();
        rest.setType(null);
        rest.setCategory("Laptop");
        rest.setDefaultLifespan(5.0);
        rest.setIsServer(false);
        rest.setRefDefaultItem("Laptop");

        ItemTypeParseResult result = ItemTypeParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemTypeCsv(file)).thenReturn(result);

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemType", file));

        assertEquals("itemType", ex.getField());
        assertEquals("itemtype.required.fields.missing", ex.getError());
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

        ImportReportRest response =
                service.importReferentialCSV(ORG, 1L, "matchingItem", file); // ✅

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
                () -> service.importReferentialCSV(ORG, 1L, "matchingItem", file)); // ✅
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
                () -> service.importReferentialCSV(ORG, 1L, "matchingItem", file)); // ✅
    }

    // =========================
    // ITEM IMPACT
    // =========================

    @Test
    void importReferentialCSV_itemImpact_success() {

        ItemImpactRest rest = new ItemImpactRest();
        rest.setName("Laptop");
        rest.setCriterion("CLIMATE_CHANGE");
        rest.setLifecycleStep("MANUFACTURING");
        rest.setCategory("Hardware");
        rest.setLevel("1");
        rest.setSource("ADEME");
        rest.setTier("1");
        rest.setUnit("kg");
        rest.setValue(12.5);

        ItemImpactParseResult result = ItemImpactParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemImpactCsv(file)).thenReturn(result);

        List<ItemImpact> entities = List.of(new ItemImpact());

        when(referentialMapper.toItemImpactEntity(any()))
                .thenReturn(entities);

        ImportReportRest response =
                service.importReferentialCSV(ORG, 1L, "itemImpact", file);

        assertNotNull(response);

        verify(referentialMapper).toItemImpactEntity(result.getData());

        verify(workspacePersistenceService)
                .syncItemImpacts(1L, entities);
    }

    // =========================
    // GENERIC CASES
    // =========================

    @Test
    void importReferentialCSV_emptyFile() {
        when(file.isEmpty()).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemType", file)); // ✅
    }

    @Test
    void importReferentialCSV_nullWorkspace() {
        assertThrows(IllegalArgumentException.class,
                () -> service.importReferentialCSV(ORG, null, "itemType", file)); // ✅
    }

    @Test
    void importReferentialCSV_invalidType() {
        when(file.isEmpty()).thenReturn(false);

        assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "invalid", file)); // ✅
    }
    @Test
    void importReferentialCSV_itemType_blankKey_shouldThrow() {

        ItemTypeRest rest = new ItemTypeRest();
        rest.setType("   ");
        rest.setCategory("Laptop");
        rest.setDefaultLifespan(5.0);
        rest.setIsServer(false);
        rest.setRefDefaultItem("Laptop");

        ItemTypeParseResult result = ItemTypeParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemTypeCsv(file)).thenReturn(result);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemType", file));
        assertEquals("itemType", ex.getField());
        assertEquals("itemtype.required.fields.missing", ex.getError());
    }

    @Test
    void importReferentialCSV_nullFile_shouldThrow() {

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemType", null));
        assertEquals("file", ex.getField());
        assertEquals("File is empty", ex.getError());
    }

    @Test
    void importReferentialCSV_validationErrorsInReport_shouldThrow() {

        ItemTypeParseResult result = ItemTypeParseResult.builder()
                .data(List.of())
                .report(
                        ImportReportRest.builder()
                                .errors(List.of("error"))
                                .build())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemTypeCsv(file)).thenReturn(result);

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemType", file));

        assertEquals("csv", ex.getField());
        assertEquals("error", ex.getError());
    }

    @Test
    void importReferentialCSV_itemImpact_duplicateTriplet() {

        ItemImpactRest r1 = new ItemImpactRest();
        r1.setName("Laptop");
        r1.setCriterion("CLIMATE_CHANGE");
        r1.setLifecycleStep("MANUFACTURING");

        ItemImpactRest r2 = new ItemImpactRest();
        r2.setName("Laptop");
        r2.setCriterion("CLIMATE_CHANGE");
        r2.setLifecycleStep("MANUFACTURING");

        ItemImpactParseResult result = ItemImpactParseResult.builder()
                .data(List.of(r1, r2))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemImpactCsv(file)).thenReturn(result);

        assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemImpact", file));

        verifyNoInteractions(workspacePersistenceService);
    }

    @Test
    void importReferentialCSV_itemImpact_missingMandatoryField() {

        ItemImpactRest rest = new ItemImpactRest();

        rest.setCriterion("CLIMATE_CHANGE");
        rest.setLifecycleStep("MANUFACTURING");
        rest.setName(null);

        ItemImpactParseResult result = ItemImpactParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemImpactCsv(file)).thenReturn(result);

        assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemImpact", file));
    }

    @Test
    void importReferentialCSV_itemImpact_usingWithConsumption() {

        ItemImpactRest rest = new ItemImpactRest();

        rest.setCriterion("CLIMATE_CHANGE");
        rest.setLifecycleStep("USING");
        rest.setName("Laptop");
        rest.setCategory("cat");
        rest.setLevel("1");
        rest.setSource("ADEME");
        rest.setTier("1");
        rest.setUnit("kg");
        rest.setValue(10d);
        rest.setAvgElectricityConsumption(5d);

        ItemImpactParseResult result = ItemImpactParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemImpactCsv(file)).thenReturn(result);

        when(referentialMapper.toItemImpactEntity(any()))
                .thenReturn(List.of(new ItemImpact()));

        service.importReferentialCSV(ORG, 1L, "itemImpact", file);

        verify(workspacePersistenceService)
                .syncItemImpacts(eq(1L), any());
    }

    @Test
    void importReferentialCSV_matchingItem_blankKey() {

        MatchingItemRest rest = new MatchingItemRest();
        rest.setItemSource(" ");

        MatchingItemParseResult result = MatchingItemParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseMatchingItemCsv(file)).thenReturn(result);

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "matchingItem", file));

        assertEquals("matchingItem", ex.getField());
        assertEquals("matchingitem.required.fields.missing", ex.getError());
    }

    @ParameterizedTest
    @MethodSource("invalidItemImpactProvider")
    void importReferentialCSV_itemImpact_invalidData(
            String criterion,
            String lifecycleStep,
            Double value) {

        ItemImpactRest rest = new ItemImpactRest();
        rest.setCriterion(criterion);
        rest.setLifecycleStep(lifecycleStep);
        rest.setName("Laptop");
        rest.setCategory("cat");
        rest.setLevel("1");
        rest.setSource("ADEME");
        rest.setTier("1");
        rest.setUnit("kg");
        rest.setValue(value);

        ItemImpactParseResult result = ItemImpactParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemImpactCsv(file)).thenReturn(result);

        assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemImpact", file));
    }

    private static Stream<Arguments> invalidItemImpactProvider() {
        return Stream.of(
                // Invalid criterion
                Arguments.of("INVALID", "MANUFACTURING", 10.0),

                // Invalid lifecycle step
                Arguments.of("CLIMATE_CHANGE", "INVALID", 10.0),

                // USING must have consumption value
                Arguments.of("CLIMATE_CHANGE", "USING", 12.0)
        );
    }

    @Test
    void importReferentialCSV_reportContainsMultipleErrors() {
        ItemTypeParseResult result = ItemTypeParseResult.builder()
                .data(List.of())
                .report(ImportReportRest.builder()
                        .errors(List.of("error1", "error2"))
                        .build())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemTypeCsv(file)).thenReturn(result);

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemType", file));

        assertEquals("csv", ex.getField());
        assertEquals("error1; error2", ex.getError());
    }

    @Test
    void importReferentialCSV_itemType_invalidLifespan() {

        ItemTypeRest item = new ItemTypeRest();
        item.setType("Laptop");
        item.setCategory("Hardware");
        item.setDefaultLifespan(0.0);
        item.setIsServer(false);
        item.setRefDefaultItem("Laptop");

        ItemTypeParseResult result = ItemTypeParseResult.builder()
                .data(List.of(item))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemTypeCsv(file)).thenReturn(result);

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemType", file));

        assertEquals("itemtype.lifespan.invalid: Laptop", ex.getError());
    }

    private static Stream<Arguments> invalidItemTypeProvider() {
        return Stream.of(
                Arguments.of(null, "cat", 5.0, true, "ref"),
                Arguments.of("Laptop", null, 5.0, true, "ref"),
                Arguments.of("Laptop", "cat", null, true, "ref"),
                Arguments.of("Laptop", "cat", 5.0, null, "ref"),
                Arguments.of("Laptop", "cat", 5.0, true, null)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidItemTypeProvider")
    void importReferentialCSV_itemType_missingMandatoryFields(
            String type,
            String category,
            Double lifespan,
            Boolean server,
            String refDefault) {

        ItemTypeRest item = new ItemTypeRest();
        item.setType(type);
        item.setCategory(category);
        item.setDefaultLifespan(lifespan);
        item.setIsServer(server);
        item.setRefDefaultItem(refDefault);

        ItemTypeParseResult result = ItemTypeParseResult.builder()
                .data(List.of(item))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemTypeCsv(file)).thenReturn(result);

        assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemType", file));
    }

    private static Stream<Arguments> invalidMatchingProvider() {
        return Stream.of(
                Arguments.of(null, "Laptop"),
                Arguments.of("Laptop", null),
                Arguments.of("", "Laptop"),
                Arguments.of("Laptop", "")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidMatchingProvider")
    void importReferentialCSV_matchingItem_missingMandatoryFields(
            String source,
            String target) {

        MatchingItemRest item = new MatchingItemRest();
        item.setItemSource(source);
        item.setRefItemTarget(target);

        MatchingItemParseResult result = MatchingItemParseResult.builder()
                .data(List.of(item))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseMatchingItemCsv(file)).thenReturn(result);

        assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "matchingItem", file));
    }

    @Test
    void importReferentialCSV_matchingItem_noValidTargets() {

        MatchingItemRest item = new MatchingItemRest();
        item.setItemSource("Laptop");
        item.setRefItemTarget("Laptop");

        MatchingItemParseResult result = MatchingItemParseResult.builder()
                .data(List.of(item))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseMatchingItemCsv(file)).thenReturn(result);

        when(itemImpactRepository.findByWorkspaceIdOrWorkspaceIdIsNull(1L))
                .thenReturn(List.of());

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "matchingItem", file));

        assertEquals("matchingitem.invalid.reference: Laptop", ex.getError());
    }
}