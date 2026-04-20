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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.function.Function;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

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
     * Export workspace referential to CSV
     */
    public InputStream exportReferentialToCSV(Long workspaceId, String type) throws IOException {

        if (workspaceId == null) {
            throw new IllegalArgumentException("workspaceId cannot be null");
        }
        if (!List.of("itemType", "itemImpact", "matchingItem").contains(type)) {
            throw new BadRequestException("type", "Unsupported type");
        }
        return switch (type) {

            case "itemType" -> exportPaginated(
                    workspaceId,
                    type,
                    ItemType.getCsvHeaders(),
                    pageable -> itemTypeRepository.findByWorkspaceId(workspaceId, pageable),
                    ItemType::toCsvRecord
            );

            case "itemImpact" -> exportPaginated(
                    workspaceId,
                    type,
                    ItemImpact.getCsvHeaders(),
                    pageable -> itemImpactRepository.findByWorkspaceId(workspaceId, pageable),
                    ItemImpact::toCsvRecord
            );

            case "matchingItem" -> exportPaginated(
                    workspaceId,
                    type,
                    MatchingItem.getCsvHeaders(),
                    pageable -> matchingItemRepository.findByWorkspaceId(workspaceId, pageable),
                    MatchingItem::toCsvRecord
            );

            default -> throw new BadRequestException(
                    "type",
                    String.format("type '%s' not supported for workspace export", type)
            );
        };
    }

    private <T> InputStream exportPaginated(
            Long workspaceId,
            String type,
            String[] headers,
            Function<Pageable, Page<T>> pageFetcher,
            Function<T, Object[]> mapper
    ) throws IOException {

        log.info("Exporting workspace {} type {}", workspaceId, type);

        long totalCount = 0;

        CSVFormat csvFormat = CSVFormat.Builder.create()
                .setHeader(headers)
                .setDelimiter(CsvUtils.DELIMITER)
                .build();

        Path tempFile = Files.createTempFile("export_", ".csv");

        boolean hasData = false;

        try {
            try (BufferedWriter writer = Files.newBufferedWriter(tempFile);
                 CSVPrinter printer = new CSVPrinter(writer, csvFormat)) {

                int pageNumber = 0;
                Page<T> page;

                do {
                    Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("id"));

                    page = pageFetcher.apply(pageable);

                    if (!page.isEmpty()) {
                        hasData = true;
                    }

                    for (T item : page.getContent()) {
                        if (item != null) {
                            Object[] record = mapper.apply(item);
                            if (record != null) {
                                printer.printRecord(record);
                            }
                        }
                    }

                    totalCount += page.getNumberOfElements();
                    pageNumber++;

                } while (page.hasNext());

                printer.flush();
            }

            String fileName = hasData
                    ? type + ".csv"
                    : type + "_template.csv";

            Path finalPath = Path.of(localWorkingFolder)
                    .resolve(REFERENTIAL)
                    .resolve(fileName);

            Files.createDirectories(finalPath.getParent());

            Files.move(tempFile, finalPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            log.info("Exported {} rows for workspace {} type {}", totalCount, workspaceId, type);
            return new FileInputStream(finalPath.toFile());

        } catch (Exception e) {
            log.error("Export failed for workspace {} type {}", workspaceId, type, e);
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ex) {
                log.warn("Failed to delete temp file {}", tempFile, ex);
            }

            throw e;
        }
    }
}