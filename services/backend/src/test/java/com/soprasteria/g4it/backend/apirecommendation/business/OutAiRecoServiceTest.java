package com.soprasteria.g4it.backend.apirecommendation.business;

import com.soprasteria.g4it.backend.apirecomandation.business.OutAiRecoService;
import com.soprasteria.g4it.backend.apirecomandation.mapper.OutAiRecoMapper;
import com.soprasteria.g4it.backend.apirecomandation.modeldb.OutAiReco;
import com.soprasteria.g4it.backend.apirecomandation.repository.OutAiRecoRepository;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutAiRecommendationRest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutAiRecoServiceTest {

    @Mock
    private OutAiRecoRepository outAiRecoRepository;

    @Mock
    private OutAiRecoMapper outAiRecoMapper;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private OutAiRecoService outAiRecoService;

    @Test
    void shouldReturnRecommendation_whenTaskExists() {
        // Given
        String digitalServiceUid = "UID-123";
        Task task = Task.builder().id(42L).build();
        OutAiReco recommendation = OutAiReco.builder().build();
        OutAiRecommendationRest recommendationRest = OutAiRecommendationRest.builder().build();

        when(taskRepository.findByDigitalServiceUid(digitalServiceUid)).thenReturn(Optional.of(task));
        when(outAiRecoRepository.findByTaskId(task.getId())).thenReturn(recommendation);
        when(outAiRecoMapper.toDto(recommendation)).thenReturn(recommendationRest);

        // When
        OutAiRecommendationRest result = outAiRecoService.getByDigitalServiceUid(digitalServiceUid);

        // Then
        assertThat(result).isEqualTo(recommendationRest);
        verify(taskRepository, times(1)).findByDigitalServiceUid(digitalServiceUid);
        verify(outAiRecoRepository, times(1)).findByTaskId(task.getId());
        verify(outAiRecoMapper, times(1)).toDto(recommendation);
    }

    @Test
    void shouldReturnEmptyRecommendation_whenNoTaskFound() {
        // Given
        String digitalServiceUid = "UNKNOWN-UID";
        when(taskRepository.findByDigitalServiceUid(digitalServiceUid)).thenReturn(Optional.empty());

        // When
        OutAiRecommendationRest result = outAiRecoService.getByDigitalServiceUid(digitalServiceUid);

        // Then
        assertThat(result).isNotNull(); // Should not return null
        assertThat(result).isEqualTo(OutAiRecommendationRest.builder().build());
        verify(taskRepository, times(1)).findByDigitalServiceUid(digitalServiceUid);
        verifyNoMoreInteractions(outAiRecoRepository, outAiRecoMapper);
    }
}