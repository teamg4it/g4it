package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.apiindicator.mapper.VirtualEquipmentIndicatorMapper;
import com.soprasteria.g4it.backend.apiindicator.model.VirtualEquipmentElecConsumptionBO;
import com.soprasteria.g4it.backend.apiindicator.model.VirtualEquipmentLowImpactBO;
import com.soprasteria.g4it.backend.apiindicator.modeldb.InVirtualEquipmentElecConsumptionView;
import com.soprasteria.g4it.backend.apiindicator.modeldb.InVirtualEquipmentLowImpactView;
import com.soprasteria.g4it.backend.apiindicator.repository.InVirtualEquipmentElecConsumptionViewRepository;
import com.soprasteria.g4it.backend.apiindicator.repository.InVirtualEquipmentLowImpactViewRepository;
import com.soprasteria.g4it.backend.apiuser.business.WorkspaceService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.external.boavizta.business.BoaviztapiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

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

    private VirtualEquipmentIndicatorService service;

    @BeforeEach
    void setup() {
        service = new VirtualEquipmentIndicatorService(
                lowImpactRepository,
                elecRepository,
                mapper,
                workspaceService,
                lowImpactService,
                boaviztapiService
        );
    }

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

    @Test
    void shouldReturnVirtualEquipmentsLowImpact() {

        String organization = "ORG";
        Long workspaceId = 1L;
        Long inventoryId = 10L;

        Workspace workspace = new Workspace();
        workspace.setName("WS");

        when(workspaceService.getWorkspaceById(workspaceId))
                .thenReturn(workspace);

        InVirtualEquipmentLowImpactView view1 = new InVirtualEquipmentLowImpactView();
        view1.setName("VM1");
        view1.setLocation("FRA");
        view1.setEquipmentType("Server");

        InVirtualEquipmentLowImpactView view2 = new InVirtualEquipmentLowImpactView();
        view2.setName("VM2");
        view2.setLocation("FRA");
        view2.setEquipmentType("Server");

        List<InVirtualEquipmentLowImpactView> repoResult =
                List.of(view1, view2);

        when(lowImpactRepository
                .findVirtualEquipmentLowImpactIndicatorsByInventoryId(inventoryId))
                .thenReturn(repoResult);

        when(boaviztapiService.getCountryMap())
                .thenReturn(Map.of("France", "FRA"));

        service.init();

        when(lowImpactService.isLowImpact("France"))
                .thenReturn(true);

        when(mapper.toLowImpactBO(repoResult))
                .thenReturn(List.of(new VirtualEquipmentLowImpactBO()));

        List<VirtualEquipmentLowImpactBO> result =
                service.getVirtualEquipmentsLowImpact(
                        organization,
                        workspaceId,
                        inventoryId
                );

        assertThat(result).isNotNull();

        verify(lowImpactService).isLowImpact("France");

        assertThat(view1.getLocation()).isEqualTo("France");
        assertThat(view1.getLowImpact()).isTrue();
    }

    @Test
    void shouldDefaultLowImpactToFalseWhenLocationUnknown() {

        String organization = "ORG";
        Long workspaceId = 1L;
        Long inventoryId = 10L;

        Workspace workspace = new Workspace();
        workspace.setName("WS");

        when(workspaceService.getWorkspaceById(workspaceId))
                .thenReturn(workspace);

        InVirtualEquipmentLowImpactView view = new InVirtualEquipmentLowImpactView();
        view.setName("VM1");
        view.setLocation("UNKNOWN");
        view.setEquipmentType("Server");

        when(lowImpactRepository
                .findVirtualEquipmentLowImpactIndicatorsByInventoryId(inventoryId))
                .thenReturn(List.of(view));

        when(boaviztapiService.getCountryMap())
                .thenReturn(Map.of());

        service.init();

        when(lowImpactService.isLowImpact("UNKNOWN"))
                .thenReturn(false);

        when(mapper.toLowImpactBO(List.of(view)))
                .thenReturn(List.of(new VirtualEquipmentLowImpactBO()));

        service.getVirtualEquipmentsLowImpact(
                organization,
                workspaceId,
                inventoryId
        );

        verify(lowImpactService).isLowImpact("UNKNOWN");

        assertThat(view.getLocation()).isEqualTo("UNKNOWN");
        assertThat(view.getLowImpact()).isFalse();
    }
}