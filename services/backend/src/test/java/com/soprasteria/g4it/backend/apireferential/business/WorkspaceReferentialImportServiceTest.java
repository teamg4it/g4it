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
import org.springframework.context.MessageSource;
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
    private MessageSource messageSource;

    @Mock
    private MultipartFile file;

    private static final String ORG = "ORG"; // ✅ added

    private ImportReportRest emptyReport() {
        return ImportReportRest.builder()
                .errors(List.of())
                .build();
    }

    // =========================
    // ITEM TYPE - AC7, AC8, AC9
    // =========================

    @Test
    void importReferentialCSV_itemType_success() {
        ItemTypeRest rest = new ItemTypeRest();
        rest.setType("Laptop");
        rest.setCategory("IT");
        rest.setDefaultLifespan(5.0);
        rest.setIsServer(false);
        rest.setRefDefaultItem("DefaultLaptop");

        ItemTypeParseResult result = ItemTypeParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemTypeCsv(file)).thenReturn(result);
        when(referentialMapper.toItemTypeEntity(any())).thenReturn(List.of(new ItemType()));

        ImportReportRest response =
                service.importReferentialCSV(ORG, 1L, "itemType", file);

        assertNotNull(response);
        verify(workspacePersistenceService).syncItemTypes(eq(1L), any());
    }

    @Test
    void importReferentialCSV_itemType_missingType() {
        ItemTypeRest rest = new ItemTypeRest();
        // Missing type
        rest.setCategory("IT");
        rest.setDefaultLifespan(5.0);
        rest.setIsServer(false);
        rest.setRefDefaultItem("DefaultLaptop");

        ItemTypeParseResult result = ItemTypeParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemTypeCsv(file)).thenReturn(result);
        when(messageSource.getMessage(eq("itemtype.required.fields.missing"), any(), any()))
                .thenReturn("Some required information is missing. Please refer to the data model for the requested data.");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemType", file));

        assertTrue(ex.getError().contains("required information is missing"));
    }

    @Test
    void importReferentialCSV_itemType_missingCategory() {
        ItemTypeRest rest = new ItemTypeRest();
        rest.setType("Laptop");
        // Missing category
        rest.setDefaultLifespan(5.0);
        rest.setIsServer(false);
        rest.setRefDefaultItem("DefaultLaptop");

        ItemTypeParseResult result = ItemTypeParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemTypeCsv(file)).thenReturn(result);
        when(messageSource.getMessage(eq("itemtype.required.fields.missing"), any(), any()))
                .thenReturn("Some required information is missing. Please refer to the data model for the requested data.");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemType", file));

        assertTrue(ex.getError().contains("required information is missing"));
    }

    @Test
    void importReferentialCSV_itemType_missingDefaultLifespan() {
        ItemTypeRest rest = new ItemTypeRest();
        rest.setType("Laptop");
        rest.setCategory("IT");
        // Missing defaultLifespan
        rest.setIsServer(false);
        rest.setRefDefaultItem("DefaultLaptop");

        ItemTypeParseResult result = ItemTypeParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemTypeCsv(file)).thenReturn(result);
        when(messageSource.getMessage(eq("itemtype.required.fields.missing"), any(), any()))
                .thenReturn("Some required information is missing. Please refer to the data model for the requested data.");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemType", file));

        assertTrue(ex.getError().contains("required information is missing"));
    }

    @Test
    void importReferentialCSV_itemType_missingIsServer() {
        ItemTypeRest rest = new ItemTypeRest();
        rest.setType("Laptop");
        rest.setCategory("IT");
        rest.setDefaultLifespan(5.0);
        // Missing isServer
        rest.setRefDefaultItem("DefaultLaptop");

        ItemTypeParseResult result = ItemTypeParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemTypeCsv(file)).thenReturn(result);
        when(messageSource.getMessage(eq("itemtype.required.fields.missing"), any(), any()))
                .thenReturn("Some required information is missing. Please refer to the data model for the requested data.");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemType", file));

        assertTrue(ex.getError().contains("required information is missing"));
    }

    @Test
    void importReferentialCSV_itemType_missingRefDefaultItem() {
        ItemTypeRest rest = new ItemTypeRest();
        rest.setType("Laptop");
        rest.setCategory("IT");
        rest.setDefaultLifespan(5.0);
        rest.setIsServer(false);
        // Missing refDefaultItem

        ItemTypeParseResult result = ItemTypeParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemTypeCsv(file)).thenReturn(result);
        when(messageSource.getMessage(eq("itemtype.required.fields.missing"), any(), any()))
                .thenReturn("Some required information is missing. Please refer to the data model for the requested data.");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemType", file));

        assertTrue(ex.getError().contains("required information is missing"));
    }

    @Test
    void importReferentialCSV_itemType_duplicateType() {
        ItemTypeRest r1 = new ItemTypeRest();
        r1.setType("Laptop");
        r1.setCategory("IT");
        r1.setDefaultLifespan(5.0);
        r1.setIsServer(false);
        r1.setRefDefaultItem("DefaultLaptop1");

        ItemTypeRest r2 = new ItemTypeRest();
        r2.setType("Laptop"); // Duplicate type
        r2.setCategory("IT");
        r2.setDefaultLifespan(6.0);
        r2.setIsServer(false);
        r2.setRefDefaultItem("DefaultLaptop2");

        ItemTypeParseResult result = ItemTypeParseResult.builder()
                .data(List.of(r1, r2))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemTypeCsv(file)).thenReturn(result);
        when(messageSource.getMessage(eq("itemtype.duplicate.type"), any(), any()))
                .thenReturn("Some types of equipment are duplicated in the import file.");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemType", file));

        assertTrue(ex.getError().contains("types of equipment are duplicated"));
    }

    @Test
    void importReferentialCSV_itemType_lifespanZero() {
        ItemTypeRest rest = new ItemTypeRest();
        rest.setType("Laptop");
        rest.setCategory("IT");
        rest.setDefaultLifespan(0.0); // Lifespan = 0
        rest.setIsServer(false);
        rest.setRefDefaultItem("DefaultLaptop");

        ItemTypeParseResult result = ItemTypeParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemTypeCsv(file)).thenReturn(result);
        when(messageSource.getMessage(eq("itemtype.lifespan.invalid"), any(), any()))
                .thenReturn("The default lifespan of a type of equipment must be strictly greater than 0.");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemType", file));

        assertTrue(ex.getError().contains("lifespan of a type of equipment must be strictly greater than 0"));
    }

    @Test
    void importReferentialCSV_itemType_lifespanNegative() {
        ItemTypeRest rest = new ItemTypeRest();
        rest.setType("Laptop");
        rest.setCategory("IT");
        rest.setDefaultLifespan(-1.0); // Negative lifespan
        rest.setIsServer(false);
        rest.setRefDefaultItem("DefaultLaptop");

        ItemTypeParseResult result = ItemTypeParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemTypeCsv(file)).thenReturn(result);
        when(messageSource.getMessage(eq("itemtype.lifespan.invalid"), any(), any()))
                .thenReturn("The default lifespan of a type of equipment must be strictly greater than 0.");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemType", file));

        assertTrue(ex.getError().contains("lifespan of a type of equipment must be strictly greater than 0"));
    }

    @Test
    void importReferentialCSV_itemType_lifespanPositive() {
        ItemTypeRest rest = new ItemTypeRest();
        rest.setType("Laptop");
        rest.setCategory("IT");
        rest.setDefaultLifespan(5.5); // Valid positive lifespan
        rest.setIsServer(false);
        rest.setRefDefaultItem("DefaultLaptop");

        ItemTypeParseResult result = ItemTypeParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemTypeCsv(file)).thenReturn(result);
        when(referentialMapper.toItemTypeEntity(any())).thenReturn(List.of(new ItemType()));

        ImportReportRest response =
                service.importReferentialCSV(ORG, 1L, "itemType", file);

        assertNotNull(response);
        verify(workspacePersistenceService).syncItemTypes(eq(1L), any());
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

        assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemType", file));
    }

    // =========================
    // MATCHING ITEM - AC10, AC11
    // =========================

    @Test
    void importReferentialCSV_matchingItem_success() {
        MatchingItemRest rest = new MatchingItemRest();
        rest.setItemSource("Dell-XPS");
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
                service.importReferentialCSV(ORG, 1L, "matchingItem", file);

        assertNotNull(response);
        verify(workspacePersistenceService).syncMatchingItems(eq(1L), any());
    }

    @Test
    void importReferentialCSV_matchingItem_missingItemSource() {
        MatchingItemRest rest = new MatchingItemRest();
        // Missing itemSource
        rest.setRefItemTarget("valid");

        MatchingItemParseResult result = MatchingItemParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseMatchingItemCsv(file)).thenReturn(result);
        when(messageSource.getMessage(eq("matchingitem.required.fields.missing"), any(), any()))
                .thenReturn("Some required information is missing. Please refer to the data model for the requested data.");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "matchingItem", file));

        assertTrue(ex.getError().contains("required information is missing"));
    }

    @Test
    void importReferentialCSV_matchingItem_missingRefItemTarget() {
        MatchingItemRest rest = new MatchingItemRest();
        rest.setItemSource("Dell-XPS");
        // Missing refItemTarget

        MatchingItemParseResult result = MatchingItemParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseMatchingItemCsv(file)).thenReturn(result);
        when(messageSource.getMessage(eq("matchingitem.required.fields.missing"), any(), any()))
                .thenReturn("Some required information is missing. Please refer to the data model for the requested data.");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "matchingItem", file));

        assertTrue(ex.getError().contains("required information is missing"));
    }

    @Test
    void importReferentialCSV_matchingItem_blankItemSource() {
        MatchingItemRest rest = new MatchingItemRest();
        rest.setItemSource("   "); // Blank itemSource
        rest.setRefItemTarget("valid");

        MatchingItemParseResult result = MatchingItemParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseMatchingItemCsv(file)).thenReturn(result);
        when(messageSource.getMessage(eq("matchingitem.required.fields.missing"), any(), any()))
                .thenReturn("Some required information is missing. Please refer to the data model for the requested data.");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "matchingItem", file));

        assertTrue(ex.getError().contains("required information is missing"));
    }

    @Test
    void importReferentialCSV_matchingItem_invalidTarget() {
        MatchingItemRest rest = new MatchingItemRest();
        rest.setItemSource("Dell-XPS");
        rest.setRefItemTarget("invalid");

        MatchingItemParseResult result = MatchingItemParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseMatchingItemCsv(file)).thenReturn(result);

        when(itemImpactRepository.findByWorkspaceIdOrWorkspaceIdIsNull(any()))
                .thenReturn(List.of(ItemImpact.builder().name("valid").build()));

        when(messageSource.getMessage(eq("matchingitem.invalid.reference"), any(), any()))
                .thenReturn("The reference invalid is unkown in ref_item_impact table.");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "matchingItem", file));

        assertTrue(ex.getError().contains("reference invalid is unkown"));
    }

    @Test
    void importReferentialCSV_matchingItem_validReferenceFromWorkspace() {
        MatchingItemRest rest = new MatchingItemRest();
        rest.setItemSource("Dell-XPS");
        rest.setRefItemTarget("WorkspaceItem");

        MatchingItemParseResult result = MatchingItemParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseMatchingItemCsv(file)).thenReturn(result);

        // Returns both workspace and global items
        when(itemImpactRepository.findByWorkspaceIdOrWorkspaceIdIsNull(any()))
                .thenReturn(List.of(
                        ItemImpact.builder().name("WorkspaceItem").workspaceId(1L).build(),
                        ItemImpact.builder().name("GlobalItem").build()
                ));

        when(referentialMapper.toMatchingEntity(any()))
                .thenReturn(List.of(new MatchingItem()));

        ImportReportRest response =
                service.importReferentialCSV(ORG, 1L, "matchingItem", file);

        assertNotNull(response);
        verify(workspacePersistenceService).syncMatchingItems(eq(1L), any());
    }

    @Test
    void importReferentialCSV_matchingItem_duplicate() {
        MatchingItemRest r1 = new MatchingItemRest();
        r1.setItemSource("Dell-XPS");
        r1.setRefItemTarget("valid");

        MatchingItemRest r2 = new MatchingItemRest();
        r2.setItemSource("Dell-XPS"); // Duplicate itemSource

        MatchingItemParseResult result = MatchingItemParseResult.builder()
                .data(List.of(r1, r2))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseMatchingItemCsv(file)).thenReturn(result);

        assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "matchingItem", file));
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
        rest.setCategory("CAT");
        rest.setLevel("LEV");
        rest.setSource("SRC");
        rest.setTier("T1");
        rest.setUnit("kg");
        rest.setValue(1.0);

        ItemImpactParseResult result = ItemImpactParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemImpactCsv(file)).thenReturn(result);

        when(referentialMapper.toItemImpactEntity(any()))
                .thenReturn(List.of(new ItemImpact()));

        ImportReportRest response =
                service.importReferentialCSV(ORG, 1L, "itemImpact", file); // ✅

        assertNotNull(response);
        verify(workspacePersistenceService).syncItemImpacts(eq(1L), any());
    }

    @Test
    void importReferentialCSV_itemImpact_missingCriterion() {
        ItemImpactRest rest = new ItemImpactRest();
        rest.setName("A");
        rest.setLifecycleStep("L");
        // Missing criterion
        rest.setCategory("CAT");
        rest.setLevel("LEV");
        rest.setSource("SRC");
        rest.setTier("T1");
        rest.setUnit("kg");
        rest.setValue(1.0);

        ItemImpactParseResult result = ItemImpactParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemImpactCsv(file)).thenReturn(result);
        when(messageSource.getMessage(eq("itemimpact.required.fields.missing"), any(), any()))
                .thenReturn("Some required information is missing. Please refer to the data model for the requested data.");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemImpact", file));

        assertTrue(ex.getError().contains("Some required information is missing"));
    }

    @Test
    void importReferentialCSV_itemImpact_missingValue() {
        ItemImpactRest rest = new ItemImpactRest();
        rest.setName("A");
        rest.setLifecycleStep("L");
        rest.setCriterion("C");
        rest.setCategory("CAT");
        rest.setLevel("LEV");
        rest.setSource("SRC");
        rest.setTier("T1");
        rest.setUnit("kg");
        // Missing value

        ItemImpactParseResult result = ItemImpactParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemImpactCsv(file)).thenReturn(result);
        when(messageSource.getMessage(eq("itemimpact.required.fields.missing"), any(), any()))
                .thenReturn("Some required information is missing. Please refer to the data model for the requested data.");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemImpact", file));

        assertTrue(ex.getError().contains("Some required information is missing"));
    }

    @Test
    void importReferentialCSV_itemImpact_blankCategory() {
        ItemImpactRest rest = new ItemImpactRest();
        rest.setName("A");
        rest.setLifecycleStep("L");
        rest.setCriterion("C");
        rest.setCategory("   "); // Blank category
        rest.setLevel("LEV");
        rest.setSource("SRC");
        rest.setTier("T1");
        rest.setUnit("kg");
        rest.setValue(1.0);

        ItemImpactParseResult result = ItemImpactParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemImpactCsv(file)).thenReturn(result);
        when(messageSource.getMessage(eq("itemimpact.required.fields.missing"), any(), any()))
                .thenReturn("Some required information is missing. Please refer to the data model for the requested data.");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemImpact", file));

        assertTrue(ex.getError().contains("Some required information is missing"));
    }

    @Test
    void importReferentialCSV_itemImpact_validCriterion_CLIMATE_CHANGE() {
        ItemImpactRest rest = new ItemImpactRest();
        rest.setName("A");
        rest.setLifecycleStep("L");
        rest.setCriterion("CLIMATE_CHANGE"); // Valid criterion
        rest.setCategory("CAT");
        rest.setLevel("LEV");
        rest.setSource("SRC");
        rest.setTier("T1");
        rest.setUnit("kg");
        rest.setValue(1.0);

        ItemImpactParseResult result = ItemImpactParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemImpactCsv(file)).thenReturn(result);
        when(referentialMapper.toItemImpactEntity(any()))
                .thenReturn(List.of(new ItemImpact()));

        ImportReportRest response =
                service.importReferentialCSV(ORG, 1L, "itemImpact", file);

        assertNotNull(response);
        verify(workspacePersistenceService).syncItemImpacts(eq(1L), any());
    }

    @Test
    void importReferentialCSV_itemImpact_invalidCriterion() {
        ItemImpactRest rest = new ItemImpactRest();
        rest.setName("A");
        rest.setLifecycleStep("L");
        rest.setCriterion("INVALID_CRITERION"); // Invalid criterion
        rest.setCategory("CAT");
        rest.setLevel("LEV");
        rest.setSource("SRC");
        rest.setTier("T1");
        rest.setUnit("kg");
        rest.setValue(1.0);

        ItemImpactParseResult result = ItemImpactParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemImpactCsv(file)).thenReturn(result);
        when(messageSource.getMessage(eq("itemimpact.criterion.invalid"), any(), any()))
                .thenReturn("Some of the impact criteria provided are unknown. Please refer to the values indicated in the data model.");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemImpact", file));

        assertTrue(ex.getError().contains("impact criteria provided are unknown"));
    }

    @Test
    void importReferentialCSV_itemImpact_unknownCriterion() {
        ItemImpactRest rest = new ItemImpactRest();
        rest.setName("A");
        rest.setLifecycleStep("L");
        rest.setCriterion("UNKNOWN_VALUE"); // Unknown criterion
        rest.setCategory("CAT");
        rest.setLevel("LEV");
        rest.setSource("SRC");
        rest.setTier("T1");
        rest.setUnit("kg");
        rest.setValue(1.0);

        ItemImpactParseResult result = ItemImpactParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemImpactCsv(file)).thenReturn(result);
        when(messageSource.getMessage(eq("itemimpact.criterion.invalid"), any(), any()))
                .thenReturn("Some of the impact criteria provided are unknown. Please refer to the values indicated in the data model.");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemImpact", file));

        assertTrue(ex.getError().contains("impact criteria provided are unknown"));
    }

    @Test
    void importReferentialCSV_itemImpact_allValidCriteria() {
        // Test with all valid criterion values
        ItemImpactRest rest = new ItemImpactRest();
        rest.setName("A");
        rest.setLifecycleStep("L");
        rest.setCriterion("RESOURCE_USE_FOSSILS"); // Another valid criterion
        rest.setCategory("CAT");
        rest.setLevel("LEV");
        rest.setSource("SRC");
        rest.setTier("T1");
        rest.setUnit("kg");
        rest.setValue(1.0);

        ItemImpactParseResult result = ItemImpactParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemImpactCsv(file)).thenReturn(result);
        when(referentialMapper.toItemImpactEntity(any()))
                .thenReturn(List.of(new ItemImpact()));

        ImportReportRest response =
                service.importReferentialCSV(ORG, 1L, "itemImpact", file);

        assertNotNull(response);
        verify(workspacePersistenceService).syncItemImpacts(eq(1L), any());
    }

    // =========================
    // AC3: LIFECYCLE STEP VALIDATION
    // =========================

    @Test
    void importReferentialCSV_itemImpact_validLifecycleStep_MANUFACTURING() {
        ItemImpactRest rest = new ItemImpactRest();
        rest.setName("A");
        rest.setLifecycleStep("MANUFACTURING"); // Valid lifecycle step
        rest.setCriterion("CLIMATE_CHANGE");
        rest.setCategory("CAT");
        rest.setLevel("LEV");
        rest.setSource("SRC");
        rest.setTier("T1");
        rest.setUnit("kg");
        rest.setValue(1.0);

        ItemImpactParseResult result = ItemImpactParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemImpactCsv(file)).thenReturn(result);
        when(referentialMapper.toItemImpactEntity(any()))
                .thenReturn(List.of(new ItemImpact()));

        ImportReportRest response =
                service.importReferentialCSV(ORG, 1L, "itemImpact", file);

        assertNotNull(response);
        verify(workspacePersistenceService).syncItemImpacts(eq(1L), any());
    }

    @Test
    void importReferentialCSV_itemImpact_invalidLifecycleStep() {
        ItemImpactRest rest = new ItemImpactRest();
        rest.setName("A");
        rest.setLifecycleStep("INVALID_STEP"); // Invalid lifecycle step
        rest.setCriterion("CLIMATE_CHANGE");
        rest.setCategory("CAT");
        rest.setLevel("LEV");
        rest.setSource("SRC");
        rest.setTier("T1");
        rest.setUnit("kg");
        rest.setValue(1.0);

        ItemImpactParseResult result = ItemImpactParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemImpactCsv(file)).thenReturn(result);
        when(messageSource.getMessage(eq("itemimpact.lifecyclestep.invalid"), any(), any()))
                .thenReturn("Some of the lifecycle step provided are unknown. Please refer to the values indicated in the data model.");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemImpact", file));

        assertTrue(ex.getError().contains("lifecycle step provided are unknown"));
    }

    @Test
    void importReferentialCSV_itemImpact_validLifecycleStep_END_OF_LIFE() {
        ItemImpactRest rest = new ItemImpactRest();
        rest.setName("A");
        rest.setLifecycleStep("END_OF_LIFE"); // Valid lifecycle step
        rest.setCriterion("CLIMATE_CHANGE");
        rest.setCategory("CAT");
        rest.setLevel("LEV");
        rest.setSource("SRC");
        rest.setTier("T1");
        rest.setUnit("kg");
        rest.setValue(1.0);

        ItemImpactParseResult result = ItemImpactParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemImpactCsv(file)).thenReturn(result);
        when(referentialMapper.toItemImpactEntity(any()))
                .thenReturn(List.of(new ItemImpact()));

        ImportReportRest response =
                service.importReferentialCSV(ORG, 1L, "itemImpact", file);

        assertNotNull(response);
        verify(workspacePersistenceService).syncItemImpacts(eq(1L), any());
    }

    // =========================
    // AC4: ELECTRICITY CONSUMPTION VALIDATION
    // =========================

    @Test
    void importReferentialCSV_itemImpact_USING_withElectricityConsumption() {
        ItemImpactRest rest = new ItemImpactRest();
        rest.setName("A");
        rest.setLifecycleStep("USING");
        rest.setCriterion("CLIMATE_CHANGE");
        rest.setCategory("CAT");
        rest.setLevel("LEV");
        rest.setSource("SRC");
        rest.setTier("T1");
        rest.setUnit("kg");
        rest.setValue(1.0);
        rest.setAvgElectricityConsumption(100.0); // Electricity consumption provided

        ItemImpactParseResult result = ItemImpactParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemImpactCsv(file)).thenReturn(result);
        when(referentialMapper.toItemImpactEntity(any()))
                .thenReturn(List.of(new ItemImpact()));

        ImportReportRest response =
                service.importReferentialCSV(ORG, 1L, "itemImpact", file);

        assertNotNull(response);
        verify(workspacePersistenceService).syncItemImpacts(eq(1L), any());
    }

    @Test
    void importReferentialCSV_itemImpact_USING_withoutElectricityConsumption() {
        ItemImpactRest rest = new ItemImpactRest();
        rest.setName("A");
        rest.setLifecycleStep("USING");
        rest.setCriterion("CLIMATE_CHANGE");
        rest.setCategory("CAT");
        rest.setLevel("LEV");
        rest.setSource("SRC");
        rest.setTier("T1");
        rest.setUnit("kg");
        rest.setValue(1.0);
        // Missing avgElectricityConsumption for USING step

        ItemImpactParseResult result = ItemImpactParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemImpactCsv(file)).thenReturn(result);
        when(messageSource.getMessage(eq("itemimpact.electricity.consumption.missing"), any(), any()))
                .thenReturn("Electricity consumption must be indicated for all rows relating to the 'USING' step of the life cycle.");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemImpact", file));

        assertTrue(ex.getError().contains("Electricity consumption must be indicated"));
    }

    @Test
    void importReferentialCSV_itemImpact_MANUFACTURING_withoutElectricityConsumption() {
        ItemImpactRest rest = new ItemImpactRest();
        rest.setName("A");
        rest.setLifecycleStep("MANUFACTURING"); // Not USING, so electricity consumption not required
        rest.setCriterion("CLIMATE_CHANGE");
        rest.setCategory("CAT");
        rest.setLevel("LEV");
        rest.setSource("SRC");
        rest.setTier("T1");
        rest.setUnit("kg");
        rest.setValue(1.0);
        // No avgElectricityConsumption - OK for non-USING steps

        ItemImpactParseResult result = ItemImpactParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemImpactCsv(file)).thenReturn(result);
        when(referentialMapper.toItemImpactEntity(any()))
                .thenReturn(List.of(new ItemImpact()));

        ImportReportRest response =
                service.importReferentialCSV(ORG, 1L, "itemImpact", file);

        assertNotNull(response);
        verify(workspacePersistenceService).syncItemImpacts(eq(1L), any());
    }

    // =========================
    // AC6: DUPLICATE TRIPLET VALIDATION
    // =========================

    @Test
    void importReferentialCSV_itemImpact_duplicateTriplet() {
        ItemImpactRest rest1 = new ItemImpactRest();
        rest1.setName("Item1");
        rest1.setLifecycleStep("MANUFACTURING");
        rest1.setCriterion("CLIMATE_CHANGE");
        rest1.setCategory("CAT");
        rest1.setLevel("LEV");
        rest1.setSource("SRC");
        rest1.setTier("T1");
        rest1.setUnit("kg");
        rest1.setValue(1.0);

        ItemImpactRest rest2 = new ItemImpactRest();
        rest2.setName("Item1"); // Same triplet
        rest2.setLifecycleStep("MANUFACTURING");
        rest2.setCriterion("CLIMATE_CHANGE");
        rest2.setCategory("CAT2"); // Different category, but triplet is the same
        rest2.setLevel("LEV2");
        rest2.setSource("SRC2");
        rest2.setTier("T2");
        rest2.setUnit("kg");
        rest2.setValue(2.0);

        ItemImpactParseResult result = ItemImpactParseResult.builder()
                .data(List.of(rest1, rest2))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemImpactCsv(file)).thenReturn(result);
        when(messageSource.getMessage(eq("itemimpact.duplicate.triplet"), any(), any()))
                .thenReturn("Duplicate value found for the triplet (criterion/lifecyclestep/name) for the following item: Item1");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemImpact", file));

        assertTrue(ex.getError().contains("Duplicate value found for the triplet"));
        assertTrue(ex.getError().contains("Item1"));
    }

    @Test
    void importReferentialCSV_itemImpact_uniqueTriplets() {
        ItemImpactRest rest1 = new ItemImpactRest();
        rest1.setName("Item1");
        rest1.setLifecycleStep("MANUFACTURING");
        rest1.setCriterion("CLIMATE_CHANGE");
        rest1.setCategory("CAT");
        rest1.setLevel("LEV");
        rest1.setSource("SRC");
        rest1.setTier("T1");
        rest1.setUnit("kg");
        rest1.setValue(1.0);

        ItemImpactRest rest2 = new ItemImpactRest();
        rest2.setName("Item2"); // Different name
        rest2.setLifecycleStep("MANUFACTURING");
        rest2.setCriterion("CLIMATE_CHANGE");
        rest2.setCategory("CAT");
        rest2.setLevel("LEV");
        rest2.setSource("SRC");
        rest2.setTier("T1");
        rest2.setUnit("kg");
        rest2.setValue(1.0);

        ItemImpactParseResult result = ItemImpactParseResult.builder()
                .data(List.of(rest1, rest2))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemImpactCsv(file)).thenReturn(result);
        when(referentialMapper.toItemImpactEntity(any()))
                .thenReturn(List.of(new ItemImpact(), new ItemImpact()));

        ImportReportRest response =
                service.importReferentialCSV(ORG, 1L, "itemImpact", file);

        assertNotNull(response);
        verify(workspacePersistenceService).syncItemImpacts(eq(1L), any());
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

        ItemTypeParseResult result = ItemTypeParseResult.builder()
                .data(List.of(rest))
                .report(emptyReport())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemTypeCsv(file)).thenReturn(result);

        assertThrows(IllegalArgumentException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemType", file));
    }

    @Test
    void importReferentialCSV_nullFile_shouldThrow() {

        assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemType", null));
    }

    @Test
    void importReferentialCSV_validationErrorsInReport_shouldThrow() {

        ItemTypeParseResult result = ItemTypeParseResult.builder()
                .data(List.of())
                .report(
                        ImportReportRest.builder()
                                .errors(List.of("error"))
                                .build()
                )
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(referentialImportService.parseItemTypeCsv(file)).thenReturn(result);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.importReferentialCSV(ORG, 1L, "itemType", file));

        assertEquals("Validation errors in file", ex.getError());
    }
}