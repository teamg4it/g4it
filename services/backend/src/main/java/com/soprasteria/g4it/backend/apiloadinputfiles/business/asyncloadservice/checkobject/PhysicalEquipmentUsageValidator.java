package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkobject;

import com.soprasteria.g4it.backend.apireferential.business.ItemCategoryResolver;
import com.soprasteria.g4it.backend.apireferential.business.PhysicalEquipmentReferenceResolver;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import lombok.RequiredArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

/** Device-only validation of raw CSV usage values, without mutating source data. */
@Service
@RequiredArgsConstructor
public class PhysicalEquipmentUsageValidator {
    private final ItemCategoryResolver categoryResolver;
    private final PhysicalEquipmentReferenceResolver referenceResolver;
    private final MessageSource messageSource;

    public List<LineError> check(Context context, InPhysicalEquipmentRest equipment, String filename, int line) {
        String usageContext = raw(equipment.getUsageContext());
        String fraction = raw(equipment.getUsageFraction());
        if (context.getInventoryId() == null || context.getDigitalServiceVersionUid() != null
                || (empty(usageContext) && empty(fraction))) return List.of();

        var reference = referenceResolver.resolve(equipment.getModel(), equipment.getType(),
                context.getOrganization(), context.getWorkspaceId(), context.getWorkspaceId() == null ? 0 : 1);
        var category = categoryResolver.prepare(context.getOrganization(), context.getWorkspaceId())
                .resolve(reference.reference(), equipment.getType());
        if (!category.isDevice()) return List.of();

        List<LineError> errors = new ArrayList<>();
        if (!empty(usageContext) && !"BYOD".equals(usageContext) && !"COPE".equals(usageContext)) {
            errors.add(error(context, filename, line, "physical.eqp.usage.context.invalid"));
        }
        if (!empty(fraction) && !validFraction(fraction)) {
            errors.add(error(context, filename, line, "physical.eqp.usage.fraction.invalid"));
        }
        return errors;
    }

    private LineError error(Context context, String filename, int line, String key) {
        return new LineError(filename, line, messageSource.getMessage(key, null, context.getLocale()));
    }

    private static boolean validFraction(String fraction) {
        try {
            BigDecimal value = new BigDecimal(fraction.trim());
            return value.signum() >= 0 && value.compareTo(BigDecimal.ONE) <= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean empty(String value) {
        return value == null || value.isEmpty();
    }

    private static String raw(JsonNullable<String> value) {
        return value == null ? null : value.orElse(null);
    }
}
