/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apievaluating.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soprasteria.g4it.backend.apievaluating.model.AggValuesBO;
import com.soprasteria.g4it.backend.apievaluating.model.EvaluateReportBO;
import com.soprasteria.g4it.backend.apievaluating.model.ImpactBO;
import com.soprasteria.g4it.backend.apiindicator.utils.CriteriaUtils;
import com.soprasteria.g4it.backend.apiindicator.utils.LifecycleStepUtils;
import com.soprasteria.g4it.backend.apiinout.modeldb.InApplication;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apirecomandation.modeldb.OutAiReco;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.StringUtils;
import com.soprasteria.g4it.backend.external.boavizta.business.BoaviztapiService;
import com.soprasteria.g4it.backend.external.ecomindai.model.RecommendationBO;
import org.mapstruct.Mapper;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactApplication;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementPhysique;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementVirtuel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.soprasteria.g4it.backend.common.utils.CsvUtils.*;

@Mapper(componentModel = "spring")
public interface ImpactToCsvRecord {

    default List<String> toCsv(Context context, Long taskId, String inventoryName, InPhysicalEquipment physicalEquipment,
                               ImpactEquipementPhysique impact, Double sipValue, boolean verbose) {
        LocalDateTime now = context.getDatetime();

        Double peopleEqImpact = impact.getImpactUnitaire() == null ? null : impact.getImpactUnitaire() / sipValue;

        return List.of(
                inventoryName,
                now.format(Constants.LOCAL_DATE_TIME_FORMATTER_MS),
                now.toLocalDate().toString(),
                taskId.toString(),
                impact.getEtapeACV(),
                CriteriaUtils.transformCriteriaKeyToCriteriaName(StringUtils.snakeToKebabCase(impact.getCritere())),
                "", // source
                impact.getStatutIndicateur(),
                verbose ? impact.getTrace() : "",
                "1.0", // calculationVersion
                print(impact.getConsoElecMoyenne()),
                print(impact.getImpactUnitaire()),
                print(impact.getQuantite()),
                printFirst(physicalEquipment.getFilters()), // status
                impact.getTypeEquipement(),
                impact.getUnite(),
                printFirst(physicalEquipment.getCommonFilters()), // entityName
                context.getOrganizationId().toString(), // organizationName (actually organizationId)
                "", // dataSourceName
                physicalEquipment.getName(),
                printFirst(physicalEquipment.getCommonFilters()), // entityName
                taskId.toString(),
                context.getOrganizationId().toString(), // organizationName (actually organizationId)
                "", // dataSourceName
                print(peopleEqImpact) // peopleEqImpact
        );
    }

    default List<String> toCsv(Context context, EvaluateReportBO evaluateReportBO, InVirtualEquipment virtualEquipment,
                               ImpactEquipementVirtuel impact, Double sipValue) {
        LocalDateTime now = context.getDatetime();

        Double peopleEqImpact = impact.getImpactUnitaire() == null ? null : impact.getImpactUnitaire() / sipValue;

        return List.of(
                evaluateReportBO.getName(),
                now.format(Constants.LOCAL_DATE_TIME_FORMATTER_MS),
                now.toLocalDate().toString(),
                evaluateReportBO.getTaskId().toString(),
                LifecycleStepUtils.getReverse(impact.getEtapeACV()),
                CriteriaUtils.transformCriteriaKeyToCriteriaName(StringUtils.snakeToKebabCase(impact.getCritere())),
                context.getOrganizationId().toString(),
                "", // dataSourceName
                print(virtualEquipment.getPhysicalEquipmentName()),
                virtualEquipment.getName(),
                virtualEquipment.getInfrastructureType(),
                print(virtualEquipment.getCloudProvider()),
                print(virtualEquipment.getInstanceType()),
                printFirst(virtualEquipment.getCommonFilters()), // entityName
                "", // source
                impact.getStatutIndicateur(),
                evaluateReportBO.isVerbose() ? print(impact.getTrace()) : "",
                "1.1", // calculationVersion
                print(impact.getImpactUnitaire()),
                print(impact.getUnite()),
                print(impact.getConsoElecMoyenne()),
                printFirst(virtualEquipment.getFilters()), // cluster
                now.toLocalDate().toString(),
                context.getOrganizationId().toString(), // organizationName (actually organizationId)
                printFirst(virtualEquipment.getCommonFilters()), // entityName
                "", // dataSourceName
                print(peopleEqImpact) // peopleEqImpact
        );
    }

