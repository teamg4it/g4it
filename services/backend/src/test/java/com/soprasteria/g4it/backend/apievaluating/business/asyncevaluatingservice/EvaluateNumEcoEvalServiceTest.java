// This file has been created (totally or partially) with the assistance of artificial intelligence tools.
// All content has been generated under the direct supervision of a named individual,
// and under the AI.Backbone Orchestrator Compliance framework

package com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.engine.numecoeval.EvaluateNumEcoEvalService;
import com.soprasteria.g4it.backend.apievaluating.mapper.InternalToNumEcoEvalCalculs;
import com.soprasteria.g4it.backend.apievaluating.utils.Constants;
import com.soprasteria.g4it.backend.apiinout.modeldb.InApplication;
import com.soprasteria.g4it.backend.apiinout.modeldb.InDatacenter;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apireferential.business.PhysicalEquipmentReferenceResolver;
import com.soprasteria.g4it.backend.apireferential.business.ReferentialService;
import com.soprasteria.g4it.backend.config.NumEcoEvalRemotingServiceConfiguration;
import com.soprasteria.g4it.backend.server.gen.api.dto.CriterionRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.ItemImpactRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.ItemTypeRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.MatchingItemRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mapstruct.factory.Mappers;
import org.mte.numecoeval.calculs.domain.data.entree.EquipementPhysique;
import org.mte.numecoeval.calculs.domain.data.demande.DemandeCalculImpactEquipementPhysique;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactApplication;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementPhysique;
import org.mte.numecoeval.calculs.domain.data.indicateurs.ImpactEquipementVirtuel;
import org.mte.numecoeval.calculs.domain.port.input.service.CalculImpactApplicationService;
import org.mte.numecoeval.calculs.domain.port.input.service.CalculImpactEquipementPhysiqueService;
import org.mte.numecoeval.calculs.domain.port.input.service.CalculImpactEquipementVirtuelService;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EvaluateNumEcoEvalServiceTest {

    @ParameterizedTest
    @ValueSource(doubles = {0d, 0.5d, 0.57d, 0.85d, 1d})
    void allocationIsAppliedOnceToEachCriterionAndLifecycleRequest(double fraction) {
        var mapper = Mappers.getMapper(InternalToNumEcoEvalCalculs.class);
        ReflectionTestUtils.setField(service, "internalToNumEcoEvalCalculs", mapper);
        var physical = InPhysicalEquipment.builder().type("laptop").quantity(2.5d)
                .electricityConsumption(100d).location("FR").build();
        var climate = new CriterionRest();
        climate.setCode("CLIMATE_CHANGE");
        var water = new CriterionRest();
        water.setCode("WATER_USE");
        var reference = new PhysicalEquipmentReferenceResolver.Reference(null, new ItemTypeRest(),
                "synthetic-reference", false);
        when(calculImpactEquipementPhysiqueService.calculerImpactEquipementPhysique(any()))
                .thenAnswer(invocation -> ImpactEquipementPhysique.builder().trace("{}").build());

        var results = service.calculateAllocatedPhysicalEquipment(physical, null, List.of(climate, water),
                List.of("MANUFACTURING", "DISTRIBUTION", "USING", "END_OF_LIFE"), List.of(),
                1L, reference, fraction);

        var captor = ArgumentCaptor.forClass(DemandeCalculImpactEquipementPhysique.class);
        verify(calculImpactEquipementPhysiqueService, times(8)).calculerImpactEquipementPhysique(captor.capture());
        assertEquals(8, results.size());
        var objectMapper = new ObjectMapper();
        Map<String, Object> originalCalculationValues = objectMapper.convertValue(mapper.map(physical), Map.class);
        originalCalculationValues.remove("quantite");
        var requests = captor.getAllValues();
        for (int index = 0; index < requests.size(); index++) {
            var calculationEquipment = requests.get(index).getEquipementPhysique();
            assertEquals(2.5d * fraction, calculationEquipment.getQuantite(), 1e-12);
            Map<String, Object> calculationValues = objectMapper.convertValue(calculationEquipment, Map.class);
            calculationValues.remove("quantite");
            // All per-unit inputs, including electricity, must stay unchanged to avoid a squared factor.
            assertEquals(originalCalculationValues, calculationValues);
            for (int previous = 0; previous < index; previous++) {
                assertNotSame(requests.get(previous).getEquipementPhysique(), calculationEquipment);
            }
        }
        assertEquals(2.5d, physical.getQuantity());
        assertEquals(100d, physical.getElectricityConsumption());
    }

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
        ReflectionTestUtils.setField(service, "physicalEquipmentReferenceResolver",
                new PhysicalEquipmentReferenceResolver(referentialService));

        mappedPhysical = new EquipementPhysique();
        mappedDataCenter = mock(org.mte.numecoeval.calculs.domain.data.entree.DataCenter.class);
        mappedPhysical.setDataCenter(mappedDataCenter);

        when(internalToNumEcoEvalCalculs.map(any(InPhysicalEquipment.class)))
                .thenReturn(mappedPhysical);

        when(internalToNumEcoEvalCalculs.map(any(InDatacenter.class)))
                .thenReturn(mappedDataCenter);

        // Prevent NPE in all tests
        when(referentialService.getItemTypeForWorkspace(any(), any()))
                .thenReturn(new ItemTypeRest());
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
        when(referentialService.getItemTypeForWorkspace(any(), any()))
                .thenReturn(new ItemTypeRest());

        List<ImpactEquipementPhysique> result =
                service.calculatePhysicalEquipment(
                        physical,
                        datacenter,
                        "ORG",
                        List.of(criterion),
                        List.of("USING"),
                        Collections.emptyList(),
                        1L,
                        1
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

        // Correct mocks for service logic
        when(referentialService.getItemTypeForWorkspace(any(), any())).thenReturn(itemType);
        when(referentialService.getMatchingItemForWorkspace(any(), any())).thenReturn(null);
        when(referentialService.getItemImpactsForWorkspace(any(), any(), any(), any(), any())).thenReturn(List.of());
        when(referentialService.getItemType(any(), any())).thenReturn(itemType);
        when(referentialService.getMatchingItem(any(), any())).thenReturn(null);
        when(referentialService.getItemImpacts(any(), any(), any(), any(), any())).thenReturn(List.of());
        when(internalToNumEcoEvalCalculs.map(any(CriterionRest.class)))
                .thenReturn(new org.mte.numecoeval.calculs.domain.data.referentiel.ReferentielCritere());

        ImpactEquipementPhysique impact = mock(ImpactEquipementPhysique.class);
        when(impact.getTrace()).thenReturn(
                "{ \"valeurReferentielFacteurCaracterisation\": 9.1,\"sourceReferentielFacteurCaracterisation\": \"ADEME\" }"
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
                List.of(),
                1L,
                1  // instead of any()
        );

        // THEN
        ArgumentCaptor<String> traceCaptor = ArgumentCaptor.forClass(String.class);
        verify(impact).setTrace(traceCaptor.capture());
        System.out.println("TRACE VALUE: " + traceCaptor.getValue()); // Debug output
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
                        Collections.emptyList(),
                        1L,
                        1 // instead of any()
                );

        assertEquals(4, result.size());
    }

    @Test
    void calculatePhysicalEquipment_scalesEachRequestOnceWithoutMutatingSource() {
        InPhysicalEquipment physical = InPhysicalEquipment.builder()
                .type("laptop").quantity(10d).electricityConsumption(100d).build();
        CriterionRest criterion = new CriterionRest();
        criterion.setCode("CLIMATE_CHANGE");
        when(internalToNumEcoEvalCalculs.map(any(InPhysicalEquipment.class))).thenAnswer(invocation -> {
            EquipementPhysique mapped = new EquipementPhysique();
            mapped.setQuantite(10d);
            mapped.setConsoElecAnnuelle(100d);
            mapped.setDataCenter(mock(org.mte.numecoeval.calculs.domain.data.entree.DataCenter.class));
            return mapped;
        });
        when(calculImpactEquipementPhysiqueService.calculerImpactEquipementPhysique(any()))
                .thenReturn(ImpactEquipementPhysique.builder().trace("{}").build());
        var reference = new PhysicalEquipmentReferenceResolver.Reference(null, new ItemTypeRest(), null, false);

        service.calculateAllocatedPhysicalEquipment(physical, null, List.of(criterion),
                List.of("MANUFACTURING", "USING"), List.of(), 1L, reference, 0.5d);

        ArgumentCaptor<DemandeCalculImpactEquipementPhysique> requests =
                ArgumentCaptor.forClass(DemandeCalculImpactEquipementPhysique.class);
        verify(calculImpactEquipementPhysiqueService, times(2))
                .calculerImpactEquipementPhysique(requests.capture());
        requests.getAllValues().forEach(request -> {
            assertEquals(5d, request.getEquipementPhysique().getQuantite());
            assertEquals(100d, request.getEquipementPhysique().getConsoElecAnnuelle());
        });
        assertEquals(10d, physical.getQuantity());
        assertEquals(100d, physical.getElectricityConsumption());
    }

    @Test
    void realCalculationScalesEachLifecycleAndElectricityWithoutChangingInput() {
        var config = new NumEcoEvalRemotingServiceConfiguration();
        ReflectionTestUtils.setField(service, "internalToNumEcoEvalCalculs",
                Mappers.getMapper(InternalToNumEcoEvalCalculs.class));
        ReflectionTestUtils.setField(service, "calculImpactEquipementPhysiqueService",
                config.calculImpactEquipementPhysiqueService(config.dureeDeVieEquipementPhysiqueService()));

        var equipment = InPhysicalEquipment.builder().name("synthetic-device").type("laptop")
                .quantity(10d).electricityConsumption(100d).location("France")
                .datePurchase(LocalDate.of(2022, 1, 1)).build();
        var datacenter = InDatacenter.builder().pue(1d).location("France").build();
        var type = new ItemTypeRest();
        type.setRefDefaultItem("synthetic-reference");
        type.setDefaultLifespan(5d);
        type.setCategory("EquipementPhysique");
        type.setIsServer(false);
        var reference = new PhysicalEquipmentReferenceResolver.Reference(null, type, "synthetic-reference", false);
        var criterion = new CriterionRest();
        criterion.setCode("CLIMATE_CHANGE");
        criterion.setUnit("kg");
        List<String> stages = List.of("MANUFACTURING", "TRANSPORTATION", "USING", "END_OF_LIFE");
        when(referentialService.getItemImpactsForWorkspace(any(), any(), any(), any(), any()))
                .thenAnswer(invocation -> {
                    String stage = invocation.getArgument(1);
                    var item = new ItemImpactRest();
                    item.setName("synthetic-reference");
                    item.setCriterion("CLIMATE_CHANGE");
                    item.setLifecycleStep(stage);
                    item.setCategory("IT equipment");
                    item.setLocation("France");
                    item.setUnit("Item");
                    item.setValue(100d);
                    item.setAvgElectricityConsumption(100d);
                    item.setIsHidden(false);
                    var mix = new ItemImpactRest();
                    mix.setName("Electricity Mix FR");
                    mix.setCriterion("CLIMATE_CHANGE");
                    mix.setLifecycleStep(stage);
                    mix.setCategory("electricity-mix");
                    mix.setLocation("France");
                    mix.setValue(0.1d);
                    mix.setIsHidden(false);
                    return List.of(item, mix);
                });

        var baseline = service.calculateAllocatedPhysicalEquipment(equipment, datacenter, List.of(criterion),
                stages, List.of(), 1L, reference, 1d);
        assertEquals(4, baseline.size());
        for (var impact : baseline) {
            assertEquals("OK", impact.getStatutIndicateur());
            assertNotNull(impact.getImpactUnitaire());
            assertTrue(impact.getImpactUnitaire() > 0d);
        }
        var baselineUsing = baseline.get(2);
        assertNotNull(baselineUsing.getConsoElecMoyenne());
        assertEquals(1000d, baselineUsing.getConsoElecMoyenne() * baselineUsing.getQuantite(), 1e-8);
        for (double fraction : List.of(0d, 0.5d, 0.57d, 0.85d)) {
            var allocated = service.calculateAllocatedPhysicalEquipment(equipment, datacenter, List.of(criterion),
                    stages, List.of(), 1L, reference, fraction);
            assertEquals(baseline.size(), allocated.size());
            for (int i = 0; i < baseline.size(); i++) {
                assertEquals("OK", allocated.get(i).getStatutIndicateur());
                assertEquals(baseline.get(i).getImpactUnitaire() * fraction,
                        allocated.get(i).getImpactUnitaire(), 1e-8);
                if (i == 2) {
                    assertNotNull(allocated.get(i).getConsoElecMoyenne());
                    assertEquals(baseline.get(i).getConsoElecMoyenne() * baseline.get(i).getQuantite() * fraction,
                            allocated.get(i).getConsoElecMoyenne() * allocated.get(i).getQuantite(), 1e-8);
                }
            }
        }
        assertEquals(10d, equipment.getQuantity());
        assertEquals(100d, equipment.getElectricityConsumption());
    }

    // ------------------------------------------------------------------
    // updateTraceForImpact
    // ------------------------------------------------------------------

    @Test
    void calculatePhysicalEquipment_shouldRewriteElectricityTrace_forUsingLifecycle() throws Exception {
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

        String initialTrace = "{ \"consoElecAnMoyenne\": { \"valeur\": 100 } }";

        when(calculImpactEquipementPhysiqueService.calculerImpactEquipementPhysique(any()))
                .thenReturn(ImpactEquipementPhysique.builder()
                        .trace(initialTrace)
                        .build());

        // Use real values instead of any()
        List<ImpactEquipementPhysique> result =
                service.calculatePhysicalEquipment(
                        physical,
                        datacenter,
                        "ORG",
                        List.of(criterion),
                        List.of("USING"),
                        Collections.emptyList(),
                        1L,
                        1
                );

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

        when(referentialService.getItemTypeForWorkspace(any(), any()))
                .thenReturn(new ItemTypeRest()); // or a properly initialized ItemTypeRest


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
                List.of(),
                1L,
                1 // instead of any()
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

        MatchingItemRest matchingItem = new MatchingItemRest();
        matchingItem.setRefItemTarget("REF_SERVER");

        when(referentialService.getMatchingItem(any(), any()))
                .thenReturn(matchingItem);

        when(referentialService.getItemType(any(), any()))
                .thenReturn(new ItemTypeRest());

        when(referentialService.getItemImpacts(any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(calculImpactEquipementPhysiqueService.calculerImpactEquipementPhysique(any()))
                .thenReturn(ImpactEquipementPhysique.builder().trace("INVALID_JSON").build());

        assertDoesNotThrow(() ->
                service.calculatePhysicalEquipment(
                        physical,
                        null,
                        "ORG",
                        List.of(criterion),
                        List.of("FABRICATION"),
                        Collections.emptyList(),
                        1L,
                        1 // instead of any()
                )
        );
    }


    @Test
    void updateFormula_shouldReplacePattern_whenFormulaExists() throws Exception {
        Map<String, Object> traceMap = new HashMap<>();
        traceMap.put(Constants.FORMULE,
                "Impact = ConsoElecAnMoyenne(10.0) * X");

        Method method = EvaluateNumEcoEvalService.class
                .getDeclaredMethod("updateFormula", Map.class, String.class, String.class);
        method.setAccessible(true);

        method.invoke(service,
                traceMap,
                "ConsoElecAnMoyenne\\((.*?)\\)",
                "ConsoElecAnMoyenne(\"hidden data\")");

        String updated = traceMap.get(Constants.FORMULE).toString();

        assertTrue(updated.contains("hidden data"));
    }

    @Test
    void updateFormula_shouldDoNothing_whenFormulaMissing() throws Exception {
        Map<String, Object> traceMap = new HashMap<>();

        Method method = EvaluateNumEcoEvalService.class
                .getDeclaredMethod("updateFormula", Map.class, String.class, String.class);
        method.setAccessible(true);

        method.invoke(service,
                traceMap,
                "regex",
                "replacement");

        assertTrue(traceMap.isEmpty());
    }

    @Test
    void shouldUpdateMixElectrique_andHideValue_whenHideValueTrue() {

        InPhysicalEquipment physical = new InPhysicalEquipment();
        physical.setType("SERVER");
        physical.setLocation("FR");

        InDatacenter dc = new InDatacenter();
        dc.setLocation("FR");

        CriterionRest criterion = new CriterionRest();
        criterion.setCode("CLIMATE_CHANGE");

        ItemImpactRest impactRest = new ItemImpactRest();
        impactRest.setName("REF_NAME");
        impactRest.setIsHidden(true); // triggers hide
        impactRest.setSource("OTHER_SOURCE"); // not base

        when(referentialService.getItemType(any(), any()))
                .thenReturn(new ItemTypeRest());

        when(referentialService.getItemImpacts(any(), any(), any(), any(), any()))
                .thenReturn(List.of(impactRest));
        when(referentialService.getItemImpactsForWorkspace(any(), any(), any(), any(), any()))
                .thenReturn(List.of(impactRest));
        when(referentialService.getItemTypeForWorkspace(any(), any()))
                .thenReturn(new ItemTypeRest());

        String traceJson = """
    {
      "mixElectrique": {
        "valeur": 1.5,
        "valeurReferentielMixElectrique": 2.0
      }
    }
    """;

        ImpactEquipementPhysique impact = mock(ImpactEquipementPhysique.class);
        when(impact.getTrace()).thenReturn(traceJson);

        when(calculImpactEquipementPhysiqueService.calculerImpactEquipementPhysique(any()))
                .thenReturn(impact);

        // Use real values instead of any() for the last four arguments
        service.calculatePhysicalEquipment(
                physical,
                dc,
                "ORG",
                List.of(criterion),
                List.of("USING"),
                List.of(),
                1L,
                1// or appropriate value
                // or appropriate value
        );

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(impact).setTrace(captor.capture());

        String updated = captor.getValue();

        assertTrue(updated.contains("\"name\":\"REF_NAME\""));
        assertTrue(updated.contains("hidden data"));
    }

    @Test
    void shouldHideConsoElec_andUpdateFormula_whenHideValueTrue() throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        InPhysicalEquipment physical = new InPhysicalEquipment();
        physical.setType("SERVER");
        physical.setLocation("FR");

        CriterionRest criterion = new CriterionRest();
        criterion.setCode("CLIMATE_CHANGE");

        ItemImpactRest impactRest = new ItemImpactRest();
        impactRest.setIsHidden(true);
        impactRest.setSource("OTHER");

        when(referentialService.getItemType(any(), any()))
                .thenReturn(new ItemTypeRest());

        when(referentialService.getItemImpacts(any(), any(), any(), any(), any()))
                .thenReturn(List.of(impactRest));

        // ✅ SAFE JSON
        Map<String, Object> conso = new HashMap<>();
        conso.put(Constants.VALEUR, 10);
        conso.put(Constants.VALEUR_REF_CONSO, 20);

        Map<String, Object> traceMap = new HashMap<>();
        traceMap.put(Constants.FORMULE, "Impact = ConsoElecAnMoyenne(10.0)");
        traceMap.put(Constants.CONSO_ELEC, conso);

        String traceJson = mapper.writeValueAsString(traceMap);

        ImpactEquipementPhysique impact = mock(ImpactEquipementPhysique.class);
        when(impact.getTrace()).thenReturn(traceJson);

        doNothing().when(impact).setTrace(anyString()); // 🔑 IMPORTANT

        when(calculImpactEquipementPhysiqueService.calculerImpactEquipementPhysique(any()))
                .thenReturn(impact);

        service.calculatePhysicalEquipment(
                physical,
                null,
                "ORG",
                List.of(criterion),
                List.of(Constants.USING),
                List.of(),
                1L,
                1 // instead of any()
        );


        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(impact).setTrace(captor.capture());

        String updated = captor.getValue();

        assertTrue(updated.contains("hidden data"));
        assertTrue(updated.contains("ConsoElecAnMoyenne"));
    }

    @Test
    void calculatePhysicalEquipment_shouldFallbackToOrganizationReferential_whenWorkspaceDataMissing() {

        InPhysicalEquipment physical = new InPhysicalEquipment();
        physical.setModel("MODEL");
        physical.setType("SERVER");
        physical.setLocation("FR");

        CriterionRest criterion = new CriterionRest();
        criterion.setCode("CLIMATE_CHANGE");

        MatchingItemRest matching = new MatchingItemRest();
        matching.setRefItemTarget("REF_MODEL");

        ItemTypeRest itemType = new ItemTypeRest();

        when(referentialService.getMatchingItemForWorkspace("MODEL", 1L))
                .thenReturn(null);

        when(referentialService.getMatchingItem("MODEL", "ORG"))
                .thenReturn(matching);

        when(referentialService.getItemTypeForWorkspace("SERVER", 1L))
                .thenReturn(null);

        when(referentialService.getItemType("SERVER", "ORG"))
                .thenReturn(itemType);

        when(referentialService.getItemImpactsForWorkspace(
                any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        when(referentialService.getItemImpacts(
                any(), any(), any(), any(), any()))
                .thenReturn(List.of(new ItemImpactRest()));

        when(calculImpactEquipementPhysiqueService
                .calculerImpactEquipementPhysique(any()))
                .thenReturn(ImpactEquipementPhysique.builder()
                        .trace("{}")
                        .build());

        service.calculatePhysicalEquipment(
                physical,
                null,
                "ORG",
                List.of(criterion),
                List.of("USING"),
                List.of(),
                1L,
                1
        );

        verify(referentialService).getMatchingItem("MODEL", "ORG");
        verify(referentialService).getItemType("SERVER", "ORG");
    }

    @Test
    void calculatePhysicalEquipment_shouldUseOrganizationReferential_whenWorkspaceCountZero() {

        InPhysicalEquipment physical = new InPhysicalEquipment();
        physical.setType("SERVER");

        CriterionRest criterion = new CriterionRest();
        criterion.setCode("CLIMATE_CHANGE");

        when(referentialService.getItemType("SERVER", "ORG"))
                .thenReturn(new ItemTypeRest());

        when(referentialService.getItemImpacts(
                any(), any(), any(), any(), any()))
                .thenReturn(List.of(new ItemImpactRest()));

        when(calculImpactEquipementPhysiqueService
                .calculerImpactEquipementPhysique(any()))
                .thenReturn(ImpactEquipementPhysique.builder()
                        .trace("{}")
                        .build());

        service.calculatePhysicalEquipment(
                physical,
                null,
                "ORG",
                List.of(criterion),
                List.of("USING"),
                List.of(),
                1L,
                0
        );

        verify(referentialService, never())
                .getItemTypeForWorkspace(any(), any());

        verify(referentialService)
                .getItemType("SERVER", "ORG");
    }


    @Test
    void calculateVirtualEquipment_shouldCopySourceFromPhysicalImpact() {

        ImpactEquipementPhysique physical = mock(ImpactEquipementPhysique.class);
        ImpactEquipementVirtuel virtualImpact = mock(ImpactEquipementVirtuel.class);

        when(physical.getSource()).thenReturn("ADEME");

        when(calculImpactEquipementVirtuelService
                .calculerImpactEquipementVirtuel(any()))
                .thenReturn(virtualImpact);

        service.calculateVirtualEquipment(
                new InVirtualEquipment(),
                List.of(physical),
                1,
                1.0,
                1.0,
                1.2,
                "FR"
        );

        verify(virtualImpact).setSource("ADEME");
    }

    @Test
    void getTotalVcpuCoreNumber_shouldReturnNull_whenValueIsZero() {

        InVirtualEquipment vm = new InVirtualEquipment();
        vm.setVcpuCoreNumber(0.0);

        assertNull(service.getTotalVcpuCoreNumber(List.of(vm)));
    }

    @Test
    void getTotalDiskSize_shouldReturnNull_whenValueIsZero() {

        InVirtualEquipment vm = new InVirtualEquipment();
        vm.setSizeDiskGb(0.0);

        assertNull(service.getTotalDiskSize(List.of(vm)));
    }



}
