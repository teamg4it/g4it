/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.common.filesystem.business;

import com.azure.core.http.rest.PagedIterable;
import com.azure.spring.cloud.core.resource.StorageBlobResource;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobItemProperties;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.soprasteria.g4it.backend.common.filesystem.model.FileDescription;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.utils.SanitizeUrl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * File Storage implementation for Azure.
 */
@Slf4j
@RequiredArgsConstructor

public class AzureFileStorage implements FileStorage {

    /**
     * The file path delimiter.
     */
    private static final String FILE_PATH_DELIMITER = "/";

    /**
     * The blob service client, to create StorageBlobResource.
     */
    private final BlobServiceClient blobServiceClient;

    /**
     * The azure blob container client, to retrieve or write file on the blob storage.
     */
    private final BlobContainerClient blobContainerClient;

    /**
     * The Organization workspace.
     */
    private final String workspace;

    /**
     * The organization azure storage (start with azure-blob://[container-name].
     */
    private final String organizationAzureStoragePrefix;

    private static final String MOVE_MESSAGE = "Moving {} to {}";
    private static final String WRITE_MESSAGE = "Writing {}";

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream readFile(final FileFolder folder, final String fileName) throws IOException {
        final BlobClient blob = blobContainerClient.getBlobClient(String.join(FILE_PATH_DELIMITER, workspace, folder.getFolderName(), fileName));
        log.info("Reading {}", blob.getBlobUrl());
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        blob.downloadStream(outputStream);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeFile(final FileFolder folder, final String fileName, final String content) throws IOException {
        final BlobClient blob = blobContainerClient.getBlobClient(String.join(FILE_PATH_DELIMITER, workspace, folder.getFolderName(), fileName));
        log.info(WRITE_MESSAGE, blob.getBlobUrl());
        blob.upload(new ByteArrayInputStream(content.getBytes()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeFile(final FileFolder folder, final String fileName, final InputStream content) throws IOException {
        final BlobClient blob = blobContainerClient.getBlobClient(String.join(FILE_PATH_DELIMITER, workspace, folder.getFolderName(), fileName));
        log.info(WRITE_MESSAGE, blob.getBlobUrl());
        blob.upload(content);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FileDescription> listFiles(final FileFolder folder) throws IOException {
        return listFiles(workspace, folder.getFolderName()).stream().map(this::fileFromBlob).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasFileInSubfolder(final FileFolder folder, final String subfolder, final FileType fileType) throws IOException {
        return !listFiles(workspace, folder.getFolderName(), subfolder, fileType.name()).stream().toList().isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource[] listResources(final FileFolder folder, final String subfolder, final FileType fileType) throws IOException {
        return listFiles(workspace, folder.getFolderName(), subfolder, fileType.name()).stream().map(this::mapToResource).toArray(Resource[]::new);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rename(final FileFolder folder, final String currentName, final String newName) {
        final BlobClient newBlobClient = blobContainerClient.getBlobClient(filePath(folder, newName));
        final BlobClient oldBlobClient = blobContainerClient.getBlobClient(filePath(folder, currentName));
        log.info(MOVE_MESSAGE, oldBlobClient.getBlobUrl(), newBlobClient.getBlobUrl());
        newBlobClient.copyFromUrl(getSasUrl(oldBlobClient));

        oldBlobClient.delete();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void move(final FileFolder srcFolder, final FileFolder destFolder, final String fileName) {
        final BlobClient newBlobClient = blobContainerClient.getBlobClient(filePath(destFolder, fileName));
        final BlobClient oldBlobClient = blobContainerClient.getBlobClient(filePath(srcFolder, fileName));
        log.info(MOVE_MESSAGE, oldBlobClient.getBlobUrl(), newBlobClient.getBlobUrl());
        newBlobClient.copyFromUrl(getSasUrl(oldBlobClient));

        oldBlobClient.delete();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void moveAndRename(final FileFolder srcFolder, final FileFolder destFolder, final String currentName, final String newName) {
        final BlobClient newBlobClient = blobContainerClient.getBlobClient(filePath(destFolder, newName));
        final BlobClient oldBlobClient = blobContainerClient.getBlobClient(filePath(srcFolder, currentName));
        log.info(MOVE_MESSAGE, oldBlobClient.getBlobUrl(), newBlobClient.getBlobUrl());
        newBlobClient.copyFromUrl(getSasUrl(oldBlobClient));

        oldBlobClient.delete();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final FileFolder folder, final String fileName) {
        final BlobClient blobClient = blobContainerClient.getBlobClient(filePath(folder, fileName));
        if (Boolean.TRUE.equals(blobClient.exists())) {
            log.info("Deleting {}", blobClient.getBlobUrl());
            blobClient.delete();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteFolder(final FileFolder folder, final String path) {

        String fullPath = String.join(FILE_PATH_DELIMITER, workspace, folder.getFolderName());
        if (path != null) fullPath = String.join(FILE_PATH_DELIMITER, fullPath, path);

        ListBlobsOptions options = new ListBlobsOptions()
                .setPrefix(fullPath)
                .setDetails(new BlobListDetails().setRetrieveDeletedBlobs(false).setRetrieveSnapshots(false));

        blobContainerClient.listBlobs(options, null).iterator()
                .forEachRemaining(item ->
                        blobContainerClient.getBlobClient(item.getName()).delete());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void upload(final String fileLocalPath, final FileFolder folder, final String fileName) {
        final BlobClient blob = blobContainerClient.getBlobClient(filePath(folder, fileName));
        log.info("Uploading {}", blob.getBlobName());
        blob.uploadFromFile(fileLocalPath, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String upload(final FileFolder folder, final String fileName, final String type, final InputStream fileContent) throws IOException {
        final BlobClient client = blobContainerClient.getBlobClient(filePath(folder, fileName));
        log.info("Uploading {}", client.getBlobName());
        try (final ByteArrayInputStream bis = new ByteArrayInputStream(fileContent.readAllBytes())) {
            client.upload(bis);
            client.setMetadata(Map.of("type", getTypeFromString(type).getValue()));
            return client.getBlobName().replaceFirst("input\\\\", "");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFileUrl(final FileFolder folder, final String fileName) {
        final BlobClient client = blobContainerClient.getBlobClient(filePath(folder, fileName));
        if (Boolean.TRUE.equals(client.exists())) {
            log.info("Generating SAS url for file {}", client.getBlobUrl());
            return SanitizeUrl.removeTokenAndEncoding(client.getBlobUrl());
        }
        log.warn("Can't generate SAS token for unknown file {}", fileName);
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getFileSize(final FileFolder folder, final String fileName) {
        final BlobClient client = blobContainerClient.getBlobClient(filePath(folder, fileName));
        if (Boolean.TRUE.equals(client.exists())) {
            log.info("Retrieve file size for {}", client.getBlobUrl());
            return client.getProperties().getBlobSize();
        }
        log.warn("Can't retrieve size for unknown file {}", fileName);
        return 0;
    }

    @Override
    public void renameWorkspace(String newWorkspace) throws IOException {
        final ListBlobsOptions options = new ListBlobsOptions();
        options.setPrefix(String.join(FILE_PATH_DELIMITER, workspace));
        options.setDetails(new BlobListDetails().setRetrieveMetadata(true));
        blobContainerClient.listBlobs(options, null).iterator()
                .forEachRemaining(item -> {
                    final String oldFilePath = Path.of(item.getName()).toString();
                    final BlobClient oldBlobClient = blobContainerClient.getBlobClient(oldFilePath);
                    final String newFilePath = oldFilePath.replaceFirst(workspace, newWorkspace);
                    final BlobClient newBlobClient = blobContainerClient.getBlobClient(newFilePath);
                    log.info(MOVE_MESSAGE, oldBlobClient.getBlobUrl(), newBlobClient.getBlobUrl());
                    newBlobClient.copyFromUrl(getSasUrl(oldBlobClient));
                    oldBlobClient.delete();
                });
    }

    /**
     * Convert Azure BlobItem to the StorageBlobResource.
     *
     * @param item the blob item.
     * @return the resource.
     */
    private Resource mapToResource(final BlobItem item) {
        return new StorageBlobResource(blobServiceClient, String.join(FILE_PATH_DELIMITER, organizationAzureStoragePrefix, item.getName()));
    }

    /**
     * Common list files function.
     *
     * @param folders The folder names that will be concatenated with the FILE_PATH_DELIMITER.
     * @return the blobItem list.
     */
    private PagedIterable<BlobItem> listFiles(final String... folders) {
        final ListBlobsOptions options = new ListBlobsOptions();
        options.setPrefix(String.join(FILE_PATH_DELIMITER, folders));
        options.setDetails(new BlobListDetails().setRetrieveMetadata(true));
        return blobContainerClient.listBlobs(options, null);
    }

    private String filePath(final FileFolder folder, final String fileName) {
        return Path.of(workspace, folder.getFolderName(), fileName).toString();
    }

    private FileDescription fileFromBlob(final BlobItem blob) {
        final FileType type = Optional.ofNullable(blob.getMetadata()).map(map -> map.get("type")).map(this::getTypeFromString).orElse(FileType.UNKNOWN);
        final Map<String, String> metadata = this.metadataFromBlobProperties(blob.getProperties());
        return FileDescription.builder().name(blob.getName()).type(type).metadata(metadata).build();
    }

    private Map<String, String> metadataFromBlobProperties(final BlobItemProperties properties) {
        return Map.of("creationTime", properties.getCreationTime().toString(),
                "size", properties.getContentLength().toString());
    }

    private FileType getTypeFromString(final String type) {
        try {
            return Optional.ofNullable(type).map(FileType::fromValue).orElse(FileType.UNKNOWN);
        } catch (final IllegalArgumentException e) {
            return FileType.UNKNOWN;
        }
    }

    /**
     * Get URL with associated SAS token
     *
     * @param client blob we want the url from
     * @return the complete URL with SAS token
     */
    private String getSasUrl(final BlobClient client) {
        return client.getBlobUrl() + "?" + getSasToken(client);
    }

    /**
     * Generate SAS token for 30 minutes
     *
     * @param client blob client
     * @return Shared Access Signature token
     */
    private String getSasToken(final BlobClient client) {
        // Creating the SAS Token to get the permission to copy the source blob
        final OffsetDateTime expiryTime = OffsetDateTime.now().plusHours(1);
        final BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);
        final BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(expiryTime, permission)
                .setStartTime(OffsetDateTime.now().minusHours(1));
        return client.generateSas(values);
    }

}
