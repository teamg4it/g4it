/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.apiindicator.mapper.VirtualEquipmentIndicatorMapper;
import com.soprasteria.g4it.backend.apiindicator.model.VirtualEquipmentCountBO;
import com.soprasteria.g4it.backend.apiindicator.model.VirtualEquipmentElecConsumptionBO;
import com.soprasteria.g4it.backend.apiindicator.model.VirtualEquipmentLowImpactBO;
import com.soprasteria.g4it.backend.apiindicator.modeldb.InVirtualEquipmentLowImpactView;
import com.soprasteria.g4it.backend.apiindicator.repository.InVirtualEquipmentCountViewRepository;
import com.soprasteria.g4it.backend.apiindicator.repository.InVirtualEquipmentElecConsumptionViewRepository;
import com.soprasteria.g4it.backend.apiindicator.repository.InVirtualEquipmentLowImpactViewRepository;
import com.soprasteria.g4it.backend.apiindicator.utils.TypeUtils;
import com.soprasteria.g4it.backend.apiuser.business.WorkspaceService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.external.boavizta.business.BoaviztapiService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VirtualEquipmentIndicatorService {

    @Autowired
    private final InVirtualEquipmentLowImpactViewRepository inVirtualEquipmentLowImpactViewRepository;

    @Autowired
    private final InVirtualEquipmentElecConsumptionViewRepository inVirtualEquipmentElecConsumptionViewRepository;

    @Autowired
    private final VirtualEquipmentIndicatorMapper virtualEquipmentIndicatorMapper;

    @Autowired
    private final InVirtualEquipmentCountViewRepository inVirtualEquipmentCountViewRepository;

    /**
     * The Workspace Service
     */
    @Autowired
    private WorkspaceService workspaceService;

    /**
     * The LowImpact Service
     */
    @Autowired
    private LowImpactService lowImpactService;

    @Autowired
    private BoaviztapiService boaviztapiService;

    private Map<String, String> isoToCountry;

    @PostConstruct
    public void init() {
        isoToCountry = boaviztapiService.getCountryMap()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getValue,
                        Map.Entry::getKey
                ));
    }


    public List<VirtualEquipmentLowImpactBO> getVirtualEquipmentsLowImpact(
            final String organization,
            final Long workspaceId,
            final Long inventoryId
    ) {

        final Workspace linkedWorkspace = workspaceService.getWorkspaceById(workspaceId);

        final List<InVirtualEquipmentLowImpactView> indicators =
                inVirtualEquipmentLowImpactViewRepository
                        .findVirtualEquipmentLowImpactIndicatorsByInventoryId(inventoryId);

        // Compute low impact per location (ISO → Full name) only once
        final Map<String, Boolean> locationLowImpactMap = indicators.stream()
                .map(InVirtualEquipmentLowImpactView::getLocation)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toMap(
                        iso -> iso,
                        iso -> {
                            String resolvedCountry =
                                    isoToCountry.getOrDefault(iso, iso);

                            boolean result =
                                    lowImpactService.isLowImpact(resolvedCountry);

                            log.info("LOW IMPACT RESULT → ISO: {} | Country: {} | Result: {}",
                                    iso, resolvedCountry, result);

                            return result;
                        }
                ));

        indicators.forEach(indicator -> {

            String iso = indicator.getLocation();
            String countryName = isoToCountry.getOrDefault(iso, iso);

            log.info("VM: {} | ISO: {} | Country: {}",
                    indicator.getName(),
                    iso,
                    countryName);

            indicator.setLocation(countryName);

            indicator.setEquipmentType(
                    TypeUtils.getShortType(
                            organization,
                            linkedWorkspace.getName(),
                            indicator.getEquipmentType()
                    )
            );

            indicator.setLowImpact(
                    locationLowImpactMap.getOrDefault(iso, false)
            );
        });

        return virtualEquipmentIndicatorMapper.toLowImpactBO(indicators);
    }

    public List<VirtualEquipmentElecConsumptionBO> getVirtualEquipmentElecConsumption(final Long taskId,
                                                                                      final Long criteriaNumber) {

        final var indicators =
                inVirtualEquipmentElecConsumptionViewRepository
                        .findVirtualEquipmentElecConsumptionIndicators(
                                taskId, criteriaNumber);

        return virtualEquipmentIndicatorMapper
                .inVirtualEquipmentElecConsumptionToDto(indicators);
    }


    public List<VirtualEquipmentCountBO> getVirtualEquipmentCount(final Long inventoryId) {
        final var indicators =
                inVirtualEquipmentCountViewRepository
                        .findVirtualEquipmentCountIndicators(inventoryId);

        return virtualEquipmentIndicatorMapper
                .inVirtualEquipmentCountToDto(indicators);
    }
}

