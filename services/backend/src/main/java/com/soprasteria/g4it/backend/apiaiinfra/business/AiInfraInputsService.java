package com.soprasteria.g4it.backend.apiaiinfra.business;

import com.soprasteria.g4it.backend.apiaiinfra.mapper.AiInfraMapper;
import com.soprasteria.g4it.backend.apiaiinfra.model.AiInfraBO;
import com.soprasteria.g4it.backend.apiaiinfra.modeldb.InAiInfrastructure;
import com.soprasteria.g4it.backend.apiaiinfra.repository.InAiInfrastructureRepository;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apiinout.mapper.InDatacenterMapper;
import com.soprasteria.g4it.backend.apiinout.mapper.InPhysicalEquipmentMapper;
import com.soprasteria.g4it.backend.apiinout.mapper.InVirtualEquipmentMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.InDatacenter;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InDatacenterRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.InAiInfrastructureRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InDatacenterRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InVirtualEquipmentRest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@Slf4j
public class AiInfraInputsService {

    @Autowired
    AiInfraMapper aiInfraMapper;

    @Autowired
    DigitalServiceRepository digitalServiceRepository;

    @Autowired
    InDatacenterMapper inDatacenterMapper;

    @Autowired
    InDatacenterRepository inDatacenterRepository;

    @Autowired
    InPhysicalEquipmentMapper inPhysicalEquipmentMapper;

    @Autowired
    InPhysicalEquipmentRepository inPhysicalEquipmentRepository;

    @Autowired
    InVirtualEquipmentMapper inVirtualEquipmentMapper;

    @Autowired
    InVirtualEquipmentRepository inVirtualEquipmentRepository;

    @Autowired
    InAiInfrastructureRepository inAiInfrastructureRepository;

    public InPhysicalEquipmentRest postDigitalServiceInputsAiInfra(String digitalServiceUid, InAiInfrastructureRest aiInfraRest) {
        Optional<DigitalService> digitalService = digitalServiceRepository.findById(digitalServiceUid);

        if (digitalService.isEmpty()) {
            throw new G4itRestException("404", String.format("the digital service of uid : %s, doesn't exist", digitalServiceUid));
        }
        final LocalDateTime now = LocalDateTime.now();

        AiInfraBO aiInfraBO = aiInfraMapper.toBO(aiInfraRest);

        final InDatacenter inDatacenterToCreate = inDatacenterMapper.toEntity(InDatacenterRest.builder().build());
        inDatacenterToCreate.setDigitalServiceUid(digitalServiceUid);
        inDatacenterToCreate.setLocation(aiInfraBO.getLocation());
        inDatacenterToCreate.setPue(aiInfraBO.getPue());
        inDatacenterToCreate.setName("DataCenter1");
        inDatacenterToCreate.setCreationDate(now);
        inDatacenterToCreate.setLastUpdateDate(now);
        inDatacenterRepository.save(inDatacenterToCreate);

        final InPhysicalEquipment inPhysicalEquipmentToCreate = inPhysicalEquipmentMapper.toEntity(InPhysicalEquipmentRest.builder().build());
        inPhysicalEquipmentToCreate.setDigitalServiceUid(digitalServiceUid);
        inPhysicalEquipmentToCreate.setName("Server1");
        inPhysicalEquipmentToCreate.setModel("blade-server--28");
        inPhysicalEquipmentToCreate.setType("Dedicated Server");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        inPhysicalEquipmentToCreate.setDateWithdrawal(LocalDate.parse("2030-05-01", formatter));
        inPhysicalEquipmentToCreate.setManufacturer("Manufacturer1");
        inPhysicalEquipmentToCreate.setDatacenterName(inDatacenterToCreate.getName());
        inPhysicalEquipmentToCreate.setCpuCoreNumber(Optional.ofNullable(aiInfraBO.getNbCpuCores()).map(Long::doubleValue).orElse(0.0));
        inPhysicalEquipmentToCreate.setLocation(inDatacenterToCreate.getLocation());
        inPhysicalEquipmentToCreate.setCpuType("CpuType1");
        inPhysicalEquipmentToCreate.setDatePurchase(now.toLocalDate());
        inPhysicalEquipmentToCreate.setSizeMemoryGb(Optional.ofNullable(aiInfraBO.getRamSize()).map(Long::doubleValue).orElse(0.0));
        inPhysicalEquipmentToCreate.setCreationDate(now);
        inPhysicalEquipmentToCreate.setQuantity(1.0);
        inPhysicalEquipmentToCreate.setLastUpdateDate(now);
        inPhysicalEquipmentRepository.save(inPhysicalEquipmentToCreate);

        final InVirtualEquipment inVirtualEquipmentToCreate = inVirtualEquipmentMapper.toEntity(InVirtualEquipmentRest.builder().build());
        inVirtualEquipmentToCreate.setDigitalServiceUid(digitalServiceUid);
        inVirtualEquipmentToCreate.setLocation(aiInfraBO.getLocation());
        inVirtualEquipmentToCreate.setName("VirtualEquipement1");
        inVirtualEquipmentToCreate.setPhysicalEquipmentName(inPhysicalEquipmentToCreate.getName());
        inVirtualEquipmentToCreate.setInfrastructureType(InAiInfrastructureRest.InfrastructureTypeEnum.SERVER_DC.getValue());
        inVirtualEquipmentToCreate.setLocation(inPhysicalEquipmentToCreate.getLocation());
        inVirtualEquipmentToCreate.setSizeMemoryGb(Optional.ofNullable(aiInfraBO.getRamSize()).map(Long::doubleValue).orElse(0.0));
        inVirtualEquipmentToCreate.setVcpuCoreNumber(Optional.ofNullable(aiInfraBO.getNbCpuCores()).map(Long::doubleValue).orElse(0.0));
        inVirtualEquipmentToCreate.setCreationDate(now);
        inVirtualEquipmentToCreate.setWorkload(0.5);
        inVirtualEquipmentToCreate.setDurationHour(8000.0);
        inVirtualEquipmentToCreate.setQuantity(1.0);
        inVirtualEquipmentToCreate.setLastUpdateDate(now);
        inVirtualEquipmentRepository.save(inVirtualEquipmentToCreate);

        final InAiInfrastructure entityToSave = aiInfraMapper.toEntity(aiInfraRest);
        entityToSave.setInfrastructureTypeEnum(aiInfraRest.getInfrastructureType());
        entityToSave.setDigitalServiceUid(digitalServiceUid);
        entityToSave.setNbGpu(aiInfraRest.getNbGpu());
        entityToSave.setGpuMemory(aiInfraRest.getGpuMemory());
        entityToSave.setComplementaryPue(aiInfraRest.getComplementaryPue());
        inAiInfrastructureRepository.save(entityToSave);

        return inPhysicalEquipmentMapper.toRest(inPhysicalEquipmentToCreate);
    }
}
