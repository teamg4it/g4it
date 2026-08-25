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
import com.soprasteria.g4it.backend.apiinout.repository.InDatacenterRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InApplicationRepository;
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
    InDatacenterRepository inDatacenterRepository;
    @Autowired
    InPhysicalEquipmentRepository inPhysicalEquipmentRepository;
    @Value("${local.working.folder}")
    private String localWorkingFolder;
    @Value("${g4it.inventory.loading.limits.datacenter:10000}")
    private long datacenterLimit;
    @Value("${g4it.inventory.loading.limits.physical-equipment:500000}")
    private long physicalEquipmentLimit;
    @Value("${g4it.inventory.loading.limits.virtual-equipment:1000000}")
    private long virtualEquipmentLimit;
    @Value("${g4it.inventory.loading.limits.application:1000000}")
    private long applicationLimit;

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
     * Check that the number of rows in each inventory file (existing + new) does not exceed the configured limit.
     * Only applies to inventory loads (inventoryId must be present in context).
     *
     * @param context the context
     * @return list of error messages for files exceeding their limits
     */
    public List<String> checkInventoryLoadingLimits(final Context context) {
        if (context.getInventoryId() == null) return List.of();

        List<String> errors = new ArrayList<>();
        long inventoryId = context.getInventoryId();

        for (FileToLoad fileToLoad : context.getFilesToLoad()) {
            long newCount = countCsvDataRows(fileToLoad.getConvertedFile());
            long existingCount;
            long limit;
            String fileTypeName;

            switch (fileToLoad.getFileType()) {
                case DATACENTER -> {
                    existingCount = inDatacenterRepository.countByInventoryId(inventoryId);
                    limit = datacenterLimit;
                    fileTypeName = "datacenter";
                }
                case EQUIPEMENT_PHYSIQUE -> {
                    existingCount = inPhysicalEquipmentRepository.countByInventoryId(inventoryId);
                    limit = physicalEquipmentLimit;
                    fileTypeName = "physical equipment";
                }
                case EQUIPEMENT_VIRTUEL -> {
                    existingCount = inVirtualEquipmentRepository.countByInventoryId(inventoryId);
                    limit = virtualEquipmentLimit;
                    fileTypeName = "virtual equipment";
                }
                case APPLICATION -> {
                    existingCount = inApplicationRepository.countByInventoryId(inventoryId);
                    limit = applicationLimit;
                    fileTypeName = "application";
                }
                default -> { continue; }
            }

            long totalCount = existingCount + newCount;
            if (totalCount > limit) {
                log.warn("Inventory loading limit exceeded for {} file '{}': existing={}, new={}, total={}, limit={}",
                        fileTypeName, fileToLoad.getOriginalFileName(), existingCount, newCount, totalCount, limit);
                errors.add(messageSource.getMessage(
                        "inventory.loading.limit.exceeded",
                        new Object[]{fileToLoad.getOriginalFileName(), fileTypeName, existingCount, newCount, totalCount, limit},
                        context.getLocale()));
            }
        }

        return errors;
    }

    /**
     * Count data rows in a CSV file (total lines minus the header line).
     *
     * @param file the CSV file
     * @return number of data rows
     */
    private long countCsvDataRows(final File file) {
        try (Stream<String> lines = Files.lines(file.toPath())) {
            long total = lines.count();
            return total > 0 ? total - 1 : 0;
        } catch (IOException e) {
            throw new AsyncTaskException("Error counting rows in file: " + file.getName(), e);
        }
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
