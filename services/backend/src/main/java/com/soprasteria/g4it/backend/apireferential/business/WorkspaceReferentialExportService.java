package com.soprasteria.g4it.backend.apireferential.business;

import com.soprasteria.g4it.backend.apireferential.modeldb.*;
import com.soprasteria.g4it.backend.apireferential.repository.*;
import com.soprasteria.g4it.backend.common.utils.CsvUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.EnumSet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.function.Function;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkspaceReferentialExportService {

    private static final String REFERENTIAL = "workspace-referential";

    @Value("${local.working.folder}")
    private String localWorkingFolder;

    private final ItemTypeRepository itemTypeRepository;
    private final ItemImpactRepository itemImpactRepository;
    private final MatchingItemRepository matchingItemRepository;

    @Value("${export.page.size:500}")
    private int pageSize;

    @PostConstruct
    public void initFolder() throws IOException {
        createSecureDirectories(Path.of(localWorkingFolder, REFERENTIAL));
    }

    /**
     * Create a directory (and its parents) restricted to the owner only, when the
     * underlying file system supports POSIX permissions, to avoid creating
     * world-readable/writable directories with the default umask.
     */
    private static void createSecureDirectories(Path directory) throws IOException {
        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            Files.createDirectories(directory, PosixFilePermissions.asFileAttribute(
                    EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE)));
            Files.setPosixFilePermissions(directory,
                    EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE));
        } else {
            Files.createDirectories(directory);
        }
    }



    /**
     * Export workspace referential zip
     */
    public InputStream exportReferentialZip(String organization, Long workspaceId) throws IOException {

        if (workspaceId == null) {
            throw new IllegalArgumentException("workspaceId cannot be null");
        }


        long startTime = System.currentTimeMillis();

        log.info("Exporting ZIP for workspace {}", workspaceId);

        Path referentialDirectory = Path.of(localWorkingFolder, REFERENTIAL);
        createSecureDirectories(referentialDirectory);

        Path zipPath = Files.createTempFile(
                referentialDirectory,
                "workspace_referential_",
                ".zip"
        );

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            zos.putNextEntry(new ZipEntry("workspace-referential/"));
            zos.closeEntry();

            addCsvToZip(zos, workspaceId, "itemType",
                    ItemType.getCsvHeadersForWorkspace(),
                    pageable -> itemTypeRepository.findByWorkspaceId(workspaceId, pageable),
                    ItemType::toCsvRecordForWorkspace);

            addCsvToZip(zos, workspaceId, "itemImpact",
                    ItemImpact.getCsvHeadersForWorkspace(),
                    pageable -> itemImpactRepository.findByWorkspaceId(workspaceId, pageable),
                    ItemImpact::toCsvRecordForWorkspace);

            addCsvToZip(zos, workspaceId, "matchingItem",
                    MatchingItem.getCsvHeadersForWorkspace(),
                    pageable -> matchingItemRepository.findByWorkspaceId(workspaceId, pageable),
                    MatchingItem::toCsvRecordForWorkspace);
        }

        log.info("ZIP export completed for workspace {}", workspaceId);

        InputStream inputStream = Files.newInputStream(zipPath);

        return new FilterInputStream(inputStream) {
            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    Files.deleteIfExists(zipPath);
                }
            }
        };
    }

    private <T> void addCsvToZip(
            ZipOutputStream zos,
            Long workspaceId,
            String type,
            String[] headers,
            Function<Pageable, Page<T>> pageFetcher,
            Function<T, Object[]> mapper
    ) throws IOException {

        long count = 0;
        int pageNumber = 0;

        Pageable firstPage = PageRequest.of(0, pageSize, Sort.by("id"));
        Page<T> page = pageFetcher.apply(firstPage);

        boolean hasData = !page.isEmpty();

        String fileName = hasData
                ? type + ".csv"
                : type + "_template.csv";

        zos.putNextEntry(new ZipEntry("workspace-referential/" + fileName));

        CSVFormat csvFormat = CSVFormat.Builder.create()
                .setDelimiter(CsvUtils.DELIMITER)
                .build();

        OutputStreamWriter writer = new OutputStreamWriter(
                new FilterOutputStream(zos) {
                    @Override
                    public void close() throws IOException {
                        // prevent closing underlying ZIP stream
                    }
                },
                StandardCharsets.UTF_8
        );

        writer.write("\uFEFF");
        writer.flush();

        try (CSVPrinter printer = new CSVPrinter(writer, csvFormat)) {

            // writing header manually AFTER BOM
            printer.printRecord((Object[]) headers);

            for (T item : page.getContent()) {
                if (item != null) {
                    Object[] record = mapper.apply(item);
                    if (record != null) {
                        printer.printRecord(record);
                    }
                }
            }


            count += page.getNumberOfElements();
            pageNumber = 1;

            while (page.hasNext()) {
                Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("id"));
                page = pageFetcher.apply(pageable);

                for (T item : page.getContent()) {
                    if (item != null) {
                        Object[] record = mapper.apply(item);
                        if (record != null) {
                            printer.printRecord(record);
                        }
                    }
                }

                count += page.getNumberOfElements();
                pageNumber++;
            }

            printer.flush();
        }
        zos.closeEntry();
        log.info("Added {} ({} rows) to ZIP", fileName, count);
    }
}