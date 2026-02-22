/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.business;

import com.google.common.math.Quantiles;
import com.soprasteria.g4it.backend.apiindicator.mapper.VirtualEquipmentIndicatorMapper;
import com.soprasteria.g4it.backend.apiindicator.repository.InVirtualEquipmentLowImpactViewRepository;
import com.soprasteria.g4it.backend.apiindicator.utils.CriteriaUtils;
import com.soprasteria.g4it.backend.apireferential.business.ReferentialGetService;
import com.soprasteria.g4it.backend.apireferential.business.ReferentialService;
import com.soprasteria.g4it.backend.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * LowImpact country Service
 */
@Service
@Slf4j
public class LowImpactService {

    /**
     * Set containing the criteria.
     */
    @Value("${g4it.criteria}")
    private Set<String> criterias;

    /**
     * Referential Service.
     */
    @Autowired
    private ReferentialService referentialService;

    /**
     * ReferentialGet Service.
     */
    @Autowired
    private ReferentialGetService referentialGetService;

    @Autowired
    InVirtualEquipmentLowImpactViewRepository inVirtualEquipmentLowImpactViewRepository;

    @Autowired
    VirtualEquipmentIndicatorMapper virtualEquipmentIndicatorMapper;

    /**
     * For a country, estimate the environment impact regarding electricity mix and returns true if the country has a low impact
     * A low impact means:
     * - getting all countries quartile position for each criterias and sum them
     * - checking if the country is in the first quartile of these countries
     *
     * @param country the country.
     * @return if country has a low impact.
     */
    @Cacheable("isLowImpact")
    public boolean isLowImpact(final String country) {

        Set<String> transformedCriteria = criterias.stream()
                .map(criterion -> StringUtils.kebabToSnakeCase(
                        CriteriaUtils.transformCriteriaNameToCriteriaKey(criterion)
                ))
                .collect(Collectors.toSet());
        var countrySumImpactMap = referentialGetService.getCountries(null).stream()
                .collect(Collectors.toMap(
                        refCountry -> refCountry,
                        refCountry -> transformedCriteria.stream()
                                .mapToInt(criteria -> {
                                    var quartile = referentialService.getElectricityMixQuartiles().get(Pair.of(refCountry, criteria));
                                    if (quartile == null) {
                                        log.error("Electricity mix not found for country: {} and criteria: {}", refCountry, criteria);
                                        quartile = 4;
                                    }
                                    return quartile;
                                })
                                .filter(Objects::nonNull)
                                .sum()
                ));

        var computeSum = Quantiles
                .quartiles()
                .indexes(1, 2, 3, 4)
                .compute(countrySumImpactMap.values());

        int firstQuartileValue = computeSum.get(1).intValue();
        Integer totalImpact = countrySumImpactMap.get(country);

        return totalImpact != null && totalImpact < firstQuartileValue;
    }

}
