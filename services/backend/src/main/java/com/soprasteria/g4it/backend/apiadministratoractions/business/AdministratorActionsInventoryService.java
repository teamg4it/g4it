/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiadministratoractions.business;

import com.google.common.base.Strings;
import com.soprasteria.g4it.backend.apiinout.modeldb.InApplication;
import com.soprasteria.g4it.backend.apiinout.modeldb.InDatacenter;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InApplicationRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InDatacenterRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.PhysicalEquipment;
import com.soprasteria.g4it.backend.apiinventory.repository.*;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.InfrastructureType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static com.soprasteria.g4it.backend.common.utils.CsvUtils.print;
import static com.soprasteria.g4it.backend.common.utils.DoubleUtils.toDouble;

@Service
@AllArgsConstructor
@Slf4j
public class AdministratorActionsInventoryService {

    @Autowired
    InventoryRepository inventoryRepository;

    @Autowired
    DataCenterRepository dataCenterRepository;

    @Autowired
    InDatacenterRepository inDatacenterRepository;

    @Autowired
    PhysicalEquipmentRepository physicalEquipmentRepository;

    @Autowired
    InPhysicalEquipmentRepository inPhysicalEquipmentRepository;

    @Autowired
    VirtualEquipmentRepository virtualEquipmentRepository;

    @Autowired
    InVirtualEquipmentRepository inVirtualEquipmentRepository;

    @Autowired
    ApplicationRepository applicationRepository;

    @Autowired
    InApplicationRepository inApplicationRepository;

    public void migrateInventoriesDataToNewFormat(Long inventoryId) {
        this.migrateDataCenter(inventoryId);
        this.migratePhysicalEquipment(inventoryId);
        this.migrateVirtualEquipment(inventoryId);
        this.migrateApplication(inventoryId);
    }

    public void migrateDataCenter(Long inventoryId) {

        List<InDatacenter> inDatacenters = dataCenterRepository.findByInventoryId(inventoryId).stream()
                .map(dataCenter -> {
                    InDatacenter inDatacenter = new InDatacenter();

                    inDatacenter.setName(dataCenter.getNomCourtDatacenter());
                    inDatacenter.setCreationDate(dataCenter.getCreationDate());
                    inDatacenter.setPue(toDouble(dataCenter.getPue(), 2d));
                    inDatacenter.setFullName(dataCenter.getNomLongDatacenter());
                    inDatacenter.setLastUpdateDate(dataCenter.getLastUpdateDate());
                    inDatacenter.setInventoryId(inventoryId);
                    inDatacenter.setLocation(dataCenter.getLocalisation());
                    return inDatacenter;
                })
                .toList();

        inDatacenterRepository.saveAll(inDatacenters);
    }

    public void migratePhysicalEquipment(Long inventoryId) {
        int pageNumber = 0;
        while (true) {
            Pageable page = PageRequest.of(pageNumber, Constants.BATCH_SIZE, Sort.by("nomEquipementPhysique"));
            final List<PhysicalEquipment> physicalEquipments = physicalEquipmentRepository.findByInventoryId(inventoryId, page);

            if (physicalEquipments.isEmpty()) {
                break;
            }
            List<InPhysicalEquipment> inPhysicalEquipments = physicalEquipments.stream().map(physicalEquipment -> {
                        InPhysicalEquipment inPhysicalEquipment = new InPhysicalEquipment();

                        inPhysicalEquipment.setName(physicalEquipment.getNomEquipementPhysique());
                        inPhysicalEquipment.setInventoryId(inventoryId);
                        inPhysicalEquipment.setDatacenterName(physicalEquipment.getNomCourtDatacenter());
                        inPhysicalEquipment.setLocation(physicalEquipment.getPaysDUtilisation());
                        inPhysicalEquipment.setQuantity(toDouble(physicalEquipment.getQuantite()));
                        inPhysicalEquipment.setType(physicalEquipment.getType());
                        inPhysicalEquipment.setModel(physicalEquipment.getModele());
                        inPhysicalEquipment.setManufacturer(physicalEquipment.getFabricant());
                        inPhysicalEquipment.setDatePurchase((Strings.isNullOrEmpty(physicalEquipment.getDateAchat())) ? null : LocalDate.parse(physicalEquipment.getDateAchat()));
                        inPhysicalEquipment.setDateWithdrawal((Strings.isNullOrEmpty(physicalEquipment.getDateRetrait())) ? null : LocalDate.parse(physicalEquipment.getDateRetrait()));
                        inPhysicalEquipment.setCpuType(physicalEquipment.getTypeDeProcesseur());
                        inPhysicalEquipment.setCpuCoreNumber(toDouble(physicalEquipment.getNbCoeur()));
                        inPhysicalEquipment.setSizeDiskGb(toDouble(physicalEquipment.getTailleDuDisque()));
                        inPhysicalEquipment.setSizeMemoryGb(toDouble(physicalEquipment.getTailleMemoire()));
                        inPhysicalEquipment.setSource(physicalEquipment.getNomSourceDonnee());
                        inPhysicalEquipment.setElectricityConsumption(toDouble(physicalEquipment.getConsoElecAnnuelle()));
                        inPhysicalEquipment.setCreationDate(physicalEquipment.getCreationDate());
                        inPhysicalEquipment.setLastUpdateDate(physicalEquipment.getLastUpdateDate());
                        inPhysicalEquipment.setCommonFilters(List.of(print(physicalEquipment.getNomEntite())));
                        inPhysicalEquipment.setFilters(List.of(print(physicalEquipment.getStatut())));

                        return inPhysicalEquipment;

                    })
                    .toList();

            inPhysicalEquipmentRepository.saveAll(inPhysicalEquipments);
            pageNumber++;
        }
    }

