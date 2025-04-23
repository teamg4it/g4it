/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apievaluating.mapper;

import com.soprasteria.g4it.backend.apievaluating.model.AggValuesBO;
import com.soprasteria.g4it.backend.apievaluating.model.EvaluateReportBO;
import com.soprasteria.g4it.backend.apievaluating.model.ImpactBO;
import com.soprasteria.g4it.backend.apievaluating.model.RefShortcutBO;
import com.soprasteria.g4it.backend.apievaluating.utils.HostUtils;
import com.soprasteria.g4it.backend.apiindicator.utils.LifecycleStepUtils;
import com.soprasteria.g4it.backend.apiinout.modeldb.*;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.external.boavizta.business.BoaviztapiService;
import org.apache.commons.lang3.tuple.Pair;
import org.mapstruct.Mapper;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactApplication;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementPhysique;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementVirtuel;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.soprasteria.g4it.backend.common.utils.CsvUtils.print;
import static com.soprasteria.g4it.backend.common.utils.InfrastructureType.CLOUD_SERVICES;

@Mapper(componentModel = "spring")
public interface AggregationToOutput {

    default List<String> keyPhysicalEquipment(InPhysicalEquipment physicalEquipment,
                                              InDatacenter datacenter,
                                              ImpactEquipementPhysique impact,
                                              RefShortcutBO refShortcutBO, boolean isDigitalService) {

        String hostingEfficiency = null;
        if (datacenter != null && datacenter.getPue() != null) {
            Integer quartile = refShortcutBO.elecMixQuartiles().get(Pair.of(
                    physicalEquipment.getLocation(),
                    impact.getCritere()
            ));
            hostingEfficiency = quartile == null ? "Bad" : HostUtils.buildHostingEfficiency(quartile, datacenter.getPue());
        }
        String numberOfUsers = Objects.toString(physicalEquipment.getNumberOfUsers(), "0.0");
        return Arrays.asList(
                isDigitalService ? physicalEquipment.getName() : "",
                physicalEquipment.getType(),
                refShortcutBO.criterionMap().get(impact.getCritere()),
                refShortcutBO.lifecycleStepMap().get(LifecycleStepUtils.get(impact.getEtapeACV())),
                physicalEquipment.getLocation(),
                impact.getStatutIndicateur(),
                impact.getReference(),
                numberOfUsers,
                hostingEfficiency,
                String.join(";", physicalEquipment.getCommonFilters() == null ? List.of("") : physicalEquipment.getCommonFilters()),
                String.join(";", physicalEquipment.getFilters() == null ? List.of("") : physicalEquipment.getFilters())
        );
    }

    /**
     * From aggregated data to Output objects
     */
    default OutPhysicalEquipment mapPhysicalEquipment(List<String> key, AggValuesBO agg, Long taskId, RefShortcutBO refShortcutBO) {
        String[] values = key.toArray(new String[0]);

        String criterion = refShortcutBO.criterionMap().inverse().get(values[2]);

        return OutPhysicalEquipment.builder()
                .taskId(taskId)
                .name(values[0])
                .equipmentType(values[1])
                .criterion(criterion) // values[2] set above
                .lifecycleStep(refShortcutBO.lifecycleStepMap().inverse().get(values[3]))
                .location(values[4])
                .statusIndicator(values[5])
                .reference(values[6])
                .numberOfUsers("null".equals(values[7]) || values[7] == null ? 0.0 : Double.parseDouble(values[7]))
                .hostingEfficiency("null".equals(values[8]) ? null : values[8])
                .commonFilters(Arrays.asList(values[9].split(";")))
                .filters(Arrays.asList(values[10].split(";")))
                .countValue(agg.getCountValue())
                .quantity(agg.getQuantity())
                .lifespan(agg.getLifespan())
                .electricityConsumption(agg.getElectricityConsumption())
                .unitImpact(agg.getUnitImpact())
                .peopleEqImpact(agg.getPeopleEqImpact())
                .unit(refShortcutBO.unitMap().get(criterion))
                .errors(agg.getErrors().isEmpty() ? null : agg.getErrors())
                .build();
    }

