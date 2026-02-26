/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apireferential.business;

import com.soprasteria.g4it.backend.apireferential.mapper.ReferentialMapper;
import com.soprasteria.g4it.backend.apireferential.persistence.ReferentialPersistenceService;
import com.soprasteria.g4it.backend.exception.BadRequestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.*;
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
        referentialImportService = new ReferentialImportService();
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
        assertNotNull(ex.getError(), "type of referential 'invalidType' does not exist");
        assertTrue(ex.getError().contains("type of referential"));
    }

    @Test
    void testProcessCriterionCsv_Valid() throws IOException {
        String csv = "header1,header2\nval1,val2\n";
        InputStream is = new ByteArrayInputStream(csv.getBytes());
        when(file.getInputStream()).thenReturn(is);
        when(file.getOriginalFilename()).thenReturn("test.csv");
        when(referentialMapper.csvCriterionToRest(any())).thenReturn(new CriterionRest());
        when(validator.validate(any())).thenReturn(Collections.emptySet());
        when(persistenceService.saveCriteria(any())).thenReturn(1);
        when(referentialMapper.toCriteriaEntity(any())).thenReturn(Collections.emptyList());
        when(cacheManager.getCache(anyString())).thenReturn(cache);
        ImportReportRest report = referentialImportService.processCriterionCsv(file);
        assertEquals("test.csv", report.getFile());
        assertEquals(1L, report.getImportedLineNumber());
        assertTrue(report.getErrors().isEmpty());
    }

    @Test
    void testProcessCriterionCsv_InvalidCsv_AddsError() throws IOException {
        String csv = "header1,header2\nval1,val2\n";
        InputStream is = new ByteArrayInputStream(csv.getBytes());
        when(file.getInputStream()).thenReturn(is);
        when(file.getOriginalFilename()).thenReturn("test.csv");
        when(referentialMapper.csvCriterionToRest(any())).thenReturn(new CriterionRest());
        @SuppressWarnings("unchecked")
        jakarta.validation.ConstraintViolation<CriterionRest> violation = mock(jakarta.validation.ConstraintViolation.class);
        jakarta.validation.Path path = mock(jakarta.validation.Path.class);
        when(path.toString()).thenReturn("field");
        when(violation.getMessage()).thenReturn("error");
        when(violation.getPropertyPath()).thenReturn(path);
        Set<jakarta.validation.ConstraintViolation<CriterionRest>> violations = new HashSet<>();
        violations.add(violation);
        when(validator.validate(any())).thenReturn((Set)violations); // Fix generics issue
        when(cacheManager.getCache("ref_getAllCriteria")).thenReturn(cache); // Fix NPE
        ImportReportRest report = referentialImportService.processCriterionCsv(file);
        assertFalse(report.getErrors().isEmpty());
    }

    @Test
    void testProcessCriterionCsv_IOException_AddsError() throws IOException {
        when(file.getInputStream()).thenThrow(new IOException("fail"));
        when(file.getOriginalFilename()).thenReturn("test.csv");
        ImportReportRest report = referentialImportService.processCriterionCsv(file);
        assertFalse(report.getErrors().isEmpty()); // Improved assertion
    }

    @Test
    void testProcessLifecycleStepCsv_Valid() throws IOException {
        String csv = "header1\nval1\n";
        InputStream is = new ByteArrayInputStream(csv.getBytes());
        when(file.getInputStream()).thenReturn(is);
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
        InputStream is = new ByteArrayInputStream(csv.getBytes());
        when(file.getInputStream()).thenReturn(is);
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
        InputStream is = new ByteArrayInputStream(csv.getBytes());
        when(file.getInputStream()).thenReturn(is);
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
        InputStream is = new ByteArrayInputStream(csv.getBytes());
        when(file.getInputStream()).thenReturn(is);
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
        InputStream is = new ByteArrayInputStream(csv.getBytes());
        when(file.getInputStream()).thenReturn(is);
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
        InputStream is = new ByteArrayInputStream(csv.getBytes());
        when(file.getInputStream()).thenReturn(is);
        when(file.getOriginalFilename()).thenReturn("test.csv");
        // Always return a mock with organization 'org' for any CSV record
        when(referentialMapper.csvItemImpactToRest(any())).thenAnswer(invocation -> {
            ItemImpactRest rest = mock(ItemImpactRest.class);
            when(rest.getOrganization()).thenReturn("org");
            return rest;
        });
        when(referentialMapper.toItemImpactEntity(any())).thenReturn(Collections.emptyList());
        doNothing().when(persistenceService).deleteItemImpactsByOrganization(any());
        doNothing().when(persistenceService).saveItemImpacts(any());
        when(cacheManager.getCache("ref_getItemImpacts")).thenReturn(cache);
        when(cacheManager.getCache("ref_getCountries")).thenReturn(cache);
        doNothing().when(cache).clear();
        ImportReportRest report = referentialImportService.processItemImpactCsv(file, "org");
        assertEquals("test.csv", report.getFile());
        assertTrue(report.getErrors().isEmpty());
        assertNotNull(report.getImportedLineNumber());
    }

    // Additional tests for error/edge cases can be added similarly
}