    public void migrateVirtualEquipment(Long inventoryId) {
        List<InVirtualEquipment> inVirtualEquipments = virtualEquipmentRepository.findByInventoryId(inventoryId).stream()
                .map(virtualEquipment -> {
                    InVirtualEquipment inVirtualEquipment = new InVirtualEquipment();

                    inVirtualEquipment.setName(virtualEquipment.getNomEquipementVirtuel());
                    inVirtualEquipment.setInventoryId(inventoryId);
                    inVirtualEquipment.setPhysicalEquipmentName(virtualEquipment.getNomEquipementPhysique());
                    inVirtualEquipment.setType(virtualEquipment.getTypeEqv());
                    inVirtualEquipment.setElectricityConsumption(toDouble(virtualEquipment.getConsoElecAn()));
                    inVirtualEquipment.setSizeMemoryGb(toDouble(virtualEquipment.getCapaciteStockage()));
                    inVirtualEquipment.setVcpuCoreNumber(toDouble(virtualEquipment.getVCPU()));
                    inVirtualEquipment.setAllocationFactor(toDouble(virtualEquipment.getCleRepartition()));
                    inVirtualEquipment.setCreationDate(virtualEquipment.getCreationDate());
                    inVirtualEquipment.setLastUpdateDate(virtualEquipment.getLastUpdateDate());
                    inVirtualEquipment.setInfrastructureType(InfrastructureType.NON_CLOUD_SERVERS.name());
                    inVirtualEquipment.setQuantity(1d);
                    inVirtualEquipment.setFilters(List.of(print(virtualEquipment.getCluster())));
                    inVirtualEquipment.setCommonFilters(List.of(print(virtualEquipment.getNomEntite())));

                    return inVirtualEquipment;
                })
                .toList();

        inVirtualEquipmentRepository.saveAll(inVirtualEquipments);
    }

    public void migrateApplication(Long inventoryId) {
        List<InApplication> inApplications = applicationRepository.findByInventoryId(inventoryId).stream()
                .map(application -> {
                    InApplication inApplication = new InApplication();

                    inApplication.setId(application.getId());
                    inApplication.setName(application.getNomApplication());
                    inApplication.setInventoryId(inventoryId);
                    inApplication.setPhysicalEquipmentName(application.getNomEquipementPhysique());
                    inApplication.setVirtualEquipmentName(application.getNomEquipementVirtuel());
                    inApplication.setEnvironment(application.getTypeEnvironnement());
                    inApplication.setCreationDate(application.getCreationDate());
                    inApplication.setLastUpdateDate(application.getLastUpdateDate());
                    inApplication.setFilters(List.of(print(application.getDomaine()), print(application.getSousDomaine())));
                    inApplication.setCommonFilters(List.of(print(application.getNomEntite())));

                    return inApplication;
                })
                .toList();

        inApplicationRepository.saveAll(inApplications);

    }
}
