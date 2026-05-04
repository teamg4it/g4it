package com.soprasteria.g4it.backend.apireferential.business;

import com.soprasteria.g4it.backend.apireferential.modeldb.ItemImpact;
import com.soprasteria.g4it.backend.apireferential.modeldb.ItemType;
import com.soprasteria.g4it.backend.apireferential.modeldb.MatchingItem;
import com.soprasteria.g4it.backend.apireferential.repository.ItemImpactRepository;
import com.soprasteria.g4it.backend.apireferential.repository.ItemTypeRepository;
import com.soprasteria.g4it.backend.apireferential.repository.MatchingItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceReferentialExportServiceTest {

    @InjectMocks
    private WorkspaceReferentialExportService service;

    @Mock
    private ItemTypeRepository itemTypeRepository;

    @Mock
    private ItemImpactRepository itemImpactRepository;

    @Mock
    private MatchingItemRepository matchingItemRepository;

    private String tempDir;
    private static final String ORG = "ORG";

    @BeforeEach
    void setup() {
        tempDir = System.getProperty("java.io.tmpdir");

        ReflectionTestUtils.setField(service, "localWorkingFolder", tempDir);
        ReflectionTestUtils.setField(service, "pageSize", 2);
    }

    // =========================
    // SUCCESS CASE
    // =========================

    @Test
    void exportReferentialZip_success_withData() throws Exception {
        Long workspaceId = 1L;

        Page<ItemType> itemTypePage = new PageImpl<>(
                List.of(mock(ItemType.class)),
                PageRequest.of(0, 2),
                1
        );

        Page<ItemImpact> itemImpactPage = new PageImpl<>(
                List.of(mock(ItemImpact.class)),
                PageRequest.of(0, 2),
                1
        );

        Page<MatchingItem> matchingPage = new PageImpl<>(
                List.of(mock(MatchingItem.class)),
                PageRequest.of(0, 2),
                1
        );

        when(itemTypeRepository.findByWorkspaceId(eq(workspaceId), any()))
                .thenReturn(itemTypePage);

        when(itemImpactRepository.findByWorkspaceId(eq(workspaceId), any()))
                .thenReturn(itemImpactPage);

        when(matchingItemRepository.findByWorkspaceId(eq(workspaceId), any()))
                .thenReturn(matchingPage);

        InputStream result = service.exportReferentialZip(ORG, workspaceId); // ✅ fixed

        assertNotNull(result);

        verify(itemTypeRepository, atLeastOnce()).findByWorkspaceId(eq(workspaceId), any());
        verify(itemImpactRepository, atLeastOnce()).findByWorkspaceId(eq(workspaceId), any());
        verify(matchingItemRepository, atLeastOnce()).findByWorkspaceId(eq(workspaceId), any());
    }

    // =========================
    // EMPTY DATA → TEMPLATE FILES
    // =========================

    @Test
    void exportReferentialZip_emptyData_createsTemplates() throws Exception {
        Long workspaceId = 1L;

        when(itemTypeRepository.findByWorkspaceId(eq(workspaceId), any()))
                .thenReturn(Page.empty());

        when(itemImpactRepository.findByWorkspaceId(eq(workspaceId), any()))
                .thenReturn(Page.empty());

        when(matchingItemRepository.findByWorkspaceId(eq(workspaceId), any()))
                .thenReturn(Page.empty());

        InputStream result = service.exportReferentialZip(ORG, workspaceId); // ✅

        assertNotNull(result);
    }

    // =========================
    // MULTI-PAGE TEST
    // =========================

    @Test
    void exportReferentialZip_multiplePages() throws Exception {
        Long workspaceId = 1L;

        ItemType item1 = mock(ItemType.class);
        ItemType item2 = mock(ItemType.class);

        Page<ItemType> firstPage = new PageImpl<>(
                List.of(item1),
                PageRequest.of(0, 1),
                2
        );

        Page<ItemType> secondPage = new PageImpl<>(
                List.of(item2),
                PageRequest.of(1, 1),
                2
        );

        when(itemTypeRepository.findByWorkspaceId(eq(workspaceId), any()))
                .thenReturn(firstPage)
                .thenReturn(secondPage);

        when(itemImpactRepository.findByWorkspaceId(eq(workspaceId), any()))
                .thenReturn(Page.empty());

        when(matchingItemRepository.findByWorkspaceId(eq(workspaceId), any()))
                .thenReturn(Page.empty());

        InputStream result = service.exportReferentialZip(ORG, workspaceId); // ✅

        assertNotNull(result);

        verify(itemTypeRepository, atLeast(2))
                .findByWorkspaceId(eq(workspaceId), any());
    }

    // =========================
    // NULL WORKSPACE
    // =========================

    @Test
    void exportReferentialZip_nullWorkspace() {
        assertThrows(IllegalArgumentException.class,
                () -> service.exportReferentialZip(ORG, null)); // ✅
    }

    // =========================
    // INIT FOLDER
    // =========================

    @Test
    void initFolder_createsDirectory() throws Exception {
        ReflectionTestUtils.setField(service, "localWorkingFolder", tempDir);

        service.initFolder();

        Path expected = Path.of(tempDir, "workspace-referential");
        assertTrue(Files.exists(expected));
    }

    // =========================
    // NULL ITEMS INSIDE PAGE
    // =========================

    @Test
    void exportReferentialZip_handlesNullItems() throws Exception {
        Long workspaceId = 1L;

        Page<ItemType> page = new PageImpl<>(
                java.util.Collections.singletonList(null),
                PageRequest.of(0, 1),
                1
        );

        when(itemTypeRepository.findByWorkspaceId(eq(workspaceId), any()))
                .thenReturn(page);

        when(itemImpactRepository.findByWorkspaceId(eq(workspaceId), any()))
                .thenReturn(Page.empty());

        when(matchingItemRepository.findByWorkspaceId(eq(workspaceId), any()))
                .thenReturn(Page.empty());

        InputStream result = service.exportReferentialZip(ORG, workspaceId); // ✅

        assertNotNull(result);
    }

    // =========================
    // NULL CSV RECORD
    // =========================

    @Test
    void exportReferentialZip_nullCsvRecordIgnored() throws Exception {
        Long workspaceId = 1L;

        ItemType item = mock(ItemType.class);
        when(item.toCsvRecord()).thenReturn(null);

        Page<ItemType> page = new PageImpl<>(
                List.of(item),
                PageRequest.of(0, 1),
                1
        );

        when(itemTypeRepository.findByWorkspaceId(eq(workspaceId), any()))
                .thenReturn(page);

        when(itemImpactRepository.findByWorkspaceId(eq(workspaceId), any()))
                .thenReturn(Page.empty());

        when(matchingItemRepository.findByWorkspaceId(eq(workspaceId), any()))
                .thenReturn(Page.empty());

        InputStream result = service.exportReferentialZip(ORG, workspaceId); // ✅

        assertNotNull(result);
    }

    @Test
    void exportReferentialZip_recordIsPrinted() throws Exception {
        Long workspaceId = 1L;

        ItemType item = mock(ItemType.class);

        when(item.toCsvRecord()).thenReturn(new Object[]{"val1", "val2"});

        Page<ItemType> page = new PageImpl<>(
                List.of(item),
                PageRequest.of(0, 1),
                1
        );

        when(itemTypeRepository.findByWorkspaceId(eq(workspaceId), any()))
                .thenReturn(page);

        when(itemImpactRepository.findByWorkspaceId(eq(workspaceId), any()))
                .thenReturn(Page.empty());

        when(matchingItemRepository.findByWorkspaceId(eq(workspaceId), any()))
                .thenReturn(Page.empty());

        InputStream result = service.exportReferentialZip("ORG", workspaceId);

        assertNotNull(result);
        verify(item, atLeastOnce()).toCsvRecord();
    }
}