/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiinout.mapper;

import com.soprasteria.g4it.backend.apiinout.modeldb.InApplication;
import com.soprasteria.g4it.backend.apiinout.modeldb.InDatacenter;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import org.mapstruct.Mapper;

import java.util.List;

import static com.soprasteria.g4it.backend.common.utils.CsvUtils.print;

@Mapper(componentModel = "spring")
public interface InputToCsvRecord {

    default List<String> toCsv(InDatacenter datacenter) {
        return List.of(
                datacenter.getName(),
                print(datacenter.getFullName()),
                print(datacenter.getPue()),
                datacenter.getLocation(),
                "", // entityName
                "" // source
        );
    }

    default List<String> toCsv(InPhysicalEquipment physicalEquipment, InDatacenter datacenter) {

        String datacenterName = datacenter == null ? "" : datacenter.getName();

        String entityName = physicalEquipment.getCommonFilters() == null ? null : physicalEquipment.getCommonFilters().getFirst();
        String status = physicalEquipment.getFilters() == null ? null : physicalEquipment.getFilters().getFirst();

        return List.of(
                physicalEquipment.getName(),
                print(entityName),
                "", // source
                print(physicalEquipment.getModel()),
                print(physicalEquipment.getQuantity()),
                print(physicalEquipment.getType()),
                print(status),
                print(physicalEquipment.getLocation()),
                "", // utilisateur
                print(physicalEquipment.getDatePurchase()),
                print(physicalEquipment.getDateWithdrawal()),
                print(physicalEquipment.getCpuCoreNumber()),
                datacenterName,
                print(physicalEquipment.getElectricityConsumption()),
                print(physicalEquipment.getManufacturer()),
                print(physicalEquipment.getSizeDiskGb()),
                print(physicalEquipment.getCpuType()) // processor type
        );
    }

    default List<String> toCsv(InVirtualEquipment virtualEquipment, String location) {

        String entityName = virtualEquipment.getCommonFilters() == null ? null : virtualEquipment.getCommonFilters().getFirst();
        String cluster = virtualEquipment.getFilters() == null ? null : virtualEquipment.getFilters().getFirst();

        return List.of(
                virtualEquipment.getName(),
                virtualEquipment.getInfrastructureType(),
                print(virtualEquipment.getQuantity()), //quantite
                print(virtualEquipment.getCloudProvider()), //cloudProvider
                print(virtualEquipment.getInstanceType()), //InstanceType
                print(location), //location
                print(virtualEquipment.getWorkload()), //averageWorkload
                print(virtualEquipment.getUsageDuration()), //annualUsageDuration
                print(virtualEquipment.getPhysicalEquipmentName()),
                "", //nomSourceDonneeEquipementPhysique
                print(virtualEquipment.getVcpuCoreNumber()),
                print(entityName),
                print(cluster),
                print(virtualEquipment.getElectricityConsumption()),
                print(virtualEquipment.getType()),
                print(virtualEquipment.getAllocationFactor()),
                "", //nomSourceDonnee
                print(virtualEquipment.getSizeDiskGb())
        );
    }

    default List<String> toCsv(InApplication application) {
        return List.of(
                application.getName(),
                application.getEnvironment(),
                application.getVirtualEquipmentName(),
                "", // source
                application.getFilters().getFirst(), // domain
                application.getFilters().get(1), // sub domain
                application.getCommonFilters().getFirst(), // entityName
                "", // source
                print(application.getPhysicalEquipmentName())
        );
    }

    default List<String> cloudToCsv(InVirtualEquipment virtualEquipment, String digitalServiceName) {
        return List.of(
                digitalServiceName,
                virtualEquipment.getName(),
                virtualEquipment.getProvider(),
                virtualEquipment.getInstanceType(),
                print(virtualEquipment.getQuantity()),
                virtualEquipment.getLocation(),
                print(virtualEquipment.getDurationHour()),
                print(virtualEquipment.getWorkload())
        );
    }

}
