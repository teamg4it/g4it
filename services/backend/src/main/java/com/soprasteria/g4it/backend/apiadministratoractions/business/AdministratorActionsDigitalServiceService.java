/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiadministratoractions.business;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.*;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apiinout.modeldb.InDatacenter;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InDatacenterRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.common.utils.InfrastructureType;
import com.soprasteria.g4it.backend.common.utils.StringUtils;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static com.soprasteria.g4it.backend.common.utils.DoubleUtils.toDouble;

@Service
@AllArgsConstructor
@Slf4j
public class AdministratorActionsDigitalServiceService {

    @Autowired
    InDatacenterRepository inDatacenterRepository;
    @Autowired
    InPhysicalEquipmentRepository inPhysicalEquipmentRepository;
    @Autowired
    InVirtualEquipmentRepository inVirtualEquipmentRepository;
    @Autowired
    DigitalServiceRepository digitalServiceRepository;

    @Transactional
    public void migrateDigitalService(String digitalServiceUid) {
        log.info("Migrating digital-service {} into new table", digitalServiceUid);

        DigitalService digitalService = digitalServiceRepository.findById(digitalServiceUid).orElseThrow();
        migrateTerminal(digitalService);
        migrateNetwork(digitalService);
        migrateDataCenterDigitalService(digitalService);
        migrateServer(digitalService);
        digitalService.setIsNewArch(true);
        digitalService.setIsMigrated(true);
        digitalServiceRepository.save(digitalService);
    }


    public void migrateDataCenterDigitalService(DigitalService digitalService) {

        for (DatacenterDigitalService datacenterDigitalService : digitalService.getDatacenterDigitalServices()) {

            InDatacenter inDatacenter = new InDatacenter();

            String name = datacenterDigitalService.getName();
            if (!"Default DC".equals(name)) {
                name += "|" + datacenterDigitalService.getUid();
            }

            inDatacenter.setName(name);
            inDatacenter.setCreationDate(datacenterDigitalService.getCreationDate());
            inDatacenter.setPue(datacenterDigitalService.getPue().doubleValue());
            inDatacenter.setFullName(datacenterDigitalService.getName());
            inDatacenter.setLastUpdateDate(datacenterDigitalService.getLastUpdateDate());
            inDatacenter.setDigitalServiceUid(digitalService.getUid());
            inDatacenter.setLocation(datacenterDigitalService.getLocation());

            inDatacenterRepository.save(inDatacenter);
        }
    }

    public void migrateTerminal(DigitalService digitalService) {

        for (Terminal terminal : digitalService.getTerminals()) {

            InPhysicalEquipment inPhysicalEquipment = new InPhysicalEquipment();

            inPhysicalEquipment.setDigitalServiceUid(digitalService.getUid());
            inPhysicalEquipment.setName(terminal.getUid());
            inPhysicalEquipment.setType("Terminal");
            inPhysicalEquipment.setLocation(terminal.getCountry());
            inPhysicalEquipment.setCreationDate(terminal.getCreationDate());
            inPhysicalEquipment.setLastUpdateDate(terminal.getLastUpdateDate());
            inPhysicalEquipment.setQuantity((terminal.getNumberOfUsers() * terminal.getYearlyUsageTimePerUser()) / (365 * 24));
            inPhysicalEquipment.setModel(terminal.getDeviceType().getReference());
            inPhysicalEquipment.setDurationHour(terminal.getYearlyUsageTimePerUser().longValue());

            LocalDate startDate = LocalDate.of(2020, 1, 1);
            inPhysicalEquipment.setDatePurchase(startDate);
            inPhysicalEquipment.setDateWithdrawal(startDate.plusDays((long) (terminal.getLifespan() * 365)));

            inPhysicalEquipmentRepository.save(inPhysicalEquipment);
        }
    }

    public void migrateNetwork(DigitalService digitalService) {

        for (Network network : digitalService.getNetworks()) {

            InPhysicalEquipment inPhysicalEquipment = new InPhysicalEquipment();

            inPhysicalEquipment.setDigitalServiceUid(digitalService.getUid());
            inPhysicalEquipment.setName(network.getUid());
            inPhysicalEquipment.setType("Network");
            inPhysicalEquipment.setModel(network.getNetworkType().getReference());
            inPhysicalEquipment.setCreationDate(network.getCreationDate());
            inPhysicalEquipment.setLastUpdateDate(network.getLastUpdateDate());
            inPhysicalEquipment.setDatePurchase(LocalDate.of(2020, 1, 1));
            inPhysicalEquipment.setDateWithdrawal(LocalDate.of(2021, 1, 1));

            // quantity calculation
            if (network.getYearlyQuantityOfGbExchanged() == null) {
                inPhysicalEquipment.setQuantity(0.0);
            } else if (network.getNetworkType().getType().equals("Mobile")) {
                inPhysicalEquipment.setQuantity(network.getYearlyQuantityOfGbExchanged());
            } else if (network.getNetworkType().getAnnualQuantityOfGo() != null && network.getNetworkType().getAnnualQuantityOfGo() > 0) {
                inPhysicalEquipment.setQuantity(network.getYearlyQuantityOfGbExchanged() / network.getNetworkType().getAnnualQuantityOfGo());
            } else {
                inPhysicalEquipment.setQuantity(0.0);
            }

            inPhysicalEquipmentRepository.save(inPhysicalEquipment);
        }
    }

