/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apirecommendationds;

import com.soprasteria.g4it.backend.apirecommendationds.business.RecommendationService;
import com.soprasteria.g4it.backend.apirecommendationds.mapper.RecommendationMapper;
import com.soprasteria.g4it.backend.apirecommendationds.modeldb.Recommendation;
import com.soprasteria.g4it.backend.apirecommendationds.repository.RecommendationRepository;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.RecommendationDSRest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.ArrayList;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    private static final Long ORGANISATION_ID = 1L;
    private static final Long RECOMMENDATION_ID = 42L;

    private static final String ORGANIZATION = "my-org";
    private static final Long WORKSPACE = 1L;

    @Mock
    private RecommendationRepository recommendationRepository;

    @Mock
    private RecommendationMapper recommendationMapper;

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    private void mockOrganizationLookup() {
        Organization organization = Organization.builder()
                .id(ORGANISATION_ID)
                .name(ORGANIZATION)
                .build();

        when(organizationRepository.findByName(ORGANIZATION))
                .thenReturn(Optional.of(organization));
    }

    @Test
    void shouldGetRecommendationsByOrganisation() {
        mockOrganizationLookup();

        List<Recommendation> general =
                List.of(Recommendation.builder().idRecommendation(1L).build());

        List<Recommendation> specific =
                List.of(Recommendation.builder().idRecommendation(RECOMMENDATION_ID).build());

        List<Recommendation> all = new ArrayList<>();
        all.addAll(general);
        all.addAll(specific);

        List<RecommendationDSRest> expected =
                List.of(new RecommendationDSRest());

        when(recommendationRepository.findByOrganisationIdIsNull())
                .thenReturn(general);

        when(recommendationRepository.findByOrganisationId(ORGANISATION_ID))
                .thenReturn(specific);

        when(recommendationMapper.toRestList(all))
                .thenReturn(expected);

        List<RecommendationDSRest> result =
                recommendationService.getRecommendations(
                        ORGANIZATION,
                        WORKSPACE
                );

        assertThat(result).isEqualTo(expected);

        verify(recommendationRepository).findByOrganisationIdIsNull();
        verify(recommendationRepository).findByOrganisationId(ORGANISATION_ID);
        verify(recommendationMapper).toRestList(all);
    }

    @Test
    void shouldReturnEmptyList_whenNoRecommendationsForOrganisation() {
        mockOrganizationLookup();

        when(recommendationRepository.findByOrganisationIdIsNull())
                .thenReturn(List.of());

        when(recommendationRepository.findByOrganisationId(ORGANISATION_ID))
                .thenReturn(List.of());

        when(recommendationMapper.toRestList(List.of()))
                .thenReturn(List.of());

        List<RecommendationDSRest> result =
                recommendationService.getRecommendations(
                        ORGANIZATION,
                        WORKSPACE
                );

        assertThat(result).isEmpty();

        verify(recommendationRepository).findByOrganisationIdIsNull();
        verify(recommendationRepository).findByOrganisationId(ORGANISATION_ID);
    }

    @Test
    void shouldCreateRecommendation() {
        mockOrganizationLookup();

        RecommendationDSRest input = new RecommendationDSRest();

        Recommendation entity = Recommendation.builder().build();

        Recommendation saved = Recommendation.builder()
                .idRecommendation(RECOMMENDATION_ID)
                .organisationId(ORGANISATION_ID)
                .build();

        RecommendationDSRest expected = new RecommendationDSRest();

        when(recommendationMapper.toEntity(input)).thenReturn(entity);

        when(recommendationRepository.save(entity)).thenReturn(saved);

        when(recommendationMapper.toRest(saved)).thenReturn(expected);

        RecommendationDSRest result =
                recommendationService.createRecommendation(
                        ORGANIZATION,
                        input
                );

        assertThat(result).isEqualTo(expected);

        verify(recommendationMapper).toEntity(input);
        verify(recommendationRepository).save(entity);

        assertThat(entity.getOrganisationId())
                .isEqualTo(ORGANISATION_ID);
    }

    @Test
    void shouldUpdateRecommendation() {
        RecommendationDSRest input = new RecommendationDSRest();

        Recommendation existing = Recommendation.builder()
                .idRecommendation(RECOMMENDATION_ID)
                .organisationId(ORGANISATION_ID)
                .build();

        Recommendation updates =
                Recommendation.builder()
                        .title("new title")
                        .build();

        RecommendationDSRest expected =
                new RecommendationDSRest();

        when(recommendationRepository.findById(RECOMMENDATION_ID))
                .thenReturn(Optional.of(existing));

        when(recommendationMapper.toEntity(input))
                .thenReturn(updates);

        when(recommendationRepository.save(existing))
                .thenReturn(existing);

        when(recommendationMapper.toRest(existing))
                .thenReturn(expected);

        RecommendationDSRest result =
                recommendationService.updateRecommendation(
                        ORGANISATION_ID,
                        RECOMMENDATION_ID,
                        input
                );

        assertThat(result).isEqualTo(expected);

        verify(recommendationRepository).findById(RECOMMENDATION_ID);
        verify(recommendationMapper).merge(existing, updates);
        verify(recommendationRepository).save(existing);
    }

    @Test
    void shouldThrow404_whenUpdatingNonExistingRecommendation() {
        when(recommendationRepository.findById(RECOMMENDATION_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                recommendationService.updateRecommendation(
                        ORGANISATION_ID,
                        RECOMMENDATION_ID,
                        new RecommendationDSRest()
                )
        ).isInstanceOf(G4itRestException.class);

        verify(recommendationRepository).findById(RECOMMENDATION_ID);
        verify(recommendationRepository, never()).save(any());
    }

    @Test
    void shouldThrow409_whenUpdatingRecommendationFromWrongOrganisation() {
        Recommendation existing = Recommendation.builder()
                .idRecommendation(RECOMMENDATION_ID)
                .organisationId(99L)
                .build();

        when(recommendationRepository.findById(RECOMMENDATION_ID))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() ->
                recommendationService.updateRecommendation(
                        ORGANISATION_ID,
                        RECOMMENDATION_ID,
                        new RecommendationDSRest()
                )
        ).isInstanceOf(G4itRestException.class);

        verify(recommendationRepository, never()).save(any());
    }

    @Test
    void shouldDeleteRecommendation() {
        Recommendation existing = Recommendation.builder()
                .idRecommendation(RECOMMENDATION_ID)
                .organisationId(ORGANISATION_ID)
                .build();

        when(recommendationRepository.findById(RECOMMENDATION_ID))
                .thenReturn(Optional.of(existing));

        recommendationService.deleteRecommendation(
                ORGANISATION_ID,
                RECOMMENDATION_ID
        );

        verify(recommendationRepository).findById(RECOMMENDATION_ID);
        verify(recommendationRepository).deleteById(RECOMMENDATION_ID);
    }

    @Test
    void shouldThrow404_whenDeletingNonExistingRecommendation() {
        when(recommendationRepository.findById(RECOMMENDATION_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                recommendationService.deleteRecommendation(
                        ORGANISATION_ID,
                        RECOMMENDATION_ID
                )
        ).isInstanceOf(G4itRestException.class);

        verify(recommendationRepository, never()).deleteById(any());
    }

    @Test
    void shouldThrow409_whenDeletingRecommendationFromWrongOrganisation() {
        Recommendation existing = Recommendation.builder()
                .idRecommendation(RECOMMENDATION_ID)
                .organisationId(99L)
                .build();

        when(recommendationRepository.findById(RECOMMENDATION_ID))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() ->
                recommendationService.deleteRecommendation(
                        ORGANISATION_ID,
                        RECOMMENDATION_ID
                )
        ).isInstanceOf(G4itRestException.class);

        verify(recommendationRepository, never()).deleteById(any());
    }
}