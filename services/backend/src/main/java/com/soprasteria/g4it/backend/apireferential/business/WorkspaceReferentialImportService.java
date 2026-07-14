package com.soprasteria.g4it.backend.apireferential.business;

import com.soprasteria.g4it.backend.apireferential.mapper.ReferentialMapper;
import com.soprasteria.g4it.backend.apireferential.modeldb.*;
import com.soprasteria.g4it.backend.apireferential.persistence.WorkspaceReferentialPersistenceService;
import com.soprasteria.g4it.backend.apireferential.repository.ItemImpactRepository;
import com.soprasteria.g4it.backend.exception.BadRequestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.*;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceReferentialImportService {

    private final ReferentialImportService referentialImportService;
    private final WorkspaceReferentialPersistenceService workspacePersistenceService;
    private final ItemImpactRepository itemImpactRepository;
    private final ReferentialMapper referentialMapper;

    /**
     * Allowed values for criterion column
     */
    private static final Set<String> VALID_CRITERION_VALUES = Set.of(
            "CLIMATE_CHANGE",
            "OZONE_DEPLETION",
            "PHOTOCHEMICAL_OZONE_FORMATION",
            "EUTROPHICATION_TERRESTRIAL",
            "EUTROPHICATION_FRESHWATER",
            "EUTROPHICATION_MARINE",
            "RESOURCE_USE_FOSSILS",
            "PARTICULATE_MATTER",
            "IONISING_RADIATION",
            "ACIDIFICATION",
            "RESOURCE_USE",
            "WATER_USE"
    );

    /**
     * Allowed values for lifecycleStep column
     */
    private static final Set<String> VALID_LIFECYCLE_STEP_VALUES = Set.of(
            "MANUFACTURING",
            "TRANSPORTATION",
            "USING",
            "END_OF_LIFE"
    );



    public ImportReportRest importReferentialCSV(
            @NotNull String organization,
            @NotNull Long workspaceId,
            String type,
            MultipartFile file
    ) {

        if (workspaceId == null) {
            throw new IllegalArgumentException("workspaceId cannot be null");
        }

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("file", "File is empty");
        }

        switch (type) {

            case "itemType":
                return handleItemType(workspaceId, file);

            case "matchingItem":
                return handleMatchingItem(workspaceId, file);

            case "itemImpact":
                return handleItemImpact(workspaceId, file);

            default:
                throw new BadRequestException("type", "Unsupported type");
        }
    }

    private ImportReportRest handleItemType(Long workspaceId, MultipartFile file) {

        ItemTypeParseResult result = referentialImportService.parseItemTypeCsv(file);

        validateReportOrFail(result.getReport());

        validateDuplicates(result.getData(), ItemTypeRest::getType, "type");

        List<ItemType> entities = referentialMapper.toItemTypeEntity(result.getData());

        workspacePersistenceService.syncItemTypes(workspaceId, entities);

        return result.getReport();
    }

    private ImportReportRest handleMatchingItem(Long workspaceId, MultipartFile file) {

        MatchingItemParseResult result = referentialImportService.parseMatchingItemCsv(file);

        validateReportOrFail(result.getReport());

        validateDuplicates(result.getData(), MatchingItemRest::getItemSource, "itemSource");
        validateMatchingItems(result.getData(), workspaceId);

        List<MatchingItem> entities = referentialMapper.toMatchingEntity(result.getData());

        workspacePersistenceService.syncMatchingItems(workspaceId, entities);

        return result.getReport();
    }

    private ImportReportRest handleItemImpact(Long workspaceId, MultipartFile file) {

        ItemImpactParseResult result = referentialImportService.parseItemImpactCsv(file);

        validateReportOrFail(result.getReport());

        validateItemImpactDuplicates(result.getData());

        validateItemImpactMandatoryFields(result.getData());

        validateItemImpactCriterionValues(result.getData());

        validateItemImpactLifecycleStepValues(result.getData());

        validateItemImpactElectricityConsumption(result.getData());

        List<ItemImpact> entities = referentialMapper.toItemImpactEntity(result.getData());

        workspacePersistenceService.syncItemImpacts(workspaceId, entities);

        return result.getReport();
    }

    private void validateReportOrFail(ImportReportRest report) {
        if (!report.getErrors().isEmpty()) {
            // Return the first error for clarity, or combine all errors
            String errorMessage = String.join("; ", report.getErrors());
            throw new BadRequestException("csv", errorMessage);
        }
    }


    private <T> void validateDuplicates(List<T> list, Function<T, String> keyExtractor, String fieldName) {

        Set<String> seen = new java.util.HashSet<>();

        for (T item : list) {
            String key = keyExtractor.apply(item);

            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("Key cannot be null");
            }

            if (!seen.add(key)) {
                throw new BadRequestException(fieldName, "Duplicate value found: " + key);
            }
        }
    }

    private void validateMatchingItems(List<MatchingItemRest> items, Long workspaceId) {

        Set<String> validTargets = itemImpactRepository
                .findByWorkspaceIdOrWorkspaceIdIsNull(workspaceId)
                .stream()
                .map(ItemImpact::getName)
                .collect(Collectors.toSet());

        for (MatchingItemRest item : items) {

            if (!validTargets.contains(item.getRefItemTarget())) {
                throw new BadRequestException(
                        "refItemTarget",
                        "Invalid reference: " + item.getRefItemTarget()
                );
            }
        }

    }

    /**
     * Validate that all mandatory fields are present for itemImpact rows.
     * Mandatory columns: criterion, lifecycleStep, name, category, level, source, tier, unit and value.
     *
     * @param items List of ItemImpactRest to validate
     * @throws BadRequestException if any mandatory field is missing
     */
    private void validateItemImpactMandatoryFields(List<ItemImpactRest> items) {
        for (ItemImpactRest item : items) {
            if (StringUtils.isBlank(item.getCriterion()) ||
                    StringUtils.isBlank(item.getLifecycleStep()) ||
                    StringUtils.isBlank(item.getName()) ||
                    StringUtils.isBlank(item.getCategory()) ||
                    StringUtils.isBlank(item.getLevel()) ||
                    StringUtils.isBlank(item.getSource()) ||
                    StringUtils.isBlank(item.getTier()) ||
                    StringUtils.isBlank(item.getUnit()) ||
                    item.getValue() == null) {

                throw new BadRequestException("itemImpact", "itemimpact.required.fields.missing");
            }
        }
    }

    /**
     * Validate that criterion values are from the allowed list.
     * Allowed values: CLIMATE_CHANGE, OZONE_DEPLETION, PHOTOCHEMICAL_OZONE_FORMATION,
     * EUTROPHICATION_TERRESTRIAL, EUTROPHICATION_FRESHWATER, EUTROPHICATION_MARINE,
     * RESOURCE_USE_FOSSILS, PARTICULATE_MATTER, IONISING_RADIATION, ACIDIFICATION,
     * RESOURCE_USE, WATER_USE
     *
     * @param items List of ItemImpactRest to validate
     * @throws BadRequestException if any criterion value is not in the allowed list
     */
    private void validateItemImpactCriterionValues(List<ItemImpactRest> items) {
        for (ItemImpactRest item : items) {
            if (item.getCriterion() != null && !VALID_CRITERION_VALUES.contains(item.getCriterion())) {
                throw new BadRequestException("itemImpact", "itemimpact.criterion.invalid");
            }
        }
    }

    /**
     * Validate that lifecycleStep values are from the allowed list.
     * Allowed values: MANUFACTURING, TRANSPORTATION, USING, END_OF_LIFE
     *
     * @param items List of ItemImpactRest to validate
     * @throws BadRequestException if any lifecycleStep value is not in the allowed list
     */
    private void validateItemImpactLifecycleStepValues(List<ItemImpactRest> items) {
        for (ItemImpactRest item : items) {
            if (item.getLifecycleStep() != null && !VALID_LIFECYCLE_STEP_VALUES.contains(item.getLifecycleStep())) {
                throw new BadRequestException("itemImpact", "itemimpact.lifecyclestep.invalid");
            }
        }
    }

    /**
     * Validate that electricity consumption is provided for USING lifecycle step.
     *
     * @param items List of ItemImpactRest to validate
     * @throws BadRequestException if avgElectricityConsumption is missing for USING step
     */
    private void validateItemImpactElectricityConsumption(List<ItemImpactRest> items) {
        for (ItemImpactRest item : items) {
            if ("USING".equals(item.getLifecycleStep()) && item.getAvgElectricityConsumption() == null) {
                throw new BadRequestException("itemImpact", "itemimpact.electricity.consumption.missing");
            }
        }
    }

    /**
     * Validate that the triplet (criterion, lifecycleStep, name) is unique.
     * Provides explicit error message with the item name.
     *
     * @param items List of ItemImpactRest to validate
     * @throws BadRequestException if duplicate triplet is found
     */
    private void validateItemImpactDuplicates(List<ItemImpactRest> items) {
        Set<String> seen = new java.util.HashSet<>();

        for (ItemImpactRest item : items) {
            String key = item.getName() + "|" + item.getLifecycleStep() + "|" + item.getCriterion();

            if (!seen.add(key)) {
                throw new BadRequestException("itemImpact", "itemimpact.duplicate.triplet:" + item.getName());
            }
        }
    }

}