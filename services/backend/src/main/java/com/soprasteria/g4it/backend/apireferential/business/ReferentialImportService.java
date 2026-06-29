/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apireferential.business;

import com.soprasteria.g4it.backend.apireferential.mapper.ReferentialMapper;
import com.soprasteria.g4it.backend.apireferential.persistence.ReferentialPersistenceService;
import com.soprasteria.g4it.backend.common.utils.*;
import com.soprasteria.g4it.backend.exception.BadRequestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.*;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Csv Import service
 */
@Service
@Slf4j
public class ReferentialImportService {

    private static final String CANNOT_READ_FILE = "Cannot read csv file, error:";
    private static final String PRINT_SUBSCRIBER_ERROR = "The column subscriber does not contain all values equal to '%s'";
    private static final String SUBSCRIBER = "subscriber";

    @Autowired
    ReferentialMapper referentialMapper;

    @Autowired
    ReferentialPersistenceService persistenceService;

    @Autowired
    Validator validator;

    @Autowired
    CacheManager cacheManager;

    @Autowired
    MessageSource messageSource;

    /**
     * Execute import
     *
     * @param type       the ref type
     * @param file       file to be imported
     * @param organization the organization
     * @return the ImportReportRest
     */
    public ImportReportRest importReferentialCSV(final String type, final MultipartFile file, final String organization) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("file", "The file does not exist or it is empty");
        }

        log.info("Referential - start importing with: type={}, file={}, subscriber={}", type, file.getOriginalFilename(), organization);

        ImportReportRest result = switch (type) {
            case "lifecycleStep" -> safeExecute(() -> processLifecycleStepCsv(file), file);
            case "criterion" -> safeExecute(() -> processCriterionCsv(file), file);
            case "hypothesis" -> safeExecute(() -> processHypothesisCsv(file, organization), file);
            case "itemType" -> safeExecute(() -> processItemTypeCsv(file, organization), file);
            case "matchingItem" -> safeExecute(() -> processMatchingItemCsv(file, organization), file);
            case "itemImpact" -> safeExecute(() -> processItemImpactCsv(file, organization), file);
            default -> throw new BadRequestException("type", String.format("type of referential '%s' does not exist", type));
        };

        log.info("Referential - end importing with: type={}, file={}, subscriber={}", type, file.getOriginalFilename(), organization);

        return result;
    }

    /**
     * Generate CSVParser
     *
     * @return the csvformat object
     */
    private CSVFormat createCsvParser() {
        return CSVFormat.RFC4180.builder()
                .setHeader()
                .setDelimiter(CsvUtils.DELIMITER)
                .setTrim(true)
                .setAllowMissingColumnNames(true)
                .setSkipHeaderRecord(true)
                .build();
    }

    /**
     * Import Criteria
     *
     * @param file file to be imported
     * @return the report
     */
    public ImportReportRest processCriterionCsv(final MultipartFile file)   {

        ImportReportRest importReportRest = ImportReportRest.builder()
                .errors(new ArrayList<>())
                .file(file.getOriginalFilename())
                .build();

        List<CriterionRest> objects = new ArrayList<>();

        byte[] bytes = getBytesSafe(file, importReportRest);
        if (bytes == null) return importReportRest;
        try (BufferedReader reader = CsvEncodingUtils.getReader(bytes)) {
            int line = 2;
            for (CSVRecord csvRecord : createCsvParser().parse(reader)) {
                CriterionRest criterionRest = referentialMapper.csvCriterionToRest(csvRecord);
                Optional<String> violations = ValidationUtils.getViolations(validator.validate(criterionRest));
                if (violations.isEmpty()) {
                    objects.add(criterionRest);
                } else {
                    importReportRest.getErrors().add(printLine(line, violations.get()));
                }
                line++;
            }
        } catch (IOException e) {
            log.error(CANNOT_READ_FILE, e);
            importReportRest.getErrors().add(CANNOT_READ_FILE + e.getMessage());
            return importReportRest;
        }

        int lines = persistenceService.saveCriteria(referentialMapper.toCriteriaEntity(objects));
        Objects.requireNonNull(cacheManager.getCache("ref_getAllCriteria")).clear();
        importReportRest.setImportedLineNumber((long) lines);
        return importReportRest;
    }

    /**
     * Import Lifecycle steps
     *
     * @param file file to be imported
     * @return the report
     */
    public ImportReportRest processLifecycleStepCsv(final MultipartFile file)   {

        ImportReportRest importReportRest = ImportReportRest.builder()
                .errors(new ArrayList<>())
                .file(file.getOriginalFilename())
                .build();

        List<LifecycleStepRest> objects = new ArrayList<>();
        byte[] bytes = getBytesSafe(file, importReportRest);
        if (bytes == null) return importReportRest;
        try (BufferedReader reader = CsvEncodingUtils.getReader(bytes)) {
            int line = 2;
            for (CSVRecord csvRecord : createCsvParser().parse(reader)) {
                LifecycleStepRest lifecycleStepRest = referentialMapper.csvLifecycleStepToRest(csvRecord);
                Optional<String> violations = ValidationUtils.getViolations(validator.validate(lifecycleStepRest));
                if (violations.isEmpty()) {
                    objects.add(lifecycleStepRest);
                } else {
                    importReportRest.getErrors().add(printLine(line, violations.get()));
                }
                line++;
            }
        } catch (IOException e) {
            log.error(CANNOT_READ_FILE, e);
            importReportRest.getErrors().add(CANNOT_READ_FILE + e.getMessage());
            return importReportRest;
        }

        int lines = persistenceService.saveLifecycleSteps(referentialMapper.toLifecycleStepEntity(objects));
        Objects.requireNonNull(cacheManager.getCache("ref_getAllLifecycleSteps")).clear();
        importReportRest.setImportedLineNumber((long) lines);
        return importReportRest;
    }

    /**
     * Import Hypotheses
     *
     * @param file       file to be imported
     * @param organization the organization(mapped to subscriber column in table)
     * @return the report
     */
    public ImportReportRest processHypothesisCsv(final MultipartFile file, final String organization)   {

        ImportReportRest importReportRest = ImportReportRest.builder()
                .errors(new ArrayList<>())
                .file(file.getOriginalFilename())
                .build();

        List<HypothesisRest> objects = new ArrayList<>();

        int line = 2;

        byte[] bytes = getBytesSafe(file, importReportRest);
        if (bytes == null) return importReportRest;

        try (BufferedReader reader = CsvEncodingUtils.getReader(bytes)) {
            for (CSVRecord csvRecord : createCsvParser().parse(reader)) {
                HypothesisRest hypothesisRest = referentialMapper.csvHypothesisToRest(csvRecord);
                Optional<String> violations = ValidationUtils.getViolations(validator.validate(hypothesisRest));
                if (violations.isEmpty()) {
                    objects.add(hypothesisRest);
                } else {
                    importReportRest.getErrors().add(printLine(line, violations.get()));
                }
                line++;
            }
        } catch (IOException e) {
            log.error(CANNOT_READ_FILE, e);
            importReportRest.getErrors().add(CANNOT_READ_FILE + e.getMessage());
            return importReportRest;
        }

        if (objects.stream().allMatch(o -> Objects.equals(o.getOrganization(), organization))) {
            int lines = persistenceService.saveHypotheses(referentialMapper.toHypothesisEntity(objects), organization);
            importReportRest.setImportedLineNumber((long) lines);
        } else {
            throw new BadRequestException(SUBSCRIBER, String.format(PRINT_SUBSCRIBER_ERROR, organization == null ? "" : organization));
        }

        Objects.requireNonNull(cacheManager.getCache("ref_getHypotheses")).clear();

        return importReportRest;
    }

    /**
     * Import Item types
     *
     * @param file       file to be imported
     * @param organization the organization(mapped to subscriber column in table)
     * @return the report
     */
    public ImportReportRest processItemTypeCsv(final MultipartFile file, final String organization)   {

        ImportReportRest importReportRest = ImportReportRest.builder()
                .errors(new ArrayList<>())
                .file(file.getOriginalFilename())
                .build();

        List<ItemTypeRest> objects = new ArrayList<>();

        int line = 2;

        byte[] bytes = getBytesSafe(file, importReportRest);
        if (bytes == null) return importReportRest;

        try (BufferedReader reader = CsvEncodingUtils.getReader(bytes)) {
            for (CSVRecord csvRecord : createCsvParser().parse(reader)) {
                ItemTypeRest itemTypeRest = referentialMapper.csvItemTypeToRest(csvRecord);
                Optional<String> violations = ValidationUtils.getViolations(validator.validate(itemTypeRest));
                if (violations.isEmpty()) {
                    objects.add(itemTypeRest);
                } else {
                    importReportRest.getErrors().add(printLine(line, violations.get()));
                }
                line++;
            }
        } catch (IOException e) {
            log.error(CANNOT_READ_FILE, e);
            importReportRest.getErrors().add(CANNOT_READ_FILE + e.getMessage());
            return importReportRest;
        }

        if (objects.stream().allMatch(o -> Objects.equals(o.getOrganization(), organization))) {
            int lines = persistenceService.saveItemTypes(referentialMapper.toItemTypeEntity(objects), organization);
            importReportRest.setImportedLineNumber((long) lines);
        } else {
            throw new BadRequestException(SUBSCRIBER, String.format(PRINT_SUBSCRIBER_ERROR, organization == null ? "" : organization));
        }

        Objects.requireNonNull(cacheManager.getCache("ref_getItemTypes")).clear();

        return importReportRest;
    }

    /**
     * Import Matching items
     *
     * @param file       file to be imported
     * @param organization the organization(mapped to subscriber column in table)
     * @return the report
     */
    public ImportReportRest processMatchingItemCsv(final MultipartFile file, final String organization)   {

        ImportReportRest importReportRest = ImportReportRest.builder()
                .errors(new ArrayList<>())
                .file(file.getOriginalFilename())
                .build();

        List<MatchingItemRest> objects = new ArrayList<>();

        int line = 2;
        byte[] bytes = getBytesSafe(file, importReportRest);
        if (bytes == null) return importReportRest;

        try (BufferedReader reader = CsvEncodingUtils.getReader(bytes)) {
            for (CSVRecord csvRecord : createCsvParser().parse(reader)) {
                MatchingItemRest matchingItemRest = referentialMapper.csvMatchingItemToRest(csvRecord);
                Optional<String> violations = ValidationUtils.getViolations(validator.validate(matchingItemRest));
                if (violations.isEmpty()) {
                    objects.add(matchingItemRest);
                } else {
                    importReportRest.getErrors().add(printLine(line, violations.get()));
                }
                line++;
            }
        } catch (IOException e) {
            log.error(CANNOT_READ_FILE, e);
            importReportRest.getErrors().add(CANNOT_READ_FILE + e.getMessage());
            return importReportRest;
        }

        if (objects.stream().allMatch(o -> Objects.equals(o.getOrganization(), organization))) {
            int lines = persistenceService.saveItemMatchings(referentialMapper.toMatchingEntity(objects), organization);
            importReportRest.setImportedLineNumber((long) lines);
        } else {
            throw new BadRequestException(SUBSCRIBER, String.format(PRINT_SUBSCRIBER_ERROR, organization == null ? "" : organization));
        }

        Objects.requireNonNull(cacheManager.getCache("ref_getMatchingItem")).clear();

        return importReportRest;
    }

    /**
     * Import Item impacts
     *
     * @param file       file to be imported
     * @param organization the organization(mapped to subscriber column in table)
     * @return the report
     */
    public ImportReportRest processItemImpactCsv(final MultipartFile file, final String organization)   {

        ImportReportRest importReportRest = ImportReportRest.builder()
                .errors(new ArrayList<>())
                .file(file.getOriginalFilename())
                .build();

        List<ItemImpactRest> objects = new ArrayList<>(Constants.BATCH_SIZE);

        int i = 0;
        int pageNumber = 0;
        int line = 0;

        byte[] bytes = getBytesSafe(file, importReportRest);
        if (bytes == null) return importReportRest;

        /*
         * ===============================
         * STEP 1 — VALIDATION PASS
         * ===============================
         */
        try (BufferedReader reader = CsvEncodingUtils.getReader(bytes)) {
            var parser = createCsvParser().parse(reader);

            // Validate headers before processing
            validateHeaders(
                    parser.getHeaderNames(),
                    List.of("criterion", "lifecycleStep", "name", "category", "avgElectricityConsumption",
                            "description", "location", "level", "source", "tier", "unit", "value", "subscriber", "version"),
                    "ItemImpact"
            );

            for (CSVRecord csvRecord : parser) {
                ItemImpactRest itemImpactRest = referentialMapper.csvItemImpactToRest(csvRecord);

                if (!Objects.equals(itemImpactRest.getOrganization(), organization)) {
                    throw new BadRequestException(
                            SUBSCRIBER,
                            String.format("Line %d : The column subscriber must be '%s'", i + 2,
                                    organization == null ? "" : organization)
                    );
                }
                i++;
            }

        } catch (IOException e) {
            log.error(CANNOT_READ_FILE, e);
            importReportRest.getErrors().add(CANNOT_READ_FILE + e.getMessage());
            return importReportRest;
        }

        /*
         * ===============================
         * STEP 2 — DELETE OLD DATA
         * ===============================
         */
        persistenceService.deleteItemImpactsByOrganization(organization);

        i = 0;

        /*
         * ===============================
         * STEP 3 — IMPORT PASS
         * ===============================
         */
        try (BufferedReader reader = CsvEncodingUtils.getReader(bytes)) {
            var parser = createCsvParser().parse(reader);

            List<String> headerNames = parser.getHeaderNames();

            // Special check: If parser only found 1 column, the file is likely using comma as delimiter
            if (headerNames.size() == 1) {
                String singleHeader = headerNames.get(0);
                if (singleHeader.contains(",")) {
                    throw new BadRequestException(SUBSCRIBER,
                        "CSV file format error: The file appears to be using COMMA (,) as delimiter instead of SEMICOLON (;). " +
                        "This often happens when you include commas in numeric values (like '51,53'). " +
                        "Solution: Use PERIOD for decimals (51.53) instead of comma, AND ensure your CSV editor saves the file with semicolon (;) as the delimiter. " +
                        "If you must keep commas in values, quote them like \"51,53\".");
                }
            }

            // Validate headers (should be consistent with STEP 1, but included for safety)
            validateHeaders(
                    headerNames,
                    List.of("criterion", "lifecycleStep", "name", "category", "avgElectricityConsumption",
                            "description", "location", "level", "source", "tier", "unit", "value", "subscriber", "version"),
                    "ItemImpact"
            );

            for (CSVRecord csvRecord : parser) {

                line = i + 2 + pageNumber * Constants.BATCH_SIZE;

                try {
                    // Check if record has expected number of columns (detect wrong delimiter early)
                    if (csvRecord.size() < 14) {
                        String errorMessage = "The CSV file appears to use an incorrect delimiter. " +
                                "Expected 14 columns but found " + csvRecord.size() + ". " +
                                "Please ensure the file uses semicolon (;) as delimiter, not comma (,).";
                        importReportRest.getErrors().add(printLine(line, errorMessage));
                        i++;
                        continue;
                    }

                    ItemImpactRest itemImpactRest = referentialMapper.csvItemImpactToRest(csvRecord);

                    Optional<String> violations = ValidationUtils.getViolations(validator.validate(itemImpactRest));

                    if (violations.isEmpty()) {
                        objects.add(itemImpactRest);
                    } else {
                        importReportRest.getErrors().add(printLine(line, violations.get()));
                    }
                } catch (NumberFormatException e) {
                    // AC5: Handle decimal format error (comma instead of period)
                    Locale locale = LocaleContextHolder.getLocale();
                    String errorMessage = messageSource.getMessage(
                            "itemimpact.decimal.comma.invalid",
                            null,
                            locale
                    );
                    importReportRest.getErrors().add(printLine(line, errorMessage));
                } catch (IllegalArgumentException e) {
                    // Handle CSV structure errors (e.g., missing columns, wrong delimiter, malformed data)
                    String msg = e.getMessage();
                    String errorMessage;

                    if (msg != null && msg.contains("Index for header") && msg.contains("but CSVRecord only has")) {
                        // This typically means wrong delimiter is used
                        errorMessage = "The CSV file appears to use an incorrect delimiter. " +
                                "Please ensure the file uses semicolon (;) as delimiter, not comma (,). " +
                                "All required columns must be present in the correct order.";
                    } else {
                        errorMessage = "Invalid CSV structure: " + msg +
                                ". Please ensure the file uses semicolon (;) as delimiter and has all required columns in the correct order.";
                    }
                    importReportRest.getErrors().add(printLine(line, errorMessage));
                }

                // ✅ batch save
                if (objects.size() >= Constants.BATCH_SIZE) {
                    persistenceService.saveItemImpacts(referentialMapper.toItemImpactEntity(objects));
                    objects.clear();
                    pageNumber++;
                }

                i++;
            }

        } catch (IOException e) {
            log.error(CANNOT_READ_FILE, e);
            importReportRest.getErrors().add(CANNOT_READ_FILE + e.getMessage());
            return importReportRest;
        }

        /*
         * ===============================
         * STEP 4 — SAVE REMAINING
         * ===============================
         */
        if (!objects.isEmpty()) {
            persistenceService.saveItemImpacts(referentialMapper.toItemImpactEntity(objects));
        }

        /*
         * ===============================
         * STEP 5 — CLEAR CACHE
         * ===============================
         */
        Objects.requireNonNull(cacheManager.getCache("ref_getItemImpacts")).clear();
        Objects.requireNonNull(cacheManager.getCache("ref_getCountries")).clear();

        importReportRest.setImportedLineNumber((long) line - 1);

        return importReportRest;
    }

    public ItemTypeParseResult parseItemTypeCsv(MultipartFile file)   {

        ImportReportRest report = ImportReportRest.builder()
                .errors(new ArrayList<>())
                .file(file.getOriginalFilename())
                .build();

        List<ItemTypeRest> objects = new ArrayList<>();

        int line = 2;
        byte[] bytes = getBytesSafe(file, report);
        if (bytes == null) {
            return ItemTypeParseResult.builder()
                    .report(report)
                    .build();
        }

        try (BufferedReader reader = CsvEncodingUtils.getReader(bytes)) {
            var parser = createCsvParser().parse(reader);

            validateHeaders(
                    parser.getHeaderNames(),
                    List.of("type", "category", "comment", "default_lifespan", "is_server", "source", "ref_default_item", "version"),
                    "ItemType"
            );

            for (CSVRecord csvRecord :parser) {

                try {
                    ItemTypeRest item = referentialMapper.csvItemTypeToRest(csvRecord);

                    Optional<String> violations =
                            ValidationUtils.getViolations(validator.validate(item));

                    if (violations.isEmpty()) {
                        item.setOrganization(null); // ignore subscriber
                        objects.add(item);
                    } else {
                        report.getErrors().add(printLine(line, violations.get()));
                    }
                } catch (IllegalArgumentException e) {
                    // Handle CSV structure errors (e.g., missing columns, wrong delimiter, malformed data)
                    String errorMessage = "Invalid CSV structure: " + e.getMessage() +
                            ". Please ensure the file uses semicolon (;) as delimiter and has all required columns in the correct order.";
                    report.getErrors().add(printLine(line, errorMessage));
                }

                line++;
            }

        } catch (IOException e) {
            report.getErrors().add("Cannot read file: " + e.getMessage());
            return ItemTypeParseResult.builder()
                    .report(report)
                    .build();
        }

        report.setImportedLineNumber((long) objects.size());

        return ItemTypeParseResult.builder()
                .data(objects)
                .report(report).build();
    }

    public MatchingItemParseResult parseMatchingItemCsv(MultipartFile file)   {

        ImportReportRest report = ImportReportRest.builder()
                .errors(new ArrayList<>())
                .file(file.getOriginalFilename())
                .build();

        List<MatchingItemRest> objects = new ArrayList<>();

        int line = 2;

        byte[] bytes = getBytesSafe(file, report);
        if (bytes == null) {
            return MatchingItemParseResult.builder()
                    .report(report)
                    .build();
        }

        try (BufferedReader reader = CsvEncodingUtils.getReader(bytes)) {
            var parser = createCsvParser().parse(reader);
            validateHeaders(
                    parser.getHeaderNames(),
                    List.of("itemSource", "refItemTarget"),
                    "MatchingItem"
            );
            for (CSVRecord csvRecord : parser) {

                try {
                    MatchingItemRest item = referentialMapper.csvMatchingItemToRest(csvRecord);

                    Optional<String> violations =
                            ValidationUtils.getViolations(validator.validate(item));

                    if (violations.isEmpty()) {
                        item.setOrganization(null);
                        objects.add(item);
                    } else {
                        report.getErrors().add(printLine(line, violations.get()));
                    }
                } catch (IllegalArgumentException e) {
                    // Handle CSV structure errors (e.g., missing columns, wrong delimiter, malformed data)
                    String errorMessage = "Invalid CSV structure: " + e.getMessage() +
                            ". Please ensure the file uses semicolon (;) as delimiter and has all required columns in the correct order.";
                    report.getErrors().add(printLine(line, errorMessage));
                }

                line++;
            }

        } catch (IOException e) {
            report.getErrors().add("Cannot read file: " + e.getMessage());
            return MatchingItemParseResult.builder().report(report).build();
        }

        report.setImportedLineNumber((long) objects.size());

        return MatchingItemParseResult.builder()
                .data(objects)
                .report(report).build();
    }

    public ItemImpactParseResult parseItemImpactCsv(MultipartFile file)   {

        ImportReportRest report = ImportReportRest.builder()
                .errors(new ArrayList<>())
                .file(file.getOriginalFilename())
                .build();

        List<ItemImpactRest> objects = new ArrayList<>();

        int line = 2;

        byte[] bytes = getBytesSafe(file, report);
        if (bytes == null) {
            return ItemImpactParseResult.builder()
                    .report(report)
                    .build();
        }

        try (BufferedReader reader = CsvEncodingUtils.getReader(bytes)) {
            var parser = createCsvParser().parse(reader);

            List<String> headerNames = parser.getHeaderNames();

            // Special check: If parser only found 1 column, the file is likely using comma as delimiter
            // This commonly happens when users put commas in field values (like "51,53" for AC5 testing)
            // and their CSV editor automatically switches the entire file to comma-delimited format
            if (headerNames.size() == 1) {
                String singleHeader = headerNames.get(0);
                if (singleHeader.contains(",")) {
                    throw new BadRequestException("csv",
                        "CSV file format error: The file appears to be using COMMA (,) as delimiter instead of SEMICOLON (;). " +
                        "This often happens when you include commas in numeric values (like '51,53'). " +
                        "Solution: Use PERIOD for decimals (51.53) instead of comma, AND ensure your CSV editor saves the file with semicolon (;) as the delimiter. " +
                        "If you must keep commas in values, quote them like \"51,53\".");
                }
            }

            validateHeaders(
                    headerNames,
                    List.of("criterion", "lifecycleStep", "name", "category", "avgElectricityConsumption",
                            "description", "location", "level", "source", "tier", "unit", "value", "version"),
                    "ItemImpact"
            );

            for (CSVRecord csvRecord : parser) {

                try {
                    // Check if record has expected number of columns (detect wrong delimiter early)
                    if (csvRecord.size() < 13) {
                        String errorMessage = "The CSV file appears to use an incorrect delimiter. " +
                                "Expected 13 columns but found " + csvRecord.size() + ". " +
                                "Please ensure the file uses semicolon (;) as delimiter, not comma (,).";
                        report.getErrors().add(printLine(line, errorMessage));
                        line++;
                        continue;
                    }

                    ItemImpactRest item = referentialMapper.csvItemImpactToRest(csvRecord);

                    Optional<String> violations =
                            ValidationUtils.getViolations(validator.validate(item));

                    if (violations.isEmpty()) {
                        item.setOrganization(null);
                        objects.add(item);
                    } else {
                        // Convert technical validation errors to user-friendly messages
                        String errorMessage = convertValidationError(violations.get(), item);
                        report.getErrors().add(printLine(line, errorMessage));
                    }
                } catch (NumberFormatException e) {
                    // AC5: Handle decimal format error (comma instead of period)
                    Locale locale = LocaleContextHolder.getLocale();
                    String errorMessage = messageSource.getMessage(
                            "itemimpact.decimal.comma.invalid",
                            null,
                            locale
                    );
                    report.getErrors().add(printLine(line, errorMessage));
                } catch (IllegalArgumentException e) {
                    // Handle CSV structure errors (e.g., missing columns, wrong delimiter, malformed data)
                    String msg = e.getMessage();
                    String errorMessage;

                    if (msg != null && msg.contains("Index for header") && msg.contains("but CSVRecord only has")) {
                        // This typically means wrong delimiter is used
                        errorMessage = "The CSV file appears to use an incorrect delimiter. " +
                                "Please ensure the file uses semicolon (;) as delimiter, not comma (,). " +
                                "All required columns must be present in the correct order.";
                    } else {
                        errorMessage = "Invalid CSV structure: " + msg +
                                ". Please ensure the file uses semicolon (;) as delimiter and has all required columns in the correct order.";
                    }
                    report.getErrors().add(printLine(line, errorMessage));
                }

                line++;
            }

        } catch (IOException e) {
            report.getErrors().add("Cannot read file: " + e.getMessage());
            return ItemImpactParseResult.builder().report(report).build();
        }

        report.setImportedLineNumber((long) objects.size());

        return ItemImpactParseResult.builder()
                .data(objects)
                .report(report).build();
    }

    private void validateHeaders(List<String> actual, List<String> expected, String type) {

        if (actual.size() != expected.size()) {
            // Check if columns are missing
            List<String> missing = new ArrayList<>(expected);
            missing.removeAll(actual);

            if (!missing.isEmpty()) {
                throw new BadRequestException("csv",
                        "Some required columns are missing in the " + type + " file. Missing columns: " + missing +
                        ". Please refer to the data model for the complete list of required columns.");
            }

            // Check if there are extra columns
            List<String> extra = new ArrayList<>(actual);
            extra.removeAll(expected);

            if (!extra.isEmpty()) {
                throw new BadRequestException("csv",
                        "Some unexpected columns are present in the " + type + " file. Unexpected columns: " + extra +
                        ". Please refer to the data model for the complete list of valid columns.");
            }
        }

        for (int i = 0; i < expected.size(); i++) {
            if (!expected.get(i).equals(actual.get(i))) {
                throw new BadRequestException("csv",
                        "Invalid column order in the " + type + " file at position " + (i + 1) +
                                ". Expected column: '" + expected.get(i) +
                                "', but found: '" + actual.get(i) +
                                "'. Please ensure columns are in the correct order as specified in the data model.");
            }
        }
    }

    private ImportReportRest safeExecute(SupplierWithException<ImportReportRest> supplier, MultipartFile file) {
        try {
            return supplier.get();
        } catch (IOException e) {
            log.error(CANNOT_READ_FILE, e);
            return ImportReportRest.builder()
                    .errors(List.of(CANNOT_READ_FILE + e.getMessage()))
                    .file(file.getOriginalFilename())
                    .build();
        }
    }

    private byte[] getBytesSafe(MultipartFile file, ImportReportRest report) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            log.error(CANNOT_READ_FILE, e);
            report.getErrors().add(CANNOT_READ_FILE + e.getMessage());
            return null;
        }
    }

    /**
     * Print the line as string
     *
     * @param line the line number
     * @param str  the str
     * @return the line n : str
     */
    private String printLine(int line, String str) {
        return String.join(" ", "line", String.valueOf(line), ": ", str);
    }

    /**
     * Convert technical validation errors to user-friendly messages
     *
     * @param technicalError The technical error from validation
     * @param item The item being validated
     * @return User-friendly error message
     */
    private String convertValidationError(String technicalError, ItemImpactRest item) {
        Locale locale = LocaleContextHolder.getLocale();

        // Check for lifecycleStep pattern error (AC3)
        if (technicalError.contains("lifecycleStep") && technicalError.contains("must match")) {
            return messageSource.getMessage(
                    "itemimpact.lifecyclestep.invalid",
                    null,
                    locale
            );
        }

        // Check for criterion pattern error (AC2)
        if (technicalError.contains("criterion") && technicalError.contains("must match")) {
            return messageSource.getMessage(
                    "itemimpact.criterion.invalid",
                    null,
                    locale
            );
        }

        // Return original error if no specific match
        return technicalError;
    }
}
