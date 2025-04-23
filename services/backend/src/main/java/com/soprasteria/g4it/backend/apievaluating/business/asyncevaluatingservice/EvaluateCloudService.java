/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice;

import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.engine.boaviztapi.EvaluateBoaviztapiService;
import com.soprasteria.g4it.backend.apievaluating.mapper.AggregationToOutput;
import com.soprasteria.g4it.backend.apievaluating.mapper.ImpactToCsvRecord;
import com.soprasteria.g4it.backend.apievaluating.model.AggValuesBO;
import com.soprasteria.g4it.backend.apievaluating.model.ImpactBO;
import com.soprasteria.g4it.backend.apiindicator.utils.CriteriaUtils;
import com.soprasteria.g4it.backend.apiinout.mapper.InputToCsvRecord;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apireferential.business.ReferentialService;
import com.soprasteria.g4it.backend.common.filesystem.business.local.CsvFileService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.InfrastructureType;
import com.soprasteria.g4it.backend.common.utils.StringUtils;
import com.soprasteria.g4it.backend.exception.AsyncTaskException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EvaluateCloudService {

    @Value("${local.working.folder}")
    private String localWorkingFolder;

    @Autowired
    ReferentialService referentialService;

    @Autowired
    InVirtualEquipmentRepository inVirtualEquipmentRepository;

    @Autowired
    CsvFileService csvFileService;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    ImpactToCsvRecord impactToCsvRecord;

    @Autowired
    InputToCsvRecord inputToCsvRecord;

    @Autowired
    AggregationToOutput aggregationToOutput;

    @Autowired
    EvaluateBoaviztapiService evaluateBoaviztapiService;

    @Autowired
    SaveService saveService;

    /**
     * Do evaluate cloud instances with boaviztapi
     *
     * @param context the context
     * @param task    the task
     */
    public void doEvaluate(final Context context, final Task task, Path exportDirectory) {

        final long start = System.currentTimeMillis();
        final Long taskId = task.getId();

        List<String> lifecycleSteps = referentialService.getLifecycleSteps();

        List<String> activeCriteria = task.getCriteria().stream().filter(CriteriaUtils.CRITERIA_MAP::containsKey)
                .map(StringUtils::kebabToSnakeCase)
                .toList();

        Map<String, Double> refSipByCriteria = referentialService.getSipValueMap(activeCriteria);

        Map<List<String>, AggValuesBO> aggregationVirtualEquipments = new HashMap<>();

        String digitalServiceUid = task.getDigitalServiceUid();

        long totalVirtualEquipments = inVirtualEquipmentRepository.countByDigitalServiceUid(digitalServiceUid);

        try (CSVPrinter csvInVirtualEquipment = csvFileService.getPrinter(FileType.VIRTUAL_EQUIPMENT_CLOUD_INSTANCE, exportDirectory);
             CSVPrinter csvVirtualEquipment = csvFileService.getPrinter(FileType.VIRTUAL_EQUIPMENT_INDICATOR_CLOUD_INSTANCE, exportDirectory)) {

            int pageNumber = 0;
            while (true) {
                Pageable page = PageRequest.of(pageNumber, Constants.BATCH_SIZE, Sort.by("name"));
                final List<InVirtualEquipment> virtualEquipments = inVirtualEquipmentRepository.findByDigitalServiceUid(digitalServiceUid, page);

                if (virtualEquipments.isEmpty()) {
                    break;
                }

                log.info("Evaluating {} virtual equipments, page {}/{}", virtualEquipments.size(), pageNumber + 1, (int) Math.ceil((double) totalVirtualEquipments / Constants.BATCH_SIZE));

                for (InVirtualEquipment virtualEquipment : virtualEquipments.stream()
                        .filter(virtualEquipment -> InfrastructureType.CLOUD_SERVICES.name().equals(virtualEquipment.getInfrastructureType()))
                        .toList()) {

                    csvInVirtualEquipment.printRecord(inputToCsvRecord.cloudToCsv(virtualEquipment, context.getDigitalServiceName()));

                    List<ImpactBO> impactBOList = evaluateBoaviztapiService.evaluate(virtualEquipment, activeCriteria, lifecycleSteps);

                    for (ImpactBO impact : impactBOList) {

                        AggValuesBO values = createAggValuesBO(virtualEquipment.getQuantity(),
                                virtualEquipment.getElectricityConsumption(), impact.getUnitImpact(), refSipByCriteria.get(impact.getCriterion()),
                                virtualEquipment.getDurationHour(), virtualEquipment.getWorkload());

                        aggregationVirtualEquipments.computeIfAbsent(
                                        aggregationToOutput.keyCloudVirtualEquipment(virtualEquipment, impact),
                                        k -> new AggValuesBO())
                                .add(values);

                        csvVirtualEquipment.printRecord(impactToCsvRecord.cloudImpactToCsv(context, task.getId(), virtualEquipment,
                                values, impact));
                    }
                }

                final long currentTotal = (long) Constants.BATCH_SIZE * pageNumber + virtualEquipments.size();

                // set progress percentage, 0% to 90% is for this process, 90% to 100% is for compressing exports
                double processFactor = 0.9;
                task.setProgressPercentage((int) Math.ceil(currentTotal * 100L * processFactor / totalVirtualEquipments) + "%");
                task.setLastUpdateDate(LocalDateTime.now());
                taskRepository.save(task);

                pageNumber++;
                virtualEquipments.clear();
            }

        } catch (IOException e) {
            log.error("Cannot write csv output files", e);
            throw new AsyncTaskException("An error occurred on writing csv files", e);
        }

        int outVirtualEquipmentSize = saveService.saveOutCloudVirtualEquipments(aggregationVirtualEquipments, taskId);

        log.info("End evaluating impacts for {}/{} in {}s and key size: {}", context.log(), taskId,
                (System.currentTimeMillis() - start) / 1000,
                outVirtualEquipmentSize);

    }

    /**
     * Create aggValuesBO  with default values
     *
     * @param quantity        the quantity
     * @param elecConsumption the elec consumption
     * @param unitImpact      the unit impact
     * @param sipValue        the sip value
     * @param usageDuration   the usage duration
     * @param workload        the workload
     * @return aggValuesBO object
     */
    private AggValuesBO createAggValuesBO(Double quantity,
                                          Double elecConsumption,
                                          Double unitImpact,
                                          Double sipValue,
                                          Double usageDuration,
                                          Double workload) {

        Double localQuantity = quantity == null ? 1d : quantity;
        Double impact = unitImpact == null ? 0d : unitImpact * localQuantity;

        return AggValuesBO.builder()
                .countValue(1L)
                .unitImpact(impact)
                .peopleEqImpact(sipValue == null ? 0d : impact / sipValue)
                .electricityConsumption(elecConsumption == null ? 0d : elecConsumption)
                .quantity(localQuantity)
                .workload(workload == null ? 0d : workload)
                .usageDuration(usageDuration == null ? 0d : usageDuration)
                .errors(new HashSet<>())
                .lifespan(0d)
                .build();
    }

}
