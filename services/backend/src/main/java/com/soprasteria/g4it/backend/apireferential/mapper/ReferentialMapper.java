/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apireferential.mapper;

import com.soprasteria.g4it.backend.apireferential.modeldb.*;
import com.soprasteria.g4it.backend.client.gen.connector.apireferentiel.dto.CritereDTO;
import com.soprasteria.g4it.backend.client.gen.connector.apireferentiel.dto.EtapeDTO;
import com.soprasteria.g4it.backend.client.gen.connector.apireferentiel.dto.TypeItemDTO;
import com.soprasteria.g4it.backend.server.gen.api.dto.*;
import org.apache.commons.csv.CSVRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

import static com.soprasteria.g4it.backend.common.utils.CsvUtils.*;

@Mapper(componentModel = "spring")
public interface ReferentialMapper {

    /**
     * To Rest
     */
    List<LifecycleStepRest> toLifecycleRest(final List<LifecycleStep> entities);

    List<CriterionRest> toCriteriaRest(final List<Criterion> entities);

    List<ItemTypeRest> toItemTypeRest(final List<ItemType> entity);

    List<ItemImpactRest> toItemImpactRest(final List<ItemImpact> entities);

    List<HypothesisRest> toHypothesesRest(final List<Hypothesis> entities);

    MatchingItemRest toMatchingItemRest(final MatchingItem entity);

    /**
     * From Csv import to Rest
     */
    default CriterionRest csvCriterionToRest(CSVRecord csvRecord) {
        return CriterionRest.builder()
                .code(read(csvRecord, "code"))
                .label(read(csvRecord, "label"))
                .description(read(csvRecord, "description"))
                .unit(read(csvRecord, "unit"))
                .build();
    }

    default LifecycleStepRest csvLifecycleStepToRest(CSVRecord csvRecord) {
        return LifecycleStepRest.builder()
                .code(read(csvRecord, "code"))
                .label(read(csvRecord, "label"))
                .build();
    }

    default HypothesisRest csvHypothesisToRest(CSVRecord csvRecord) {
        return HypothesisRest.builder()
                .code(read(csvRecord, "code"))
                .value(readDouble(csvRecord, "value"))
                .source(read(csvRecord, "source"))
                .description(read(csvRecord, "description"))
                .organization(read(csvRecord, "subscriber"))
                .version(read(csvRecord, "version"))
                .build();
    }

    default ItemTypeRest csvItemTypeToRest(CSVRecord csvRecord) {
        return ItemTypeRest.builder()
                .type(read(csvRecord, "type"))
                .category(read(csvRecord, "category"))
                .comment(read(csvRecord, "comment"))
                .defaultLifespan(readDouble(csvRecord, "defaultLifespan"))
                .isServer(readBoolean(csvRecord, "isServer"))
                .source(read(csvRecord, "source"))
                .refDefaultItem(read(csvRecord, "refDefaultItem"))
                .organization(read(csvRecord, "subscriber"))
                .version(read(csvRecord, "version"))
                .build();
    }

    default MatchingItemRest csvMatchingItemToRest(CSVRecord csvRecord) {
        return MatchingItemRest.builder()
                .itemSource(read(csvRecord, "itemSource"))
                .refItemTarget(read(csvRecord, "refItemTarget"))
                .organization(read(csvRecord, "subscriber"))
                .build();
    }

    default ItemImpactRest csvItemImpactToRest(CSVRecord csvRecord) {
        return ItemImpactRest.builder()
                .name(read(csvRecord, "name"))
                .lifecycleStep(read(csvRecord, "lifecycleStep"))
                .criterion(read(csvRecord, "criterion"))
                .description(read(csvRecord, "description"))
                .level(read(csvRecord, "level"))
                .tier(read(csvRecord, "tier"))
                .category(read(csvRecord, "category"))
                .avgElectricityConsumption(readDouble(csvRecord, "avgElectricityConsumption"))
                .location(read(csvRecord, "location"))
                .value(readDouble(csvRecord, "value"))
                .unit(read(csvRecord, "unit"))
                .source(read(csvRecord, "source"))
                .organization(read(csvRecord, "subscriber"))
                .version(read(csvRecord, "version"))
                .build();
    }

    /**
     * To Entity
     */
    Criterion toEntity(final CriterionRest criterionRest);

    LifecycleStep toEntity(final LifecycleStepRest lifecycleStepRest);

    Hypothesis toEntity(final HypothesisRest hypothesisRest);

    MatchingItem toEntity(final MatchingItemRest matchingItemRest);

    ItemType toEntity(final ItemTypeRest itemTypeRest);

    ItemImpact toEntity(final ItemImpactRest itemImpactRest);

    List<Criterion> toCriteriaEntity(final List<CriterionRest> criterionRest);

    List<ItemType> toItemTypeEntity(final List<ItemTypeRest> itemTypeRest);

    List<ItemImpact> toItemImpactEntity(final List<ItemImpactRest> itemImpactRest);

    List<LifecycleStep> toLifecycleStepEntity(final List<LifecycleStepRest> lifecycleStepRest);

    List<Hypothesis> toHypothesisEntity(final List<HypothesisRest> hypothesisRest);

    List<MatchingItem> toMatchingEntity(final List<MatchingItemRest> matchingItemRest);


    /**
     * NumEcoEval referentials migration to G4IT referentials
     */
    @Mapping(target = "label", source = "nomCritere")
    @Mapping(target = "unit", source = "unite")
    CriterionRest criteriaDtoToRest(final CritereDTO dto);

    @Mapping(target = "code", source = "code")
    @Mapping(target = "label", source = "libelle")
    LifecycleStepRest lifecycleDtoToRest(final EtapeDTO dto);

    @Mapping(target = "type", source = "type")
    @Mapping(target = "category", source = "categorie")
    @Mapping(target = "isServer", source = "serveur")
    @Mapping(target = "comment", source = "commentaire")
    @Mapping(target = "defaultLifespan", source = "dureeVieDefaut")
    @Mapping(target = "source", source = "source")
    @Mapping(target = "refDefaultItem", source = "refItemParDefaut")
    ItemTypeRest itemTypeDtoToRest(final TypeItemDTO dto);
}
