/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.engine.numecoeval;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soprasteria.g4it.backend.apievaluating.mapper.InternalToNumEcoEvalCalculs;
import com.soprasteria.g4it.backend.apievaluating.utils.Constants;
import com.soprasteria.g4it.backend.apiindicator.utils.LifecycleStepUtils;
import com.soprasteria.g4it.backend.apiinout.modeldb.InApplication;
import com.soprasteria.g4it.backend.apiinout.modeldb.InDatacenter;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apireferential.business.ReferentialService;
import com.soprasteria.g4it.backend.server.gen.api.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mte.numecoeval.calculs.domain.data.demande.DemandeCalculImpactApplication;
import org.mte.numecoeval.calculs.domain.data.demande.DemandeCalculImpactEquipementPhysique;
import org.mte.numecoeval.calculs.domain.data.demande.DemandeCalculImpactEquipementVirtuel;
import org.mte.numecoeval.calculs.domain.data.demande.OptionsCalcul;
import org.mte.numecoeval.calculs.domain.data.entree.EquipementPhysique;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactApplication;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementPhysique;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementVirtuel;
import org.mte.numecoeval.calculs.domain.data.referentiel.ReferentielEtapeACV;
import org.mte.numecoeval.calculs.domain.port.input.service.CalculImpactApplicationService;
import org.mte.numecoeval.calculs.domain.port.input.service.CalculImpactEquipementPhysiqueService;
import org.mte.numecoeval.calculs.domain.port.input.service.CalculImpactEquipementVirtuelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EvaluateNumEcoEvalService {

    @Autowired
    InternalToNumEcoEvalCalculs internalToNumEcoEvalCalculs;

    @Autowired
    ReferentialService referentialService;

    @Autowired
    CalculImpactEquipementPhysiqueService calculImpactEquipementPhysiqueService;

    @Autowired
    CalculImpactEquipementVirtuelService calculImpactEquipementVirtuelService;

    @Autowired
    CalculImpactApplicationService calculImpactApplicationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Calculate physical equipment impacts with NumEcoEval library
     *
     * @param physicalEquipment the physicalEquipment
     * @param datacenter        the datacenter
     * @param organization      the organization
     * @param criteria          the criteria
     * @param lifecycleSteps    the lifecycleSteps
     * @param hypotheses        the hypotheses
     * @return the list of impact
     */

    public List<ImpactEquipementPhysique> calculatePhysicalEquipment(final InPhysicalEquipment physicalEquipment,
                                                                     final InDatacenter datacenter,
                                                                     final String organization,
                                                                     List<CriterionRest> criteria,
                                                                     List<String> lifecycleSteps,
                                                                     List<HypothesisRest> hypotheses,Long workspaceId) {

        MatchingItemRest matchingItem = null;
        boolean isModelMatched = true;

        if (physicalEquipment.getModel() != null) {
            matchingItem = referentialService.getMatchingItemForWorkspace(physicalEquipment.getModel(), organization,workspaceId);
        }

        ItemTypeRest itemTypeRest = referentialService.getItemTypeForWorkspace(physicalEquipment.getType(), organization,workspaceId);

        List<ImpactEquipementPhysique> result = new ArrayList<>(criteria.size() * lifecycleSteps.size());
        LocalDateTime now = LocalDateTime.now();

        for (final String lifecycleStep : lifecycleSteps) {
            for (final CriterionRest criterion : criteria) {

                String itemImpactName = null;

                if (matchingItem == null) {
                    if (itemTypeRest.getRefDefaultItem() != null) {
                        itemImpactName = itemTypeRest.getRefDefaultItem();
                        isModelMatched = false;
                    }
                } else {
                    itemImpactName = matchingItem.getRefItemTarget();
                }

                List<ItemImpactRest> itemImpacts = referentialService.getItemImpactsForWorkspace(
                        criterion.getCode(), lifecycleStep, itemImpactName,
                        physicalEquipment.getLocation(), organization,workspaceId);

                ItemImpactRest firstImpact = itemImpacts.stream().findFirst().orElse(null);
                boolean hideValue = true;
                if(firstImpact != null){
                    boolean isNotHidden  = Boolean.FALSE.equals(firstImpact.getIsHidden());
                    hideValue = !isNotHidden;
                }

                EquipementPhysique equipementPhysique = internalToNumEcoEvalCalculs.map(physicalEquipment);
                if (datacenter != null) {
                    equipementPhysique.setDataCenter(internalToNumEcoEvalCalculs.map(datacenter));
                }

                Double pue = datacenter != null ? datacenter.getPue() : null;
                String location = datacenter != null ? datacenter.getLocation() : null;
                equipementPhysique.getDataCenter().setPue(pue);
                equipementPhysique.getDataCenter().setLocalisation(location);

                DemandeCalculImpactEquipementPhysique demandeCalculImpactEquipementPhysique =
                        DemandeCalculImpactEquipementPhysique.builder()
                                .dateCalcul(now)
                                .equipementPhysique(equipementPhysique)
                                .etape(ReferentielEtapeACV.builder().code(LifecycleStepUtils.getReverse(lifecycleStep)).build())
                                .critere(internalToNumEcoEvalCalculs.map(criterion))
                                .typeItem(internalToNumEcoEvalCalculs.map(itemTypeRest))
                                .correspondanceRefEquipement(internalToNumEcoEvalCalculs.map(matchingItem))
                                .hypotheses(hypotheses.stream()
                                        .map(h -> internalToNumEcoEvalCalculs.map(h)).toList())
                                .facteurCaracterisations(itemImpacts.stream()
                                        .map(impact -> internalToNumEcoEvalCalculs.map(impact)).toList())
                                .optionsCalcul(new OptionsCalcul("REEL"))
                                .build();

                ImpactEquipementPhysique impactEquipementPhysique =
                        calculImpactEquipementPhysiqueService.calculerImpactEquipementPhysique(demandeCalculImpactEquipementPhysique);
                String refName = firstImpact != null ? firstImpact.getName() : null;
                updateTraceForImpact(impactEquipementPhysique, lifecycleStep, isModelMatched,refName,hideValue, objectMapper);
                result.add(impactEquipementPhysique);
            }
        }

        return result;
    }

    private void extractAndSetSource(ImpactEquipementPhysique impact,
                                     Map<String, Object> traceMap,
                                     ObjectMapper objectMapper) {

        try {

            if (traceMap.containsKey(Constants.MIX_ELECTRIQUE)) {
                Map<String, Object> mixMap = objectMapper.convertValue(
                        traceMap.get(Constants.MIX_ELECTRIQUE),
                        new TypeReference<>() {}
                );

                Object source = mixMap.get("sourceReferentielMixElectrique");

                if (source != null) {
                    impact.setSource(source.toString());
                    return;
                }
            }

            // 2. FALLBACK → ReferentielFacteurCaracterisation
            if (traceMap.containsKey(Constants.REFERENTIEL_FACTEUR)) {
                Map<String, Object> refMap = objectMapper.convertValue(
                        traceMap.get(Constants.REFERENTIEL_FACTEUR),
                        new TypeReference<>() {}
                );

                Object source = refMap.get(Constants.SOURCE);

                if (source != null) {
                    impact.setSource(source.toString());
                }
            }

        } catch (Exception e) {
            log.warn("Failed to extract source from trace", e);
        }
    }

    /**
     * Update trace information
     *
     * @param impactEquipementPhysique the impactEquipementPhysique
     * @param isModelMatched           is MODELE or TYPE
     * @param objectMapper             the objectMapper
     */
    private void updateTraceForImpact(ImpactEquipementPhysique impactEquipementPhysique,
                                      String lifecycleStep,
                                      boolean isModelMatched,
                                      String refName,
                                      boolean hideValue,
                                      ObjectMapper objectMapper) {

        String trace = impactEquipementPhysique.getTrace();
        if (StringUtils.isEmpty(trace) || trace.contains("erreur")) {
            return;
        }
        try {
            Map<String, Object> traceMap = objectMapper.readValue(trace, new TypeReference<>() {});

            boolean modified = handleUsingStage(traceMap, lifecycleStep, refName, hideValue, objectMapper)
                    || handleReferenceFactor(traceMap, isModelMatched, refName, hideValue);

            if (modified) {
                impactEquipementPhysique.setTrace(objectMapper.writeValueAsString(traceMap));
            }
            extractAndSetSource(impactEquipementPhysique, traceMap, objectMapper);
        } catch (Exception e) {
            log.warn("Failed to update trace for impact equipment: {}", impactEquipementPhysique, e);
        }
    }

    private boolean handleUsingStage(Map<String, Object> traceMap,
                                     String lifecycleStep,
                                     String refName,
                                     boolean hideValue,
                                     ObjectMapper objectMapper) {

        if (!Constants.USING.equalsIgnoreCase(lifecycleStep)) {
            return false;
        }

        boolean modified = false;

        if (traceMap.containsKey(Constants.CONSO_ELEC)) {

            Map<String, Object> consoMap = objectMapper.convertValue(traceMap.get(Constants.CONSO_ELEC), new TypeReference<>() {});
            consoMap.put(Constants.IMPACT_SOURCE, "REELLE");

            if (hideValue) {

                consoMap.put(Constants.VALEUR, Constants.HIDDEN_DATA);
                consoMap.put(Constants.VALEUR_REF_CONSO, Constants.HIDDEN_DATA);

                updateFormula(traceMap,
                        "ConsoElecAnMoyenne\\([^)]*\\)",
                        "ConsoElecAnMoyenne(\"hidden data\")");
            }

            traceMap.put(Constants.CONSO_ELEC, consoMap);
            modified = true;
        }

        if (traceMap.containsKey(Constants.MIX_ELECTRIQUE)) {

            Map<String, Object> mixMap = objectMapper.convertValue(traceMap.get(Constants.MIX_ELECTRIQUE),
                    new TypeReference<>() {});

            mixMap.put(Constants.NAME, refName);

            if (hideValue && mixMap.containsKey(Constants.VALEUR_REF_MIX)) {
                mixMap.put(Constants.VALEUR_REF_MIX, Constants.HIDDEN_DATA);
            }

            traceMap.put(Constants.MIX_ELECTRIQUE, mixMap);
            modified = true;
        }

        return modified;
    }

    private boolean handleReferenceFactor(Map<String, Object> traceMap,
                                          boolean isModelMatched,
                                          String refName,
                                          boolean hideValue) {

        if (!traceMap.containsKey(Constants.VALEUR_REF_FACTEUR)
                || !traceMap.containsKey(Constants.SOURCE_REF_FACTEUR)) {
            return false;
        }

        String source = traceMap.get(Constants.SOURCE_REF_FACTEUR).toString();
        Double value = Double.valueOf(traceMap.get(Constants.VALEUR_REF_FACTEUR).toString());

        Map<String, Object> ReferentielFacteurCaracterisation = new HashMap<>();

        ReferentielFacteurCaracterisation.put(Constants.IMPACT_SOURCE,
                isModelMatched ? "MODELE" : "TYPE");

        ReferentielFacteurCaracterisation.put(Constants.NAME, refName);
        ReferentielFacteurCaracterisation.put(Constants.SOURCE, source);

        if (hideValue) {

            ReferentielFacteurCaracterisation.put(Constants.VALEUR, Constants.HIDDEN_DATA);
            updateFormula(traceMap,
                    "referentielFacteurCaracterisation\\((.*?)\\)",
                    "referentielFacteurCaracterisation(\"hidden data\")");

        } else {
            ReferentielFacteurCaracterisation.put(Constants.VALEUR, value);
        }

        traceMap.remove(Constants.VALEUR_REF_FACTEUR);
        traceMap.remove(Constants.SOURCE_REF_FACTEUR);

        traceMap.put(Constants.REFERENTIEL_FACTEUR, ReferentielFacteurCaracterisation);

        return true;
    }

    private void updateFormula(Map<String, Object> traceMap,
                               String regex,
                               String replacement) {

        if (!traceMap.containsKey(Constants.FORMULE)) {
            return;
        }

        String formule = traceMap.get(Constants.FORMULE).toString();
        formule = formule.replaceAll(regex, replacement);

        traceMap.put(Constants.FORMULE, formule);
    }

    /**
     * Calculate virtual equipment impacts with NumEcoEval library
     *
     * @param virtualEquipment             the virtualEquipment
     * @param impactEquipementPhysiqueList the impactEquipementPhysiqueList
     * @param virtualEquipmentNumber       the virtualEquipmentNumber
     * @param totalVcpuNumber              the totalVcpuNumber
     * @param totalStorage                 the totalStorage
     * @return the list of impact
     */
    public List<ImpactEquipementVirtuel> calculateVirtualEquipment(final InVirtualEquipment virtualEquipment,
                                                                   final List<ImpactEquipementPhysique> impactEquipementPhysiqueList,
                                                                   Integer virtualEquipmentNumber,
                                                                   Double totalVcpuNumber,
                                                                   Double totalStorage,
                                                                   Double pue,
                                                                   String location) {

        if (impactEquipementPhysiqueList.isEmpty()) {
            log.warn("No physical equipment impacts provided → no virtual impact will be calculated");
            return new ArrayList<>();
        }

        LocalDateTime now = LocalDateTime.now();

        return impactEquipementPhysiqueList.stream()
                .map(impact -> {
                    ImpactEquipementVirtuel impactVirtuel =
                            calculImpactEquipementVirtuelService.calculerImpactEquipementVirtuel(
                                    DemandeCalculImpactEquipementVirtuel.builder()
                                            .dateCalcul(now)
                                            .equipementVirtuel(internalToNumEcoEvalCalculs.map(virtualEquipment))
                                            .nbEquipementsVirtuels(virtualEquipmentNumber)
                                            .nbTotalVCPU(totalVcpuNumber)
                                            .stockageTotalVirtuel(totalStorage)
                                            .impactEquipement(impact)
                                            .pue(pue)
                                            .localisation(location)
                                            .facteurCaracterisations(impact.getFacteurCaracterisations())
                                            .build()
                            );
                    impactVirtuel.setSource(impact.getSource());
                    return impactVirtuel;
                })
                .toList();

    }

    /**
     * Calculate application impacts with NumEcoEval library
     *
     * @param application                 the application
     * @param impactEquipementVirtuelList the impactEquipementVirtuelList
     * @param applicationNumber           the applicationNumber
     * @return the list of impact
     */
    public List<ImpactApplication> calculateApplication(final InApplication application,
                                                        final List<ImpactEquipementVirtuel> impactEquipementVirtuelList,
                                                        Integer applicationNumber) {

        if (impactEquipementVirtuelList.isEmpty()) return new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();

        return impactEquipementVirtuelList.stream()
                .map(impact -> calculImpactApplicationService.calculImpactApplicatif(
                        DemandeCalculImpactApplication.builder()
                                .dateCalcul(now)
                                .application(internalToNumEcoEvalCalculs.map(application))
                                .nbApplications(applicationNumber)
                                .impactEquipementVirtuel(impact)
                                .build()))
                .toList();

    }

    /**
     * Calculate the total vcpuCoreNumber of virtual equipment lists
     * Sum field vcpuCoreNumber
     *
     * @param virtualEquipments virtual equipment list
     * @return total vcpuCoreNumber
     */
    public Double getTotalVcpuCoreNumber(List<InVirtualEquipment> virtualEquipments) {
        Double totalVCPU = null;
        if (virtualEquipments.stream().noneMatch(vm -> vm.getVcpuCoreNumber() == null || vm.getVcpuCoreNumber() == 0)) {
            totalVCPU = virtualEquipments.stream().mapToDouble(InVirtualEquipment::getVcpuCoreNumber).sum();
        }

        return totalVCPU;
    }

    /**
     * Calculate the total disk size of virtual equipment lists
     * Sum field storage
     *
     * @param virtualEquipments virtual equipment list
     * @return le total de capaciteStockage
     */
    public Double getTotalDiskSize(List<InVirtualEquipment> virtualEquipments) {
        Double totalStorage = null;
        if (virtualEquipments.stream().noneMatch(vm -> vm.getSizeDiskGb() == null || vm.getSizeDiskGb() == 0)) {
            totalStorage = virtualEquipments.stream().mapToDouble(InVirtualEquipment::getSizeDiskGb).sum();
        }
        return totalStorage;
    }
}
