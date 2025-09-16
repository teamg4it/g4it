/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval
 */

package com.soprasteria.g4it.backend.apireferential.business;

import com.soprasteria.g4it.backend.apireferential.modeldb.*;
import com.soprasteria.g4it.backend.apireferential.repository.*;
import com.soprasteria.g4it.backend.common.utils.CsvUtils;
import com.soprasteria.g4it.backend.exception.BadRequestException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * Referential service to write the csv files
 */
@Service
@Slf4j
public class ReferentialExportService {

    private static final int PAGE_SIZE = 200;
    private static final String REFERENTIAL = "referential";
    @Value("${local.working.folder}")
    private String localWorkingFolder;
    // Repositories for accessing different data types
    @Autowired
    private CriterionRepository criterionRepository;
    @Autowired
    private LifecycleStepRepository lifecycleStepRepository;
    @Autowired
    private HypothesisRepository hypothesisRepository;
    @Autowired
    private ItemTypeRepository itemTypeRepository;
    @Autowired
    private MatchingItemRepository matchingItemRepository;
    @Autowired
    private ItemImpactRepository itemImpactRepository;

    @PostConstruct
    public void initFolder() throws IOException {
        Files.createDirectories(Path.of(localWorkingFolder, REFERENTIAL));
    }

    /**
     * Export a referential to csv format
     *
     * @param type       the referential type
     * @param organization organization (mapped to subscriber column in table)
     * @return the inputstream
     */
    public InputStream exportReferentialToCSV(String type, String organization) throws IOException {

        return switch (type) {
            case "lifecycleStep" -> exportToCsv(lifecycleStepRepository.findAll(), type, LifecycleStep.getCsvHeaders());
            case "criterion" -> exportToCsv(criterionRepository.findAll(), type, Criterion.getCsvHeaders());
            case "hypothesis" ->
                    exportToCsv(hypothesisRepository.findByOrganization(organization), type, Hypothesis.getCsvHeaders());
            case "itemType" ->
                    exportToCsv(itemTypeRepository.findByOrganization(organization), type, ItemType.getCsvHeaders());
            case "matchingItem" -> exportMatchingItems(type, organization, MatchingItem.getCsvHeaders());
            case "itemImpact" -> exportItemImpacts(type, organization, ItemImpact.getCsvHeaders());
            default ->
                    throw new BadRequestException("type", String.format("type of referential '%s' does not exist", type));
        };
    }

    /**
     * Exports a list of records to CSV
     *
     * @param records The list of records to export.
     * @param type    The type
     * @param header  the header array
     * @return the inputstream
     */
    private InputStream exportToCsv(List<?> records, final String type, final String[] header) throws IOException {

        CSVFormat csvFormat = CSVFormat.Builder.create()
                .setHeader(header)
                .setDelimiter(CsvUtils.DELIMITER)
                .build();

        File outputFile = Path.of(localWorkingFolder).resolve(REFERENTIAL).resolve(type + "_" + UUID.randomUUID() + ".csv").toFile();
        try (FileWriter csvout = new FileWriter(outputFile);
             CSVPrinter csvPrinter = new CSVPrinter(csvout, csvFormat)) {
            for (Object object : records) {
                csvPrinter.printRecord(getCsvRecord(object));
            }
        }

        return new FileInputStream(outputFile);
    }


    /**
     * Retrieves the CSV representation of a record based on its type.
     *
     * @param object The object to convert.
     * @return An array of objects representing the CSV record.
     */
    private Object[] getCsvRecord(Object object) {
        return switch (object) {
            case null -> new Object[]{};
            case Criterion obj -> obj.toCsvRecord();
            case LifecycleStep obj -> obj.toCsvRecord();
            case Hypothesis obj -> obj.toCsvRecord();
            case ItemType obj -> obj.toCsvRecord();
            case MatchingItem obj -> obj.toCsvRecord();
            default -> new Object[]{};
        };
    }


    /**
     * Exports matching items to CSV format for large data
     *
     * @param type       The type
     * @param organization The organization(mapped to subscriber column in table) to filter matching items by, or null for all.
     * @param header     The header array
     * @return the inputstream
     */
    public InputStream exportMatchingItems(String type, String organization, final String[] header) throws IOException {
        CSVFormat csvFormat = CSVFormat.Builder.create()
                .setHeader(header)
                .setDelimiter(CsvUtils.DELIMITER)
                .build();

        File outputFile = Path.of(localWorkingFolder).resolve(REFERENTIAL).resolve(type + "_" + UUID.randomUUID() + ".csv").toFile();
        try (FileWriter csvout = new FileWriter(outputFile);
             CSVPrinter csvPrinter = new CSVPrinter(csvout, csvFormat)) {

            int pageNumber = 0;
            Page<MatchingItem> matchingItems;
            do {
                Pageable pageable = PageRequest.of(pageNumber, PAGE_SIZE);

                matchingItems = matchingItemRepository.findByOrganization(organization, pageable);

                for (MatchingItem item : matchingItems.getContent()) {
                    csvPrinter.printRecord(item.toCsvRecord());
                }

                pageNumber++;
            } while (matchingItems.hasNext());

            csvPrinter.flush();
        }

        return new FileInputStream(outputFile);
    }

    /**
     * Exports item impacts to CSV format for large data
     *
     * @param type       The type
     * @param organization The organization(mapped to subscriber column in table) to filter item impacts by, or null for all.
     * @param header     The header array
     * @return the inputstream
     */
    public InputStream exportItemImpacts(String type, String organization, final String[] header) throws IOException {
        CSVFormat csvFormat = CSVFormat.Builder.create()
                .setHeader(header)
                .setDelimiter(CsvUtils.DELIMITER)
                .build();

        File outputFile = Path.of(localWorkingFolder).resolve(REFERENTIAL).resolve(type + "_" + UUID.randomUUID() + ".csv").toFile();
        try (FileWriter csvout = new FileWriter(outputFile);
             CSVPrinter csvPrinter = new CSVPrinter(csvout, csvFormat)) {

            int pageNumber = 0;
            Page<ItemImpact> itemImpacts;
            do {
                Pageable pageable = PageRequest.of(pageNumber, PAGE_SIZE);

                itemImpacts = itemImpactRepository.findByOrganization(organization, pageable);

                for (ItemImpact item : itemImpacts.getContent()) {
                    csvPrinter.printRecord(item.toCsvRecord());
                }

                pageNumber++;
            } while (itemImpacts.hasNext());

            csvPrinter.flush();
        }

        return new FileInputStream(outputFile);

    }

}

