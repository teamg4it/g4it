package com.soprasteria.g4it.backend.apireferential.persistence;

import com.google.common.collect.Lists;
import com.soprasteria.g4it.backend.apireferential.modeldb.*;
import com.soprasteria.g4it.backend.apireferential.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.function.BiConsumer;

@Service
@RequiredArgsConstructor
public class WorkspaceReferentialPersistenceService {

    private final ItemTypeRepository itemTypeRepository;
    private final ItemImpactRepository itemImpactRepository;
    private final MatchingItemRepository matchingItemRepository;
    private final HypothesisRepository hypothesisRepository;

    // =====================================================
    // ITEM TYPE
    // =====================================================
    public void syncItemTypes(Long workspaceId, List<ItemType> uploaded) {

        if (uploaded.isEmpty()) {
            itemTypeRepository.deleteByWorkspaceId(workspaceId);
            return;
        }

        List<ItemType> existing = itemTypeRepository.findByWorkspaceId(workspaceId);

        sync(
                workspaceId,
                uploaded,
                existing,
                itemTypeRepository,
                ItemType::getType,
                this::updateItemType
        );
    }

    private void updateItemType(ItemType target, ItemType source) {
        target.setCategory(source.getCategory());
        target.setComment(source.getComment());
        target.setDefaultLifespan(source.getDefaultLifespan());
        target.setIsServer(source.getIsServer());
        target.setSource(source.getSource());
        target.setRefDefaultItem(source.getRefDefaultItem());
        target.setVersion(source.getVersion());
    }

    // =====================================================
    // ITEM IMPACT
    // =====================================================
    public void syncItemImpacts(Long workspaceId, List<ItemImpact> uploaded) {

        if (uploaded.isEmpty()) {
            itemImpactRepository.deleteByWorkspaceId(workspaceId);
            return;
        }

        List<ItemImpact> existing = itemImpactRepository.findByWorkspaceId(workspaceId);

        sync(
                workspaceId,
                uploaded,
                existing,
                itemImpactRepository,
                i -> i.getName() + "|" + i.getLifecycleStep() + "|" + i.getCriterion(),
                this::updateItemImpact
        );
    }

    private void updateItemImpact(ItemImpact target, ItemImpact source) {
        target.setCategory(source.getCategory());
        target.setAvgElectricityConsumption(source.getAvgElectricityConsumption());
        target.setDescription(source.getDescription());
        target.setLocation(source.getLocation());
        target.setLevel(source.getLevel());
        target.setSource(source.getSource());
        target.setTier(source.getTier());
        target.setUnit(source.getUnit());
        target.setValue(source.getValue());
        target.setVersion(source.getVersion());
        target.setIsHidden(source.getIsHidden());
    }

    // =====================================================
    // MATCHING ITEM
    // =====================================================
    public void syncMatchingItems(Long workspaceId, List<MatchingItem> uploaded) {

        if (uploaded.isEmpty()) {
            matchingItemRepository.deleteByWorkspaceId(workspaceId);
            return;
        }

        List<MatchingItem> existing = matchingItemRepository.findByWorkspaceId(workspaceId);

        sync(
                workspaceId,
                uploaded,
                existing,
                matchingItemRepository,
                MatchingItem::getItemSource,
                this::updateMatchingItem
        );
    }

    private void updateMatchingItem(MatchingItem target, MatchingItem source) {
        target.setRefItemTarget(source.getRefItemTarget());
    }


    // =====================================================
    // GENERIC SYNC ENGINE
    // =====================================================
    private <T> void sync(
            Long workspaceId,
            List<T> uploaded,
            List<T> existing,
            org.springframework.data.jpa.repository.JpaRepository<T, Long> repository,
            Function<T, String> keyExtractor,
            BiConsumer<T, T> updater
    ) {

        Map<String, T> existingMap = existing.stream()
                .collect(Collectors.toMap(keyExtractor, Function.identity()));

        Set<String> processed = new HashSet<>();

        List<T> toSave = new ArrayList<>();
        List<T> toDelete = new ArrayList<>();

        // CREATE + UPDATE
        for (T item : uploaded) {

            String key = keyExtractor.apply(item);
           // if (key == null || key.isBlank()) throw error;
            processed.add(key);

            T db = existingMap.get(key);

            if (db != null) {
                updater.accept(db, item);
                toSave.add(db);
            } else {
                setWorkspace(item, workspaceId);
                toSave.add(item);
            }
        }

        // DELETE
        for (T db : existing) {
            if (!processed.contains(keyExtractor.apply(db))) {
                toDelete.add(db);
            }
        }

        Lists.partition(toSave, 500).forEach(repository::saveAll);
        repository.deleteAll(toDelete);
    }

    // =====================================================
    // SET WORKSPACE GENERICALLY
    // =====================================================
    private <T> void setWorkspace(T entity, Long workspaceId) {
        if (entity instanceof ItemType e) e.setWorkspaceId(workspaceId);
        if (entity instanceof ItemImpact e) e.setWorkspaceId(workspaceId);
        if (entity instanceof MatchingItem e) e.setWorkspaceId(workspaceId);
    }
}