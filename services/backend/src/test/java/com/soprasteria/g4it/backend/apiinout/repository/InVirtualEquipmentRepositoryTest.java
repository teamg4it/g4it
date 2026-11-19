/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.repository;

import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.apiuser.repository.WorkspaceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Repository-level test for {@link InVirtualEquipmentRepository}'s distinct-count queries,
 * verifying that virtual equipment and cloud service counts are mutually exclusive and
 * correctly deduplicated by (name, quantity) against a real (H2) database.
 */
@SpringBootTest(
        properties = {
                "spring.cloud.azure.storage.blob.enabled=false",
                "spring.liquibase.enabled=false",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.cloud.azure.keyvault.secret.enabled=false"
        }
)
@ActiveProfiles({"local", "test"})
class InVirtualEquipmentRepositoryTest {

    private static final String ORGANIZATION = "ORGANIZATION";

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InVirtualEquipmentRepository inVirtualEquipmentRepository;

    @AfterEach
    void cleanUp() {
        inVirtualEquipmentRepository.deleteAll();
        inventoryRepository.deleteAll();
        workspaceRepository.deleteAll();
    }

    @Test
    void countVirtualEquipmentAndCloudServiceCounts_excludeEachOther() {

        var workspace = workspaceRepository.save(Workspace.builder()
                .name("CLOUD-COUNT-DEMO")
                .organization(Organization.builder().name(ORGANIZATION).build())
                .build());

        var inventory = inventoryRepository.save(Inventory.builder()
                .name("Cloud Count Inventory")
                .workspace(workspace)
                .doExportVerbose(true)
                .build());

        // Two distinct non-cloud virtual equipments (quantity 2 and 3 -> sum = 5)
        inVirtualEquipmentRepository.save(InVirtualEquipment.builder()
                .name("vm-1")
                .inventoryId(inventory.getId())
                .quantity(2d)
                .infrastructureType("NON_CLOUD_SERVERS")
                .build());
        inVirtualEquipmentRepository.save(InVirtualEquipment.builder()
                .name("vm-2")
                .inventoryId(inventory.getId())
                .quantity(3d)
                .infrastructureType("NON_CLOUD_SERVERS")
                .build());

        // Two distinct cloud services (quantity 4 and 6 -> sum = 10)
        inVirtualEquipmentRepository.save(InVirtualEquipment.builder()
                .name("cloud-1")
                .inventoryId(inventory.getId())
                .quantity(4d)
                .infrastructureType("CLOUD_SERVICES")
                .build());
        inVirtualEquipmentRepository.save(InVirtualEquipment.builder()
                .name("cloud-2")
                .inventoryId(inventory.getId())
                .quantity(6d)
                .infrastructureType("CLOUD_SERVICES")
                .build());

        // Duplicate row for the same cloud service name/quantity: must be deduplicated,
        // not double counted (mirrors the dedup-by-name-and-quantity grouping used by both queries)
        inVirtualEquipmentRepository.save(InVirtualEquipment.builder()
                .name("cloud-1")
                .inventoryId(inventory.getId())
                .quantity(4d)
                .infrastructureType("CLOUD_SERVICES")
                .build());

        Long virtualEquipmentCount = inVirtualEquipmentRepository.countQuantityByDistinctNameByInventoryId(inventory.getId());
        Long cloudServiceCount = inVirtualEquipmentRepository.countCloudServiceQuantityByDistinctNameByInventoryId(inventory.getId());

        assertEquals(5L, virtualEquipmentCount, "virtual equipment count must exclude cloud services");
        assertEquals(10L, cloudServiceCount, "cloud service count must only include cloud services and dedup by name+quantity");
    }

    @Test
    void countCloudServiceQuantity_returnsZero_whenNoCloudServices() {

        var workspace = workspaceRepository.save(Workspace.builder()
                .name("NO-CLOUD-DEMO")
                .organization(Organization.builder().name(ORGANIZATION).build())
                .build());

        var inventory = inventoryRepository.save(Inventory.builder()
                .name("No Cloud Inventory")
                .workspace(workspace)
                .doExportVerbose(true)
                .build());

        inVirtualEquipmentRepository.save(InVirtualEquipment.builder()
                .name("vm-only")
                .inventoryId(inventory.getId())
                .quantity(1d)
                .infrastructureType("NON_CLOUD_SERVERS")
                .build());

        Long cloudServiceCount = inVirtualEquipmentRepository.countCloudServiceQuantityByDistinctNameByInventoryId(inventory.getId());

        assertEquals(0L, cloudServiceCount, "cloud service count must be 0 when no cloud services exist");
    }
}
