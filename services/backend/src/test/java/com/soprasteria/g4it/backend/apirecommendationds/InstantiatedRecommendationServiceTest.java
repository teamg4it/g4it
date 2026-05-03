/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apirecommendationds.business;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soprasteria.g4it.backend.apiinout.business.OutPhysicalEquipmentService;
import com.soprasteria.g4it.backend.apiinout.business.OutVirtualEquipmentService;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InDatacenterRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apirecommendationds.mapper.RecommendationMapper;
import com.soprasteria.g4it.backend.apirecommendationds.modeldb.Recommendation;
import com.soprasteria.g4it.backend.apireferential.business.ReferentialService;
import com.soprasteria.g4it.backend.server.gen.api.dto.InstantiatedRecommendationRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.RecommendationDSRest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstantiatedRecommendationServiceTest {

    @Mock
    private RecommendationService recommendationService;
    @Mock
    private RecommendationMapper recommendationMapper;
    @Mock
    private OutPhysicalEquipmentService outPhysicalEquipmentService;
    @Mock
    private OutVirtualEquipmentService outVirtualEquipmentService;
    @Mock
    private InDatacenterRepository inDatacenterRepository;
    @Mock
    private InPhysicalEquipmentRepository inPhysicalEquipmentRepository;
    @Mock
    private InVirtualEquipmentRepository inVirtualEquipmentRepository;
    @Mock
    private ReferentialService referentialService;
    
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private InstantiatedRecommendationService instantiatedRecommendationService;

    private static final String DS_VERSION_UID = "ds-version-uid-123";
    private static final Long ORG_ID = 1L;

    @Test
    void shouldReturnEmptyList_whenNoRecommendationsFound() {
        // Given
        when(recommendationService.findByOrganisationIdIsNull()).thenReturn(Collections.emptyList());
        when(recommendationService.findByOrganisationId(ORG_ID)).thenReturn(Collections.emptyList());

        // When
        List<InstantiatedRecommendationRest> result = instantiatedRecommendationService.getInstantiatedRecommendations(DS_VERSION_UID, ORG_ID);

        // Then
        assertThat(result).isEmpty();
        verify(recommendationService).findByOrganisationIdIsNull();
        verify(recommendationService).findByOrganisationId(ORG_ID);
    }

    @Test
    void shouldGetInstantiatedRecommendations_withBasicSetup() {
        // Given: setup one recommendation
        Recommendation rec = Recommendation.builder()
                .idRecommendation(100L)
                .category(List.of("TERMINAL"))
                .difficulty("EASY")
                .baseImpact(2)
                .affectedAttributes("\"yearlyUsageTimePerUser\"")
                .heuristicRange("{\"1\":[0,100],\"2\":[101,500]}")
                .build();

        when(recommendationService.findByOrganisationIdIsNull()).thenReturn(List.of(rec));
        when(recommendationService.findByOrganisationId(ORG_ID)).thenReturn(Collections.emptyList());

        // Setup mock inputs for heuristic context
        InPhysicalEquipment terminal = InPhysicalEquipment.builder()
                .type("Terminal")
                .durationHour(200.0)
                .quantity(1.0)
                .build();
        when(inPhysicalEquipmentRepository.findByDigitalServiceVersionUid(DS_VERSION_UID)).thenReturn(List.of(terminal));
        when(inVirtualEquipmentRepository.findByDigitalServiceVersionUid(DS_VERSION_UID)).thenReturn(Collections.emptyList());
        when(inDatacenterRepository.findByDigitalServiceVersionUid(DS_VERSION_UID)).thenReturn(Collections.emptyList());

        // Setup mock emissions (for proportions)
        when(outPhysicalEquipmentService.getByDigitalServiceVersionUid(DS_VERSION_UID)).thenReturn(Collections.emptyList());
        when(outVirtualEquipmentService.getByDigitalServiceVersionUid(DS_VERSION_UID)).thenReturn(Collections.emptyList());

        // Mapper
        RecommendationDSRest dsRest = RecommendationDSRest.builder().build();
        when(recommendationMapper.toRest(rec)).thenReturn(dsRest);

        // When
        List<InstantiatedRecommendationRest> result = instantiatedRecommendationService.getInstantiatedRecommendations(DS_VERSION_UID, ORG_ID);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdRecommendation()).isEqualTo(100L);
        assertThat(result.get(0).getDigitalServiceVersionUid()).isEqualTo(DS_VERSION_UID);
        assertThat(result.get(0).getPriority()).isBetween(0.0, 1.0);
        
        verify(recommendationMapper).toRest(any());
    }

    @Test
    void shouldHandleHeuristicScore_whenAttributeIsMissing() {
        // Given: recommendation with null affected attributes
        Recommendation rec = Recommendation.builder()
                .idRecommendation(1L)
                .category(List.of("PUBLIC_CLOUD"))
                .affectedAttributes(null)
                .build();

        when(recommendationService.findByOrganisationIdIsNull()).thenReturn(List.of(rec));
        when(recommendationService.findByOrganisationId(ORG_ID)).thenReturn(Collections.emptyList());
        
        // Mocking empty infra/emissions
        when(inPhysicalEquipmentRepository.findByDigitalServiceVersionUid(DS_VERSION_UID)).thenReturn(Collections.emptyList());
        when(inVirtualEquipmentRepository.findByDigitalServiceVersionUid(DS_VERSION_UID)).thenReturn(Collections.emptyList());
        when(outPhysicalEquipmentService.getByDigitalServiceVersionUid(DS_VERSION_UID)).thenReturn(Collections.emptyList());
        when(outVirtualEquipmentService.getByDigitalServiceVersionUid(DS_VERSION_UID)).thenReturn(Collections.emptyList());
        when(recommendationMapper.toRest(any())).thenReturn(RecommendationDSRest.builder().build());

        // When
        List<InstantiatedRecommendationRest> result = instantiatedRecommendationService.getInstantiatedRecommendations(DS_VERSION_UID, ORG_ID);

        // Then: Should compute successfully with neutral score
        assertThat(result).hasSize(1);
    }
    
    @Test
    void shouldSortRecommendationsByPriority() {
        // Given
        Recommendation rec1 = Recommendation.builder().idRecommendation(1L).difficulty("HARD").build();
        Recommendation rec2 = Recommendation.builder().idRecommendation(2L).difficulty("EASY").build();

        when(recommendationService.findByOrganisationIdIsNull()).thenReturn(List.of(rec1, rec2));
        when(recommendationService.findByOrganisationId(ORG_ID)).thenReturn(Collections.emptyList());
        
        // Technical mocks to let the flow run
        when(inPhysicalEquipmentRepository.findByDigitalServiceVersionUid(anyString())).thenReturn(Collections.emptyList());
        when(inVirtualEquipmentRepository.findByDigitalServiceVersionUid(anyString())).thenReturn(Collections.emptyList());
        when(outPhysicalEquipmentService.getByDigitalServiceVersionUid(anyString())).thenReturn(Collections.emptyList());
        when(outVirtualEquipmentService.getByDigitalServiceVersionUid(anyString())).thenReturn(Collections.emptyList());
        when(recommendationMapper.toRest(any())).thenReturn(RecommendationDSRest.builder().build());

        // When
        List<InstantiatedRecommendationRest> result = instantiatedRecommendationService.getInstantiatedRecommendations(DS_VERSION_UID, ORG_ID);

        // Then: EASY should generally be prioritized over HARD if other factors are equal
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPriority()).isGreaterThanOrEqualTo(result.get(1).getPriority());
    }
}