    public void migrateServer(DigitalService digitalService) {

        String digitalServiceId = digitalService.getUid();

        List<String> serverNames = digitalService.getServers().stream().map(Server::getName).toList();
        List<String> lstDuplicateServers = serverNames.stream().filter(i -> Collections.frequency(serverNames, i) > 1).toList();
        int count = 1;

        for (Server server : digitalService.getServers()) {
            if (!lstDuplicateServers.isEmpty() && lstDuplicateServers.contains(server.getName())) {
                server.setName(server.getName() + "_" + count);
                count++;
            }
            InPhysicalEquipment inPhysicalEquipment = new InPhysicalEquipment();
            inPhysicalEquipment.setDigitalServiceUid(digitalServiceId);
            inPhysicalEquipment.setName(server.getName());
            inPhysicalEquipment.setLocation(server.getDatacenterDigitalService().getLocation());
            inPhysicalEquipment.setType(StringUtils.capitalize(server.getMutualizationType()) + " Server");
            inPhysicalEquipment.setModel(server.getServerHost().getReference());
            inPhysicalEquipment.setDescription(server.getServerHost().getDescription());
            inPhysicalEquipment.setDurationHour(8760L);
            inPhysicalEquipment.setElectricityConsumption(toDouble(server.getAnnualElectricityConsumption()));
            inPhysicalEquipment.setDurationHour(Long.valueOf(server.getAnnualOperatingTime()));
            inPhysicalEquipment.setCreationDate(server.getCreationDate());
            inPhysicalEquipment.setLastUpdateDate(server.getLastUpdateDate());
            LocalDate startDate = LocalDate.of(2020, 1, 1);
            inPhysicalEquipment.setDatePurchase(startDate);
            inPhysicalEquipment.setDateWithdrawal(startDate.plusDays((long) (server.getLifespan() * 365)));

            if (server.getMutualizationType().equals("DEDICATED")) {
                inPhysicalEquipment.setQuantity(toDouble(server.getQuantity()) * (server.getAnnualOperatingTime().doubleValue() / 8760));
            } else {
                inPhysicalEquipment.setQuantity(1d);
            }

            String datacenterName = server.getDatacenterDigitalService().getName();
            if (!"Default DC".equals(datacenterName)) {
                datacenterName += "|" + server.getDatacenterDigitalService().getUid();
            }
            inPhysicalEquipment.setDatacenterName(datacenterName);

            var charc = server.getServerCharacteristic();
            if (charc.getType().equals("Disk")) {
                inPhysicalEquipment.setSizeDiskGb(server.getServerCharacteristic().getCharacteristicValue().doubleValue());
            } else {
                inPhysicalEquipment.setCpuCoreNumber(server.getServerCharacteristic().getCharacteristicValue().doubleValue());
            }
            inPhysicalEquipmentRepository.save(inPhysicalEquipment);

            List<InVirtualEquipment> virtualEquipments = server.getVirtualEquipmentDigitalServices().stream()
                    .map(virtualEquipmentDigitalService -> {
                        InVirtualEquipment inVirtualEquipment = new InVirtualEquipment();

                        inVirtualEquipment.setName(virtualEquipmentDigitalService.getName());
                        inVirtualEquipment.setDigitalServiceUid(digitalServiceId);
                        inVirtualEquipment.setPhysicalEquipmentName(server.getName());
                        inVirtualEquipment.setDurationHour(virtualEquipmentDigitalService.getAnnualUsageTime().doubleValue());
                        inVirtualEquipment.setCreationDate(virtualEquipmentDigitalService.getCreationDate());
                        inVirtualEquipment.setLastUpdateDate(virtualEquipmentDigitalService.getLastUpdateDate());
                        inVirtualEquipment.setQuantity(virtualEquipmentDigitalService.getQuantity().doubleValue());
                        inVirtualEquipment.setInfrastructureType(InfrastructureType.NON_CLOUD_SERVERS.name());

                        String characType = virtualEquipmentDigitalService.getVirtualEquipmentCharacteristic().getType();
                        double characValue = virtualEquipmentDigitalService.getVirtualEquipmentCharacteristic().getCharacteristicValue().doubleValue();
                        if ("vcpu".equalsIgnoreCase(characType)) {
                            inVirtualEquipment.setVcpuCoreNumber(characValue);
                            inVirtualEquipment.setAllocationFactor((characValue / server.getServerCharacteristic().getCharacteristicValue().doubleValue()) * (inVirtualEquipment.getDurationHour() / 8760d) * inVirtualEquipment.getQuantity());
                        } else {
                            inVirtualEquipment.setSizeDiskGb(characValue);
                            inVirtualEquipment.setAllocationFactor((characValue / server.getServerCharacteristic().getCharacteristicValue().doubleValue()) * (inVirtualEquipment.getDurationHour() / 8760d) * inVirtualEquipment.getQuantity());
                        }
                        log.info("AllocationFactor: {} ", inVirtualEquipment.getAllocationFactor());
                        return inVirtualEquipment;
                    })
                    .toList();
            inVirtualEquipmentRepository.saveAll(virtualEquipments);
        }
    }
}
