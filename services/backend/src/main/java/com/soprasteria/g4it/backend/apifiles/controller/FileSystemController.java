/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apifiles.controller;

import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobStorageException;
import com.soprasteria.g4it.backend.apifiles.business.FileSystemService;
import com.soprasteria.g4it.backend.apiinventory.business.InventoryService;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryBO;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryIntegrationReportBO;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.task.model.TaskStatus;
import com.soprasteria.g4it.backend.common.task.model.TaskType;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.FileSystemApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.FileDescriptionRest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * FileSystem service to access file storage.
 */
@Slf4j
@Service
public class FileSystemController implements FileSystemApiDelegate {

    /**
     * File System Service.
     */
    @Autowired
    private FileSystemService fileSystemService;

    /**
     * InventoryService
     */
    @Autowired
    private InventoryService inventoryService;

    /**
     * InventoryRepository
     */
    @Autowired
    private InventoryRepository inventoryRepository;

    /**
     * Task repository
     */
    @Autowired
    private TaskRepository taskRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<FileDescriptionRest>> listFiles(final String subscriber,
                                                               final Long organization,
                                                               final Long inventoryId) {
        try {
            return ResponseEntity.ok().body(fileSystemService.listFiles(subscriber, organization));
        } catch (final IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred while get file: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Map<String, List<String>>> uploadCSV(final String subscriber,
                                                               final Long organization,
                                                               final Long inventoryId,
                                                               final List<MultipartFile> datacenters,
                                                               final List<MultipartFile> applications,
                                                               final List<MultipartFile> physicalEquipments,
                                                               final List<MultipartFile> virtualEquipments

    ) {

        List<String> datacenterFiles = fileSystemService.manageFiles(subscriber, organization, datacenters);
        List<String> physicalEquipmentFiles = fileSystemService.manageFiles(subscriber, organization, physicalEquipments);
        List<String> virtualEquipmentFiles = fileSystemService.manageFiles(subscriber, organization, virtualEquipments);
        List<String> applicationFiles = fileSystemService.manageFiles(subscriber, organization, applications);

        return ResponseEntity.ok(Map.of(
                FileType.DATACENTER.getValue(), datacenterFiles,
                FileType.APPLICATION.getValue(), applicationFiles,
                FileType.EQUIPEMENT_PHYSIQUE.getValue(), physicalEquipmentFiles,
                FileType.EQUIPEMENT_VIRTUEL.getValue(), virtualEquipmentFiles
        ));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Resource> downloadResultsFile(@PathVariable final String subscriber,
                                                        @PathVariable final Long organization,
                                                        @PathVariable final Long inventoryId,
                                                        @PathVariable final String batchName) {
        final InventoryBO inventory = inventoryService.getInventory(subscriber, organization, inventoryId);
        String filename = String.join("/", batchName, Constants.REJECTED_FILES_ZIP);

        if (Boolean.FALSE.equals(inventory.getIsNewArch())) {
            Inventory inventoryEntity = inventoryRepository.findById(inventoryId).orElseThrow();

            InventoryIntegrationReportBO integrationReport = inventory.getIntegrationReports().stream()
                    .filter(integrationReportBO -> batchName.equals(integrationReportBO.getBatchName())
                            && integrationReportBO.getResultFileUrl() != null)
                    .findFirst()
                    .orElseThrow(() -> new G4itRestException("404",
                            String.format("Integration report not found in inventory %s on %s/%s",
                                    inventoryId, subscriber, organization)));

            String filenameOldArch = fileSystemService.getFilenameFromUrl(integrationReport.getResultFileUrl(), 1);

            List<Task> tasks = taskRepository.findByInventoryAndStatusAndType(
                            inventoryEntity,
                            TaskStatus.COMPLETED_WITH_ERRORS.toString(),
                            TaskType.LOADING.toString()).stream()
                    .filter(task -> task.getLastUpdateDate().isBefore(integrationReport.getCreateTime()))
                    .toList();

            final String filePath = String.join("/", subscriber, organization.toString(),
                    FileFolder.OUTPUT.getFolderName(), filenameOldArch);

            // Return existing rejected zip if no file for rejected clouds found
            if (CollectionUtils.isEmpty(tasks)) {
                try {
                    InputStream oldArchiveInputStream = fileSystemService.downloadFile(subscriber,
                            organization, FileFolder.OUTPUT, filenameOldArch);
                    return ResponseEntity.ok(new InputStreamResource(oldArchiveInputStream));
                } catch (BlobStorageException e) {
                    if (e.getErrorCode().equals(BlobErrorCode.BLOB_NOT_FOUND)) {
                        throw new G4itRestException("404",
                                String.format("File %s not found in storage", filePath));
                    }
                    throw new G4itRestException("500",
                            String.format("Error downloading file %s", filePath), e);
                } catch (IOException e) {
                    throw new G4itRestException("500",
                            String.format("Error processing file %s", filePath), e);
                }
            }

            Task latestTask = tasks.stream()
                    .max(Comparator.comparing(Task::getLastUpdateDate))
                    .orElseThrow(() -> new G4itRestException("404", "No completed tasks found with errors"));

            if (latestTask == null || latestTask.getId() == null) {
                throw new G4itRestException("500", "Task or Task ID is null");
            }

            String filename2 = String.join("/", latestTask.getId().toString(), Constants.REJECTED_FILES_ZIP);

            try {
                InputStream inputStreamCloud = null;
                try {
                    inputStreamCloud = fileSystemService.downloadFile(subscriber, organization,
                            FileFolder.OUTPUT, filename2);
                } catch (Exception e) {
                    InputStream oldArchiveInputStream = fileSystemService.downloadFile(subscriber,
                            organization, FileFolder.OUTPUT, filenameOldArch);
                    return ResponseEntity.ok(new InputStreamResource(oldArchiveInputStream));
                }

                if (inputStreamCloud != null) {
                    ByteArrayOutputStream baosVirtualEquip = new ByteArrayOutputStream();
                    ByteArrayOutputStream baosApplication = new ByteArrayOutputStream();
                    boolean virtualEquipFound = false;
                    boolean applicationFound = false;

                    try (ZipInputStream zis = new ZipInputStream(inputStreamCloud)) {
                        ZipEntry entry;
                        while ((entry = zis.getNextEntry()) != null) {
                            if (entry.getName().contains("rejected_INVENTORY_VIRTUAL_EQUIPMENT_CLOUD")) {
                                virtualEquipFound = true;
                                byte[] buffer = new byte[8192];
                                int len;
                                while ((len = zis.read(buffer)) > 0) {
                                    baosVirtualEquip.write(buffer, 0, len);
                                }
                            }
                            if (entry.getName().contains("rejected_application")) {
                                applicationFound = true;
                                byte[] buffer = new byte[8192];
                                int len;
                                while ((len = zis.read(buffer)) > 0) {
                                    baosApplication.write(buffer, 0, len);
                                }
                            }
                            zis.closeEntry();
                        }
                    }

                    if (virtualEquipFound || applicationFound) {
                        byte[] virtualEquipData = baosVirtualEquip.toByteArray();
                        byte[] applicationData = baosApplication.toByteArray();

                        try (InputStream oldArchiveInputStream = fileSystemService.downloadFile(subscriber,
                                organization, FileFolder.OUTPUT, filenameOldArch);
                             ZipInputStream oldArchiveZis = new ZipInputStream(oldArchiveInputStream);
                             ByteArrayOutputStream updatedZipBaos = new ByteArrayOutputStream();
                             ZipOutputStream zos = new ZipOutputStream(updatedZipBaos)) {

                            // Copy all existing entries
                            ZipEntry archiveEntry;
                            byte[] buffer = new byte[8192];
                            while ((archiveEntry = oldArchiveZis.getNextEntry()) != null) {
                                ZipEntry newEntry = new ZipEntry(archiveEntry.getName());
                                zos.putNextEntry(newEntry);
                                int len;
                                while ((len = oldArchiveZis.read(buffer)) > 0) {
                                    zos.write(buffer, 0, len);
                                }
                                zos.closeEntry();
                            }
                            // Add rejected_virtual_equipment.csv if found
                            if (virtualEquipFound) {
                                ZipEntry csvEntry = new ZipEntry("rejected_cloud_virtual_equipment.csv");
                                csvEntry.setSize(virtualEquipData.length);
                                zos.putNextEntry(csvEntry);
                                zos.write(virtualEquipData);
                                zos.closeEntry();
                            }
                            // Add rejected_cloud_application.csv if found
                            if (applicationFound) {
                                ZipEntry csvEntry = new ZipEntry("rejected_cloud_application.csv");
                                csvEntry.setSize(applicationData.length);
                                zos.putNextEntry(csvEntry);
                                zos.write(applicationData);
                                zos.closeEntry();
                            }

                            zos.finish();

                            byte[] finalZipContent = updatedZipBaos.toByteArray();
                            return ResponseEntity.ok()
                                    .header(HttpHeaders.CONTENT_DISPOSITION,
                                            "attachment; filename=\"" + filenameOldArch + "\"")
                                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                    .body(new InputStreamResource(new ByteArrayInputStream(finalZipContent)));
                        }
                    }
                }
            } catch (BlobStorageException e) {
                if (e.getErrorCode().equals(BlobErrorCode.BLOB_NOT_FOUND)) {
                    throw new G4itRestException("404",
                            String.format("File %s not found in storage", filePath));
                }
                throw new G4itRestException("500",
                        String.format("Error downloading file %s", filePath), e);
            } catch (IOException e) {
                throw new G4itRestException("500",
                        String.format("Error processing file %s", filePath), e);
            }
        } else {
            Inventory inventoryEntity = inventoryRepository.findById(inventoryId).orElseThrow();
            if (Boolean.TRUE.equals(inventoryEntity.getIsMigrated())) {
                Task task = taskRepository.findById(Long.valueOf(batchName)).orElseThrow();
                filename = fileSystemService.getFilenameFromUrl(task.getResultFileUrl(), 1);
            }
            String filePath = String.join("/", subscriber, organization.toString(),
                    FileFolder.OUTPUT.getFolderName(), filename);
            try {
                InputStream inputStream = fileSystemService.downloadFile(subscriber, organization, FileFolder.OUTPUT, filename);
                return ResponseEntity.ok(new InputStreamResource(inputStream));
            } catch (BlobStorageException e) {
                if (e.getErrorCode().equals(BlobErrorCode.BLOB_NOT_FOUND)) {
                    throw new G4itRestException("404",
                            String.format("file %s not found in filestorage", filePath));
                }
                throw new G4itRestException("500",
                        String.format("Something went wrong downloading file %s", filePath), e);
            } catch (FileNotFoundException e) {
                throw new G4itRestException("404",
                        String.format("file %s not found in filestorage", filePath));
            } catch (IOException e) {
                throw new G4itRestException("500",
                        String.format("Something went wrong downloading file %s", filePath), e);
            }
        }
        return null;
    }


}
