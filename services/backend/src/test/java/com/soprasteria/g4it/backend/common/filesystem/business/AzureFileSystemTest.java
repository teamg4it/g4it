package com.soprasteria.g4it.backend.common.filesystem.business;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.ListBlobContainersOptions;
import com.soprasteria.g4it.backend.common.filesystem.exception.FileStorageAccessExcepton;
import com.soprasteria.g4it.backend.common.filesystem.external.VaultAccessClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AzureFileSystemTest {
    @Mock
    private VaultAccessClient vaultAccessClient;

    @Test
    void mount_shouldReturnAzureFileStorage_withoutSubEnv() {

        String organization = "org1";
        String workspace = "workspace1";
        String connectionString = "connection-string";
        String containerName = "g4it-container";

        when(vaultAccessClient.getConnectionStringForOrganization(organization))
                .thenReturn(connectionString);

        try (MockedConstruction<BlobServiceClientBuilder> mockedBuilder =
                     mockConstruction(BlobServiceClientBuilder.class,
                             (builderMock, context) -> {

                                 BlobServiceClient blobServiceClient = mock(BlobServiceClient.class);
                                 BlobContainerItem containerItem = mock(BlobContainerItem.class);
                                 BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);

                                 when(builderMock.connectionString(connectionString)).thenReturn(builderMock);
                                 when(builderMock.buildClient()).thenReturn(blobServiceClient);

                                 when(containerItem.getName()).thenReturn(containerName);
                                 PagedIterable<BlobContainerItem> pagedIterable = mock(PagedIterable.class);

                                 when(pagedIterable.stream()).thenReturn(Stream.of(containerItem));

                                 when(blobServiceClient.listBlobContainers(any(ListBlobContainersOptions.class), isNull()))
                                         .thenReturn(pagedIterable);

                                 when(blobServiceClient.getBlobContainerClient(containerName))
                                         .thenReturn(blobContainerClient);
                             })) {

            AzureFileSystem fileSystem = new AzureFileSystem(vaultAccessClient, "");

            FileStorage storage = fileSystem.mount(organization, workspace);

            assertNotNull(storage);
            verify(vaultAccessClient).getConnectionStringForOrganization(organization);
        }
    }

    @Test
    void mount_shouldPrefixWorkspace_whenSubEnvIsPresent() {

        String organization = "org1";
        String workspace = "workspace1";
        String subEnv = "dev";
        String connectionString = "connection-string";
        String containerName = "g4it-container";

        when(vaultAccessClient.getConnectionStringForOrganization(organization))
                .thenReturn(connectionString);

        try (MockedConstruction<BlobServiceClientBuilder> mockedBuilder =
                     mockConstruction(BlobServiceClientBuilder.class,
                             (builderMock, context) -> {

                                 BlobServiceClient blobServiceClient = mock(BlobServiceClient.class);
                                 BlobContainerItem containerItem = mock(BlobContainerItem.class);
                                 BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);

                                 when(builderMock.connectionString(connectionString)).thenReturn(builderMock);
                                 when(builderMock.buildClient()).thenReturn(blobServiceClient);

                                 when(containerItem.getName()).thenReturn(containerName);

                                 PagedIterable<BlobContainerItem> pagedIterable = mock(PagedIterable.class);

                                 when(pagedIterable.stream()).thenReturn(Stream.of(containerItem));

                                 when(blobServiceClient.listBlobContainers(any(ListBlobContainersOptions.class), isNull()))
                                         .thenReturn(pagedIterable);

                                 when(blobServiceClient.getBlobContainerClient(containerName))
                                         .thenReturn(blobContainerClient);
                             })) {

            AzureFileSystem fileSystem = new AzureFileSystem(vaultAccessClient, subEnv);

            FileStorage storage = fileSystem.mount(organization, workspace);

            assertNotNull(storage);
        }
    }

    @Test
    void mount_shouldThrowException_whenNoContainerFound() {

        String organization = "org1";
        String workspace = "workspace1";
        String connectionString = "connection-string";

        when(vaultAccessClient.getConnectionStringForOrganization(organization))
                .thenReturn(connectionString);

        try (MockedConstruction<BlobServiceClientBuilder> mockedBuilder =
                     mockConstruction(BlobServiceClientBuilder.class,
                             (builderMock, context) -> {

                                 BlobServiceClient blobServiceClient = mock(BlobServiceClient.class);
                                 when(builderMock.connectionString(connectionString)).thenReturn(builderMock);
                                 when(builderMock.buildClient()).thenReturn(blobServiceClient);
                                 PagedIterable<BlobContainerItem> pagedIterable = mock(PagedIterable.class);

                                 // Return empty stream to trigger exception
                                 when(pagedIterable.stream()).thenReturn(Stream.empty());

                                 when(blobServiceClient.listBlobContainers(any(ListBlobContainersOptions.class), isNull()))
                                         .thenReturn(pagedIterable);
                             })) {

            AzureFileSystem fileSystem = new AzureFileSystem(vaultAccessClient, "");

            assertThrows(FileStorageAccessExcepton.class,
                    () -> fileSystem.mount(organization, workspace));
        }
    }
}
