/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.controller;

import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobStorageException;
import com.soprasteria.g4it.backend.apifiles.business.FileSystemService;
import com.soprasteria.g4it.backend.apiindicator.business.InventoryIndicatorService;
import com.soprasteria.g4it.backend.apiindicator.mapper.IndicatorRestMapper;
import com.soprasteria.g4it.backend.apiindicator.model.ApplicationImpactBO;
import com.soprasteria.g4it.backend.apiindicator.model.ApplicationIndicatorBO;
import com.soprasteria.g4it.backend.apiindicator.model.EquipmentIndicatorBO;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiuser.business.AuthService;
import com.soprasteria.g4it.backend.apiuser.business.RoleService;
import com.soprasteria.g4it.backend.apiuser.modeldb.UserOrganization;
import com.soprasteria.g4it.backend.apiuser.repository.SubscriberRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserOrganizationRepository;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.InventoryIndicatorApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.*;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Inventory Indicator Job Service.
 */
@Service
@NoArgsConstructor
public class InventoryIndicatorController implements InventoryIndicatorApiDelegate {

    private static final String ERROR_DOWNLOADING_FILE = "Something went wrong downloading file %s";
    private static final String FILE_NOT_FOUND = "file %s not found in filestorage";
    @Autowired
    private InventoryIndicatorService inventoryIndicatorService;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private IndicatorRestMapper indicatorRestMapper;
    @Autowired
    private FileSystemService fileSystemService;
    @Autowired
    private AuthService authService;
    @Autowired
    private UserOrganizationRepository userOrganizationRepository;
    @Autowired
    private SubscriberRepository subscriberRepository;
    @Autowired
    private RoleService roleService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Map<String, EquipmentIndicatorRest>> getEquipmentIndicators(final String subscriber,
                                                                                      final Long organization,
                                                                                      final Long inventoryId) {
        final Map<String, EquipmentIndicatorBO> indicators = inventoryIndicatorService.getEquipmentIndicators(subscriber, organization, inventoryId);
        return ResponseEntity.ok().body(indicators.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, map -> this.indicatorRestMapper.toDto(map.getValue()))));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<ApplicationIndicatorRest>> getApplicationIndicators(final String subscriber,
                                                                                   final Long organization,
                                                                                   final Long inventoryId) {
        final List<ApplicationIndicatorBO<ApplicationImpactBO>> indicators = inventoryIndicatorService.getApplicationIndicators(subscriber, organization, inventoryId);
        return ResponseEntity.ok().body(this.indicatorRestMapper.toApplicationIndicatorDto(indicators));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<PhysicalEquipmentLowImpactRest>> getPhysicalEquipmentsLowImpact(final String subscriber,
                                                                                               final Long organization,
                                                                                               final Long inventoryId) {
        return ResponseEntity.ok(indicatorRestMapper.toLowImpactDto(inventoryIndicatorService.getPhysicalEquipmentsLowImpact(subscriber, organization, inventoryId)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<PhysicalEquipmentsAvgAgeRest>> getPhysicalEquipmentAvgAge(final String subscriber,
                                                                                         final Long organization,
                                                                                         final Long inventoryId) {
        return ResponseEntity.ok(indicatorRestMapper.toAvgAgeDto(inventoryIndicatorService.getPhysicalEquipmentAvgAge(inventoryId)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<PhysicalEquipmentElecConsumptionRest>> getPhysicalEquipmentElecConsumption(final String subscriber,
                                                                                                          final Long organizationId,
                                                                                                          final Long inventoryId) {
        return ResponseEntity.ok(indicatorRestMapper.toElecConsumptionDto(inventoryIndicatorService.getPhysicalEquipmentElecConsumption(subscriber, organizationId, inventoryId)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteIndicators(final String subscriber,
                                                 final Long organization,
                                                 final Long inventoryId) {
        inventoryIndicatorService.deleteIndicators(subscriber, organization, inventoryId);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<DataCentersInformationRest>> getDataCenterIndicators(final String subscriber,
                                                                                    final Long organization,
                                                                                    final Long inventoryId) {
        return ResponseEntity.ok().body(
                indicatorRestMapper.toDataCenterDto(inventoryIndicatorService.getDataCenterIndicators(inventoryId))
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Resource> getIndicatorsExportResult(final String subscriber,
                                                              final Long organization,
                                                              final Long inventoryId) {

        Task task = taskRepository.findByInventoryAndLastCreationDate(Inventory.builder().id(inventoryId).build())
                .orElseThrow(() -> new G4itRestException("404", String.format("task of inventoryId '%d' is not found", inventoryId)));

        Long userId = authService.getUser().getId();
        boolean isAdmin = roleService.hasAdminRightOnSubscriberOrOrganization(authService.getUser(), subscriberRepository.findByName(subscriber).get().getId(), organization);
        if (!isAdmin) {
            UserOrganization userOrganization = userOrganizationRepository.findByOrganizationIdAndUserId(organization, userId).orElseThrow();

            boolean isDefaultOrganization = userOrganization.getOrganization().getName().equalsIgnoreCase("DEMO");
            boolean hasAccess = userOrganization.getRoles().stream().anyMatch(role -> "ROLE_INVENTORY_WRITE".equals(role.getName()));

            if (!(isDefaultOrganization || hasAccess)) {
                throw new G4itRestException("403", "Not authorized");
            }
        }

        String filename = task.getId() + Constants.ZIP;

        final String filePath = String.join("/", subscriber, organization.toString(), FileFolder.EXPORT.getFolderName(), filename);

        try {
            InputStream inputStream = fileSystemService.downloadFile(subscriber, organization, FileFolder.EXPORT, filename);
            return ResponseEntity.ok(new InputStreamResource(inputStream));
        } catch (BlobStorageException e) {
            if (e.getErrorCode().equals(BlobErrorCode.BLOB_NOT_FOUND)) {
                throw new G4itRestException("404", String.format("file %s not found in filestorage", filePath));
            } else {
                throw new G4itRestException("500", String.format("Something went wrong downloading file %s", filePath), e);
            }
        } catch (FileNotFoundException e) {
            throw new G4itRestException("404", String.format("file %s not found in filestorage", filePath));
        } catch (IOException e) {
            throw new G4itRestException("500", String.format("Something went wrong downloading file %s", filePath), e);
        }
    }

}
