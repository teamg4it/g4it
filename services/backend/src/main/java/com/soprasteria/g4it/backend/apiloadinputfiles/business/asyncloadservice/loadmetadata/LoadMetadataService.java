/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadmetadata;

import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadmetadata.loaders.*;
import com.soprasteria.g4it.backend.apiloadinputfiles.mapper.CsvToInMapper;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.CsvUtils;
import com.soprasteria.g4it.backend.exception.AsyncTaskException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class LoadMetadataService {

    @Autowired
    private CsvToInMapper csvToInMapper;
    @Autowired
    private LoadDatacenterMetadataService loadDatacenterMetadataService;
    @Autowired
    private LoadVirtualEquipmentMetadataService loadVirtualEquipmentMetadataService;

    @Autowired
    private LoadPhysicalEquipmentMetadataService loadPhysicalEquipmentMetadataService;
    @Autowired
    private LoadApplicationMetadataService loadApplicationMetadataService;

    /**
     * Load the metadata file
     *
     * @param fileToLoad : the file to load
     * @param context    : the file loading context
     */
    public void loadMetadataFile(FileToLoad fileToLoad, Context context) {

        log.info("Load metadata for file {} {}", fileToLoad.getFilename(), context.log());

        IMetadataLoaderService retrieveMetadataLoaderService = retrieveMetadataLoaderService(fileToLoad.getFileType());

        try (BufferedReader reader = new BufferedReader(new FileReader(fileToLoad.getConvertedFile()))) {
            CSVParser records = CSVFormat.RFC4180.builder()
                    .setHeader()
                    .setDelimiter(CsvUtils.DELIMITER)
                    .setAllowMissingColumnNames(true)
                    .setSkipHeaderRecord(false)
                    .build()
                    .parse(reader);

            int row = 1;
            int pageNumber = 0;

            // read file locally by PAGE_SIZE lines page
            List<Object> objects = new ArrayList<>(Constants.BATCH_SIZE);

            for (CSVRecord csvRecord : records) {
                objects.add(mapCsvToInMetadataObject(csvRecord, context.getInventoryId(), context.getDigitalServiceUid(), fileToLoad.getFileType()));
                if (row >= Constants.BATCH_SIZE) {
                    retrieveMetadataLoaderService.execute(context, fileToLoad, pageNumber, objects);
                    objects.clear();
                    row = 1;
                    pageNumber++;
                } else {
                    row++;
                }
            }
            retrieveMetadataLoaderService.execute(context, fileToLoad, pageNumber, objects);
        } catch (IOException e) {
            throw new AsyncTaskException(String.format("%s - Error during metadata loading of file '%s'", context.log(),
                    fileToLoad.getOriginalFileName()), e);
        }

    }

    /**
     * Map the csv record to the InMetadata object
     *
     * @param csvRecord   : the csv record
     * @param inventoryId : the inventory id
     * @param fileType    : the file type
     * @return The mapped Rest Object
     */
    private Object mapCsvToInMetadataObject(CSVRecord csvRecord, Long inventoryId, String digitalServiceUid, FileType fileType) {
        Object mappedObject = null;
        switch (fileType) {
            case DATACENTER -> mappedObject = csvToInMapper.csvInDatacenterToRest(csvRecord, inventoryId, digitalServiceUid);
            case EQUIPEMENT_PHYSIQUE ->
                    mappedObject = csvToInMapper.csvInPhysicalEquipmentToRest(csvRecord, inventoryId, digitalServiceUid);
            case EQUIPEMENT_VIRTUEL -> mappedObject = csvToInMapper.csvInVirtualEquipmentToRest(csvRecord, inventoryId, digitalServiceUid);
            case APPLICATION -> mappedObject = csvToInMapper.csvInApplicationToRest(csvRecord, inventoryId);
        }
        return mappedObject;
    }

    /**
     * Retrieve the metadata loader service depending on the fileType
     *
     * @param fileType : the file type
     * @return the metadata loader service
     */
    private IMetadataLoaderService retrieveMetadataLoaderService(FileType fileType) {
        IMetadataLoaderService metadataLoaderService = null;
        switch (fileType) {
            case DATACENTER -> metadataLoaderService = loadDatacenterMetadataService;
            case EQUIPEMENT_PHYSIQUE -> metadataLoaderService = loadPhysicalEquipmentMetadataService;
            case EQUIPEMENT_VIRTUEL -> metadataLoaderService = loadVirtualEquipmentMetadataService;
            case APPLICATION -> metadataLoaderService = loadApplicationMetadataService;
            default -> throw new IllegalArgumentException("Unexpected value: " + fileType);
        }
        return metadataLoaderService;
    }


}