    default List<String> keyVirtualEquipment(InPhysicalEquipment physicalEquipment, InVirtualEquipment virtualEquipment,
                                             ImpactEquipementVirtuel impact, RefShortcutBO refShortcutBO, EvaluateReportBO evaluateReportBO) {

        String equipmentName = physicalEquipment == null ? "" : physicalEquipment.getName();
        String equipmentType = physicalEquipment == null ? "" : physicalEquipment.getType();
        String location = physicalEquipment == null ? null : physicalEquipment.getLocation();
        if (location == null) location = virtualEquipment.getLocation();

        List<String> physicalEquipmentFilters = List.of();
        if (physicalEquipment != null && physicalEquipment.getFilters() != null) {
            physicalEquipmentFilters = physicalEquipment.getFilters();
        }

        List<String> commonFilters = virtualEquipment.getCommonFilters() == null ? List.of() : virtualEquipment.getCommonFilters();
        List<String> filters = virtualEquipment.getFilters() == null ? List.of() : virtualEquipment.getFilters();

        return Arrays.asList(
                virtualEquipment.getName(),
                equipmentType,
                refShortcutBO.criterionMap().get(impact.getCritere()),
                refShortcutBO.lifecycleStepMap().get(LifecycleStepUtils.get(impact.getEtapeACV(), impact.getEtapeACV())),
                location,
                evaluateReportBO.isDigitalService() ? equipmentName : "",
                impact.getStatutIndicateur(),
                impact.getUnite(),
                print(virtualEquipment.getInfrastructureType()),
                print(virtualEquipment.getInstanceType()),
                print(virtualEquipment.getProvider()),
                String.join(";", physicalEquipmentFilters),
                String.join(";", commonFilters),
                String.join(";", filters)
        );
    }

    default OutVirtualEquipment mapVirtualEquipment(List<String> key, AggValuesBO agg, Long taskId, RefShortcutBO refShortcutBO) {
        String[] values = key.toArray(new String[0]);
        String criterion = refShortcutBO.criterionMap().inverse().get(values[2]);

        return OutVirtualEquipment.builder()
                .taskId(taskId)
                .name(values[0])
                .equipmentType(values[1])
                .criterion(refShortcutBO.criterionMap().inverse().get(values[2]))
                .lifecycleStep(refShortcutBO.lifecycleStepMap().inverse().get(values[3]))
                .location(values[4])
                .physicalEquipmentName(values[5])
                .statusIndicator(values[6])
                .unit(refShortcutBO.unitMap().get(criterion))
                .infrastructureType(values[8])
                .instanceType(values[9])
                .provider(values[10])
                .filtersPhysicalEquipment(Arrays.asList(values[11].split(";")))
                .commonFilters(Arrays.asList(values[12].split(";")))
                .filters(Arrays.asList(values[13].split(";")))
                .countValue(agg.getCountValue())
                .quantity(agg.getQuantity())
                .electricityConsumption(agg.getElectricityConsumption())
                .unitImpact(agg.getUnitImpact())
                .peopleEqImpact(agg.getPeopleEqImpact())
                .usageDuration(agg.getUsageDuration())
                .workload(agg.getQuantity() == 0 ? 0 : agg.getWorkload() / agg.getQuantity())
                .errors(agg.getErrors().isEmpty() ? null : agg.getErrors())
                .build();
    }

    default List<String> keyApplication(InPhysicalEquipment physicalEquipment, InVirtualEquipment virtualEquipment, InApplication application,
                                        ImpactApplication impact, RefShortcutBO refShortcutBO) {

        String equipmentType;
        if (CLOUD_SERVICES.name().equals(virtualEquipment.getInfrastructureType())) {
            equipmentType = "Cloud " + virtualEquipment.getProvider().toUpperCase();
        } else {
            equipmentType = physicalEquipment != null ? physicalEquipment.getType() : "";
        }

        String location = physicalEquipment == null ? "" : physicalEquipment.getLocation();
        List<String> physicalEquipmentFilters = physicalEquipment == null ? List.of() : physicalEquipment.getFilters();
        List<String> virtualEquipmentFilters = virtualEquipment.getFilters() == null ? List.of() : virtualEquipment.getFilters();
        List<String> commonFilters = application.getCommonFilters() == null ? List.of() : application.getCommonFilters();
        List<String> filters = application.getFilters() == null ? List.of() : application.getFilters();

        return Arrays.asList(
                application.getName(),
                virtualEquipment.getName(),
                equipmentType,
                refShortcutBO.criterionMap().get(impact.getCritere()),
                refShortcutBO.lifecycleStepMap().get(LifecycleStepUtils.get(impact.getEtapeACV(), impact.getEtapeACV())),
                location,
                impact.getStatutIndicateur(),
                impact.getUnite(),
                application.getEnvironment(),
                virtualEquipment.getProvider(),
                String.join(";", physicalEquipmentFilters),
                String.join(";", virtualEquipmentFilters),
                String.join(";", commonFilters),
                String.join(";", filters)
        );
    }

