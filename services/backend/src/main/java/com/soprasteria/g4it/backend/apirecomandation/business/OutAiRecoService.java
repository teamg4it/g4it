package com.soprasteria.g4it.backend.apirecomandation.business;

import com.soprasteria.g4it.backend.apirecomandation.mapper.OutAiRecoMapper;
import com.soprasteria.g4it.backend.apirecomandation.repository.OutAiRecoRepository;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutAiRecommendationRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OutAiRecoService {

    @Autowired
    private OutAiRecoRepository outAiRecoRepository;

    @Autowired
    private OutAiRecoMapper outAiRecoMapper;
    @Autowired
    private TaskRepository taskRepository;

    /**
     * Get Ai Recommendation by digital service uid
     * Find by last task
     *
     * @param digitalServiceUid the digital service uid
     * @return the ai recommendation
     */
    public OutAiRecommendationRest getByDigitalServiceUid(final String digitalServiceUid) {

        Optional<Task> task = taskRepository.findByDigitalServiceUid(digitalServiceUid);

        if (task.isEmpty()) {
            return OutAiRecommendationRest.builder().build();
        }

        return outAiRecoMapper.toDto(outAiRecoRepository.findByTaskId(task.get().getId()));

    }
}
