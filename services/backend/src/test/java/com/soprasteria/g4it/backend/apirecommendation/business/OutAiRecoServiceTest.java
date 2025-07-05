package com.soprasteria.g4it.backend.apirecommendation.business;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apirecomandation.business.OutAiRecoService;
import com.soprasteria.g4it.backend.apirecomandation.mapper.OutAiRecoMapper;
import com.soprasteria.g4it.backend.apirecomandation.modeldb.OutAiReco;
import com.soprasteria.g4it.backend.apirecomandation.repository.OutAiRecoRepository;
import com.soprasteria.g4it.backend.common.task.model.TaskType;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutAiRecommendationRest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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
    @Mock
    private DigitalServiceRepository digitalServiceRepository;

    @InjectMocks
    private OutAiRecoService outAiRecoService;

    @Test
    void shouldReturnRecommendation_whenTaskExists() {
        // Given
        String digitalServiceUid = "UID-123";
        Task task = Task.builder().id(42L).build();
        DigitalService digitalService = new DigitalService();
        digitalService.setUid(digitalServiceUid);
        digitalService.setUid(digitalServiceUid);
        OutAiReco recommendation = OutAiReco.builder().build();
        OutAiRecommendationRest recommendationRest = OutAiRecommendationRest.builder().build();

        when(taskRepository.findByDigitalServiceAndType(digitalService, TaskType.EVALUATING_DIGITAL_SERVICE.toString())).thenReturn(List.of(task));
        when(digitalServiceRepository.findById(digitalServiceUid)).thenReturn(Optional.of(digitalService));
        when(outAiRecoRepository.findByTaskId(task.getId())).thenReturn(recommendation);
        when(outAiRecoMapper.toDto(recommendation)).thenReturn(recommendationRest);

        // When
        OutAiRecommendationRest result = outAiRecoService.getByDigitalServiceUid(digitalServiceUid);

        // Then
        assertThat(result).isEqualTo(recommendationRest);
        verify(taskRepository, times(1)).findByDigitalServiceAndType(digitalService, TaskType.EVALUATING_DIGITAL_SERVICE.toString());
        verify(digitalServiceRepository).findById(digitalServiceUid);
        verify(outAiRecoRepository, times(1)).findByTaskId(task.getId());
        verify(outAiRecoMapper, times(1)).toDto(recommendation);
    }

    @Test
    void shouldReturnEmptyRecommendation_whenNoTaskFound() {
        // Given
        String digitalServiceUid = "UNKNOWN-UID";
        DigitalService digitalService = new DigitalService();
        digitalService.setUid(digitalServiceUid);
        digitalService.setUid(digitalServiceUid);
        when(taskRepository.findByDigitalServiceAndType(digitalService, TaskType.EVALUATING_DIGITAL_SERVICE.toString())).thenReturn(List.of());
        when(digitalServiceRepository.findById(digitalServiceUid)).thenReturn(Optional.of(digitalService));

        // When
        OutAiRecommendationRest result = outAiRecoService.getByDigitalServiceUid(digitalServiceUid);

        // Then
        assertThat(result).isNotNull(); // Should not return null
        assertThat(result).isEqualTo(OutAiRecommendationRest.builder().build());
        verify(taskRepository, times(1)).findByDigitalServiceAndType(digitalService, TaskType.EVALUATING_DIGITAL_SERVICE.toString());
        verify(digitalServiceRepository).findById(digitalServiceUid);
        verifyNoMoreInteractions(outAiRecoRepository, outAiRecoMapper);
    }
}