/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apireferential.business;

import com.soprasteria.g4it.backend.apireferential.modeldb.Criterion;
import com.soprasteria.g4it.backend.apireferential.modeldb.LifecycleStep;
import com.soprasteria.g4it.backend.apireferential.modeldb.Hypothesis;
import com.soprasteria.g4it.backend.apireferential.modeldb.ItemType;
import com.soprasteria.g4it.backend.apireferential.modeldb.MatchingItem;
import com.soprasteria.g4it.backend.apireferential.modeldb.ItemImpact;
import com.soprasteria.g4it.backend.apireferential.repository.CriterionRepository;
import com.soprasteria.g4it.backend.apireferential.repository.LifecycleStepRepository;
import com.soprasteria.g4it.backend.apireferential.repository.HypothesisRepository;
import com.soprasteria.g4it.backend.apireferential.repository.ItemTypeRepository;
import com.soprasteria.g4it.backend.apireferential.repository.MatchingItemRepository;
import com.soprasteria.g4it.backend.apireferential.repository.ItemImpactRepository;
import com.soprasteria.g4it.backend.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReferentialExportServiceTest {
    @Mock
    private CriterionRepository criterionRepository;
    @Mock private LifecycleStepRepository lifecycleStepRepository;
    @Mock private HypothesisRepository hypothesisRepository;
    @Mock private ItemTypeRepository itemTypeRepository;
    @Mock private MatchingItemRepository matchingItemRepository;
    @Mock private ItemImpactRepository itemImpactRepository;
    @InjectMocks
    private ReferentialExportService referentialExportService;

    @BeforeEach
    void setUp() throws IOException {
        // Set a temp folder for file output
        String tempDir = System.getProperty("java.io.tmpdir");
        ReflectionTestUtils.setField(referentialExportService, "localWorkingFolder", tempDir);
        // Explicitly create the referential directory
        Files.createDirectories(Path.of(tempDir, "referential"));
        referentialExportService.initFolder(); // Call after setting the field
    }

    @Test
    void testExportReferentialToCSV_Criterion() throws IOException {
        String tempDir = System.getProperty("java.io.tmpdir");
        String referentialDir = Path.of(tempDir, "referential").toString();
        System.out.println("Referential directory: " + referentialDir);
        assertTrue(new java.io.File(referentialDir).exists(), "Referential directory should exist before export");
        Criterion criterion = Criterion.builder()
                .code("C1")
                .label("Label1")
                .description("Desc1")
                .unit("Unit1")
                .build();
        when(criterionRepository.findAll()).thenReturn(List.of(criterion));
        try (InputStream is = referentialExportService.exportReferentialToCSV("criterion", null);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String header = reader.readLine();
            String data = reader.readLine();
            assertNotNull(header);
            assertTrue(header.contains("code"));
            assertNotNull(data);
            assertTrue(data.contains("C1"));
            assertTrue(data.contains("Label1"));
        }
    }

    @Test
    void testExportReferentialToCSV_LifecycleStep() throws IOException {
        LifecycleStep step = LifecycleStep.builder().code("LS1").label("Step1").build();
        when(lifecycleStepRepository.findAll()).thenReturn(List.of(step));
        try (InputStream is = referentialExportService.exportReferentialToCSV("lifecycleStep", null);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String header = reader.readLine();
            String data = reader.readLine();
            assertNotNull(header);
            assertTrue(header.contains("code"));
            assertNotNull(data);
            assertTrue(data.contains("LS1"));
            assertTrue(data.contains("Step1"));
        }
    }

    @Test
    void testExportReferentialToCSV_Hypothesis() throws IOException {
        Hypothesis hyp = Hypothesis.builder().code("H1").value(1.23).description("desc").source("src").organization("org").version("v1").build();
        when(hypothesisRepository.findByOrganization("org")).thenReturn(List.of(hyp));
        try (InputStream is = referentialExportService.exportReferentialToCSV("hypothesis", "org");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String header = reader.readLine();
            String data = reader.readLine();
            assertNotNull(header);
            assertTrue(header.contains("code"));
            assertNotNull(data);
            assertTrue(data.contains("H1"));
            assertTrue(data.contains("org"));
        }
    }

    @Test
    void testExportReferentialToCSV_ItemType() throws IOException {
        ItemType itemType = ItemType.builder().type("T1").category("cat").comment("cmt").defaultLifespan(10.0).isServer(true).source("src").refDefaultItem("ref").organization("org").version("v1").build();
        when(itemTypeRepository.findByOrganization("org")).thenReturn(List.of(itemType));
        try (InputStream is = referentialExportService.exportReferentialToCSV("itemType", "org");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String header = reader.readLine();
            String data = reader.readLine();
            assertNotNull(header);
            assertTrue(header.contains("type"));
            assertNotNull(data);
            assertTrue(data.contains("T1"));
            assertTrue(data.contains("org"));
        }
    }

    @Test
    void testExportReferentialToCSV_MatchingItem() throws IOException {
        MatchingItem item = MatchingItem.builder().itemSource("src").refItemTarget("target").organization("org").build();
        Pageable pageable = PageRequest.of(0, 200);
        Page<MatchingItem> page = new PageImpl<>(List.of(item), pageable, 1);
        when(matchingItemRepository.findByOrganization(eq("org"), any(Pageable.class))).thenReturn(page).thenReturn(Page.empty());
        try (InputStream is = referentialExportService.exportReferentialToCSV("matchingItem", "org");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String header = reader.readLine();
            String data = reader.readLine();
            assertNotNull(header);
            assertTrue(header.contains("itemSource"));
            assertNotNull(data);
            assertTrue(data.contains("src"));
            assertTrue(data.contains("org"));
        }
    }

    @Test
    void testExportReferentialToCSV_ItemImpact() throws IOException {
        ItemImpact impact = ItemImpact.builder().criterion("crit").lifecycleStep("ls").name("n").category("cat").avgElectricityConsumption(2.0).description("desc").location("loc").level("lvl").source("src").tier("tier").unit("u").value(3.0).organization("org").version("v1").build();
        Pageable pageable = PageRequest.of(0, 200);
        Page<ItemImpact> page = new PageImpl<>(List.of(impact), pageable, 1);
        when(itemImpactRepository.findByOrganization(eq("org"), any(Pageable.class))).thenReturn(page).thenReturn(Page.empty());
        try (InputStream is = referentialExportService.exportReferentialToCSV("itemImpact", "org");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String header = reader.readLine();
            String data = reader.readLine();
            assertNotNull(header);
            assertTrue(header.contains("criterion"));
            assertNotNull(data);
            assertTrue(data.contains("crit"));
            assertTrue(data.contains("org"));
        }
    }

    @Test
    void testExportReferentialToCSV_InvalidType() {
        BadRequestException ex = assertThrows(BadRequestException.class, () -> referentialExportService.exportReferentialToCSV("invalid", null));
        assertTrue(ex.getError().contains("type of referential"));
    }
}
