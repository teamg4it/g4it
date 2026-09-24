package com.soprasteria.g4it.backend.apiinout.business;

import com.soprasteria.g4it.backend.apiinout.mapper.OutAiServiceMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.OutAiService;
import com.soprasteria.g4it.backend.apiinout.repository.OutAiServiceRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutAiServiceServiceTest {

    @InjectMocks
    private OutAiServiceService outAiServiceService;

    @Mock
    private OutAiServiceRepository outAiServiceRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private OutAiServiceMapper outAiServiceMapper;

    @Mock
    private EntityManager entityManager;

    @Test
    void getByInventory_returnsEmptyList_whenNoTaskFound() {
        Inventory inventory = new Inventory();
        when(taskRepository.findByInventoryAndLastCreationDate(inventory)).thenReturn(Optional.empty());

        List<?> result = outAiServiceService.getByInventory(inventory);

        assertEquals(List.of(), result);
        verifyNoInteractions(outAiServiceRepository, outAiServiceMapper);
    }

    @Test
    void getByInventory_returnsMappedAiServices_whenTaskFound() {
        Inventory inventory = new Inventory();
        Task task = new Task();
        task.setId(1L);

        List<OutAiService> aiServices = List.of(new OutAiService());
        List<?> mapped = List.of(new Object());

        when(taskRepository.findByInventoryAndLastCreationDate(inventory)).thenReturn(Optional.of(task));
        doReturn(aiServices, List.of()).when(outAiServiceRepository)
                .findByTaskIdOrderByIdAsc(eq(1L), any(Pageable.class));
        when(outAiServiceMapper.toRest(aiServices)).thenReturn((List) mapped);

        List<?> result = outAiServiceService.getByInventory(inventory);

        assertEquals(mapped, result);
        verify(outAiServiceRepository, atLeast(1)).findByTaskIdOrderByIdAsc(eq(1L), any(Pageable.class));
        verify(outAiServiceMapper).toRest(aiServices);
        verify(entityManager).clear();
    }
}