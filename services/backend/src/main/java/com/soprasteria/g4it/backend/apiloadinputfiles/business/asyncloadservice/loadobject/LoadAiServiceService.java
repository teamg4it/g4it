/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadobject;

import com.soprasteria.g4it.backend.apiinout.mapper.InAiServiceMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.InAiService;
import com.soprasteria.g4it.backend.apiinout.repository.InAiServiceRepository;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.server.gen.api.dto.InAiServiceRest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LoadAiServiceService {

    @Autowired
    InAiServiceMapper inAiServiceMapper;

    @Autowired
    InAiServiceRepository inAiServiceRepository;

    @Autowired
    MessageSource messageSource;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Validate and persist a page of AI service rows.
     *
     * @param context    the context
     * @param fileToLoad the file to load
     * @param pageNumber the page number (used to compute line numbers)
     * @param aiServices the AI service rows of this page
     * @return the list of errors found for this page
     */
    @Transactional
    public List<LineError> execute(final Context context, final FileToLoad fileToLoad, final int pageNumber, final List<InAiServiceRest> aiServices) {
        if (aiServices.isEmpty()) return List.of();

        log.info("Load ai services for {}, size = {}", context.log(), aiServices.size());

        final List<LineError> errors = new ArrayList<>();
        final List<InAiService> aiServicesToSave = new ArrayList<>();

        for (int i = 0; i < aiServices.size(); i++) {
            final int line = Constants.BATCH_SIZE * pageNumber + i + 2;
            final InAiServiceRest aiService = aiServices.get(i);
            final List<LineError> mandatoryErrors = checkMandatoryFields(context, fileToLoad.getFilename(), line, aiService);
            if (mandatoryErrors.isEmpty()) {
                aiServicesToSave.add(inAiServiceMapper.toEntity(aiService));
            } else {
                errors.addAll(mandatoryErrors);
            }
        }

        // Delete existing AI services with the same service names to avoid duplicates on reload
        final Set<String> serviceNames = aiServicesToSave.stream()
                .map(InAiService::getServiceName)
                .collect(Collectors.toSet());
        if (!serviceNames.isEmpty() && context.getInventoryId() != null) {
            inAiServiceRepository.deleteByInventoryIdAndServiceNameIn(context.getInventoryId(), serviceNames);
        }

        inAiServiceRepository.saveAll(aiServicesToSave);
        entityManager.flush();
        entityManager.clear();

        return errors;
    }

    /**
     * Check mandatory fields of an AI service row.
     */
    private List<LineError> checkMandatoryFields(final Context context, final String filename, final int line, final InAiServiceRest aiService) {
        final List<LineError> errors = new ArrayList<>();
        checkMandatory(errors, context, filename, line, "serviceName", aiService.getServiceName());
        checkMandatory(errors, context, filename, line, "provider", aiService.getProvider());
        checkMandatory(errors, context, filename, line, "model", aiService.getModel());
        if (aiService.getOutputTokens() == null) {
            errors.add(new LineError(filename, line,
                    messageSource.getMessage("field.mandatory", new String[]{"outputTokens"}, context.getLocale())));
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
     * Count AI services linked to an inventory.
     *
     * @param inventoryId the inventory id
     * @return the number of AI services
     */
    public Long getAiServiceCount(final Long inventoryId) {
        return (long) inAiServiceRepository.findByInventoryId(inventoryId).size();
    }
}