    default List<String> toCsv(Context context, EvaluateReportBO evaluateReportBO, InApplication application,
                               ImpactApplication impact, Double sipValue) {
        LocalDateTime now = context.getDatetime();

        Double peopleEqImpact = impact.getImpactUnitaire() == null ? null : impact.getImpactUnitaire() / sipValue;

        return List.of(
                evaluateReportBO.getName(),
                now.format(Constants.LOCAL_DATE_TIME_FORMATTER_MS),
                now.toLocalDate().toString(),
                evaluateReportBO.getTaskId().toString(),
                impact.getEtapeACV(),
                CriteriaUtils.transformCriteriaKeyToCriteriaName(StringUtils.snakeToKebabCase(impact.getCritere())),
                context.getOrganizationId().toString(),
                "", // dataSourceName
                application.getName(),
                application.getEnvironment(),
                print(application.getPhysicalEquipmentName()),
                application.getVirtualEquipmentName(),
                printFirst(application.getCommonFilters()), // entityName
                "", // source
                impact.getStatutIndicateur(),
                evaluateReportBO.isVerbose() ? impact.getTrace() : "",
                "1.1", // calculationVersion
                printFirst(application.getFilters()), // domain
                printSecond(application.getFilters()), // sub domain
                print(impact.getImpactUnitaire()),
                print(impact.getUnite()),
                print(impact.getConsoElecMoyenne()),
                now.toLocalDate().toString(),
                context.getOrganizationId().toString(), // organizationName (actually organizationId)
                printFirst(application.getCommonFilters()), // entityName
                "", // dataSourceName
                print(peopleEqImpact) // peopleEqImpact
        );
    }

    default List<String> cloudImpactToCsv(Context context, Long taskId, InVirtualEquipment virtualEquipment,
                                          AggValuesBO values, ImpactBO impactBO) {

        return List.of(
                context.getDigitalServiceName(),
                context.getDatetime().format(Constants.LOCAL_DATE_TIME_FORMATTER_MS),
                context.getOrganizationId().toString(),
                taskId.toString(),
                impactBO.getLifecycleStep(),
                impactBO.getCriterion(),
                "",
                virtualEquipment.getName(),
                virtualEquipment.getInstanceType(),
                virtualEquipment.getProvider(),
                virtualEquipment.getLocation(),
                BoaviztapiService.BOAVIZTAPI_ENGINE,
                BoaviztapiService.BOAVIZTAPI_VERSION,
                Constants.REFERENTIAL_VERSION_CLOUD,
                impactBO.getIndicatorStatus(),
                print(impactBO.getUnit()),
                print(values.getUnitImpact()),
                print(values.getPeopleEqImpact()),
                print(values.getQuantity()),
                values.getErrors() == null ? "" : String.join(", ", values.getErrors())
        );

    }

    default List<String[]> toCsv(OutAiReco outAiReco) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        RecommendationBO[] recommendationBOS = objectMapper.readValue(outAiReco.getRecommendations(), RecommendationBO[].class);

        List<String[]> result = new ArrayList<>();

        Arrays.stream(recommendationBOS).map(recommendationBO -> new String[]{recommendationBO.getType(), recommendationBO.getTopic(), recommendationBO.getExample(), recommendationBO.getExpectedReduction()}).forEach(result::add);
        return result;
    }
}
