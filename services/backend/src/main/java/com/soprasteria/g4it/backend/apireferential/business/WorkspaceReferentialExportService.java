package com.soprasteria.g4it.backend.apireferential.business;

import com.soprasteria.g4it.backend.apireferential.modeldb.*;
import com.soprasteria.g4it.backend.apireferential.repository.*;
import com.soprasteria.g4it.backend.common.utils.CsvUtils;
import com.soprasteria.g4it.backend.exception.BadRequestException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
        Files.createDirectories(Path.of(localWorkingFolder, REFERENTIAL));
    }



    /**
     * Export workspace referential zip
     */
    public InputStream exportReferentialZip(Long workspaceId) throws IOException {

        if (workspaceId == null) {
            throw new IllegalArgumentException("workspaceId cannot be null");
        }

        log.info("Exporting ZIP for workspace {}", workspaceId);
        long start = System.currentTimeMillis();

        Path zipPath = Files.createTempFile("workspace_referential_", ".zip");
        zipPath.toFile().deleteOnExit();
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            zos.putNextEntry(new ZipEntry("workspace-referential/"));
            zos.closeEntry();

            addCsvToZip(zos, workspaceId, "itemType",
                    ItemType.getCsvHeaders(),
                    pageable -> itemTypeRepository.findByWorkspaceId(workspaceId, pageable),
                    ItemType::toCsvRecord);

            addCsvToZip(zos, workspaceId, "itemImpact",
                    ItemImpact.getCsvHeaders(),
                    pageable -> itemImpactRepository.findByWorkspaceId(workspaceId, pageable),
                    ItemImpact::toCsvRecord);

            addCsvToZip(zos, workspaceId, "matchingItem",
                    MatchingItem.getCsvHeaders(),
                    pageable -> matchingItemRepository.findByWorkspaceId(workspaceId, pageable),
                    MatchingItem::toCsvRecord);
        }

        log.info("ZIP export completed for workspace {} in {} ms",
                workspaceId,
                System.currentTimeMillis() - start);
        return Files.newInputStream(zipPath);
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