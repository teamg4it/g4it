// This file has been created (totally or partially) with the assistance of artificial intelligence tools.
// All content has been generated under the direct supervision of a named individual,
// and under the AI.Backbone Orchestrator Compliance framework

package com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice;

import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.PhysicalEquipmentEvaluationTrace;
import com.soprasteria.g4it.backend.apiinout.repository.PhysicalEquipmentEvaluationTraceRepository;
import com.soprasteria.g4it.backend.apireferential.business.ItemCategoryResolver;
import com.soprasteria.g4it.backend.apireferential.business.PhysicalEquipmentReferenceResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PhysicalEquipmentAllocationService {
    private final ItemCategoryResolver categoryResolver;
    private final PhysicalEquipmentReferenceResolver referenceResolver;
    private final PhysicalEquipmentEvaluationTraceRepository traceRepository;

    public ItemCategoryResolver.Lookup begin(Long taskId, String organization, Long workspaceId) {
        traceRepository.deleteByTaskId(taskId);
        return categoryResolver.prepare(organization, workspaceId);
    }

    public Allocation resolve(InPhysicalEquipment equipment, Long taskId, String organization,
                              Long workspaceId, long workspaceCount, ItemCategoryResolver.Lookup categories) {
        var reference = referenceResolver.resolve(equipment.getModel(), equipment.getType(),
                organization, workspaceId, workspaceCount);
        var category = categories.resolve(reference.reference(), equipment.getType());
        double fraction = effectiveFraction(equipment, category.isDevice());
        var trace = PhysicalEquipmentEvaluationTrace.builder()
                .taskId(taskId)
                .sourceEquipmentId(equipment.getId())
                .itemType(category.itemType())
                .itemCategory(category.itemCategory())
                .reference(reference.reference())
                .usageContext(equipment.getUsageContext())
                .usageFraction(equipment.getUsageFraction())
                .effectiveFraction(fraction)
                .originalQuantity(equipment.getQuantity())
                .build();
        return new Allocation(reference, trace);
    }

    public void save(List<Allocation> allocations) {
        traceRepository.saveAll(allocations.stream().map(Allocation::trace).toList());
    }

    private double effectiveFraction(InPhysicalEquipment equipment, boolean device) {
        if (!device) {
            return 1d;
        }
        String context = equipment.getUsageContext();
        if (context != null && !context.isBlank() && !"BYOD".equals(context) && !"COPE".equals(context)) {
            throw new IllegalArgumentException("Invalid usage context for evaluated device");
        }
        String fraction = equipment.getUsageFraction();
        if (fraction != null && !fraction.isBlank()) {
            final double value;
            try {
                value = Double.parseDouble(fraction);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid usage fraction for evaluated device");
            }
            if (!Double.isFinite(value) || value < 0d || value > 1d) {
                throw new IllegalArgumentException("Invalid usage fraction for evaluated device");
            }
            return value;
        }
        if ("BYOD".equals(context)) {
            return 0.57d;
        }
        if ("COPE".equals(context)) {
            return 0.85d;
        }
        return 1d;
    }

    public record Allocation(PhysicalEquipmentReferenceResolver.Reference reference,
                             PhysicalEquipmentEvaluationTrace trace) {
    }
}
