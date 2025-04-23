/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkmetadata;

import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.common.model.LineError;
import org.springframework.stereotype.Service;


import com.soprasteria.g4it.backend.common.model.LineError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
public class CheckMetadataInventoryFileService {

    @Autowired
    private CheckConstraintService checkConstraintService;

    /**
     * Check the metadata inventory file
     *
     * @param taskId : The task id to check
     * @return Map of filename, Map of line number, List of LineError : filename -> [ line number -> LineError ]
     * The LineErrors of the filename line
     */
    public Map<String, Map<Integer, List<LineError>>> checkMetadataInventoryFile(Long taskId, Long inventoryId) {
        // check unicity
        Map<String, Map<Integer, List<LineError>>> duplicatesMap = checkConstraintService.checkUnicity(taskId);

        // check coherence
        Map<String, Map<Integer, List<LineError>>> coherenceMap = checkConstraintService.checkCoherence(taskId,inventoryId, duplicatesMap);

        // get all the rejected data
        Map<String, Map<Integer, List<LineError>>> resultMap = new HashMap<>(duplicatesMap);

        coherenceMap.forEach((fileName, lineErrors) -> {
            resultMap.merge(fileName, lineErrors, (existing, newValues) -> {
                Map<Integer, List<LineError>> merged = new HashMap<>(existing);
                newValues.forEach((lineNumber, errors) -> {
                    merged.merge(lineNumber, errors, (existingErrors, newErrors) -> {
                        List<LineError> combinedErrors = new ArrayList<>(existingErrors);
                        combinedErrors.addAll(newErrors);
                        return combinedErrors;
                    });
                });
                return merged;
            });
        });

        return resultMap;
    }

}
