package com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.engine.numecoeval.EvaluateNumEcoEvalService;
import com.soprasteria.g4it.backend.apievaluating.mapper.InternalToNumEcoEvalCalculs;
import com.soprasteria.g4it.backend.apiinout.modeldb.InApplication;
import com.soprasteria.g4it.backend.apiinout.modeldb.InDatacenter;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apireferential.business.ReferentialService;
import com.soprasteria.g4it.backend.server.gen.api.dto.CriterionRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.ItemImpactRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.ItemTypeRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.MatchingItemRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mte.numecoeval.calculs.domain.data.entree.EquipementPhysique;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactApplication;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementPhysique;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementVirtuel;
import org.mte.numecoeval.calculs.domain.port.input.service.CalculImpactApplicationService;
import org.mte.numecoeval.calculs.domain.port.input.service.CalculImpactEquipementPhysiqueService;
import org.mte.numecoeval.calculs.domain.port.input.service.CalculImpactEquipementVirtuelService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    private EquipementPhysique mappedPhysical;
    private org.mte.numecoeval.calculs.domain.data.entree.DataCenter mappedDataCenter;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mappedPhysical = new EquipementPhysique();

        mappedDataCenter = mock(org.mte.numecoeval.calculs.domain.data.entree.DataCenter.class);
        mappedPhysical.setDataCenter(mappedDataCenter);

        when(internalToNumEcoEvalCalculs.map(any(InPhysicalEquipment.class)))
                .thenReturn(mappedPhysical);

        when(internalToNumEcoEvalCalculs.map(any(InDatacenter.class)))
                .thenReturn(mappedDataCenter);

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

    @Test
    void calculatePhysicalEquipment_shouldReturnOneImpact_whenModelMatched() {
        InPhysicalEquipment physical = new InPhysicalEquipment();
        physical.setModel("MODEL_X");
        physical.setType("SERVER");
        physical.setLocation("FR");

        InDatacenter datacenter = new InDatacenter();
        datacenter.setPue(1.5);
        datacenter.setLocation("FR");

        CriterionRest criterion = new CriterionRest();
        criterion.setCode("CLIMATE_CHANGE");

        MatchingItemRest matchingItem = new MatchingItemRest();
        matchingItem.setRefItemTarget("REF_MODEL");

        ItemTypeRest itemType = new ItemTypeRest();
        ItemImpactRest itemImpact = new ItemImpactRest();

        when(referentialService.getMatchingItem("MODEL_X", "ORG"))
                .thenReturn(matchingItem);
        when(referentialService.getItemType("SERVER", "ORG"))
                .thenReturn(itemType);
        when(referentialService.getItemImpacts(any(), any(), any(), any(), any()))
                .thenReturn(List.of(itemImpact));
        when(calculImpactEquipementPhysiqueService.calculerImpactEquipementPhysique(any()))
                .thenReturn(ImpactEquipementPhysique.builder().trace("{}").build());

        List<ImpactEquipementPhysique> result =
                service.calculatePhysicalEquipment(
                        physical,
                        datacenter,
                        "ORG",
                        List.of(criterion),
                        List.of("USING"),
                        Collections.emptyList()
                );

        assertEquals(1, result.size());
        verify(calculImpactEquipementPhysiqueService).calculerImpactEquipementPhysique(any());
    }

    @Test
    void calculatePhysicalEquipment_shouldMarkReferentialSourceAsType_whenNoModelMatch() {

        // GIVEN
        InPhysicalEquipment physical = new InPhysicalEquipment();
        physical.setType("SERVER");
        physical.setLocation("FR");

        CriterionRest criterion = new CriterionRest();
        criterion.setCode("CLIMATE_CHANGE");

        ItemTypeRest itemType = new ItemTypeRest();
        itemType.setRefDefaultItem("DEFAULT_SERVER");

        when(referentialService.getItemType(any(), any()))
                .thenReturn(itemType);

        when(referentialService.getItemImpacts(any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        when(internalToNumEcoEvalCalculs.map(any(CriterionRest.class)))
                .thenReturn(new org.mte.numecoeval.calculs.domain.data.referentiel.ReferentielCritere());

        ImpactEquipementPhysique impact = mock(ImpactEquipementPhysique.class);
        when(impact.getTrace()).thenReturn(
                "{ \"valeurReferentielFacteurCaracterisation\": 9.1," +
                        "  \"sourceReferentielFacteurCaracterisation\": \"ADEME\" }"
        );
        when(impact.getStatutIndicateur()).thenReturn("OK");

        when(calculImpactEquipementPhysiqueService.calculerImpactEquipementPhysique(any()))
                .thenReturn(impact);
        
        service.calculatePhysicalEquipment(
                physical,
                null,
                "ORG",
                List.of(criterion),
                List.of("MANUFACTURING"),
                List.of()
        );

        // THEN
        ArgumentCaptor<String> traceCaptor = ArgumentCaptor.forClass(String.class);
        verify(impact).setTrace(traceCaptor.capture());

        assertTrue(traceCaptor.getValue().contains("\"impact source\":\"TYPE\""));
    }

    @Test
    void calculatePhysicalEquipment_shouldCreateOneImpactPerCriterionAndLifecycle() {
        InPhysicalEquipment physical = new InPhysicalEquipment();
        physical.setType("SERVER");

        CriterionRest criterion = new CriterionRest();
        criterion.setCode("CLIMATE_CHANGE");

        when(referentialService.getItemType(any(), any()))
                .thenReturn(new ItemTypeRest());
        when(referentialService.getItemImpacts(any(), any(), any(), any(), any()))
                .thenReturn(List.of(new ItemImpactRest()));
        when(calculImpactEquipementPhysiqueService.calculerImpactEquipementPhysique(any()))
                .thenReturn(ImpactEquipementPhysique.builder().trace("{}").build());

        List<ImpactEquipementPhysique> result =
                service.calculatePhysicalEquipment(
                        physical,
                        null,
                        "ORG",
                        List.of(criterion, criterion),
                        List.of("FABRICATION", "USING"),
                        Collections.emptyList()
                );

        assertEquals(4, result.size());
    }

    // ------------------------------------------------------------------
    // updateTraceForImpact
    // ------------------------------------------------------------------

    @Test
    void calculatePhysicalEquipment_shouldRewriteElectricityTrace_forUsingLifecycle() throws Exception {

        // GIVEN
        InPhysicalEquipment physical = new InPhysicalEquipment();
        physical.setType("SERVER");
        physical.setLocation("FR");

        InDatacenter datacenter = new InDatacenter();
        datacenter.setPue(1.2);
        datacenter.setLocation("FR");

        CriterionRest criterion = new CriterionRest();
        criterion.setCode("CLIMATE_CHANGE");

        when(referentialService.getItemType(any(), any()))
                .thenReturn(new ItemTypeRest());

        when(referentialService.getItemImpacts(any(), any(), any(), any(), any()))
                .thenReturn(List.of(new ItemImpactRest()));

        String initialTrace =
                "{ \"consoElecAnMoyenne\": { \"valeur\": 100 } }";

        when(calculImpactEquipementPhysiqueService.calculerImpactEquipementPhysique(any()))
                .thenReturn(ImpactEquipementPhysique.builder()
                        .trace(initialTrace)
                        .build());

        // WHEN
        List<ImpactEquipementPhysique> result =
                service.calculatePhysicalEquipment(
                        physical,
                        datacenter,
                        "ORG",
                        List.of(criterion),
                        List.of("USING"),
                        Collections.emptyList()
                );

        // THEN
        assertEquals(1, result.size());

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> trace =
                mapper.readValue(result.get(0).getTrace(), Map.class);

        Map<?, ?> conso = (Map<?, ?>) trace.get("consoElecAnMoyenne");
        assertNotNull(conso);
        assertEquals("REELLE", conso.get("impact source"));
    }

    @Test
    void calculatePhysicalEquipment_shouldMarkReferentialSourceAsModel_whenMatched() {

        // GIVEN
        InPhysicalEquipment physical = new InPhysicalEquipment();
        physical.setModel("Dell R740");
        physical.setType("SERVER");
        physical.setLocation("FR");

        CriterionRest criterion = new CriterionRest();
        criterion.setCode("CLIMATE_CHANGE");

        MatchingItemRest matchingItem = new MatchingItemRest();
        matchingItem.setRefItemTarget("REF_SERVER");

        when(referentialService.getMatchingItem(any(), any()))
                .thenReturn(matchingItem);

        when(referentialService.getItemType(any(), any()))
                .thenReturn(new ItemTypeRest());

        when(referentialService.getItemImpacts(any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        when(internalToNumEcoEvalCalculs.map(any(CriterionRest.class)))
                .thenReturn(new org.mte.numecoeval.calculs.domain.data.referentiel.ReferentielCritere());

        ImpactEquipementPhysique impact = mock(ImpactEquipementPhysique.class);
        when(impact.getTrace()).thenReturn(
                "{ \"valeurReferentielFacteurCaracterisation\": 12.5," +
                        "  \"sourceReferentielFacteurCaracterisation\": \"ADEME\" }"
        );
        when(impact.getStatutIndicateur()).thenReturn("OK");

        when(calculImpactEquipementPhysiqueService.calculerImpactEquipementPhysique(any()))
                .thenReturn(impact);

        // WHEN
        service.calculatePhysicalEquipment(
                physical,
                null,
                "ORG",
                List.of(criterion),
                List.of("MANUFACTURING"),
                List.of()
        );

        // THEN
        ArgumentCaptor<String> traceCaptor = ArgumentCaptor.forClass(String.class);
        verify(impact).setTrace(traceCaptor.capture());

        String rewrittenTrace = traceCaptor.getValue();
        assertNotNull(rewrittenTrace);
        assertTrue(rewrittenTrace.contains("\"impact source\":\"MODELE\""));
    }

    @Test
    void calculatePhysicalEquipment_shouldNotFail_whenTraceIsInvalidJson() {
        InPhysicalEquipment physical = new InPhysicalEquipment();
        physical.setType("SERVER");

        CriterionRest criterion = new CriterionRest();
        criterion.setCode("CLIMATE_CHANGE");

        when(referentialService.getItemType(any(), any()))
                .thenReturn(new ItemTypeRest());
        when(referentialService.getItemImpacts(any(), any(), any(), any(), any()))
                .thenReturn(List.of(new ItemImpactRest()));
        when(calculImpactEquipementPhysiqueService.calculerImpactEquipementPhysique(any()))
                .thenReturn(ImpactEquipementPhysique.builder().trace("INVALID_JSON").build());

        assertDoesNotThrow(() ->
                service.calculatePhysicalEquipment(
                        physical,
                        null,
                        "ORG",
                        List.of(criterion),
                        List.of("FABRICATION"),
                        Collections.emptyList()
                )
        );
    }

}