package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.apiindicator.mapper.VirtualEquipmentIndicatorMapper;
import com.soprasteria.g4it.backend.apiindicator.model.VirtualEquipmentElecConsumptionBO;
import com.soprasteria.g4it.backend.apiindicator.modeldb.InVirtualEquipmentElecConsumptionView;
import com.soprasteria.g4it.backend.apiindicator.repository.InVirtualEquipmentElecConsumptionViewRepository;
import com.soprasteria.g4it.backend.apiindicator.repository.InVirtualEquipmentLowImpactViewRepository;
import com.soprasteria.g4it.backend.apiuser.business.WorkspaceService;
import com.soprasteria.g4it.backend.external.boavizta.business.BoaviztapiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VirtualEquipmentIndicatorServiceTest {

    @Mock
    private InVirtualEquipmentLowImpactViewRepository lowImpactRepository;

    @Mock
    private InVirtualEquipmentElecConsumptionViewRepository elecRepository;

    @Mock
    private VirtualEquipmentIndicatorMapper mapper;

    @Mock
    private WorkspaceService workspaceService;

    @Mock
    private LowImpactService lowImpactService;

    @Mock
    private BoaviztapiService boaviztapiService;

    @InjectMocks
    private VirtualEquipmentIndicatorService service;

    @Test
    void shouldReturnVirtualEquipmentElecConsumption() {

        Long taskId = 100L;

        List<InVirtualEquipmentElecConsumptionView> repoResult =
                List.of(new InVirtualEquipmentElecConsumptionView());

        List<VirtualEquipmentElecConsumptionBO> mapped =
                List.of(new VirtualEquipmentElecConsumptionBO());

        when(elecRepository
                .findVirtualEquipmentElecConsumptionIndicators(taskId))
                .thenReturn(repoResult);

        when(mapper.inVirtualEquipmentElecConsumptionToDto(repoResult))
                .thenReturn(mapped);

        List<VirtualEquipmentElecConsumptionBO> result =
                service.getVirtualEquipmentElecConsumption(taskId);

        assertThat(result).isEqualTo(mapped);

        verify(elecRepository)
                .findVirtualEquipmentElecConsumptionIndicators(taskId);

        verify(mapper)
                .inVirtualEquipmentElecConsumptionToDto(repoResult);
    }

    @Test
    void shouldReturnEmptyElecConsumptionList() {

        Long taskId = 200L;

        when(elecRepository
                .findVirtualEquipmentElecConsumptionIndicators(taskId))
                .thenReturn(List.of());

        when(mapper.inVirtualEquipmentElecConsumptionToDto(List.of()))
                .thenReturn(List.of());

        List<VirtualEquipmentElecConsumptionBO> result =
                service.getVirtualEquipmentElecConsumption(taskId);

        assertThat(result).isEmpty();
    }
}