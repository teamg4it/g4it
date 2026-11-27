// This file has been created (totally or partially) with the assistance of artificial intelligence tools.
// All content has been generated under the direct supervision of a named individual,
// and under the AI.Backbone Orchestrator Compliance framework

package com.soprasteria.g4it.backend.apireferential.business;

import com.soprasteria.g4it.backend.apireferential.modeldb.ItemType;
import com.soprasteria.g4it.backend.apireferential.repository.ItemTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemCategoryResolver {

    private final ItemTypeRepository itemTypeRepository;

    public Lookup prepare(String organization, Long workspaceId) {
        List<ItemType> itemTypes = itemTypeRepository.findByWorkspaceIdOrWorkspaceIdIsNull(workspaceId).stream()
                .filter(itemType -> itemType.getOrganization() == null
                        || Objects.equals(itemType.getOrganization(), organization))
                .filter(itemType -> itemType.getItemCategory() != null)
                .sorted(Comparator
                        .comparing((ItemType itemType) -> !Objects.equals(itemType.getWorkspaceId(), workspaceId))
                        .thenComparing(itemType -> !Objects.equals(itemType.getOrganization(), organization)))
                .toList();
        return new Lookup(itemTypes);
    }

    public record Category(String itemType, String itemCategory) {
        public boolean isDevice() {
            return "DEVICES".equals(itemCategory);
        }
    }

    public static final class Lookup {
        private final Map<String, ItemType> byType;
        private final Map<String, ItemType> byReference;

        private Lookup(List<ItemType> itemTypes) {
            byType = itemTypes.stream().collect(Collectors.toMap(
                    ItemType::getType, itemType -> itemType, (preferred, ignored) -> preferred));
            byReference = itemTypes.stream()
                    .filter(itemType -> itemType.getRefDefaultItem() != null)
                    .collect(Collectors.toMap(
                            ItemType::getRefDefaultItem, itemType -> itemType, (preferred, ignored) -> preferred));
        }

        public Category resolve(String reference, String type) {
            ItemType itemType = byReference.get(reference);
            if (itemType == null) {
                itemType = byType.get(type);
            }
            return itemType == null
                    ? new Category(type, "INFRASTRUCTURE")
                    : new Category(itemType.getType(), itemType.getItemCategory());
        }
    }
}
