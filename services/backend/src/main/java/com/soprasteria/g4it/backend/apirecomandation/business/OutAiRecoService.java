package com.soprasteria.g4it.backend.apirecomandation.business;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutAiRecommendationRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutPhysicalEquipmentRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.soprasteria.g4it.backend.apirecomandation.modeldb.OutAiReco;
import com.soprasteria.g4it.backend.apirecomandation.repository.OutAiRecoRepository;
import com.soprasteria.g4it.backend.apirecomandation.mapper.OutAiRecoMapper;
import java.util.List;
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
     * @return the list of aggregated physical equipments
     */
    public List<OutAiRecommendationRest> getByDigitalServiceUid(final String digitalServiceUid) {

        Optional<Task> task = taskRepository.findByDigitalServiceUid(digitalServiceUid);

        if (task.isEmpty()) {
            return List.of();
        }

        return outAiRecoMapper.toDtoList(
                outAiRecoRepository.findByTaskId(task.get().getId())
        );

    }
}
