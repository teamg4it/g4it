package com.soprasteria.g4it.backend.apiinout.business;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceVersion;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceVersionRepository;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.server.gen.api.dto.DigitalServiceVersionComparison;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutPhysicalEquipmentRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutVirtualEquipmentRest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class OutDigitalServiceComparisonService {

    private final OutPhysicalEquipmentService outPhysicalEquipmentService;
    private final OutVirtualEquipmentService outVirtualEquipmentService;
    private final DigitalServiceVersionRepository digitalServiceVersionRepository;
    private final TaskRepository taskRepository;

    public List<DigitalServiceVersionComparison> compareDigitalServiceVersions(String versionAUid, String versionBUid) {

        // ---- Load Version A ----
        DigitalServiceVersion digitalServiceVersionA = digitalServiceVersionRepository.findById(versionAUid)
                .orElseThrow();

        Optional<Task> taskA = taskRepository.findByDigitalServiceVersionAndLastCreationDate(digitalServiceVersionA);

        List<OutPhysicalEquipmentRest> physicalA =
                taskA.isEmpty() ? List.of() : outPhysicalEquipmentService.getByDigitalServiceVersionUid(versionAUid);

        List<OutVirtualEquipmentRest> virtualA =
                taskA.isEmpty() ? List.of() : outVirtualEquipmentService.getByDigitalServiceVersionUid(versionAUid);

        DigitalServiceVersionComparison itemA = DigitalServiceVersionComparison.builder()
                .versionId(versionAUid)
                .versionName(digitalServiceVersionA.getDescription())
                .physicalEquipment(physicalA)
                .virtualEquipment(virtualA)
                .build();


        // ---- Load Version B ----
        DigitalServiceVersion digitalServiceVersionB = digitalServiceVersionRepository.findById(versionBUid)
                .orElseThrow();

        Optional<Task> taskB = taskRepository.findByDigitalServiceVersionAndLastCreationDate(digitalServiceVersionB);

        List<OutPhysicalEquipmentRest> physicalB =
                taskB.isEmpty() ? List.of() : outPhysicalEquipmentService.getByDigitalServiceVersionUid(versionBUid);

        List<OutVirtualEquipmentRest> virtualB =
                taskB.isEmpty() ? List.of() : outVirtualEquipmentService.getByDigitalServiceVersionUid(versionBUid);

        DigitalServiceVersionComparison itemB = DigitalServiceVersionComparison.builder()
                .versionId(versionBUid)
                .versionName(digitalServiceVersionB.getDescription())
                .physicalEquipment(physicalB)
                .virtualEquipment(virtualB)
                .build();


        List<DigitalServiceVersionComparison> result = new ArrayList<>();
        result.add(itemA);
        result.add(itemB);
        return result;


    }
}