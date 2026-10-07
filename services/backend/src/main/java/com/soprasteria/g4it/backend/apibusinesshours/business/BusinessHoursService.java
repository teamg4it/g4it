/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apibusinesshours.business;

import com.soprasteria.g4it.backend.apibusinesshours.mapper.BusinessHoursMapper;
import com.soprasteria.g4it.backend.apibusinesshours.modeldb.BusinessHours;
import com.soprasteria.g4it.backend.apibusinesshours.repository.BusinessHoursRepository;
import com.soprasteria.g4it.backend.server.gen.api.dto.BusinessHoursRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class BusinessHoursService {

    private final BusinessHoursRepository repository;
    private final BusinessHoursMapper mapper;

    @Cacheable("getBusinessHours")
    public List<BusinessHoursRest> getBusinessHours() {
        Sort sortBy = Sort.by(new Sort.Order(Sort.Direction.ASC, "id").ignoreCase());
        List<BusinessHours> lstBusinessHours = repository.findAll(sortBy);
        return mapper.toBusinessHoursRest(lstBusinessHours);
    }

}
