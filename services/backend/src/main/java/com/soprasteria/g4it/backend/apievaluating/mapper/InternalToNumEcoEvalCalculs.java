/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apievaluating.mapper;

import com.soprasteria.g4it.backend.apiinout.modeldb.InApplication;
import com.soprasteria.g4it.backend.apiinout.modeldb.InDatacenter;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.server.gen.api.dto.*;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mte.numecoeval.calculs.domain.data.entree.Application;
import org.mte.numecoeval.calculs.domain.data.entree.DataCenter;
import org.mte.numecoeval.calculs.domain.data.entree.EquipementPhysique;
import org.mte.numecoeval.calculs.domain.data.entree.EquipementVirtuel;
import org.mte.numecoeval.calculs.domain.data.referentiel.*;

@Mapper(componentModel = "spring")
public interface InternalToNumEcoEvalCalculs {

    /**
     * From G4IT internal object model to NumEcoEval calculs object
     */
    @Mapping(target = "nomEquipementPhysique", source = "name")
    @Mapping(target = "modele", source = "model")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "consoElecAnnuelle", source = "electricityConsumption")
    @Mapping(target = "paysDUtilisation", source = "location")
    @Mapping(target = "dateAchat", source = "datePurchase")
    @Mapping(target = "dateRetrait", source = "dateWithdrawal")
    @Mapping(target = "quantite", source = "quantity")
    @Mapping(target = "nbCoeur", source = "cpuCoreNumber")
    EquipementPhysique map(InPhysicalEquipment inPhysicalEquipment);

    @AfterMapping
    default void ensureDataCenter(
            @MappingTarget EquipementPhysique equipementPhysique,
            InPhysicalEquipment source) {

        if (equipementPhysique.getDataCenter() == null) {
            equipementPhysique.setDataCenter(DataCenter.builder().build());
        }
    }


    @Mapping(target = "localisation", source = "location")
    @Mapping(target = "pue", source = "pue")
    DataCenter map(InDatacenter datacenter);

    @Mapping(target = "unite", source = "unit")
    @Mapping(target = "nomCritere", source = "code")
    ReferentielCritere map(CriterionRest criterionRest);

    @Mapping(target = "categorie", source = "category")
    @Mapping(target = "serveur", source = "isServer")
    @Mapping(target = "dureeVieDefaut", source = "defaultLifespan")
    @Mapping(target = "refItemParDefaut", source = "refDefaultItem")
    ReferentielTypeItem map(ItemTypeRest itemTypeRest);

    @Mapping(target = "valeur", source = "value")
    ReferentielHypothese map(HypothesisRest hypothesisRest);

    @Mapping(target = "nom", source = "name")
    @Mapping(target = "etape", expression = "java(com.soprasteria.g4it.backend.apiindicator.utils.LifecycleStepUtils.getReverse(itemImpactRest.getLifecycleStep()))")
    @Mapping(target = "critere", source = "criterion")
    @Mapping(target = "valeur", source = "value")
    @Mapping(target = "categorie", source = "category")
    @Mapping(target = "localisation", source = "location")
    @Mapping(target = "consoElecMoyenne", source = "avgElectricityConsumption")
    ReferentielFacteurCaracterisation map(ItemImpactRest itemImpactRest);

    @Mapping(target = "modeleEquipementSource", source = "itemSource")
    @Mapping(target = "refEquipementCible", source = "refItemTarget")
    ReferentielCorrespondanceRefEquipement map(MatchingItemRest matchingItemRest);


    @Mapping(target = "nomEquipementVirtuel", source = "name")
    @Mapping(target = "nomEquipementPhysique", source = "physicalEquipmentName")
    @Mapping(target = "typeEqv", source = "type")
    @Mapping(target = "consoElecAnnuelle", source = "electricityConsumption")
    @Mapping(target = "vCPU", source = "vcpuCoreNumber")
    @Mapping(target = "capaciteStockage", source = "sizeDiskGb")
    @Mapping(target = "cleRepartition", source = "allocationFactor")
    EquipementVirtuel map(InVirtualEquipment inVirtualEquipment);

    @Mapping(target = "nomApplication", source = "name")
    @Mapping(target = "nomEquipementVirtuel", source = "virtualEquipmentName")
    @Mapping(target = "nomEquipementPhysique", source = "physicalEquipmentName")
    @Mapping(target = "typeEnvironnement", source = "environment")
    Application map(InApplication inApplication);


}
