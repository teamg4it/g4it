package com.soprasteria.g4it.backend.apirecomandation.business;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apirecomandation.mapper.OutAiRecoMapper;
import com.soprasteria.g4it.backend.apirecomandation.repository.OutAiRecoRepository;
import com.soprasteria.g4it.backend.common.task.model.TaskType;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutAiRecommendationRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OutAiRecoService {

    @Autowired
    private OutAiRecoRepository outAiRecoRepository;

    @Autowired
    private OutAiRecoMapper outAiRecoMapper;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private DigitalServiceRepository digitalServiceRepository;

    /**
     * Get Ai Recommendation by digital service uid
     * Find by last task
     *
     * @param digitalServiceUid the digital service uid
     * @return the ai recommendation
     */
    public OutAiRecommendationRest getByDigitalServiceUid(final String digitalServiceUid) {

        DigitalService digitalService = digitalServiceRepository.findById(digitalServiceUid).orElseThrow();
        List<Task> task = taskRepository.findByDigitalServiceAndType(digitalService, TaskType.EVALUATING_DIGITAL_SERVICE.toString());

        if (task.isEmpty()) {
            return OutAiRecommendationRest.builder().build();
        }

        return outAiRecoMapper.toDto(outAiRecoRepository.findByTaskId(task.getLast().getId()));

    }
}
