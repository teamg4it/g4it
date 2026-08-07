/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice;

import com.soprasteria.g4it.backend.apiinout.modeldb.InApplication;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InApplicationRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InDatacenterRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadobject.LoadApplicationService;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadobject.LoadDatacenterService;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadobject.LoadPhysicalEquipmentService;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadobject.LoadVirtualEquipmentService;
import com.soprasteria.g4it.backend.apiloadinputfiles.mapper.CsvToInMapper;
import com.soprasteria.g4it.backend.common.filesystem.model.CsvFileMapperInfo;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.CsvUtils;
import com.soprasteria.g4it.backend.config.FileEquipmentLimitConfiguration;
import com.soprasteria.g4it.backend.exception.AsyncTaskException;
import com.soprasteria.g4it.backend.server.gen.api.dto.InApplicationRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InDatacenterRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InVirtualEquipmentRest;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

@Service
@Slf4j
public class LoadFileService {

    private static final String REJECTED = "rejected";

    @Autowired
    CsvFileMapperInfo csvFileMapperInfo;
    @Autowired
    MessageSource messageSource;
    @Autowired
    LoadDatacenterService loadDatacenterService;
    @Autowired
    LoadPhysicalEquipmentService loadPhysicalEquipmentService;
    @Autowired
    LoadVirtualEquipmentService loadVirtualEquipmentService;
    @Autowired
    LoadApplicationService loadApplicationService;
    @Autowired
    CsvToInMapper csvToInMapper;
    @Autowired
    InventoryRepository inventoryRepository;
    @Autowired
    InVirtualEquipmentRepository inVirtualEquipmentRepository;
    @Autowired
    InApplicationRepository inApplicationRepository;
    @Autowired
    InPhysicalEquipmentRepository inPhysicalEquipmentRepository;
    @Autowired
    InDatacenterRepository inDatacenterRepository;
    @Autowired
    FileEquipmentLimitConfiguration fileEquipmentLimitConfiguration;
    @Value("${local.working.folder}")
    private String localWorkingFolder;

    @PostConstruct
    public void initFolder() throws IOException {
        Files.createDirectories(Path.of(localWorkingFolder, REJECTED));
    }

    /**
     * Manage an uploaded file
     * <p>
     * Converts the original file to a CSV file and processes the data
     *
     * @param context    the context
     * @param fileToLoad the file to load
     * @return the list of errors that occurred while processing the data
     */
    public List<String> manageFile(final Context context, FileToLoad fileToLoad) {


        List<String> errors = new ArrayList<>();
        errors.addAll(manageConvertedFile(context, fileToLoad));
        return errors;
    }

    /**
     * Manage a csv formatted file
     *
     * @param context    the context
     * @param fileToLoad the file to load
     * @return the list of errors
     */
    private List<String> manageConvertedFile(final Context context, FileToLoad fileToLoad) {
        List<String> errors = new ArrayList<>();
        List<LineError> readErrors;

        try (BufferedReader reader = new BufferedReader(new FileReader(fileToLoad.getConvertedFile()))) {
            CSVParser records = CSVFormat.RFC4180.builder()
                    .setHeader()
                    .setDelimiter(CsvUtils.DELIMITER)
                    .setAllowMissingColumnNames(true)
                    .setSkipHeaderRecord(false)
                    .build()
                    .parse(reader);

            Set<String> fileHeader = new HashSet<>(records.getHeaderNames());
            fileHeader.remove("");
            csvFileMapperInfo.getHeaderFields(fileToLoad.getFileType(), false).forEach(fileHeader::remove);
            if (!fileHeader.isEmpty()) {
                errors.add(messageSource.getMessage(
                        "header.unknown",
                        new String[]{fileToLoad.getOriginalFileName(), String.join(", ", fileHeader)},
                        context.getLocale()));
            }

            readErrors = switch (fileToLoad.getFileType()) {
                case DATACENTER -> readDatacenters(context, fileToLoad, records);
                case EQUIPEMENT_PHYSIQUE -> readPhysicalEquipments(context, fileToLoad, records);
                case EQUIPEMENT_VIRTUEL -> readVirtualEquipments(context, fileToLoad, records);
                case APPLICATION -> readApplications(context, fileToLoad, records);
                default -> throw new IllegalArgumentException();
            };
        } catch (AsyncTaskException e) {
            throw e;
        }
        catch ( Exception e) {
            throw new AsyncTaskException(String.format("%s - Error while managing converted file '%s'", context.log(),
                    fileToLoad.getConvertedFile().getName()), e);
        }

        if (!readErrors.isEmpty()) {
            writeRejected(context, readErrors, fileToLoad.getFileType(), fileToLoad.getConvertedFile(), fileToLoad.getOriginalFileName());
        }

        return errors;
    }


