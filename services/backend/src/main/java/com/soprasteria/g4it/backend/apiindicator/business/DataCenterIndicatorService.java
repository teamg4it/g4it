/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.apiindicator.mapper.DataCenterIndicatorMapper;
import com.soprasteria.g4it.backend.apiindicator.model.DataCentersInformationBO;
import com.soprasteria.g4it.backend.apiindicator.repository.InDatacenterViewRepository;
import com.soprasteria.g4it.backend.apiuser.business.WorkspaceService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * DataCenter indicator service.
 */
@Service
@AllArgsConstructor
public class DataCenterIndicatorService {

    @Autowired
    private WorkspaceService workspaceService;
    /**
     * DataCenter indicators mapper.
     */
    @Autowired
    private DataCenterIndicatorMapper dataCenterIndicatorMapper;

    @Autowired
    private InDatacenterViewRepository inDatacenterViewRepository;

    /**
     * Retrieve datacenter indicators.
     *
     * @param inventoryId the inventory id.
     * @return datacenter indicators.
     */
    public List<DataCentersInformationBO> getDataCenterIndicators(final Long inventoryId) {

        final var result = inDatacenterViewRepository.findDataCenterIndicators(inventoryId);
        return dataCenterIndicatorMapper.toInDataCentersDto(result);

    }
}
