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
import com.soprasteria.g4it.backend.apireferential.persistence.ReferentialPersistenceService;
import com.soprasteria.g4it.backend.exception.BadRequestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    void testProcessItemImpactCsv_SubscriberMismatch_ThrowsBadRequest() throws IOException {
        String csv = "header1\nval1\n";

         when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("test.csv");
        ItemImpactRest rest = mock(ItemImpactRest.class);
        when(rest.getOrganization()).thenReturn("otherOrg");
        when(referentialMapper.csvItemImpactToRest(any())).thenReturn(rest);
        assertThrows(BadRequestException.class, () ->
                referentialImportService.processItemImpactCsv(file, "org")
        );
    }

    @Test
    void testProcessItemImpactCsv_Valid() throws IOException {
        String csv = "subscriber,field1\norg,val1\norg,val2\n";

        when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("test.csv");

        when(referentialMapper.csvItemImpactToRest(any()))
                .thenAnswer(invocation -> {
                    ItemImpactRest rest = new ItemImpactRest();
                    rest.setOrganization("org");
                    return rest;
                });

        when(cacheManager.getCache("ref_getItemImpacts")).thenReturn(cache);
        when(cacheManager.getCache("ref_getCountries")).thenReturn(cache);
        doNothing().when(cache).clear();

        ImportReportRest report =
                referentialImportService.processItemImpactCsv(file, "org");

        assertEquals("test.csv", report.getFile());
        assertTrue(report.getErrors().isEmpty());
        assertNotNull(report.getImportedLineNumber());
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
    void testProcessItemImpactCsv_WithValidationErrors() throws Exception {
        String csv = """
        criterion;lifecycleStep;name;category;avgElectricityConsumption;description;location;level;source;tier;unit;value;version
        c;l;n;cat;1;desc;loc;lev;src;t;u;10;1
        """;

         when(file.getBytes()).thenReturn(csv.getBytes());
        when(file.getOriginalFilename()).thenReturn("file.csv");

        when(referentialMapper.csvItemImpactToRest(any()))
                .thenAnswer(invocation -> {
                    ItemImpactRest rest = new ItemImpactRest();
                    rest.setOrganization("org");
                    return rest;
                });

        when(cacheManager.getCache(anyString())).thenReturn(cache);

        ImportReportRest report = referentialImportService.processItemImpactCsv(file, "org");

        assertNotNull(report);

        verify(referentialMapper, atLeastOnce()).csvItemImpactToRest(any());
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
    void testProcessItemImpactCsv_BatchSaveTriggered() throws Exception {

        StringBuilder csv = new StringBuilder("subscriber;field\n");

        for (int i = 0; i < 110; i++) {
            csv.append("org;val\n");
        }

        when(file.getBytes()).thenReturn(csv.toString().getBytes());
        when(file.getOriginalFilename()).thenReturn("test.csv");

        when(referentialMapper.csvItemImpactToRest(any()))
                .thenAnswer(invocation -> {
                    ItemImpactRest r = new ItemImpactRest();
                    r.setOrganization("org");
                    return r;
                });

        when(cacheManager.getCache(anyString())).thenReturn(cache);

        referentialImportService.processItemImpactCsv(file, "org");

        verify(persistenceService, atLeastOnce())
                .saveItemImpacts(any());
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
    void testProcessCriterionCsv_GetBytesFails_ReturnsEarly() throws Exception {
        when(file.getBytes()).thenThrow(new IOException("fail"));
        when(file.getOriginalFilename()).thenReturn("test.csv");

        ImportReportRest report =
                referentialImportService.processCriterionCsv(file);

        assertFalse(report.getErrors().isEmpty());
    }

    @Test
    void testImportReferentialCSV_SafeExecuteIOException() throws Exception {
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.csv");

        when(file.getBytes()).thenThrow(new IOException("boom"));

        ImportReportRest report =
                referentialImportService.importReferentialCSV("criterion", file, "org");

        assertFalse(report.getErrors().isEmpty());
    }
}