    /**
     * Append file rejected_${fileType.getFileName()}_local_date-time.csv
     *
     * @param context          the context
     * @param readErrors       the read errors
     * @param fileType         the file type
     * @param file             the file
     * @param originalFileName the original file name
     */
    private void writeRejected(final Context context, final List<LineError> readErrors, final FileType fileType, final File file, final String originalFileName) {
        Map<Integer, List<String>> errorsByLine = readErrors.stream()
                .collect(groupingBy(LineError::line, mapping(LineError::error, toList())));

        String rejectedFileName = String.join("_", REJECTED, fileType.getFileName(), context.getDatetime().format(Constants.FILE_DATE_TIME_FORMATTER)) + Constants.CSV;
        String pathId = context.getInventoryId() != null ? String.valueOf(context.getInventoryId()) : context.getDigitalServiceVersionUid();

        String path = localWorkingFolder + File.separator + REJECTED + File.separator + pathId + File.separator + rejectedFileName;

        try {
            Files.createDirectories(Path.of(localWorkingFolder).resolve(REJECTED).resolve(pathId));
        } catch (IOException e) {
            throw new AsyncTaskException(String.format("%s - Cannot create local rejected folder", context.log()), e);
        }
        try (Reader reader = new FileReader(file);
             BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path), true))
        ) {

            int lineNumber = 2;
            BufferedReader br = new BufferedReader(reader);
            String line = br.readLine();
            if (line != null) {
                // add to header
                writer.write(String.join(CsvUtils.DELIMITER, line, "inputFileName", "lineNumber", "message"));
                writer.newLine();
            }

            while (line != null) {
                line = br.readLine();
                List<String> errorLines = errorsByLine.get(lineNumber);
                if (errorLines != null) {
                    writer.write(String.join(CsvUtils.DELIMITER, line, originalFileName, String.valueOf(lineNumber), String.join(", ", errorLines)));
                    writer.newLine();
                }
                lineNumber++;
            }
        } catch (IOException e) {
            throw new AsyncTaskException(String.format("%s - Cannot read local csv file %s", context.log(), originalFileName), e);
        }
    }

    /**
     * Read datacenters from records
     *
     * @param context the context
     * @param records the CSVParser records
     * @return the list of error
     */
    private List<LineError> readDatacenters(final Context context, FileToLoad fileToLoad, final CSVParser records) {
        int row = 1;
        int pageNumber = 0;
        List<LineError> errors = new ArrayList<>();

        // read file locally by PAGE_SIZE lines page
        List<InDatacenterRest> objects = new ArrayList<>(Constants.BATCH_SIZE);

        for (CSVRecord csvRecord : records) {
            objects.add(csvToInMapper.csvInDatacenterToRest(csvRecord, context.getInventoryId(), context.getDigitalServiceVersionUid()));
            if (row >= Constants.BATCH_SIZE) {
                errors.addAll(loadDatacenterService.execute(context, fileToLoad, pageNumber, objects));
                objects.clear();
                row = 1;
                pageNumber++;
            } else {
                row++;
            }
        }

        errors.addAll(loadDatacenterService.execute(context, fileToLoad, pageNumber, objects));
        objects.clear();

        return errors;
    }

    /**
     * Read physical equipments from records
     *
     * @param context the context
     * @param records the CSVParser records
     * @return the list of error
     */
    private List<LineError> readPhysicalEquipments(final Context context, final FileToLoad fileToLoad, final CSVParser records) {
        int row = 1;
        int pageNumber = 0;
        List<LineError> errors = new ArrayList<>();

        // read file locally by PAGE_SIZE lines page
        List<InPhysicalEquipmentRest> objects = new ArrayList<>(Constants.BATCH_SIZE);

        for (CSVRecord csvRecord : records) {
            objects.add(csvToInMapper.csvInPhysicalEquipmentToRest(csvRecord, context.getInventoryId(), context.getDigitalServiceVersionUid()));
            if (row >= Constants.BATCH_SIZE) {
                errors.addAll(loadPhysicalEquipmentService.execute(context, fileToLoad, pageNumber, objects));
                objects.clear();
                row = 1;
                pageNumber++;
            } else {
                row++;
            }
        }

        errors.addAll(loadPhysicalEquipmentService.execute(context, fileToLoad, pageNumber, objects));
        objects.clear();

        return errors;
    }

    /**
     * Read virtual equipments from records
     *
     * @param context the context
     * @param records the CSVParser records
     * @return the list of error
     */
    private List<LineError> readVirtualEquipments(final Context context, final FileToLoad fileToLoad, final CSVParser records) {
        int row = 1;
        int pageNumber = 0;
        List<LineError> errors = new ArrayList<>();

        // read file locally by PAGE_SIZE lines page
        List<InVirtualEquipmentRest> objects = new ArrayList<>(Constants.BATCH_SIZE);

        for (CSVRecord csvRecord : records) {
            objects.add(csvToInMapper.csvInVirtualEquipmentToRest(csvRecord, context.getInventoryId(), context.getDigitalServiceVersionUid()));
            if (row >= Constants.BATCH_SIZE) {
                errors.addAll(loadVirtualEquipmentService.execute(context, fileToLoad, pageNumber, objects));
                objects.clear();
                row = 1;
                pageNumber++;
            } else {
                row++;
            }
        }

        errors.addAll(loadVirtualEquipmentService.execute(context, fileToLoad, pageNumber, objects));
        objects.clear();

        return errors;
    }

    /**
     * Read applications from records
     *
     * @param context the context
     * @param records the CSVParser records
     * @return the list of error
     */
    private List<LineError> readApplications(final Context context, final FileToLoad fileToLoad, final CSVParser records) {
        int row = 1;
        int pageNumber = 0;
        List<LineError> errors = new ArrayList<>();

        // read file locally by PAGE_SIZE lines page
        List<InApplicationRest> objects = new ArrayList<>(Constants.BATCH_SIZE);

        for (CSVRecord csvRecord : records) {
            objects.add(csvToInMapper.csvInApplicationToRest(csvRecord, context.getInventoryId()));
            if (row >= Constants.BATCH_SIZE) {
                errors.addAll(loadApplicationService.execute(context, fileToLoad, pageNumber, objects));
                objects.clear();
                row = 1;
                pageNumber++;
            } else {
                row++;
            }
        }

        errors.addAll(loadApplicationService.execute(context, fileToLoad, pageNumber, objects));
        objects.clear();

        return errors;
    }

    @Transactional
    void setInventoryCounts(final Long inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow();

        inventory.setDataCenterCount(loadDatacenterService.getDatacenterCount(inventoryId));
        inventory.setPhysicalEquipmentCount(loadPhysicalEquipmentService.getPhysicalEquipmentCount(inventoryId));
        inventory.setVirtualEquipmentCount(loadVirtualEquipmentService.getVirtualEquipmentCount(inventoryId));
        inventory.setApplicationCount(loadApplicationService.getApplicationCount(inventoryId));

        inventoryRepository.save(inventory);
    }

    /**
     * Check mandatory headers
     *
     * @param context the context
     * @return list of missing mandatory headers
     */
    public List<String> mandatoryHeadersCheck(final Context context) {
        List<String> errors = new ArrayList<>();

        for (FileType fileType : List.of(FileType.DATACENTER, FileType.EQUIPEMENT_PHYSIQUE, FileType.EQUIPEMENT_VIRTUEL, FileType.APPLICATION)) {
            for (FileToLoad fileToLoad : context.getFilesToLoad()) {
                if (fileType.equals(fileToLoad.getFileType())) {

                    try (BufferedReader reader = new BufferedReader(new FileReader(fileToLoad.getConvertedFile()))) {
                        CSVParser records = CSVFormat.RFC4180.builder()
                                .setHeader()
                                .setDelimiter(CsvUtils.DELIMITER)
                                .setAllowMissingColumnNames(true)
                                .setSkipHeaderRecord(false)
                                .build()
                                .parse(reader);

                        Set<String> mandatoryHeaderFields = csvFileMapperInfo.getHeaderFields(fileToLoad.getFileType(), true);
                        records.getHeaderNames().forEach(mandatoryHeaderFields::remove);

                        if (!mandatoryHeaderFields.isEmpty()) {
                            errors.add(messageSource.getMessage(
                                    "header.mandatory",
                                    new String[]{fileToLoad.getOriginalFileName(), String.join(", ", mandatoryHeaderFields)},
                                    context.getLocale())
                            );
                        }

                    } catch (Exception e) {
                        throw new AsyncTaskException(String.format("%s - Error while managing converted file '%s'", context.log(),
                                fileToLoad.getConvertedFile().getName()), e);
                    }
                }
            }
        }

        return errors;
    }

    /**
     * Check equipment count limits
     * Validates that existing inventory equipment count + new files count does not exceed limits
     * Uses parallel processing to efficiently calculate equipment counts for multiple files
     *
     * @param context the context
     * @return list of equipment count limit violations
     */
    public List<String> equipmentCountLimitCheck(final Context context) {
        log.info("Starting equipment count limit check for {}", context.log());
        
        // Group files by type to calculate total count per type
        Map<FileType, Long> newCountsByType = context.getFilesToLoad().parallelStream()
                .collect(Collectors.groupingBy(
                        FileToLoad::getFileType,
                        Collectors.summingLong(fileToLoad -> {
                            long count = calculateFileEquipmentCount(context, fileToLoad);
                            log.info("File '{}' type '{}' has {} equipment", 
                                    fileToLoad.getOriginalFileName(), fileToLoad.getFileType(), count);
                            return count;
                        })
                ));

        log.info("Equipment counts by type: {}", newCountsByType);

        // Check each file type against limits
        return Stream.of(FileType.DATACENTER, FileType.EQUIPEMENT_PHYSIQUE, FileType.EQUIPEMENT_VIRTUEL, FileType.APPLICATION)
                .map(fileType -> checkEquipmentTypeLimit(context, fileType, newCountsByType.getOrDefault(fileType, 0L)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Check equipment limit for a specific equipment type
     *
     * @param context     the context
     * @param fileType    the file type
     * @param newCount    the new equipment count from files
     * @return Optional containing error message if limit is exceeded
     */
    private Optional<String> checkEquipmentTypeLimit(final Context context, final FileType fileType, final long newCount) {
        if (newCount == 0) {
            log.debug("No new equipment for type: {}", fileType);
            return Optional.empty(); // No new equipment of this type
        }

        Integer limit = getEquipmentLimitForFileType(fileType);
        log.info("Checking type: {}, newCount: {}, limit: {}", fileType, newCount, limit);
        
        if (limit == null) {
            log.warn("No limit configured for file type: {}", fileType);
            return Optional.empty(); // No limit configured for this file type
        }

        // Get existing equipment count from inventory
        long existingCount = getExistingEquipmentCount(context, fileType);
        long totalCount = existingCount + newCount;
        
        log.info("Type: {}, Existing: {}, New: {}, Total: {}, Limit: {}", 
                fileType, existingCount, newCount, totalCount, limit);

        if (totalCount > limit) {
            String fileTypeLabel = getFileTypeLabel(fileType);
            String errorMessage = messageSource.getMessage(
                    "equipment.limit.exceeded",
                    new String[]{
                            fileTypeLabel,
                            String.valueOf(existingCount),
                            String.valueOf(newCount),
                            String.valueOf(totalCount),
                            String.valueOf(limit)
                    },
                    context.getLocale()
            );
            log.error("Equipment limit exceeded for {}: {}", fileType, errorMessage);
            return Optional.of(errorMessage);
        }

        return Optional.empty();
    }

    /**
     * Get existing equipment count from inventory or digital service version
     *
     * @param context  the context
     * @param fileType the file type
     * @return existing equipment count
     */
    private long getExistingEquipmentCount(final Context context, final FileType fileType) {
        // Only check for inventory, digital service versions are typically new/temporary
        if (context.getInventoryId() == null) {
            return 0L;
        }

        Long count = switch (fileType) {
            case DATACENTER -> inDatacenterRepository.countDistinctNameByInventoryId(context.getInventoryId());
            case EQUIPEMENT_PHYSIQUE -> inPhysicalEquipmentRepository.sumQuantityByInventoryId(context.getInventoryId());
            case EQUIPEMENT_VIRTUEL -> inVirtualEquipmentRepository.sumQuantityByInventoryId(context.getInventoryId());
            case APPLICATION -> inApplicationRepository.countDistinctNameByInventoryId(context.getInventoryId());
            default -> 0L;
        };

        return count != null ? count : 0L;
    }

    /**
     * Calculate equipment count for a single file
     *
     * @param context    the context
     * @param fileToLoad the file to calculate
     * @return equipment count
     */
    private long calculateFileEquipmentCount(final Context context, final FileToLoad fileToLoad) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileToLoad.getConvertedFile()))) {
            CSVParser records = CSVFormat.RFC4180.builder()
                    .setHeader()
                    .setDelimiter(CsvUtils.DELIMITER)
                    .setAllowMissingColumnNames(true)
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(reader);

            return calculateEquipmentCount(fileToLoad.getFileType(), records);

        } catch (Exception e) {
            throw new AsyncTaskException(String.format("%s - Error while checking equipment count for file '%s'", context.log(),
                    fileToLoad.getConvertedFile().getName()), e);
        }
    }

    /**
     * Get user-friendly label for file type
     *
     * @param fileType the file type
     * @return file type label
     */
    private String getFileTypeLabel(FileType fileType) {
        return switch (fileType) {
            case APPLICATION -> "Application";
            case EQUIPEMENT_PHYSIQUE -> "Physical Equipment";
            case EQUIPEMENT_VIRTUEL -> "Virtual Equipment";
            case DATACENTER -> "Datacenter";
            default -> fileType.toString();
        };
    }

    /**
     * Calculate equipment count based on file type
     * Mimics the logic used in setInventoryCounts:
     * - DATACENTER: count distinct names
     * - EQUIPEMENT_PHYSIQUE: sum of quantity field
     * - EQUIPEMENT_VIRTUEL: count quantity by distinct name (approximation: sum of quantity if available)
     * - APPLICATION: count distinct names
     *
     * @param fileType the file type
     * @param records  the CSV records
     * @return the equipment count
     */
    private long calculateEquipmentCount(FileType fileType, CSVParser records) {
        try {
            return switch (fileType) {
                case DATACENTER -> countDistinctNames(records, "nomCourtDatacenter");
                case EQUIPEMENT_PHYSIQUE -> sumQuantityField(records, "quantite");
                case EQUIPEMENT_VIRTUEL -> sumQuantityField(records, "quantite");
                case APPLICATION -> countDistinctNames(records, "nomApplication");
                default -> records.stream().count();
            };
        } catch (Exception e) {
            log.error("Error calculating equipment count for type {}: {}", fileType, e.getMessage(), e);
            // If we can't calculate properly, count rows as fallback
            return records.stream().count();
        }
    }

    /**
     * Count distinct values in a specific column
     *
     * @param records    the CSV records
     * @param columnName the column name to count distinct values
     * @return count of distinct values
     */
    private long countDistinctNames(CSVParser records, String columnName) {
        List<CSVRecord> recordList = new ArrayList<>();
        records.forEach(recordList::add);
        
        log.debug("Counting distinct values in column '{}', total records: {}", columnName, recordList.size());
        
        long count = recordList.stream()
                .map(record -> {
                    try {
                        return record.get(columnName);
                    } catch (IllegalArgumentException e) {
                        log.warn("Column '{}' not found in record", columnName);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(name -> !name.isBlank())
                .distinct()
                .count();
        
        log.debug("Distinct count for column '{}': {}", columnName, count);
        return count;
    }

    /**
     * Sum quantity field values
     *
     * @param records    the CSV records
     * @param columnName the quantity column name
     * @return sum of quantity values
     */
    private long sumQuantityField(CSVParser records, String columnName) {
        List<CSVRecord> recordList = new ArrayList<>();
        records.forEach(recordList::add);
        
        log.debug("Summing quantity field '{}', total records: {}", columnName, recordList.size());
        
        if (recordList.isEmpty()) {
            return 0L;
        }
        
        // Check if column exists
        Set<String> headers = recordList.get(0).getParser().getHeaderNames().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        
        log.debug("Available headers: {}", headers);
        
        if (!headers.contains(columnName.toLowerCase())) {
            log.warn("Column '{}' not found. Falling back to row count. Available columns: {}", 
                    columnName, recordList.get(0).getParser().getHeaderNames());
            // For physical equipment, if quantite doesn't exist, count rows (each row = 1 equipment)
            return recordList.size();
        }
        
        long sum = recordList.stream()
                .mapToLong(record -> {
                    try {
                        String quantityStr = record.get(columnName);
                        if (quantityStr == null || quantityStr.isBlank()) {
                            log.debug("Empty quantity value at line {}, defaulting to 1", record.getRecordNumber());
                            return 1L; // Default to 1 if quantity is missing
                        }
                        // Parse as double first to handle decimal values like "1000.0"
                        // then convert to long (truncates decimal part)
                        double quantityDouble = Double.parseDouble(quantityStr.trim());
                        return (long) quantityDouble;
                    } catch (NumberFormatException e) {
                        log.warn("Could not parse quantity '{}' at line {}, defaulting to 1: {}", 
                                record.get(columnName), record.getRecordNumber(), e.getMessage());
                        return 1L;
                    } catch (IllegalArgumentException e) {
                        log.warn("Error reading quantity at line {}, defaulting to 1", record.getRecordNumber());
                        return 1L;
                    }
                })
                .sum();
        
        log.debug("Sum of quantity field '{}': {}", columnName, sum);
        return sum;
    }

    /**
     * Get equipment limit for a specific file type
     *
     * @param fileType the file type
     * @return the equipment limit or null if not configured
     */
    private Integer getEquipmentLimitForFileType(FileType fileType) {
        return switch (fileType) {
            case APPLICATION -> fileEquipmentLimitConfiguration.getApplication();
            case EQUIPEMENT_PHYSIQUE -> fileEquipmentLimitConfiguration.getPhysicalEquipment();
            case EQUIPEMENT_VIRTUEL -> fileEquipmentLimitConfiguration.getVirtualEquipment();
            case DATACENTER -> fileEquipmentLimitConfiguration.getDatacenter();
            default -> null;
        };
    }

    @Transactional
    public void linkApplicationsToVirtualEquipments(Long inventoryId) {
        // Load all virtual equipments
        Map<String, InVirtualEquipment> virtualMap = inVirtualEquipmentRepository.findByInventoryId(inventoryId)
                .stream()
                .collect(Collectors.toMap(InVirtualEquipment::getName, ve -> ve));

        // Find all applications with missing physical equipment
        List<InApplication> applicationsToLink = inApplicationRepository.findByInventoryIdAndPhysicalEquipmentNameIsNull(inventoryId);

        for (InApplication app : applicationsToLink) {
            InVirtualEquipment ve = virtualMap.get(app.getVirtualEquipmentName());
            if (ve != null) {
                app.setPhysicalEquipmentName(ve.getPhysicalEquipmentName());
                // optionally update other derived fields
                inApplicationRepository.save(app);
            }
        }
    }


}
