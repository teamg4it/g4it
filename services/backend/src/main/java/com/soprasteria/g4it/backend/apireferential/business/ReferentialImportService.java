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
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.CsvUtils;
import com.soprasteria.g4it.backend.common.utils.ValidationUtils;
import com.soprasteria.g4it.backend.exception.BadRequestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.*;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    /**
     * Execute import
     *
     * @param type       the ref type
     * @param file       file to be imported
     * @param subscriber the subscriber
     * @return the ImportReportRest
     */
    public ImportReportRest importReferentialCSV(final String type, final MultipartFile file, final String subscriber) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("file", "The file does not exist or it is empty");
        }

        log.info("Referential - start importing with: type={}, file={}, subscriber={}", type, file.getOriginalFilename(), subscriber);

        ImportReportRest result = switch (type) {
            case "lifecycleStep" -> processLifecycleStepCsv(file);
            case "criterion" -> processCriterionCsv(file);
            case "hypothesis" -> processHypothesisCsv(file, subscriber);
            case "itemType" -> processItemTypeCsv(file, subscriber);
            case "matchingItem" -> processMatchingItemCsv(file, subscriber);
            case "itemImpact" -> processItemImpactCsv(file, subscriber);
            default ->
                    throw new BadRequestException("type", String.format("type of referential '%s' does not exist", type));
        };

        log.info("Referential - end importing with: type={}, file={}, subscriber={}", type, file.getOriginalFilename(), subscriber);

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
    public ImportReportRest processCriterionCsv(final MultipartFile file) {

        ImportReportRest importReportRest = ImportReportRest.builder()
                .errors(new ArrayList<>())
                .file(file.getOriginalFilename())
                .build();

        List<CriterionRest> objects = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
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
    public ImportReportRest processLifecycleStepCsv(final MultipartFile file) {

        ImportReportRest importReportRest = ImportReportRest.builder()
                .errors(new ArrayList<>())
                .file(file.getOriginalFilename())
                .build();

        List<LifecycleStepRest> objects = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
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
     * @param subscriber the subscriber
     * @return the report
     */
    public ImportReportRest processHypothesisCsv(final MultipartFile file, final String subscriber) {

        ImportReportRest importReportRest = ImportReportRest.builder()
                .errors(new ArrayList<>())
                .file(file.getOriginalFilename())
                .build();

        List<HypothesisRest> objects = new ArrayList<>();

        int line = 2;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
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

        if (objects.stream().allMatch(o -> Objects.equals(o.getSubscriber(), subscriber))) {
            int lines = persistenceService.saveHypotheses(referentialMapper.toHypothesisEntity(objects), subscriber);
            importReportRest.setImportedLineNumber((long) lines);
        } else {
            throw new BadRequestException(SUBSCRIBER, String.format(PRINT_SUBSCRIBER_ERROR, subscriber == null ? "" : subscriber));
        }

        Objects.requireNonNull(cacheManager.getCache("ref_getHypotheses")).clear();

        return importReportRest;
    }

    /**
     * Import Item types
     *
     * @param file       file to be imported
     * @param subscriber the subscriber
     * @return the report
     */
    public ImportReportRest processItemTypeCsv(final MultipartFile file, final String subscriber) {

        ImportReportRest importReportRest = ImportReportRest.builder()
                .errors(new ArrayList<>())
                .file(file.getOriginalFilename())
                .build();

        List<ItemTypeRest> objects = new ArrayList<>();

        int line = 2;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
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

        if (objects.stream().allMatch(o -> Objects.equals(o.getSubscriber(), subscriber))) {
            int lines = persistenceService.saveItemTypes(referentialMapper.toItemTypeEntity(objects), subscriber);
            importReportRest.setImportedLineNumber((long) lines);
        } else {
            throw new BadRequestException(SUBSCRIBER, String.format(PRINT_SUBSCRIBER_ERROR, subscriber == null ? "" : subscriber));
        }

        Objects.requireNonNull(cacheManager.getCache("ref_getItemTypes")).clear();

        return importReportRest;
    }

    /**
     * Import Matching items
     *
     * @param file       file to be imported
     * @param subscriber the subscriber
     * @return the report
     */
    public ImportReportRest processMatchingItemCsv(final MultipartFile file, final String subscriber) {

        ImportReportRest importReportRest = ImportReportRest.builder()
                .errors(new ArrayList<>())
                .file(file.getOriginalFilename())
                .build();

        List<MatchingItemRest> objects = new ArrayList<>();

        int line = 2;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
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

        if (objects.stream().allMatch(o -> Objects.equals(o.getSubscriber(), subscriber))) {
            int lines = persistenceService.saveItemMatchings(referentialMapper.toMatchingEntity(objects), subscriber);
            importReportRest.setImportedLineNumber((long) lines);
        } else {
            throw new BadRequestException(SUBSCRIBER, String.format(PRINT_SUBSCRIBER_ERROR, subscriber == null ? "" : subscriber));
        }

        Objects.requireNonNull(cacheManager.getCache("ref_getMatchingItem")).clear();

        return importReportRest;
    }

    /**
     * Import Item impacts
     *
     * @param file       file to be imported
     * @param subscriber the subscriber
     * @return the report
     */
    public ImportReportRest processItemImpactCsv(final MultipartFile file, final String subscriber) {

        ImportReportRest importReportRest = ImportReportRest.builder()
                .errors(new ArrayList<>())
                .file(file.getOriginalFilename())
                .build();

        List<ItemImpactRest> objects = new ArrayList<>(Constants.BATCH_SIZE);

        int i = 0;
        int pageNumber = 0;
        int line = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            Iterable<CSVRecord> records = createCsvParser().parse(reader);
            for (CSVRecord csvRecord : records) {
                ItemImpactRest itemImpactRest = referentialMapper.csvItemImpactToRest(csvRecord);
                if (!Objects.equals(itemImpactRest.getSubscriber(), subscriber)) {
                    throw new BadRequestException(SUBSCRIBER, String.format("Line %d : The column subscriber does not contain all values equal to '%s'", i + 2, subscriber == null ? "" : subscriber));
                }
                i++;
            }
        } catch (IOException e) {
            log.error(CANNOT_READ_FILE, e);
            importReportRest.getErrors().add(CANNOT_READ_FILE + e.getMessage());
            return importReportRest;
        }

        persistenceService.deleteItemImpactsByOrganization(subscriber);

        i = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            Iterable<CSVRecord> records = createCsvParser().parse(reader);
            for (CSVRecord csvRecord : records) {
                line = i + 2 + pageNumber * Constants.BATCH_SIZE;
                ItemImpactRest itemImpactRest = referentialMapper.csvItemImpactToRest(csvRecord);
                Optional<String> violations = ValidationUtils.getViolations(validator.validate(itemImpactRest));
                if (violations.isEmpty()) {
                    objects.add(itemImpactRest);
                } else {
                    importReportRest.getErrors().add(printLine(line, violations.get()));
                }

                if (i >= Constants.BATCH_SIZE) {
                    persistenceService.saveItemImpacts(referentialMapper.toItemImpactEntity(objects));
                    objects.clear();
                    pageNumber++;
                    i = 0;
                }
                i++;
            }
        } catch (IOException e) {
            log.error(CANNOT_READ_FILE, e);
            importReportRest.getErrors().add(CANNOT_READ_FILE + e.getMessage());
            return importReportRest;
        }

        persistenceService.saveItemImpacts(referentialMapper.toItemImpactEntity(objects));
        Objects.requireNonNull(cacheManager.getCache("ref_getItemImpacts")).clear();
        Objects.requireNonNull(cacheManager.getCache("ref_getCountries")).clear();

        importReportRest.setImportedLineNumber((long) line - 1);
        return importReportRest;
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
}
