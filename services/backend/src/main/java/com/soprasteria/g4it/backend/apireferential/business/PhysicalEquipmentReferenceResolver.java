// This file has been created (totally or partially) with the assistance of artificial intelligence tools.
// All content has been generated under the direct supervision of a named individual,
// and under the AI.Backbone Orchestrator Compliance framework

package com.soprasteria.g4it.backend.apireferential.business;

import com.soprasteria.g4it.backend.server.gen.api.dto.ItemTypeRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.MatchingItemRest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PhysicalEquipmentReferenceResolver {
    private final ReferentialService referentialService;

    public Reference resolve(String model, String type, String organization, Long workspaceId, long workspaceCount) {
        MatchingItemRest matchingItem = null;
        if (model != null) {
            if (workspaceCount > 0) {
                matchingItem = referentialService.getMatchingItemForWorkspace(model, workspaceId);
            }
            if (matchingItem == null) {
                matchingItem = referentialService.getMatchingItem(model, organization);
            }
        }

        ItemTypeRest itemType = null;
        if (workspaceCount > 0) {
            itemType = referentialService.getItemTypeForWorkspace(type, workspaceId);
        }
        if (itemType == null) {
            itemType = referentialService.getItemType(type, organization);
        }

        String reference = matchingItem != null ? matchingItem.getRefItemTarget()
                : itemType != null ? itemType.getRefDefaultItem() : null;
        return new Reference(matchingItem, itemType, reference, matchingItem != null || reference == null);
    }

    public record Reference(MatchingItemRest matchingItem, ItemTypeRest itemType,
                            String reference, boolean modelMatched) {
    }
}
