/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apireferential.business;

import com.soprasteria.g4it.backend.apireferential.mapper.ReferentialMapper;
import com.soprasteria.g4it.backend.apireferential.modeldb.Criterion;
import com.soprasteria.g4it.backend.apireferential.modeldb.ItemImpact;
import com.soprasteria.g4it.backend.apireferential.persistence.ReferentialPersistenceService;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.exception.BadRequestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MockitoExtension.class)
class ReferentialImportServiceTest {
    @Mock
    ReferentialMapper referentialMapper;
    @Mock
    ReferentialPersistenceService persistenceService;
    @Mock
    Validator validator;
    @Mock
    CacheManager cacheManager;
    @Mock
    MultipartFile file;
    @Mock
    Cache cache;

    @InjectMocks
    ReferentialImportService referentialImportService;

    @BeforeEach
    void setUp() {
        referentialImportService.referentialMapper = referentialMapper;
        referentialImportService.persistenceService = persistenceService;
        referentialImportService.validator = validator;
        referentialImportService.cacheManager = cacheManager;
    }

    @Test
    void testImportReferentialCSV_FileNull_ThrowsBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                referentialImportService.importReferentialCSV("criterion", null, "org")
        );
        assertTrue(ex.getError().contains("file"));
    }

    @Test
    void testImportReferentialCSV_FileEmpty_ThrowsBadRequest() {
        when(file.isEmpty()).thenReturn(true);
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                referentialImportService.importReferentialCSV("criterion", file, "org")
        );
        assertTrue(ex.getError().contains("file"));
    }

    @Test
    void testImportReferentialCSV_InvalidType_ThrowsBadRequest() {
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.csv");
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                referentialImportService.importReferentialCSV("invalidType", file, "org")
        );
        assertTrue(ex.getError().contains("type of referential"));
    }

    @Test
    void testProcessCriterionCsv_Valid() throws IOException {
        String csv = "header1,header2\nval1,val2\n";
         when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("test.csv");
        when(referentialMapper.csvCriterionToRest(any())).thenReturn(new CriterionRest());
        when(validator.validate(any())).thenReturn(Collections.emptySet());
        when(persistenceService.saveCriteria(any())).thenReturn(1);
        when(referentialMapper.toCriteriaEntity(any()))
                .thenReturn(List.of(new Criterion()));
        when(cacheManager.getCache(anyString())).thenReturn(cache);
        ImportReportRest report = referentialImportService.processCriterionCsv(file);
        assertEquals("test.csv", report.getFile());
        assertEquals(1L, report.getImportedLineNumber());
        assertTrue(report.getErrors().isEmpty());
    }

    @Test
    void testProcessCriterionCsv_InvalidCsv_AddsError() throws IOException {
        String csv = "header1,header2\nval1,val2\n";

        when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("test.csv");

        when(referentialMapper.csvCriterionToRest(any()))
                .thenReturn(new CriterionRest());

        ConstraintViolation<CriterionRest> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);

        when(path.toString()).thenReturn("field");
        when(violation.getMessage()).thenReturn("error");
        when(violation.getPropertyPath()).thenReturn(path);

        Set<ConstraintViolation<CriterionRest>> violations = new HashSet<>();
        violations.add(violation);

        when(validator.validate(any(CriterionRest.class)))
                .thenReturn((Set) violations);

        when(cacheManager.getCache(anyString())).thenReturn(cache);

        ImportReportRest report = referentialImportService.processCriterionCsv(file);

        assertFalse(report.getErrors().isEmpty());
    }

    @Test
    void testProcessCriterionCsv_IOException_AddsError() throws IOException {
        when(file.getBytes()).thenThrow(new IOException("fail"));
        when(file.getOriginalFilename()).thenReturn("test.csv");

        ImportReportRest report = referentialImportService.processCriterionCsv(file);

        assertFalse(report.getErrors().isEmpty());
    }

    @Test
    void testProcessLifecycleStepCsv_Valid() throws IOException {
        String csv = "header1\nval1\n";
         when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("test.csv");
        when(referentialMapper.csvLifecycleStepToRest(any())).thenReturn(new LifecycleStepRest());
        when(validator.validate(any())).thenReturn(Collections.emptySet());
        when(persistenceService.saveLifecycleSteps(any())).thenReturn(1);
        when(referentialMapper.toLifecycleStepEntity(any())).thenReturn(Collections.emptyList());
        when(cacheManager.getCache(anyString())).thenReturn(cache);
        ImportReportRest report = referentialImportService.processLifecycleStepCsv(file);
        assertEquals(1L, report.getImportedLineNumber());
    }

    @Test
    void testProcessHypothesisCsv_OrganizationMismatch_ThrowsBadRequest() throws IOException {
        String csv = "header1\nval1\n";
         when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("test.csv");
        HypothesisRest rest = mock(HypothesisRest.class);
        when(rest.getOrganization()).thenReturn("otherOrg");
        when(referentialMapper.csvHypothesisToRest(any())).thenReturn(rest);
        when(validator.validate(any())).thenReturn(Collections.emptySet());
        assertThrows(BadRequestException.class, () ->
                referentialImportService.processHypothesisCsv(file, "org")
        );
    }

    @Test
    void testProcessItemTypeCsv_OrganizationMismatch_ThrowsBadRequest() throws IOException {
        String csv = "header1\nval1\n";
         when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("test.csv");
        ItemTypeRest rest = mock(ItemTypeRest.class);
        when(rest.getOrganization()).thenReturn("otherOrg");
        when(referentialMapper.csvItemTypeToRest(any())).thenReturn(rest);
        when(validator.validate(any())).thenReturn(Collections.emptySet());
        assertThrows(BadRequestException.class, () ->
                referentialImportService.processItemTypeCsv(file, "org")
        );
    }

    @Test
    void testProcessMatchingItemCsv_OrganizationMismatch_ThrowsBadRequest() throws IOException {
        String csv = "header1\nval1\n";

         when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("test.csv");
        MatchingItemRest rest = mock(MatchingItemRest.class);
        when(rest.getOrganization()).thenReturn("otherOrg");
        when(referentialMapper.csvMatchingItemToRest(any())).thenReturn(rest);
        when(validator.validate(any())).thenReturn(Collections.emptySet());
        assertThrows(BadRequestException.class, () ->
                referentialImportService.processMatchingItemCsv(file, "org")
        );
    }


    @Test
    void testProcessHypothesisCsv_Valid() throws IOException {
        String csv = "header1\nval1\n";
         when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("test.csv");

        HypothesisRest rest = mock(HypothesisRest.class);
        when(rest.getOrganization()).thenReturn("org");

        when(referentialMapper.csvHypothesisToRest(any())).thenReturn(rest);
        when(validator.validate(any())).thenReturn(Collections.emptySet());
        when(referentialMapper.toHypothesisEntity(any())).thenReturn(Collections.emptyList());
        when(persistenceService.saveHypotheses(any(), eq("org"))).thenReturn(1);
        when(cacheManager.getCache(anyString())).thenReturn(cache);

        ImportReportRest report = referentialImportService.processHypothesisCsv(file, "org");

        assertEquals(1L, report.getImportedLineNumber());
        assertTrue(report.getErrors().isEmpty());
    }

    @Test
    void testProcessItemTypeCsv_Valid() throws IOException {
        String csv = "header1\nval1\n";
         when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("test.csv");

        ItemTypeRest rest = mock(ItemTypeRest.class);
        when(rest.getOrganization()).thenReturn("org");

        when(referentialMapper.csvItemTypeToRest(any())).thenReturn(rest);
        when(validator.validate(any())).thenReturn(Collections.emptySet());
        when(referentialMapper.toItemTypeEntity(any())).thenReturn(Collections.emptyList());
        when(persistenceService.saveItemTypes(any(), eq("org"))).thenReturn(1);
        when(cacheManager.getCache(anyString())).thenReturn(cache);

        ImportReportRest report = referentialImportService.processItemTypeCsv(file, "org");

        assertEquals(1L, report.getImportedLineNumber());
        assertTrue(report.getErrors().isEmpty());
    }

    @Test
    void testProcessMatchingItemCsv_Valid() throws IOException {
        String csv = "header1\nval1\n";
         when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("test.csv");

        MatchingItemRest rest = mock(MatchingItemRest.class);
        when(rest.getOrganization()).thenReturn("org");

        when(referentialMapper.csvMatchingItemToRest(any())).thenReturn(rest);
        when(validator.validate(any())).thenReturn(Collections.emptySet());
        when(referentialMapper.toMatchingEntity(any())).thenReturn(Collections.emptyList());
        when(persistenceService.saveItemMatchings(any(), eq("org"))).thenReturn(1);
        when(cacheManager.getCache(anyString())).thenReturn(cache);

        ImportReportRest report = referentialImportService.processMatchingItemCsv(file, "org");

        assertEquals(1L, report.getImportedLineNumber());
        assertTrue(report.getErrors().isEmpty());
    }

    @Test
    void testProcessLifecycleStepCsv_Invalid_AddsError() throws IOException {
        String csv = "header1\nval1\n";
         when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("test.csv");

        when(referentialMapper.csvLifecycleStepToRest(any())).thenReturn(new LifecycleStepRest());

        jakarta.validation.ConstraintViolation<LifecycleStepRest> violation = mock(jakarta.validation.ConstraintViolation.class);
        jakarta.validation.Path path = mock(jakarta.validation.Path.class);

        when(path.toString()).thenReturn("field");
        when(violation.getMessage()).thenReturn("error");
        when(violation.getPropertyPath()).thenReturn(path);

        Set<jakarta.validation.ConstraintViolation<LifecycleStepRest>> violations = new HashSet<>();
        violations.add(violation);

        when(validator.validate(any()))
                .thenReturn((Set) violations);
        when(cacheManager.getCache(anyString())).thenReturn(cache);

        ImportReportRest report = referentialImportService.processLifecycleStepCsv(file);

        assertFalse(report.getErrors().isEmpty());
    }

    @Test
    void testProcessHypothesisCsv_IOException() throws IOException {
        when(file.getBytes()).thenThrow(new IOException("fail"));
        when(file.getOriginalFilename()).thenReturn("test.csv");

        ImportReportRest report = referentialImportService.processHypothesisCsv(file, "org");

        assertFalse(report.getErrors().isEmpty());
    }

    @Test
    void testParseItemTypeCsv_Valid() throws IOException {
        String csv = """
            type;category;comment;default_lifespan;is_server;source;ref_default_item;version
t;c;com;1;true;s;ref;1
""";
         when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("test.csv");

        ItemTypeRest rest = new ItemTypeRest();

        when(referentialMapper.csvItemTypeToRest(any())).thenReturn(rest);
        when(validator.validate(any())).thenReturn(Collections.emptySet());

        ItemTypeParseResult result = referentialImportService.parseItemTypeCsv(file);

        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        assertTrue(result.getReport().getErrors().isEmpty());
    }

    @Test
    void testParseItemTypeCsv_InvalidHeaders_ThrowsException() throws IOException {
        String csv = "wrong,header\nval1,val2\n";

         when(file.getBytes()).thenReturn(csv.getBytes());

        assertThrows(BadRequestException.class, () ->
                referentialImportService.parseItemTypeCsv(file)
        );
    }

    @Test
    void testParseMatchingItemCsv_Valid() throws IOException {
        String csv = """
        itemSource;refItemTarget
        A;B
        """;

         when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("test.csv");

        MatchingItemRest rest = new MatchingItemRest();

        when(referentialMapper.csvMatchingItemToRest(any())).thenReturn(rest);
        when(validator.validate(any())).thenReturn(Collections.emptySet());

        MatchingItemParseResult result = referentialImportService.parseMatchingItemCsv(file);

        assertEquals(1, result.getData().size());
        assertTrue(result.getReport().getErrors().isEmpty());
    }

    @Test
    void testParseItemImpactCsv_Valid() throws IOException {
        String csv = """
        criterion;lifecycleStep;name;category;avgElectricityConsumption;description;location;level;source;tier;unit;value;version
        c;l;n;cat;1;desc;loc;lev;src;t;u;10;1
        """;

         when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("test.csv");

        ItemImpactRest rest = new ItemImpactRest();

        when(referentialMapper.csvItemImpactToRest(any())).thenReturn(rest);
        when(validator.validate(any())).thenReturn(Collections.emptySet());

        ItemImpactParseResult result = referentialImportService.parseItemImpactCsv(file);

        assertEquals(1, result.getData().size());
        assertTrue(result.getReport().getErrors().isEmpty());
    }

    @Test
    void testParseItemTypeCsv_IOException() throws Exception {

        when(file.getBytes()).thenThrow(new IOException("fail"));
        when(file.getOriginalFilename()).thenReturn("test.csv");

        ItemTypeParseResult result =
                referentialImportService.parseItemTypeCsv(file);

        assertNotNull(result.getReport());
        assertFalse(result.getReport().getErrors().isEmpty());
    }

    @Test
    void testProcessItemImpactCsv_IOExceptionDuringParsing() throws Exception {

        when(file.getBytes()).thenThrow(new IOException("fail"));
        when(file.getOriginalFilename()).thenReturn("test.csv");

        ImportReportRest report =
                referentialImportService.processItemImpactCsv(file, "org");

        assertFalse(report.getErrors().isEmpty());
    }


    @Test
    void testParseMatchingItemCsv_HeaderWrongOrder() throws IOException {
        String csv = "refItemTarget;itemSource\nA;B\n";

        when(file.getBytes()).thenReturn(csv.getBytes());

        assertThrows(BadRequestException.class,
                () -> referentialImportService.parseMatchingItemCsv(file));
    }

    @Test
    void testParseItemTypeCsv_HeaderSizeMismatch() throws IOException {
        String csv = "a;b\n1;2\n";

        when(file.getBytes()).thenReturn(csv.getBytes());

        assertThrows(BadRequestException.class,
                () -> referentialImportService.parseItemTypeCsv(file));
    }

    @Test
    void testParseItemTypeCsv_WithValidationErrors() throws IOException {

        String csv = """
    type;category;comment;default_lifespan;is_server;source;ref_default_item;version
    t;c;com;1;true;s;ref;1
    """;

        when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("test.csv");

        ItemTypeRest rest = new ItemTypeRest();
        when(referentialMapper.csvItemTypeToRest(any())).thenReturn(rest);

        ConstraintViolation<ItemTypeRest> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);

        when(path.toString()).thenReturn("field");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("error");

        Set<ConstraintViolation<ItemTypeRest>> violations = new HashSet<>();
        violations.add(violation);

        when(validator.validate(any()))
                .thenReturn((Set) violations);

        ItemTypeParseResult result =
                referentialImportService.parseItemTypeCsv(file);

        assertFalse(result.getReport().getErrors().isEmpty());
    }

    @Test
    void testGetBytesSafe_ReturnsNull() throws Exception {

        when(file.getBytes()).thenThrow(new IOException("fail"));
        when(file.getOriginalFilename()).thenReturn("test.csv");

        ImportReportRest report =
                referentialImportService.processLifecycleStepCsv(file);

        assertFalse(report.getErrors().isEmpty());
    }

    @Test
    void testParseItemImpactCsv_CommaInAvgElectricityConsumption() throws Exception {

        String csv = """
    criterion;lifecycleStep;name;category;avgElectricityConsumption;description;location;level;source;tier;unit;value;version
    c;l;n;cat;1,5;desc;loc;lev;src;t;u;10.0;1
    """;

        when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("test.csv");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> referentialImportService.parseItemImpactCsv(file));

        assertEquals("csv.decimal.comma.invalid: 2", ex.getError());
    }

    @Test
    void testParseItemImpactCsv_CommaInValue() throws Exception {

        String csv = """
    criterion;lifecycleStep;name;category;avgElectricityConsumption;description;location;level;source;tier;unit;value;version
    c;l;n;cat;1.5;desc;loc;lev;src;t;u;10,5;1
    """;

        when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("test.csv");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> referentialImportService.parseItemImpactCsv(file));

        assertEquals("csv.decimal.comma.invalid: 2", ex.getError());
    }

    @Test
    void testProcessItemImpactCsv_CommaInAvgElectricityConsumption() throws Exception {

        String csv = """
    criterion;lifecycleStep;name;category;avgElectricityConsumption;description;location;level;source;tier;unit;value;subscriber;version
    c;l;n;cat;1,5;desc;loc;lev;src;t;u;10.0;org;1
    """;

        when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("test.csv");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> referentialImportService.processItemImpactCsv(file, "org"));

        assertEquals("csv.decimal.comma.invalid: 2", ex.getError());
    }

    @Test
    void testProcessItemImpactCsv_CommaInValue() throws Exception {

        String csv = """
    criterion;lifecycleStep;name;category;avgElectricityConsumption;description;location;level;source;tier;unit;value;subscriber;version
    c;l;n;cat;1.5;desc;loc;lev;src;t;u;10,5;org;1
    """;

        when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("test.csv");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> referentialImportService.processItemImpactCsv(file, "org"));

        assertEquals("csv.decimal.comma.invalid: 2", ex.getError());
    }

    @Test
    void testParseItemImpactCsv_NumberFormatExceptionCaught() throws Exception {

        String csv = """
    criterion;lifecycleStep;name;category;avgElectricityConsumption;description;location;level;source;tier;unit;value;version
    c;l;n;cat;;desc;loc;lev;src;t;u;10.5;1
    """;

        when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("test.csv");

        // Mock the mapper to throw NumberFormatException when parsing
        when(referentialMapper.csvItemImpactToRest(any())).thenThrow(new NumberFormatException("For input string: \"1,5\""));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> referentialImportService.parseItemImpactCsv(file));

        assertEquals("csv.decimal.comma.invalid: 2", ex.getError());
    }

    @Test
    void shouldThrowBadRequestWhenFileIsNull() {
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> referentialImportService.importReferentialCSV(
                        "criterion",
                        null,
                        "org")
        );

        assertEquals("file", exception.getField());
        assertTrue(exception.getError().contains("The file does not exist or it is empty"));
    }

    @Test
    void shouldThrowBadRequestWhenFileIsEmpty() {
        when(file.isEmpty()).thenReturn(true);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> referentialImportService.importReferentialCSV(
                        "criterion",
                        file,
                        "org")
        );

        assertEquals("file", exception.getField());
        assertTrue(exception.getError().contains("The file does not exist or it is empty"));

        verify(file).isEmpty();
        verifyNoInteractions(referentialMapper);
        verifyNoInteractions(persistenceService);
    }

    @Test
    void shouldThrowBadRequestWhenReferentialTypeIsInvalid() {
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("referential.csv");

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> referentialImportService.importReferentialCSV(
                        "invalidType",
                        file,
                        "org")
        );

        assertEquals("type", exception.getField());
        assertEquals(
                "type of referential 'invalidType' does not exist",
                exception.getError()
        );

        verify(file).isEmpty();
        verify(file).getOriginalFilename();

        verifyNoInteractions(referentialMapper);
        verifyNoInteractions(persistenceService);
    }

    @Test
    void shouldProcessCriterionCsvSuccessfully() throws IOException {
        // Given
        String csv = """
            criterion
            value
            """;

        CriterionRest criterionRest = new CriterionRest();
        List<Criterion> criterionEntities = List.of(new Criterion());

        when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("criterion.csv");

        when(referentialMapper.csvCriterionToRest(any(CSVRecord.class)))
                .thenReturn(criterionRest);

        when(validator.validate(criterionRest))
                .thenReturn(Collections.emptySet());

        when(referentialMapper.toCriteriaEntity(anyList()))
                .thenReturn(criterionEntities);

        when(persistenceService.saveCriteria(criterionEntities))
                .thenReturn(1);

        when(cacheManager.getCache("ref_getAllCriteria"))
                .thenReturn(cache);

        // When
        ImportReportRest report = referentialImportService.processCriterionCsv(file);

        // Then
        assertNotNull(report);
        assertEquals("criterion.csv", report.getFile());
        assertEquals(1L, report.getImportedLineNumber());
        assertTrue(report.getErrors().isEmpty());

        verify(referentialMapper).csvCriterionToRest(any(CSVRecord.class));
        verify(referentialMapper).toCriteriaEntity(anyList());
        verify(persistenceService).saveCriteria(criterionEntities);
        verify(cache).clear();
    }

    @Test
    void shouldAddValidationErrorWhenCriterionIsInvalid() throws IOException {
        // Given
        String csv = """
            criterion
            value
            """;

        CriterionRest criterionRest = new CriterionRest();

        @SuppressWarnings("unchecked")
        ConstraintViolation<CriterionRest> violation = mock(ConstraintViolation.class);
        Path propertyPath = mock(Path.class);

        when(propertyPath.toString()).thenReturn("name");
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn("must not be blank");

        Set<ConstraintViolation<CriterionRest>> violations = Set.of(violation);

        when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("criterion.csv");

        when(referentialMapper.csvCriterionToRest(any(CSVRecord.class)))
                .thenReturn(criterionRest);

        when(validator.validate(criterionRest))
                .thenReturn(violations);

        when(referentialMapper.toCriteriaEntity(anyList()))
                .thenReturn(Collections.emptyList());

        when(persistenceService.saveCriteria(anyList()))
                .thenReturn(0);

        when(cacheManager.getCache("ref_getAllCriteria"))
                .thenReturn(cache);

        // When
        ImportReportRest report = referentialImportService.processCriterionCsv(file);

        // Then
        assertNotNull(report);
        assertEquals(1, report.getErrors().size());
        assertTrue(report.getErrors().get(0).contains("line 2"));
        assertTrue(report.getErrors().get(0).contains("name"));
        assertTrue(report.getErrors().get(0).contains("must not be blank"));

        assertEquals(0L, report.getImportedLineNumber());

        verify(persistenceService).saveCriteria(Collections.emptyList());
        verify(cache).clear();
    }

    @Test
    void shouldReturnErrorWhenReadingCriterionFileFails() throws IOException {
        // Given
        when(file.getBytes()).thenThrow(new IOException("Unable to read file"));
        when(file.getOriginalFilename()).thenReturn("criterion.csv");

        // When
        ImportReportRest report = referentialImportService.processCriterionCsv(file);

        // Then
        assertNotNull(report);
        assertEquals("criterion.csv", report.getFile());
        assertNull(report.getImportedLineNumber());
        assertEquals(1, report.getErrors().size());
        assertTrue(report.getErrors().get(0).contains("Cannot read csv file"));
        assertTrue(report.getErrors().get(0).contains("Unable to read file"));

        verify(file).getBytes();
        verify(file).getOriginalFilename();

        verifyNoInteractions(referentialMapper);
        verifyNoInteractions(persistenceService);
        verifyNoInteractions(cacheManager);
    }

    @Test
    void shouldProcessLifecycleStepCsvSuccessfully() throws IOException {
        // Given
        String csv = """
            lifecycleStep
            Manufacturing
            """;

        LifecycleStepRest lifecycleStepRest = new LifecycleStepRest();

        when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("lifecycle-step.csv");

        when(referentialMapper.csvLifecycleStepToRest(any(CSVRecord.class)))
                .thenReturn(lifecycleStepRest);

        when(validator.validate(lifecycleStepRest))
                .thenReturn(Collections.emptySet());

        when(referentialMapper.toLifecycleStepEntity(anyList()))
                .thenReturn(Collections.emptyList());

        when(persistenceService.saveLifecycleSteps(anyList()))
                .thenReturn(1);

        when(cacheManager.getCache("ref_getAllLifecycleSteps"))
                .thenReturn(cache);

        // When
        ImportReportRest report = referentialImportService.processLifecycleStepCsv(file);

        // Then
        assertNotNull(report);
        assertEquals("lifecycle-step.csv", report.getFile());
        assertEquals(1L, report.getImportedLineNumber());
        assertTrue(report.getErrors().isEmpty());

        verify(referentialMapper).csvLifecycleStepToRest(any(CSVRecord.class));
        verify(referentialMapper).toLifecycleStepEntity(anyList());
        verify(persistenceService).saveLifecycleSteps(anyList());
        verify(cache).clear();
    }

    @Test
    void shouldAddValidationErrorWhenLifecycleStepIsInvalid() throws IOException {
        // Given
        String csv = """
            lifecycleStep
            Manufacturing
            """;

        LifecycleStepRest lifecycleStepRest = new LifecycleStepRest();

        @SuppressWarnings("unchecked")
        ConstraintViolation<LifecycleStepRest> violation = mock(ConstraintViolation.class);
        Path propertyPath = mock(Path.class);

        when(propertyPath.toString()).thenReturn("name");
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn("must not be blank");

        Set<ConstraintViolation<LifecycleStepRest>> violations = Set.of(violation);

        when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("lifecycle-step.csv");

        when(referentialMapper.csvLifecycleStepToRest(any(CSVRecord.class)))
                .thenReturn(lifecycleStepRest);

        when(validator.validate(lifecycleStepRest))
                .thenReturn(violations);

        when(referentialMapper.toLifecycleStepEntity(anyList()))
                .thenReturn(Collections.emptyList());

        when(persistenceService.saveLifecycleSteps(anyList()))
                .thenReturn(0);

        when(cacheManager.getCache("ref_getAllLifecycleSteps"))
                .thenReturn(cache);

        // When
        ImportReportRest report = referentialImportService.processLifecycleStepCsv(file);

        // Then
        assertNotNull(report);
        assertEquals("lifecycle-step.csv", report.getFile());
        assertEquals(0L, report.getImportedLineNumber());

        assertEquals(1, report.getErrors().size());
        assertTrue(report.getErrors().get(0).contains("line 2"));
        assertTrue(report.getErrors().get(0).contains("name"));
        assertTrue(report.getErrors().get(0).contains("must not be blank"));

        verify(referentialMapper).csvLifecycleStepToRest(any(CSVRecord.class));
        verify(referentialMapper).toLifecycleStepEntity(anyList());
        verify(persistenceService).saveLifecycleSteps(anyList());
        verify(cache).clear();
    }

    @Test
    void shouldReturnErrorWhenReadingLifecycleStepFileFails() throws IOException {
        // Given
        when(file.getBytes()).thenThrow(new IOException("Unable to read file"));
        when(file.getOriginalFilename()).thenReturn("lifecycle-step.csv");

        // When
        ImportReportRest report = referentialImportService.processLifecycleStepCsv(file);

        // Then
        assertNotNull(report);
        assertEquals("lifecycle-step.csv", report.getFile());
        assertNull(report.getImportedLineNumber());

        assertEquals(1, report.getErrors().size());
        assertTrue(report.getErrors().get(0).contains("Cannot read csv file"));
        assertTrue(report.getErrors().get(0).contains("Unable to read file"));

        verify(file).getBytes();
        verify(file).getOriginalFilename();

        verifyNoInteractions(referentialMapper);
        verifyNoInteractions(persistenceService);
        verifyNoInteractions(cacheManager);
    }

    @Test
    void shouldAddValidationErrorWhenHypothesisIsInvalid() throws IOException {

        String csv = """
            hypothesis
            Laptop
            """;

        HypothesisRest hypothesis = new HypothesisRest();
        hypothesis.setOrganization("org");

        @SuppressWarnings("unchecked")
        ConstraintViolation<HypothesisRest> violation = mock(ConstraintViolation.class);
        Path propertyPath = mock(Path.class);

        when(propertyPath.toString()).thenReturn("name");
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn("must not be blank");

        Set<ConstraintViolation<HypothesisRest>> violations =
                Set.of(violation);

        when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("hypothesis.csv");

        when(referentialMapper.csvHypothesisToRest(any(CSVRecord.class)))
                .thenReturn(hypothesis);

        when(validator.validate(hypothesis))
                .thenReturn(violations);

        when(referentialMapper.toHypothesisEntity(anyList()))
                .thenReturn(Collections.emptyList());

        when(persistenceService.saveHypotheses(anyList(), eq("org")))
                .thenReturn(0);

        when(cacheManager.getCache("ref_getHypotheses"))
                .thenReturn(cache);

        ImportReportRest report =
                referentialImportService.processHypothesisCsv(file, "org");

        assertEquals(1, report.getErrors().size());
        assertEquals(0L, report.getImportedLineNumber());

        verify(persistenceService)
                .saveHypotheses(anyList(), eq("org"));

        verify(cache).clear();
    }

    @Test
    void shouldThrowBadRequestWhenHypothesisOrganizationDoesNotMatch() throws IOException {

        String csv = """
            hypothesis
            Laptop
            """;

        HypothesisRest hypothesis = new HypothesisRest();
        hypothesis.setOrganization("another-org");

        when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("hypothesis.csv");

        when(referentialMapper.csvHypothesisToRest(any(CSVRecord.class)))
                .thenReturn(hypothesis);

        when(validator.validate(hypothesis))
                .thenReturn(Collections.emptySet());

        BadRequestException exception =
                assertThrows(
                        BadRequestException.class,
                        () -> referentialImportService.processHypothesisCsv(file, "org")
                );

        assertEquals("subscriber", exception.getField());

        assertEquals(
                "The column subscriber does not contain all values equal to 'org'",
                exception.getError());

        verify(referentialMapper, never())
                .toHypothesisEntity(anyList());

        verify(persistenceService, never())
                .saveHypotheses(anyList(), anyString());

        verifyNoInteractions(cache);
    }

    @Test
    void shouldReturnErrorWhenReadingHypothesisFileFails() throws IOException {

        when(file.getBytes())
                .thenThrow(new IOException("Unable to read file"));

        when(file.getOriginalFilename())
                .thenReturn("hypothesis.csv");

        ImportReportRest report =
                referentialImportService.processHypothesisCsv(file, "org");

        assertNotNull(report);
        assertEquals("hypothesis.csv", report.getFile());
        assertNull(report.getImportedLineNumber());

        assertEquals(1, report.getErrors().size());

        assertTrue(report.getErrors().get(0)
                .contains("Cannot read csv file"));

        verify(file).getBytes();

        verifyNoInteractions(referentialMapper);
        verifyNoInteractions(persistenceService);
        verifyNoInteractions(cacheManager);
    }

    @Test
    void shouldProcessItemTypeCsvSuccessfully() throws IOException {

        String csv = """
            type
            Laptop
            """;

        ItemTypeRest itemType = new ItemTypeRest();
        itemType.setOrganization("org");

        when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("item-type.csv");

        when(referentialMapper.csvItemTypeToRest(any(CSVRecord.class)))
                .thenReturn(itemType);

        when(validator.validate(itemType))
                .thenReturn(Collections.emptySet());

        when(referentialMapper.toItemTypeEntity(anyList()))
                .thenReturn(Collections.emptyList());

        when(persistenceService.saveItemTypes(anyList(), eq("org")))
                .thenReturn(1);

        when(cacheManager.getCache("ref_getItemTypes"))
                .thenReturn(cache);

        ImportReportRest report =
                referentialImportService.processItemTypeCsv(file, "org");

        assertNotNull(report);
        assertEquals("item-type.csv", report.getFile());
        assertEquals(1L, report.getImportedLineNumber());
        assertTrue(report.getErrors().isEmpty());

        verify(referentialMapper).csvItemTypeToRest(any(CSVRecord.class));
        verify(referentialMapper).toItemTypeEntity(anyList());
        verify(persistenceService).saveItemTypes(anyList(), eq("org"));
        verify(cache).clear();
    }

    @Test
    void shouldAddValidationErrorWhenItemTypeIsInvalid() throws IOException {

        String csv = """
            type
            Laptop
            """;

        ItemTypeRest itemType = new ItemTypeRest();
        itemType.setOrganization("org");

        @SuppressWarnings("unchecked")
        ConstraintViolation<ItemTypeRest> violation =
                mock(ConstraintViolation.class);

        Path propertyPath = mock(Path.class);

        when(propertyPath.toString()).thenReturn("type");
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn("must not be blank");

        Set<ConstraintViolation<ItemTypeRest>> violations =
                Set.of(violation);

        when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("item-type.csv");

        when(referentialMapper.csvItemTypeToRest(any(CSVRecord.class)))
                .thenReturn(itemType);

        when(validator.validate(itemType))
                .thenReturn(violations);

        when(referentialMapper.toItemTypeEntity(anyList()))
                .thenReturn(Collections.emptyList());

        when(persistenceService.saveItemTypes(anyList(), eq("org")))
                .thenReturn(0);

        when(cacheManager.getCache("ref_getItemTypes"))
                .thenReturn(cache);

        ImportReportRest report =
                referentialImportService.processItemTypeCsv(file, "org");

        assertEquals(1, report.getErrors().size());
        assertEquals(0L, report.getImportedLineNumber());

        verify(persistenceService)
                .saveItemTypes(anyList(), eq("org"));

        verify(cache).clear();
    }

    @Test
    void shouldThrowBadRequestWhenItemTypeOrganizationDoesNotMatch() throws IOException {

        String csv = """
            type
            Laptop
            """;

        ItemTypeRest itemType = new ItemTypeRest();
        itemType.setOrganization("another-org");

        when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("item-type.csv");

        when(referentialMapper.csvItemTypeToRest(any(CSVRecord.class)))
                .thenReturn(itemType);

        when(validator.validate(itemType))
                .thenReturn(Collections.emptySet());

        BadRequestException exception =
                assertThrows(
                        BadRequestException.class,
                        () -> referentialImportService.processItemTypeCsv(file, "org")
                );

        assertEquals("subscriber", exception.getField());

        assertEquals(
                "The column subscriber does not contain all values equal to 'org'",
                exception.getError());

        verify(referentialMapper, never())
                .toItemTypeEntity(anyList());

        verify(persistenceService, never())
                .saveItemTypes(anyList(), anyString());

        verifyNoInteractions(cache);
    }

    @Test
    void shouldReturnErrorWhenReadingItemTypeFileFails() throws IOException {

        when(file.getBytes()).thenThrow(new IOException("Unable to read file"));
        when(file.getOriginalFilename()).thenReturn("item-type.csv");

        ImportReportRest report =
                referentialImportService.processItemTypeCsv(file, "org");

        assertNotNull(report);
        assertEquals("item-type.csv", report.getFile());

        // importedLineNumber is never set when getBytesSafe() fails
        assertNull(report.getImportedLineNumber());

        assertEquals(1, report.getErrors().size());
        assertTrue(report.getErrors().get(0).contains("Cannot read csv file"));

        verify(file).getBytes();
        verifyNoInteractions(referentialMapper);
        verifyNoInteractions(persistenceService);
        verifyNoInteractions(cacheManager);
    }

    @Test
    void shouldProcessMatchingItemCsvSuccessfully() throws IOException {

        String csv = """
            itemSource
            Laptop
            """;

        MatchingItemRest matchingItem = new MatchingItemRest();
        matchingItem.setOrganization("org");

        when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("matching-item.csv");

        when(referentialMapper.csvMatchingItemToRest(any(CSVRecord.class)))
                .thenReturn(matchingItem);

        when(validator.validate(matchingItem))
                .thenReturn(Collections.emptySet());

        when(referentialMapper.toMatchingEntity(anyList()))
                .thenReturn(Collections.emptyList());

        when(persistenceService.saveItemMatchings(anyList(), eq("org")))
                .thenReturn(1);

        when(cacheManager.getCache("ref_getMatchingItem"))
                .thenReturn(cache);

        ImportReportRest report =
                referentialImportService.processMatchingItemCsv(file, "org");

        assertNotNull(report);
        assertEquals("matching-item.csv", report.getFile());
        assertEquals(1L, report.getImportedLineNumber());
        assertTrue(report.getErrors().isEmpty());

        verify(referentialMapper).csvMatchingItemToRest(any(CSVRecord.class));
        verify(referentialMapper).toMatchingEntity(anyList());
        verify(persistenceService).saveItemMatchings(anyList(), eq("org"));
        verify(cache).clear();
    }

    @Test
    void shouldAddValidationErrorWhenMatchingItemIsInvalid() throws IOException {

        String csv = """
            itemSource
            Laptop
            """;

        MatchingItemRest matchingItem = new MatchingItemRest();
        matchingItem.setOrganization("org");

        @SuppressWarnings("unchecked")
        ConstraintViolation<MatchingItemRest> violation =
                mock(ConstraintViolation.class);

        Path propertyPath = mock(Path.class);

        when(propertyPath.toString()).thenReturn("itemSource");
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn("must not be blank");

        Set<ConstraintViolation<MatchingItemRest>> violations =
                Set.of(violation);

        when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("matching-item.csv");

        when(referentialMapper.csvMatchingItemToRest(any(CSVRecord.class)))
                .thenReturn(matchingItem);

        when(validator.validate(matchingItem))
                .thenReturn(violations);

        when(referentialMapper.toMatchingEntity(anyList()))
                .thenReturn(Collections.emptyList());

        when(persistenceService.saveItemMatchings(anyList(), eq("org")))
                .thenReturn(0);

        when(cacheManager.getCache("ref_getMatchingItem"))
                .thenReturn(cache);

        ImportReportRest report =
                referentialImportService.processMatchingItemCsv(file, "org");

        assertEquals(1, report.getErrors().size());
        assertEquals(0L, report.getImportedLineNumber());

        verify(persistenceService)
                .saveItemMatchings(anyList(), eq("org"));

        verify(cache).clear();
    }

    @Test
    void shouldThrowBadRequestWhenMatchingItemOrganizationDoesNotMatch() throws IOException {

        String csv = """
            itemSource
            Laptop
            """;

        MatchingItemRest matchingItem = new MatchingItemRest();
        matchingItem.setOrganization("another-org");

        when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("matching-item.csv");

        when(referentialMapper.csvMatchingItemToRest(any(CSVRecord.class)))
                .thenReturn(matchingItem);

        when(validator.validate(matchingItem))
                .thenReturn(Collections.emptySet());

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> referentialImportService.processMatchingItemCsv(file, "org")
        );

        assertEquals("subscriber", exception.getField());

        assertEquals(
                "The column subscriber does not contain all values equal to 'org'",
                exception.getError());

        verify(referentialMapper, never()).toMatchingEntity(anyList());
        verify(persistenceService, never()).saveItemMatchings(anyList(), anyString());
        verifyNoInteractions(cache);
    }

    @Test
    void shouldReturnErrorWhenReadingMatchingItemFileFails() throws IOException {

        when(file.getBytes())
                .thenThrow(new IOException("Unable to read file"));

        when(file.getOriginalFilename())
                .thenReturn("matching-item.csv");

        ImportReportRest report =
                referentialImportService.processMatchingItemCsv(file, "org");

        assertNotNull(report);
        assertEquals("matching-item.csv", report.getFile());

        assertNull(report.getImportedLineNumber());

        assertEquals(1, report.getErrors().size());
        assertTrue(report.getErrors().get(0).contains("Cannot read csv file"));
        assertTrue(report.getErrors().get(0).contains("Unable to read file"));

        verify(file).getBytes();

        verifyNoInteractions(referentialMapper);
        verifyNoInteractions(persistenceService);
        verifyNoInteractions(cacheManager);
    }

    @Test
    void shouldProcessItemImpactCsvSuccessfully() throws IOException {

        String csv = """
            criterion;lifecycleStep;name;category;avgElectricityConsumption;description;location;level;source;tier;unit;value;subscriber;version
            c1;l1;n1;cat;10.5;desc;FR;1;ADEME;1;kg;100.2;org;1
            """;

        ItemImpactRest rest = new ItemImpactRest();
        rest.setOrganization("org");

        when(file.getBytes()).thenReturn(csv.getBytes(StandardCharsets.UTF_8));
        when(file.getOriginalFilename()).thenReturn("item-impact.csv");

        when(referentialMapper.csvItemImpactToRest(any(CSVRecord.class)))
                .thenReturn(rest);

        when(validator.validate(rest))
                .thenReturn(Collections.emptySet());

        when(referentialMapper.toItemImpactEntity(anyList()))
                .thenReturn(Collections.emptyList());

        when(cacheManager.getCache("ref_getItemImpacts"))
                .thenReturn(cache);

        when(cacheManager.getCache("ref_getCountries"))
                .thenReturn(cache);

        ImportReportRest report =
                referentialImportService.processItemImpactCsv(file, "org");

        assertNotNull(report);
        assertTrue(report.getErrors().isEmpty());
        assertEquals(1L, report.getImportedLineNumber());

        verify(persistenceService)
                .deleteItemImpactsByOrganization("org");

        verify(persistenceService)
                .saveItemImpacts(anyList());

        verify(cache, times(2)).clear();
    }

    @Test
    void shouldThrowExceptionWhenHeadersAreMissing() throws IOException {

        String csv = """
            criterion;lifecycleStep;name;category
            c1;l1;n1;cat
            """;

        when(file.getBytes()).thenReturn(csv.getBytes(StandardCharsets.UTF_8));

        BadRequestException exception =
                assertThrows(
                        BadRequestException.class,
                        () -> referentialImportService.processItemImpactCsv(file, "org")
                );

        assertEquals(
                "csv.columns.missing:ItemImpact:[avgElectricityConsumption, description, location, level, source, tier, unit, value, subscriber, version]",
                exception.getError());

        verifyNoInteractions(persistenceService);
    }

    @Test
    void shouldThrowExceptionWhenSubscriberDoesNotMatch() throws IOException {

        String csv = """
            criterion;lifecycleStep;name;category;avgElectricityConsumption;description;location;level;source;tier;unit;value;subscriber;version
            c1;l1;n1;cat;10.5;desc;FR;1;ADEME;1;kg;100.2;another-org;1
            """;

        ItemImpactRest rest = new ItemImpactRest();
        rest.setOrganization("another-org");

        when(file.getBytes()).thenReturn(csv.getBytes(StandardCharsets.UTF_8));
        when(file.getOriginalFilename()).thenReturn("item-impact.csv");

        when(referentialMapper.csvItemImpactToRest(any(CSVRecord.class)))
                .thenReturn(rest);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> referentialImportService.processItemImpactCsv(file, "org")
        );

        assertEquals("subscriber", exception.getField());
        assertEquals(
                "Line 2 : The column subscriber must be 'org'",
                exception.getError());

        verify(persistenceService, never())
                .deleteItemImpactsByOrganization(anyString());

        verify(persistenceService, never())
                .saveItemImpacts(anyList());
    }

    @Test
    void shouldThrowExceptionWhenMapperThrowsNumberFormatException() throws IOException {

        String csv = """
            criterion;lifecycleStep;name;category;avgElectricityConsumption;description;location;level;source;tier;unit;value;version
            c1;l1;n1;cat;10.5;desc;FR;1;ADEME;1;kg;100.2;1
            """;

        when(file.getBytes()).thenReturn(csv.getBytes(StandardCharsets.UTF_8));

        when(referentialMapper.csvItemImpactToRest(any()))
                .thenThrow(new NumberFormatException());

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> referentialImportService.parseItemImpactCsv(file));

        assertEquals("csv.decimal.comma.invalid: 2", exception.getError());
    }

    @Test
    void shouldAddValidationErrorsDuringImport() throws IOException {

        String csv = """
            criterion;lifecycleStep;name;category;avgElectricityConsumption;description;location;level;source;tier;unit;value;subscriber;version
            c1;l1;n1;cat;10.5;desc;FR;1;ADEME;1;kg;100.2;org;1
            """;

        ItemImpactRest rest = new ItemImpactRest();
        rest.setOrganization("org");

        @SuppressWarnings("unchecked")
        ConstraintViolation<ItemImpactRest> violation = mock(ConstraintViolation.class);
        Path propertyPath = mock(Path.class);

        when(propertyPath.toString()).thenReturn("name");
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn("must not be blank");

        when(file.getBytes()).thenReturn(csv.getBytes(StandardCharsets.UTF_8));
        when(file.getOriginalFilename()).thenReturn("item-impact.csv");

        when(referentialMapper.csvItemImpactToRest(any(CSVRecord.class)))
                .thenReturn(rest);

        when(validator.validate(rest))
                .thenReturn(Set.of(violation));

        when(cacheManager.getCache("ref_getItemImpacts")).thenReturn(cache);
        when(cacheManager.getCache("ref_getCountries")).thenReturn(cache);

        ImportReportRest report =
                referentialImportService.processItemImpactCsv(file, "org");

        assertEquals(1, report.getErrors().size());
        assertEquals(1L, report.getImportedLineNumber());

        verify(persistenceService).deleteItemImpactsByOrganization("org");
        verify(persistenceService, never()).saveItemImpacts(anyList());
    }

    @Test
    void shouldDeleteExistingItemImpactsBeforeImport() throws IOException {

        String csv = """
        criterion;lifecycleStep;name;category;avgElectricityConsumption;description;location;level;source;tier;unit;value;subscriber;version
        c1;l1;n1;cat;10.5;desc;FR;1;ADEME;1;kg;100.2;org;1
        """;

        ItemImpactRest rest = new ItemImpactRest();
        rest.setOrganization("org");

        when(file.getBytes()).thenReturn(csv.getBytes(StandardCharsets.UTF_8));

        when(referentialMapper.csvItemImpactToRest(any(CSVRecord.class)))
                .thenReturn(rest);

        when(validator.validate(rest))
                .thenReturn(Collections.emptySet());

        when(referentialMapper.toItemImpactEntity(anyList()))
                .thenReturn(List.of(new ItemImpact()));

        when(cacheManager.getCache("ref_getItemImpacts")).thenReturn(cache);
        when(cacheManager.getCache("ref_getCountries")).thenReturn(cache);

        referentialImportService.processItemImpactCsv(file, "org");

        InOrder inOrder = inOrder(persistenceService);

        inOrder.verify(persistenceService)
                .deleteItemImpactsByOrganization("org");

        inOrder.verify(persistenceService)
                .saveItemImpacts(anyList());
    }

    @Test
    void shouldSaveRemainingBatch() throws IOException {

        StringBuilder csv = new StringBuilder();
        csv.append("criterion;lifecycleStep;name;category;avgElectricityConsumption;description;location;level;source;tier;unit;value;subscriber;version\n");

        for (int i = 0; i < Constants.BATCH_SIZE - 1; i++) {
            csv.append("c")
                    .append(i)
                    .append(";l;n;cat;10.5;desc;FR;1;ADEME;1;kg;100.2;org;1\n");
        }

        ItemImpactRest rest = new ItemImpactRest();
        rest.setOrganization("org");

        when(file.getBytes()).thenReturn(csv.toString().getBytes(StandardCharsets.UTF_8));
        when(file.getOriginalFilename()).thenReturn("item-impact.csv");

        when(referentialMapper.csvItemImpactToRest(any(CSVRecord.class)))
                .thenReturn(rest);

        when(validator.validate(rest))
                .thenReturn(Collections.emptySet());

        when(referentialMapper.toItemImpactEntity(anyList()))
                .thenReturn(List.of(new ItemImpact()));

        Cache itemImpactCache = mock(Cache.class);
        Cache countryCache = mock(Cache.class);

        when(cacheManager.getCache("ref_getItemImpacts"))
                .thenReturn(itemImpactCache);
        when(cacheManager.getCache("ref_getCountries"))
                .thenReturn(countryCache);

        ImportReportRest report =
                referentialImportService.processItemImpactCsv(file, "org");

        verify(persistenceService).deleteItemImpactsByOrganization("org");

        // Only the final save should happen
        verify(persistenceService, times(1))
                .saveItemImpacts(anyList());

        verify(itemImpactCache).clear();
        verify(countryCache).clear();

        assertEquals(Constants.BATCH_SIZE - 1L, report.getImportedLineNumber());
    }

    @Test
    void shouldReturnErrorWhenReadingFileFails() throws IOException {

        when(file.getBytes()).thenThrow(new IOException("Unable to read file"));
        when(file.getOriginalFilename()).thenReturn("item-impact.csv");

        ImportReportRest report =
                referentialImportService.processItemImpactCsv(file, "org");

        assertNotNull(report);
        assertEquals("item-impact.csv", report.getFile());
        assertNull(report.getImportedLineNumber());

        assertEquals(1, report.getErrors().size());
        assertTrue(report.getErrors().get(0).contains("Cannot read csv file"));
        assertTrue(report.getErrors().get(0).contains("Unable to read file"));

        verify(file).getBytes();

        verifyNoInteractions(referentialMapper);
        verifyNoInteractions(persistenceService);
        verifyNoInteractions(cacheManager);
    }

    @Test
    void shouldClearCachesAfterSuccessfulImport() throws IOException {

        String csv = """
            criterion;lifecycleStep;name;category;avgElectricityConsumption;description;location;level;source;tier;unit;value;subscriber;version
            c1;l1;n1;cat;10.5;desc;FR;1;ADEME;1;kg;100.2;org;1
            """;

        ItemImpactRest rest = new ItemImpactRest();
        rest.setOrganization("org");

        when(file.getBytes()).thenReturn(csv.getBytes(StandardCharsets.UTF_8));

        when(referentialMapper.csvItemImpactToRest(any(CSVRecord.class)))
                .thenReturn(rest);

        when(validator.validate(rest))
                .thenReturn(Collections.emptySet());

        when(referentialMapper.toItemImpactEntity(anyList()))
                .thenReturn(List.of(new ItemImpact()));

        Cache itemImpactCache = mock(Cache.class);
        Cache countryCache = mock(Cache.class);

        when(cacheManager.getCache("ref_getItemImpacts"))
                .thenReturn(itemImpactCache);

        when(cacheManager.getCache("ref_getCountries"))
                .thenReturn(countryCache);

        referentialImportService.processItemImpactCsv(file, "org");

        verify(itemImpactCache).clear();
        verify(countryCache).clear();
    }

    @Test
    void shouldSetImportedLineNumberCorrectly() throws IOException {

        String csv = """
            criterion;lifecycleStep;name;category;avgElectricityConsumption;description;location;level;source;tier;unit;value;subscriber;version
            c1;l1;n1;cat;10.5;desc;FR;1;ADEME;1;kg;100.2;org;1
            c2;l2;n2;cat;20.5;desc;FR;1;ADEME;1;kg;200.2;org;1
            c3;l3;n3;cat;30.5;desc;FR;1;ADEME;1;kg;300.2;org;1
            """;

        ItemImpactRest rest = new ItemImpactRest();
        rest.setOrganization("org");

        when(file.getBytes()).thenReturn(csv.getBytes(StandardCharsets.UTF_8));

        when(referentialMapper.csvItemImpactToRest(any(CSVRecord.class)))
                .thenReturn(rest);

        when(validator.validate(rest))
                .thenReturn(Collections.emptySet());

        when(referentialMapper.toItemImpactEntity(anyList()))
                .thenReturn(List.of(new ItemImpact()));

        when(cacheManager.getCache("ref_getItemImpacts"))
                .thenReturn(cache);

        when(cacheManager.getCache("ref_getCountries"))
                .thenReturn(cache);

        ImportReportRest report =
                referentialImportService.processItemImpactCsv(file, "org");

        assertEquals(3L, report.getImportedLineNumber());
        assertTrue(report.getErrors().isEmpty());

        verify(persistenceService).saveItemImpacts(anyList());
    }

    @Test
    void shouldParseItemTypeCsvSuccessfully() throws IOException {

        String csv = """
            type;category;comment;default_lifespan;is_server;source;ref_default_item;version
            Laptop;Device;comment;5;false;ADEME;Default;1
            """;

        ItemTypeRest item = new ItemTypeRest();
        item.setOrganization("org");

        when(file.getBytes()).thenReturn(csv.getBytes(StandardCharsets.UTF_8));
        when(file.getOriginalFilename()).thenReturn("item-type.csv");

        when(referentialMapper.csvItemTypeToRest(any(CSVRecord.class)))
                .thenReturn(item);

        when(validator.validate(item))
                .thenReturn(Collections.emptySet());

        ItemTypeParseResult result =
                referentialImportService.parseItemTypeCsv(file);

        assertNotNull(result);
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());

        assertNull(result.getData().get(0).getOrganization());

        assertEquals(1L, result.getReport().getImportedLineNumber());
        assertTrue(result.getReport().getErrors().isEmpty());

        verify(referentialMapper).csvItemTypeToRest(any(CSVRecord.class));
    }

    @Test
    void shouldReturnValidationErrorWhenItemTypeIsInvalid() throws IOException {

        String csv = """
            type;category;comment;default_lifespan;is_server;source;ref_default_item;version
            Laptop;Device;comment;5;false;ADEME;Default;1
            """;

        ItemTypeRest item = new ItemTypeRest();

        @SuppressWarnings("unchecked")
        ConstraintViolation<ItemTypeRest> violation =
                mock(ConstraintViolation.class);

        Path property = mock(Path.class);

        when(property.toString()).thenReturn("type");
        when(violation.getPropertyPath()).thenReturn(property);
        when(violation.getMessage()).thenReturn("must not be blank");

        when(file.getBytes()).thenReturn(csv.getBytes(StandardCharsets.UTF_8));
        when(file.getOriginalFilename()).thenReturn("item-type.csv");

        when(referentialMapper.csvItemTypeToRest(any(CSVRecord.class)))
                .thenReturn(item);

        when(validator.validate(item))
                .thenReturn(Set.of(violation));

        ItemTypeParseResult result =
                referentialImportService.parseItemTypeCsv(file);

        assertTrue(result.getData().isEmpty());

        assertEquals(1, result.getReport().getErrors().size());

        assertEquals(0L,
                result.getReport().getImportedLineNumber());
    }

    @Test
    void shouldParseItemImpactCsvSuccessfully() throws IOException {

        String csv = """
            criterion;lifecycleStep;name;category;avgElectricityConsumption;description;location;level;source;tier;unit;value;version
            c1;l1;n1;cat;10.5;desc;FR;1;ADEME;1;kg;100.2;1
            """;

        ItemImpactRest item = new ItemImpactRest();
        item.setOrganization("org");

        when(file.getBytes()).thenReturn(csv.getBytes(StandardCharsets.UTF_8));
        when(file.getOriginalFilename()).thenReturn("item-impact.csv");

        when(referentialMapper.csvItemImpactToRest(any(CSVRecord.class)))
                .thenReturn(item);

        when(validator.validate(item))
                .thenReturn(Collections.emptySet());

        ItemImpactParseResult result =
                referentialImportService.parseItemImpactCsv(file);

        assertEquals(1, result.getData().size());
        assertNull(result.getData().get(0).getOrganization());
        assertEquals(1L, result.getReport().getImportedLineNumber());
        assertTrue(result.getReport().getErrors().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("invalidItemImpactCsvProvider")
    void shouldThrowExceptionWhenItemImpactCsvIsInvalid(
            String csv,
            String expectedError) throws IOException {

        when(file.getBytes()).thenReturn(csv.getBytes(StandardCharsets.UTF_8));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> referentialImportService.parseItemImpactCsv(file));

        assertEquals(expectedError, exception.getError());
    }

    private static Stream<Arguments> invalidItemImpactCsvProvider() {
        return Stream.of(
                Arguments.of(
                        """
                        lifecycleStep;criterion;name;category;avgElectricityConsumption;description;location;level;source;tier;unit;value;version
                        l1;c1;n1;cat;10.5;desc;FR;1;ADEME;1;kg;100.2;1
                        """,
                        "csv.columns.order.invalid:ItemImpact:1:criterion:lifecycleStep"
                ),
                Arguments.of(
                        """
                        criterion;lifecycleStep;name;category;avgElectricityConsumption;description;location;level;source;tier;unit;value;version;extra
                        c1;l1;n1;cat;10.5;desc;FR;1;ADEME;1;kg;100.2;1;x
                        """,
                        "csv.columns.unexpected:ItemImpact:[extra]"
                ),
                Arguments.of(
                        """
                        criterion;lifecycleStep;name;category;avgElectricityConsumption;description;location;level;source;tier;unit;value;version
                        c1;l1;n1;cat;10,5;desc;FR;1;ADEME;1;kg;100.2;1
                        """,
                        "csv.decimal.comma.invalid: 2"
                ),
                Arguments.of(
                        """
                        criterion;lifecycleStep;name;category;avgElectricityConsumption;description;location;level;source;tier;unit;value;version
                        c1;l1;n1;cat;10.5;desc;FR;1;ADEME;1;kg;100,2;1
                        """,
                        "csv.decimal.comma.invalid: 2"
                )
        );
    }

    @Test
    void shouldReturnLineNumberWhenMapperThrowsNumberFormatException() throws Exception {

        String csv = """
        criterion;lifecycleStep;name;category;avgElectricityConsumption;description;location;level;source;tier;unit;value;version
        c;l;n;cat;1.5;desc;loc;lev;src;t;u;10.5;1
        """;

        when(file.getBytes()).thenReturn(csv.getBytes(StandardCharsets.UTF_8));

        when(referentialMapper.csvItemImpactToRest(any()))
                .thenThrow(new NumberFormatException());

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> referentialImportService.parseItemImpactCsv(file));

        assertTrue(ex.getError().startsWith("csv.decimal.comma.invalid"));
    }

    @Test
    void shouldReturnColumnNameWhenValueContainsComma() throws Exception {

        String csv = """
        criterion;lifecycleStep;name;category;avgElectricityConsumption;description;location;level;source;tier;unit;value;version
        c;l;n;cat;1.5;desc;loc;lev;src;t;u;10,5;1
        """;

        when(file.getBytes()).thenReturn(csv.getBytes(StandardCharsets.UTF_8));

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> referentialImportService.parseItemImpactCsv(file));

        assertTrue(ex.getError().contains("csv.decimal.comma.invalid"));
    }


    @Test
    void shouldReturnColumnNameWhenAvgElectricityContainsComma() throws Exception {

        String csv = """
        criterion;lifecycleStep;name;category;avgElectricityConsumption;description;location;level;source;tier;unit;value;version
        c;l;n;cat;1,5;desc;loc;lev;src;t;u;10.5;1
        """;

        when(file.getBytes()).thenReturn(csv.getBytes(StandardCharsets.UTF_8));

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> referentialImportService.parseItemImpactCsv(file));

        assertTrue(ex.getError().contains("csv.decimal.comma.invalid"));
    }

    @Test
    void shouldParseItemImpactWithValidDecimals() throws Exception {

        String csv = """
        criterion;lifecycleStep;name;category;avgElectricityConsumption;description;location;level;source;tier;unit;value;version
        c;l;n;cat;1.5;desc;loc;lev;src;t;u;10.5;1
        """;

        ItemImpactRest item = new ItemImpactRest();

        when(file.getBytes()).thenReturn(csv.getBytes(StandardCharsets.UTF_8));
        when(referentialMapper.csvItemImpactToRest(any())).thenReturn(item);
        when(validator.validate(any())).thenReturn(Collections.emptySet());

        ItemImpactParseResult result =
                referentialImportService.parseItemImpactCsv(file);

        assertTrue(result.getReport().getErrors().isEmpty());
        assertEquals(1, result.getData().size());
    }


}