    default OutApplication mapApplication(List<String> key, AggValuesBO agg, Long taskId, RefShortcutBO refShortcutBO) {
        String[] values = key.toArray(new String[0]);
        String criterion = refShortcutBO.criterionMap().inverse().get(values[3]);

        return OutApplication.builder()
                .taskId(taskId)
                .name(values[0])
                .virtualEquipmentName(values[1])
                .equipmentType(values[2])
                .criterion(refShortcutBO.criterionMap().inverse().get(values[3]))
                .lifecycleStep(refShortcutBO.lifecycleStepMap().inverse().get(values[4]))
                .location(values[5])
                .statusIndicator(values[6])
                .unit(values[7])
                .environment(values[8])
                .provider("null".equals(values[9]) ? null : values[9])
                .filtersPhysicalEquipment(Arrays.asList(values[10].split(";")))
                .filtersVirtualEquipment(Arrays.asList(values[11].split(";")))
                .commonFilters(Arrays.asList(values[12].split(";")))
                .filters(Arrays.stream(values[13].split(";")).map(String::trim).toList())
                .countValue(agg.getCountValue())
                .quantity(agg.getQuantity())
                .electricityConsumption(agg.getElectricityConsumption())
                .unit(refShortcutBO.unitMap().get(criterion))
                .unitImpact(agg.getUnitImpact())
                .peopleEqImpact(agg.getPeopleEqImpact())
                .errors(agg.getErrors().isEmpty() ? null : agg.getErrors())
                .build();
    }


    default List<String> keyCloudVirtualEquipment(InVirtualEquipment virtualEquipment, ImpactBO impactBO) {
        return Arrays.asList(
                impactBO.getCriterion(),
                impactBO.getLifecycleStep(),
                virtualEquipment.getName(),
                virtualEquipment.getLocation(),
                impactBO.getIndicatorStatus(),
                impactBO.getUnit(),
                virtualEquipment.getInfrastructureType(),
                virtualEquipment.getInstanceType(),
                virtualEquipment.getProvider()
        );
    }

    default OutVirtualEquipment mapCloudVirtualEquipment(List<String> key, AggValuesBO agg, Long taskId) {
        String[] values = key.toArray(new String[0]);

        return OutVirtualEquipment.builder()
                .taskId(taskId)
                .type(Constants.CLOUD_TYPE)
                .criterion(values[0])
                .lifecycleStep(values[1])
                .name(values[2])
                .location(values[3])
                .statusIndicator(values[4])
                .unit(values[5])
                .infrastructureType(values[6])
                .instanceType(values[7])
                .provider(values[8])
                .countValue(agg.getCountValue())
                .quantity(agg.getQuantity())
                .electricityConsumption(agg.getElectricityConsumption())
                .unitImpact(agg.getUnitImpact())
                .peopleEqImpact(agg.getPeopleEqImpact())
                .usageDuration(agg.getUsageDuration())
                .workload(agg.getQuantity() == 0 ? 0 : agg.getWorkload() / agg.getQuantity())
                .errors(agg.getErrors().isEmpty() ? null : agg.getErrors())
                .engineName(BoaviztapiService.BOAVIZTAPI_ENGINE)
                .engineVersion(BoaviztapiService.BOAVIZTAPI_VERSION)
                .referentialVersion(Constants.REFERENTIAL_VERSION_CLOUD)
                .build();
    }

}
