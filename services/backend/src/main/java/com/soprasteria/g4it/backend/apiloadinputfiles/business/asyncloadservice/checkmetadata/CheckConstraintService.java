/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkmetadata;

import com.soprasteria.g4it.backend.apiloadinputfiles.dto.CoherenceParentDTO;
import com.soprasteria.g4it.backend.apiloadinputfiles.dto.DuplicateEquipmentDTO;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckApplicationRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckDatacenterRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.common.model.LineError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CheckConstraintService {
    @Autowired
    MessageSource messageSource;
    @Autowired
    private CheckDatacenterRepository checkDatacenterRepository;
    @Autowired
    private CheckPhysicalEquipmentRepository checkPhysicalEqpRepository;
    @Autowired
    private CheckVirtualEquipmentRepository checkVirtualEqpRepository;
    @Autowired
    private CheckApplicationRepository checkApplicationRepository;

    /**
     * Check the metadata inventory files
     *
     * @param taskId the task id
     * @return Map of filename, Map of line number, List of LineError : filename -> [ line number -> LineError ]
     * The duplicated LineErrors of the filename line
     */
    public Map<String, Map<Integer, List<LineError>>> checkUnicity(Long taskId) {
        Map<String, Map<Integer, List<LineError>>> duplicatesMap = new HashMap<>();

        // Check Virtual Equipments
        List<DuplicateEquipmentDTO> duplicateVirtualEqp = checkVirtualEqpRepository.findDuplicateVirtualEquipments(taskId);
        processDuplicates(duplicateVirtualEqp, duplicatesMap, "virtual equipment");

        // Check Physical Equipments
        List<DuplicateEquipmentDTO> duplicatePhysicalEqp = checkPhysicalEqpRepository.findDuplicatePhysicalEquipments(taskId);
        processDuplicates(duplicatePhysicalEqp, duplicatesMap, "physical equipment");

        // Check Datacenters
        List<DuplicateEquipmentDTO> duplicateDatacenters = checkDatacenterRepository.findDuplicateDatacenters(taskId);
        processDuplicates(duplicateDatacenters, duplicatesMap, "datacenter");

        // Check Applications
        List<DuplicateEquipmentDTO> duplicateApplications = checkApplicationRepository.findDuplicateApplications(taskId);
        processDuplicates(duplicateApplications, duplicatesMap, "application");

        return duplicatesMap;
    }

    /**
     * @param duplicatedRecords the data
     * @param duplicatesMap     the map
     * @param entityType        the entity to be checked
     *                          Add duplicated records to the  Map of filename, Map of line number, List of LineError : filename -> [ line number -> LineError ]
     *                          The duplicated LineErrors of the filename line
     */
    private void processDuplicates(List<DuplicateEquipmentDTO> duplicatedRecords,
                                   Map<String, Map<Integer, List<LineError>>> duplicatesMap,
                                   String entityType) {
        for (DuplicateEquipmentDTO duplicate : duplicatedRecords) {
            String filenamesWithLines = duplicate.getFilenameLineInfo();
            String[] entries = filenamesWithLines.split(",");

            for (String entry : entries) {
                String[] parts = entry.split(":");
                String filename = parts[0];
                int lineNumber = Integer.parseInt(parts[1]);
                String errorMessage;
                // Create error message based on entity type
                if ("datacenter".equals(entityType)) {
                    errorMessage = String.format(
                            "The datacenter %s already exists in the inventory." +
                                    " The short name of a datacenter must be unique." +
                                    " Please check and modify the name to avoid duplicates.",
                            duplicate.getEquipmentName());
                } else if ("application".equals(entityType)) {
                    errorMessage = String.format(
                            "The combination of the fields (%s) already exists in the inventory." +
                                    " The combination of the fields (nomApplication, typeEnvironnement, nomEquipementVirtuel) must be unique." +
                                    " Please check and modify the name to avoid duplicates.",
                            duplicate.getEquipmentName());
                } else {
                    errorMessage = String.format(
                            "The %s %s already exists in the inventory. The %s must be unique." +
                                    " Please check and modify the name to avoid duplicates.",
                            entityType,
                            duplicate.getEquipmentName(),
                            entityType);
                }

                // Create LineError
                LineError error = new LineError(filename, lineNumber, errorMessage, duplicate.getEquipmentName());

                duplicatesMap.computeIfAbsent(filename, k -> new HashMap<>())
                        .computeIfAbsent(lineNumber, k -> new ArrayList<>())
                        .add(error);
            }
        }
    }

    /**
     * Check the metadata inventory files
     *
     * @param taskId     the task id
     * @param unicityMap to exclude the lines present in the map
     * @return Map of filename, Map of line number, List of LineError : filename -> [ line number -> LineError ]
     * The coherence LineErrors of the filename line
     */
    public Map<String, Map<Integer, List<LineError>>> checkCoherence(Long taskId, Long inventoryId, Map<String, Map<Integer, List<LineError>>> unicityMap) {
        Map<String, Map<Integer, List<LineError>>> coherenceMap = new HashMap<>();

        // Check virtual equipment references to physical equipment
        checkVirtualEquipmentCoherence(taskId, inventoryId, unicityMap, coherenceMap);

        // Check application references to virtual equipment
        checkApplicationCoherence(taskId, inventoryId, coherenceMap, unicityMap);

        return coherenceMap;
    }

    /**
     * Check the metadata virtual equipment files
     *
     * @param taskId the task id
     */
    private void checkVirtualEquipmentCoherence(Long taskId, Long inventoryId,
                                                Map<String, Map<Integer, List<LineError>>> unicityMap,
                                                Map<String, Map<Integer, List<LineError>>> coherenceMap) {

        //Refactor in order to request virtual equipement which don't have parent in checkCoherence table (the parent must not be in the list of duplicated parents)
        // and does not have parent in the inventory for this given inventoryId.

        List<String> errorenousPhysicalEquipement = unicityMap.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .flatMap(map -> map.entrySet().stream())
                .map(Map.Entry::getValue)
                .flatMap(List::stream)
                .map(LineError::equipementName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        List<CoherenceParentDTO> incoherentVirtualEquipement = new ArrayList<>();

        if (errorenousPhysicalEquipement.isEmpty()) {
            incoherentVirtualEquipement = checkVirtualEqpRepository.findIncoherentVirtualEquipments(taskId, inventoryId);
        } else {
            incoherentVirtualEquipement = checkVirtualEqpRepository.findIncoherentVirtualEquipments(taskId, inventoryId, errorenousPhysicalEquipement);
        }


        for (CoherenceParentDTO coherenceParentDTO : incoherentVirtualEquipement) {

            String errorMessage = messageSource.getMessage(
                    "equipementphysique.should.exist",
                    new String[]{coherenceParentDTO.getParentEquipmentName()},
                    LocaleContextHolder.getLocale()
            );

            addError(coherenceMap, coherenceParentDTO.getFilename(), coherenceParentDTO.getLineNb(), errorMessage);

        }
    }

    /**
     * Check the metadata application files
     *
     * @param taskId       the task id
     * @param coherenceMap the rejected virtual equipments
     * @param unicityMap   the duplicated
     */
    private void checkApplicationCoherence(Long taskId, Long inventoryId,
                                           Map<String, Map<Integer, List<LineError>>> coherenceMap,
                                           Map<String, Map<Integer, List<LineError>>> unicityMap) {


        List<String> errorenousVirtualEquipement = unicityMap.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .flatMap(map -> map.entrySet().stream())
                .map(Map.Entry::getValue)
                .flatMap(List::stream)
                .map(LineError::equipementName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(equipementName -> {
                    String[] parts = equipementName.split(" - ");
                    if (parts.length > 2) {
                        return parts[2];
                    } else {
                        return parts[0];
                    }
                })
                .toList();

        List<CoherenceParentDTO> incoherentApplications = new ArrayList<>();

        if (errorenousVirtualEquipement.isEmpty()) {
            incoherentApplications = checkApplicationRepository.findIncoherentApplications(taskId, inventoryId);
        } else {
            incoherentApplications = checkApplicationRepository.findIncoherentApplications(taskId, inventoryId, errorenousVirtualEquipement);
        }


        for (CoherenceParentDTO coherenceParentDTO : incoherentApplications) {

            String errorMessage = messageSource.getMessage(
                    "equipementvirtuel.should.exist",
                    new String[]{coherenceParentDTO.getParentEquipmentName()},
                    LocaleContextHolder.getLocale()
            );

            addError(coherenceMap, coherenceParentDTO.getFilename(), coherenceParentDTO.getLineNb(), errorMessage);

        }
    }

    private void addError(Map<String, Map<Integer, List<LineError>>> coherenceMap,
                          String filename, Integer lineNb, String errorMessage) {
        coherenceMap.computeIfAbsent(filename, k -> new HashMap<>())
                .computeIfAbsent(lineNb, k -> new ArrayList<>())
                .add(new LineError(filename, lineNb, errorMessage));
    }


}
