/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinventory.business;


import com.soprasteria.g4it.backend.TestUtils;
import com.soprasteria.g4it.backend.apiinventory.mapper.InventoryMapperImpl;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryBO;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiuser.business.WorkspaceService;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.common.dbmodel.Note;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.InventoryCreateRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InventoryType;
import com.soprasteria.g4it.backend.server.gen.api.dto.InventoryUpdateRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.NoteUpsertRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    private static final String SUBSCRIBER = "SUBSCRIBER";
    private static final Long ORGANIZATION_ID = 1L;
    private static final Long INVENTORY_ID = 2L;

    @InjectMocks
    private InventoryService inventoryService;

    @Mock
    private WorkspaceService workspaceService;

    @Mock
    private InventoryRepository inventoryRepo;
    @Mock
    private TaskRepository taskRepo;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(inventoryService, "inventoryMapper", new InventoryMapperImpl());
    }

    @Test
    void testGetCriteriaNumberReturnsSize() {
        Long taskId = 1L;

        Task task = Task.builder().id(taskId).criteria(List.of("Criteria1", "Criteria2")).build();

        when(taskRepo.findById(taskId)).thenReturn(Optional.of(task));
        Long result = inventoryService.getCriteriaNumber(taskId);

        assertThat(result).isEqualTo(2L);
        verify(taskRepo).findById(taskId);
    }

    @Test
    void testGetCriteriaNumberTaskNotFoundThenThrow() {
        Long taskId = 2L;

        when(taskRepo.findById(taskId)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> inventoryService.getCriteriaNumber(taskId));
    }

    @Test
    void testInventoryExists() {

        final Organization linkedOrganization = TestUtils.createOrganization();

        when(organizationService.getOrganizationById(ORGANIZATION_ID)).thenReturn(linkedOrganization);
        when(inventoryRepo.findByOrganizationAndId(linkedOrganization, INVENTORY_ID))
                .thenReturn(Optional.of(new Inventory()));

        boolean result = inventoryService.inventoryExists(SUBSCRIBER, ORGANIZATION_ID, INVENTORY_ID);

        assertThat(result).isTrue();
        verify(organizationService).getOrganizationById(ORGANIZATION_ID);
        verify(inventoryRepo).findByOrganizationAndId(linkedOrganization, INVENTORY_ID);
    }

    @Test
    void testInventoryDoesNotExist() {
        final Organization linkedOrganization = TestUtils.createOrganization();

        when(organizationService.getOrganizationById(ORGANIZATION_ID)).thenReturn(linkedOrganization);
        when(inventoryRepo.findByOrganizationAndId(linkedOrganization, INVENTORY_ID)).thenReturn(Optional.empty());

        boolean result = inventoryService.inventoryExists(SUBSCRIBER, ORGANIZATION_ID, INVENTORY_ID);
        assertThat(result).isFalse();
        verify(organizationService).getOrganizationById(ORGANIZATION_ID);
        verify(inventoryRepo).findByOrganizationAndId(linkedOrganization, INVENTORY_ID);
    }

    @Test
    void canRetrieveAllInventories() {
        final Workspace linkedWorkspace = TestUtils.createOrganization();

        final InventoryBO inventory1 = InventoryBO.builder().build();
        final InventoryBO inventory2 = InventoryBO.builder().build();
        final List<InventoryBO> expectedInventoryList = List.of(inventory1, inventory2);

        final Inventory inventoryEntity1 = Inventory.builder().id(1L).name("03-2023").build();
        final Inventory inventoryEntity2 = Inventory.builder().id(2L).name("04-2023").build();
        final List<Inventory> inventorysEntitiesList = List.of(inventoryEntity1, inventoryEntity2);

        when(workspaceService.getWorkspaceById(ORGANIZATION_ID)).thenReturn(linkedWorkspace);
        when(inventoryRepo.findByWorkspace(linkedWorkspace)).thenReturn(inventorysEntitiesList);

        final List<InventoryBO> result = inventoryService.getInventories( ORGANIZATION_ID, null);

        assertThat(result).hasSameSizeAs(expectedInventoryList);

        verify(workspaceService, times(1)).getWorkspaceById(ORGANIZATION_ID);
        verify(inventoryRepo, times(1)).findByWorkspace(linkedWorkspace);

    }

    @Test
    void canRetrieveInventoriesFilteredByInventoryId() {
        final Workspace linkedWorkspace = TestUtils.createOrganization();
        final Long inventoryId = 2L;

        final InventoryBO inventory1 = InventoryBO.builder().build();
        final List<InventoryBO> expectedInventoryList = List.of(inventory1);

        final Inventory inventoryEntity1 = Inventory.builder().id(1L).name("03-2023").lastUpdateDate(LocalDateTime.now()).build();
        var inventoryOptional = Optional.of(inventoryEntity1);

        when(workspaceService.getWorkspaceById(ORGANIZATION_ID)).thenReturn(linkedWorkspace);
        when(this.inventoryRepo.findByWorkspaceAndId(linkedWorkspace, inventoryId)).thenReturn(inventoryOptional);

        final List<InventoryBO> result = this.inventoryService.getInventories(ORGANIZATION_ID, INVENTORY_ID);

        assertThat(result).hasSameSizeAs(expectedInventoryList);

        verify(workspaceService, times(1)).getWorkspaceById(ORGANIZATION_ID);
        verify(inventoryRepo, times(1)).findByWorkspaceAndId(linkedWorkspace, inventoryId);
    }

    @Test
    void canRetrieveOneInventory() {

        final Inventory inventory = Inventory.builder()
                .id(1L)
                .build();
        final InventoryBO expected = InventoryBO.builder()
                .id(1L)
                .dataCenterCount(0L)
                .physicalEquipmentCount(0L)
                .virtualEquipmentCount(0L)
                .applicationCount(0L)
                .tasks(List.of())
                .build();

        when(inventoryRepo.findByWorkspaceAndId(any(), eq(inventoryId))).thenReturn(Optional.of(inventory));

        final InventoryBO result = inventoryService.getInventory(SUBSCRIBER, ORGANIZATION_ID, INVENTORY_ID);

        assertThat(result).isEqualTo(expected);

        verify(inventoryRepo, times(1)).findByWorkspaceAndId(any(), eq(inventoryId));
    }


    @Test
    void shouldCreateAnInventory() {
        final Workspace linkedWorkspace = TestUtils.createOrganization();
        final String inventoryName = "03-2023";
        final InventoryCreateRest inventoryCreateRest = InventoryCreateRest.builder()
                .name(inventoryName)
                .type(InventoryType.SIMULATION)
                .build();

        final Inventory inventory = Inventory
                .builder()
                .name("03-2023")
                .workspace(linkedWorkspace).build();

        final UserBO userBo = TestUtils.createUserBONoRole();

        when(workspaceService.getWorkspaceById(ORGANIZATION_ID)).thenReturn(linkedWorkspace);
        when(inventoryRepo.findByWorkspaceAndName(linkedWorkspace, inventoryName))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(inventory));
        when(inventoryRepo.save(any())).thenReturn(inventory);

        InventoryBO actual = inventoryService.createInventory(SUBSCRIBER, ORGANIZATION_ID, inventoryCreateRest, userBo);

        verify(workspaceService, times(1)).getWorkspaceById(ORGANIZATION_ID);
        verify(inventoryRepo, times(1)).findByWorkspaceAndName(linkedWorkspace, inventoryCreateRest.getName());
        verify(inventoryRepo, times(1)).save(any());

        assertThat(actual.getName()).isEqualTo("03-2023");
    }

    @Test
    void shouldThrowWhenInventoryAlreadyExists() {
        final Organization linkedOrganization = TestUtils.createOrganization();
        final String inventoryName = "existingInventory";
        final InventoryCreateRest inventoryCreateRest = InventoryCreateRest.builder()
                .name(inventoryName)
                .type(InventoryType.SIMULATION)
                .build();

        final UserBO userBo = TestUtils.createUserBONoRole();

        when(organizationService.getOrganizationById(ORGANIZATION_ID)).thenReturn(linkedOrganization);
        when(inventoryRepo.findByOrganizationAndName(linkedOrganization, inventoryName))
                .thenReturn(Optional.of(new Inventory()));

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                inventoryService.createInventory(SUBSCRIBER, ORGANIZATION_ID, inventoryCreateRest, userBo)
        );
        assertThat(exception.getCode()).isEqualTo("409");
        assertThat(
                String.format("inventory %s already exists in %s/%s", inventoryName, SUBSCRIBER, ORGANIZATION_ID))
                .isEqualTo(exception.getMessage());

        verify(organizationService, times(1)).getOrganizationById(ORGANIZATION_ID);
        verify(inventoryRepo, times(1)).findByOrganizationAndName(linkedOrganization, inventoryName);
        verify(inventoryRepo, never()).save(any());
    }

    @Test
    void shouldUpdateInventoryUpdateCriteria() {
        Long organizationId = 1L;
        final Workspace linkedWorkspace = TestUtils.createOrganization();
        UserBO userBo = TestUtils.createUserBONoRole();
        final String inventoryName = "03-2023";
        String subscriberName = "SUBSCRIBER";

        final InventoryUpdateRest inventoryUpdateRest = InventoryUpdateRest.builder()
                .id(1L)
                .name(inventoryName)
                .criteria(List.of("criteria"))
                .build();
        final Inventory inventory = Inventory
                .builder()
                .id(1L)
                .workspace(linkedWorkspace).build();

        when(workspaceService.getWorkspaceById(ORGANIZATION_ID)).thenReturn(linkedWorkspace);
        when(inventoryRepo.findByWorkspaceAndId(linkedWorkspace, 1L)).thenReturn(Optional.of(inventory));

        InventoryBO result = inventoryService.updateInventory(subscriberName, organizationId, inventoryUpdateRest, userBo);

        verify(inventoryRepo, times(1)).save(any());

        assertThat(result.getCriteria()).isEqualTo(List.of("criteria"));
    }

    @Test
    void shouldUpdateInventoryCreateNote() {
        Long organizationId = 1L;
        final Organization linkedOrganization = TestUtils.createOrganization();
        UserBO userBo = TestUtils.createUserBONoRole();
        final String inventoryName = "03-2023";
        String subscriberName = "SUBSCRIBER";

        final InventoryUpdateRest inventoryUpdateRest = InventoryUpdateRest.builder()
                .id(1L)
                .name(inventoryName)
                .note(NoteUpsertRest.builder().content("newNote").build())
                .build();
        final Inventory inventory = Inventory
                .builder()
                .id(1L)
                .organization(linkedOrganization).build();


        when(organizationService.getOrganizationById(ORGANIZATION_ID)).thenReturn(linkedOrganization);
        when(inventoryRepo.findByOrganizationAndId(linkedOrganization, 1L)).thenReturn(Optional.of(inventory));

        InventoryBO result = inventoryService.updateInventory(subscriberName, organizationId, inventoryUpdateRest, userBo);

        verify(inventoryRepo, times(1)).save(any());
        assertThat(result.getNote().getContent()).isEqualTo("newNote");

    }

    @Test
    void shouldUpdateInventoryUpdateNote() {
        Long organizationId = 1L;
        final Workspace linkedWorkspace = TestUtils.createOrganization();
        UserBO userBo = TestUtils.createUserBONoRole();
        final String inventoryName = "03-2023";
        String subscriberName = "SUBSCRIBER";

        final InventoryUpdateRest inventoryUpdateRest = InventoryUpdateRest.builder()
                .id(1L)
                .name(inventoryName)
                .note(NoteUpsertRest.builder().content("newNote").build())
                .build();
        final Inventory inventory = Inventory
                .builder()
                .id(1L)
                .note(Note.builder().content("note").build())
                .workspace(linkedWorkspace).build();


        when(workspaceService.getWorkspaceById(ORGANIZATION_ID)).thenReturn(linkedWorkspace);
        when(inventoryRepo.findByWorkspaceAndId(linkedWorkspace, 1L)).thenReturn(Optional.of(inventory));

        InventoryBO result = inventoryService.updateInventory(subscriberName, organizationId, inventoryUpdateRest, userBo);

        verify(inventoryRepo, times(1)).save(any());
        assertThat(result.getNote().getContent()).isEqualTo("newNote");

    }
    @Test
    void UpdateInventoryNotFoundThrow() {
        UserBO userBo = TestUtils.createUserBONoRole();
        final InventoryUpdateRest inventoryUpdateRest = InventoryUpdateRest.builder()
                .id(INVENTORY_ID)
                .name("03-2023")
                .build();
            final Organization linkedOrganization = TestUtils.createOrganization();
            when(organizationService.getOrganizationById(ORGANIZATION_ID)).thenReturn(linkedOrganization);
            when(inventoryRepo.findByOrganizationAndId(linkedOrganization, INVENTORY_ID)).thenReturn(Optional.empty());

            G4itRestException exception = assertThrows(G4itRestException.class, () ->
                    inventoryService.updateInventory(SUBSCRIBER, ORGANIZATION_ID, inventoryUpdateRest, userBo)
            );
            assertThat(exception.getCode()).isEqualTo("404");
            assertThat(exception.getMessage()).isEqualTo(String.format("inventory %d not found in %s/%s",
                    INVENTORY_ID, SUBSCRIBER, ORGANIZATION_ID));
            verify(organizationService).getOrganizationById(ORGANIZATION_ID);
            verify(inventoryRepo).findByOrganizationAndId(linkedOrganization, INVENTORY_ID);

    }

}
