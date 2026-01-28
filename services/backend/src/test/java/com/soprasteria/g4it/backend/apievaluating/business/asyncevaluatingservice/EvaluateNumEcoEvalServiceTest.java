package com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice;

import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.engine.numecoeval.EvaluateNumEcoEvalService;
import com.soprasteria.g4it.backend.apievaluating.mapper.InternalToNumEcoEvalCalculs;
import com.soprasteria.g4it.backend.apiinout.modeldb.InApplication;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apireferential.business.ReferentialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactApplication;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementPhysique;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementVirtuel;
import org.mte.numecoeval.calculs.domain.port.input.service.CalculImpactApplicationService;
import org.mte.numecoeval.calculs.domain.port.input.service.CalculImpactEquipementPhysiqueService;
import org.mte.numecoeval.calculs.domain.port.input.service.CalculImpactEquipementVirtuelService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EvaluateNumEcoEvalServiceTest {

    @InjectMocks
    private EvaluateNumEcoEvalService service;

    @Mock
    private InternalToNumEcoEvalCalculs internalToNumEcoEvalCalculs;
    @Mock
    private ReferentialService referentialService;
    @Mock
    private CalculImpactEquipementPhysiqueService calculImpactEquipementPhysiqueService;
    @Mock
    private CalculImpactEquipementVirtuelService calculImpactEquipementVirtuelService;
    @Mock
    private CalculImpactApplicationService calculImpactApplicationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ------------------------------------------------------------------
    // getTotalVcpuCoreNumber
    // ------------------------------------------------------------------

    @Test
    void getTotalVcpuCoreNumber_shouldReturnSum_whenAllValuesPresent() {
        InVirtualEquipment vm1 = new InVirtualEquipment();
        vm1.setVcpuCoreNumber(2.0);

        InVirtualEquipment vm2 = new InVirtualEquipment();
        vm2.setVcpuCoreNumber(4.0);

        Double result = service.getTotalVcpuCoreNumber(List.of(vm1, vm2));

        assertEquals(6.0, result);
    }

    @Test
    void getTotalVcpuCoreNumber_shouldReturnNull_whenAnyValueMissing() {
        InVirtualEquipment vm1 = new InVirtualEquipment();
        vm1.setVcpuCoreNumber(2.0);

        InVirtualEquipment vm2 = new InVirtualEquipment(); // null vcpu

        Double result = service.getTotalVcpuCoreNumber(List.of(vm1, vm2));

        assertNull(result);
    }

    // ------------------------------------------------------------------
    // getTotalDiskSize
    // ------------------------------------------------------------------

    @Test
    void getTotalDiskSize_shouldReturnSum_whenAllValuesPresent() {
        InVirtualEquipment vm1 = new InVirtualEquipment();
        vm1.setSizeDiskGb(100.0);

        InVirtualEquipment vm2 = new InVirtualEquipment();
        vm2.setSizeDiskGb(50.0);

        Double result = service.getTotalDiskSize(List.of(vm1, vm2));

        assertEquals(150.0, result);
    }

    @Test
    void getTotalDiskSize_shouldReturnNull_whenAnyValueMissing() {
        InVirtualEquipment vm1 = new InVirtualEquipment();
        vm1.setSizeDiskGb(100.0);

        InVirtualEquipment vm2 = new InVirtualEquipment(); // null disk

        Double result = service.getTotalDiskSize(List.of(vm1, vm2));

        assertNull(result);
    }

    // ------------------------------------------------------------------
    // calculateVirtualEquipment
    // ------------------------------------------------------------------

    @Test
    void calculateVirtualEquipment_shouldReturnEmptyList_whenNoPhysicalImpacts() {
        InVirtualEquipment vm = new InVirtualEquipment();

        List<ImpactEquipementVirtuel> result =
                service.calculateVirtualEquipment(
                        vm,
                        Collections.emptyList(), // 🔑 no physical impacts
                        1,
                        4.0,
                        100.0,
                        1.5,
                        "FR"
                );

        assertTrue(result.isEmpty());

        verifyNoInteractions(calculImpactEquipementVirtuelService);
    }

    @Test
    void calculateVirtualEquipment_shouldDelegateCalculation_whenPhysicalImpactsExist() {
        InVirtualEquipment vm = new InVirtualEquipment();

        ImpactEquipementPhysique physicalImpact = mock(ImpactEquipementPhysique.class);
        ImpactEquipementVirtuel virtualImpact = mock(ImpactEquipementVirtuel.class);

        when(calculImpactEquipementVirtuelService.calculerImpactEquipementVirtuel(any()))
                .thenReturn(virtualImpact);

        List<ImpactEquipementVirtuel> result =
                service.calculateVirtualEquipment(
                        vm,
                        List.of(physicalImpact),
                        1,
                        4.0,
                        100.0,
                        1.5,
                        "FR"
                );

        assertEquals(1, result.size());
        verify(calculImpactEquipementVirtuelService).calculerImpactEquipementVirtuel(any());
    }

    // ------------------------------------------------------------------
    // calculateApplication
    // ------------------------------------------------------------------

    @Test
    void calculateApplication_shouldReturnEmptyList_whenNoVirtualImpacts() {
        InApplication app = new InApplication();

        List<ImpactApplication> result =
                service.calculateApplication(app, Collections.emptyList(), 1);

        assertTrue(result.isEmpty());
        verifyNoInteractions(calculImpactApplicationService);
    }

    @Test
    void calculateApplication_shouldDelegateCalculation_whenVirtualImpactsExist() {
        InApplication app = new InApplication();

        ImpactEquipementVirtuel virtualImpact = mock(ImpactEquipementVirtuel.class);
        ImpactApplication appImpact = mock(ImpactApplication.class);

        when(calculImpactApplicationService.calculImpactApplicatif(any()))
                .thenReturn(appImpact);

        List<ImpactApplication> result =
                service.calculateApplication(app, List.of(virtualImpact), 1);

        assertEquals(1, result.size());
        verify(calculImpactApplicationService).calculImpactApplicatif(any());
    }
}