package com.soprasteria.g4it.backend.apireferential.business;

import com.soprasteria.g4it.backend.apireferential.mapper.ReferentialMapper;
import com.soprasteria.g4it.backend.apireferential.modeldb.*;
import com.soprasteria.g4it.backend.apireferential.persistence.WorkspaceReferentialPersistenceService;
import com.soprasteria.g4it.backend.apireferential.repository.ItemImpactRepository;
import com.soprasteria.g4it.backend.apiuser.business.AuthService;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.auditevent.business.AuditEventService;
import com.soprasteria.g4it.backend.auditevent.model.AuditContext;
import com.soprasteria.g4it.backend.auditevent.model.AuditEventType;
import com.soprasteria.g4it.backend.auditevent.modeldb.AuditEvent;
import com.soprasteria.g4it.backend.auditevent.utils.Constants;
import com.soprasteria.g4it.backend.exception.BadRequestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.*;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Primary
@Service
@RequiredArgsConstructor
public class WorkspaceReferentialImportService {

    private final ReferentialImportService referentialImportService;
    private final WorkspaceReferentialPersistenceService workspacePersistenceService;
    private final ItemImpactRepository itemImpactRepository;
    private final ReferentialMapper referentialMapper;


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

        validateDuplicates(
                result.getData(),
                i -> i.getName() + "|" + i.getLifecycleStep() + "|" + i.getCriterion(),
                "itemImpact"
        );

        List<ItemImpact> entities = referentialMapper.toItemImpactEntity(result.getData());

        workspacePersistenceService.syncItemImpacts(workspaceId, entities);

        return result.getReport();
    }

    private void validateReportOrFail(ImportReportRest report) {
        if (report != null && !report.getErrors().isEmpty()) {
            throw new BadRequestException("csv", "Validation errors in file");
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

}