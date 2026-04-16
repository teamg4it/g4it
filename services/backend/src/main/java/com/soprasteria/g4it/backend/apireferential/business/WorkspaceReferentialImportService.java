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

    public ImportReportRest importReferentialCSV(@NotNull Long workspaceId, String type, MultipartFile file) {

        if (workspaceId == null) {
            throw new IllegalArgumentException("workspaceId cannot be null");
        }

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("file", "File is empty");
        }

        switch (type) {

            // ===============================
            // ITEM TYPE
            // ===============================
            case "itemType": {

                ItemTypeParseResult result = referentialImportService.parseItemTypeCsv(file);

                if (result.getReport() != null && !result.getReport().getErrors().isEmpty())
                {
                    return result.getReport();
                }

                validateDuplicates(result.getData(), ItemTypeRest::getType, "type");

                List<ItemType> entities =
                        referentialMapper.toItemTypeEntity(result.getData());

                workspacePersistenceService.syncItemTypes(workspaceId, entities);

                return result.getReport();
            }

            // ===============================
            // MATCHING ITEM
            // ===============================
            case "matchingItem": {

                MatchingItemParseResult result = referentialImportService.parseMatchingItemCsv(file);

                if (result.getReport() != null && !result.getReport().getErrors().isEmpty()) {
                    return result.getReport();
                }

                validateDuplicates(result.getData(), MatchingItemRest::getItemSource, "itemSource");

                validateMatchingItems(result.getData(), workspaceId);

                List<MatchingItem> entities =
                        referentialMapper.toMatchingEntity(result.getData());

                workspacePersistenceService.syncMatchingItems(workspaceId, entities);

                return result.getReport();
            }

            // ===============================
            // ITEM IMPACT
            // ===============================
            case "itemImpact": {

                ItemImpactParseResult result = referentialImportService.parseItemImpactCsv(file);

                if (result.getReport() != null && !result.getReport().getErrors().isEmpty()) {
                    return result.getReport();
                }

                validateDuplicates(result.getData(),
                        i -> i.getName() + "|" + i.getLifecycleStep() + "|" + i.getCriterion(),
                        "itemImpact");

                List<ItemImpact> entities =
                        referentialMapper.toItemImpactEntity(result.getData());

                workspacePersistenceService.syncItemImpacts(workspaceId, entities);

                return result.getReport();
            }

            default:
                throw new BadRequestException("type", "Unsupported type");
        }
    }

    // ===============================
    // DUPLICATE VALIDATION
    // ===============================
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

    // ===============================
    // MATCHING VALIDATION
    // ===============================
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
}