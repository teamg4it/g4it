/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.business;


import com.soprasteria.g4it.backend.apiindicator.mapper.DigitalServiceIndicatorMapper;
import com.soprasteria.g4it.backend.apiindicator.model.DigitalServiceIndicatorBO;
import com.soprasteria.g4it.backend.apiindicator.repository.DigitalServiceIndicatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Digital Service Indicator service.
 */
@Service
public class DigitalServiceIndicatorViewService {

    /**
     * Repository to access digital service indicator data.
     */
    @Autowired
    private DigitalServiceIndicatorRepository digitalServiceIndicatorRepository;

    /**
     * Digital service indicator mapper.
     */
    @Autowired
    private DigitalServiceIndicatorMapper digitalServiceIndicatorMapper;


    /**
     * Retrieve digital service indicator.
     *
     * @param uid the digital service uid.
     * @return indicator list.
     */
    public List<DigitalServiceIndicatorBO> getDigitalServiceIndicators(final String uid) {
        return digitalServiceIndicatorMapper.toDto(digitalServiceIndicatorRepository.findDigitalServiceIndicators(uid));
    }


}
