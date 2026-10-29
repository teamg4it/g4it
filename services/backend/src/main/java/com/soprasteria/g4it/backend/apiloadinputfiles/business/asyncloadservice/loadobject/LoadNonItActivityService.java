/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadobject;

import com.soprasteria.g4it.backend.apiinout.mapper.InNonItActivityMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.InNonItActivity;
import com.soprasteria.g4it.backend.apiinout.repository.InNonItActivityRepository;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.server.gen.api.dto.InNonItActivityRest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoadNonItActivityService {

    private final InNonItActivityMapper inNonItActivityMapper;
    private final InNonItActivityRepository inNonItActivityRepository;
    private final MessageSource messageSource;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Validate and persist a page of non-IT activity rows.
     *
     * @param context         the context
     * @param fileToLoad      the file to load
     * @param pageNumber      the page number (used to compute line numbers)
     * @param nonItActivities the non-IT activity rows of this page
     * @return the list of errors found for this page
     */
    @Transactional
    public List<LineError> execute(final Context context, final FileToLoad fileToLoad, final int pageNumber, final List<InNonItActivityRest> nonItActivities) {
        if (nonItActivities.isEmpty()) return List.of();

        log.info("Load non-IT activities for {}, size = {}", context.log(), nonItActivities.size());

        final List<LineError> errors = new ArrayList<>();
        final List<InNonItActivity> nonItActivitiesToSave = new ArrayList<>();

        for (int i = 0; i < nonItActivities.size(); i++) {
            final int line = Constants.BATCH_SIZE * pageNumber + i + 2;
            final InNonItActivityRest nonItActivity = nonItActivities.get(i);
            final List<LineError> mandatoryErrors = checkMandatoryFields(context, fileToLoad.getFilename(), line, nonItActivity);
            if (mandatoryErrors.isEmpty()) {
                nonItActivitiesToSave.add(inNonItActivityMapper.toEntity(nonItActivity));
            } else {
                errors.addAll(mandatoryErrors);
            }
        }

        // Delete existing non-IT activities with the same activity names to avoid duplicates on reload
        final Set<String> activityNames = nonItActivitiesToSave.stream()
                .map(InNonItActivity::getActivityName)
                .collect(Collectors.toSet());
        if (!activityNames.isEmpty() && context.getInventoryId() != null) {
            inNonItActivityRepository.deleteByInventoryIdAndActivityNameIn(context.getInventoryId(), activityNames);
        }

        inNonItActivityRepository.saveAll(nonItActivitiesToSave);
        entityManager.flush();
        entityManager.clear();

        return errors;
    }

    /**
     * Check mandatory fields of a non-IT activity row.
     */
    private List<LineError> checkMandatoryFields(final Context context, final String filename, final int line, final InNonItActivityRest nonItActivity) {
        final List<LineError> errors = new ArrayList<>();
        checkMandatory(errors, context, filename, line, "activityName", nonItActivity.getActivityName());
        checkMandatory(errors, context, filename, line, "reference", nonItActivity.getReference());
        if (nonItActivity.getQuantity() == null) {
            errors.add(new LineError(filename, line,
                    messageSource.getMessage("field.mandatory", new String[]{"quantity"}, context.getLocale())));
        }
        return errors;
    }

    private void checkMandatory(final List<LineError> errors, final Context context, final String filename, final int line,
                                final String fieldName, final String value) {
        if (StringUtils.isBlank(value)) {
            errors.add(new LineError(filename, line,
                    messageSource.getMessage("field.mandatory", new String[]{fieldName}, context.getLocale())));
        }
    }

    /**
     * Count non-IT activities linked to an inventory.
     *
     * @param inventoryId the inventory id
     * @return the number of non-IT activities
     */
    public Long getNonItActivityCount(final Long inventoryId) {
        return inNonItActivityRepository.countByInventoryId(inventoryId);
    }
}
