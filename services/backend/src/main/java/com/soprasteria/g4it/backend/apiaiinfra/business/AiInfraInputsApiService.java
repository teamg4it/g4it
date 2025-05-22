package com.soprasteria.g4it.backend.apiaiinfra.business;

import com.soprasteria.g4it.backend.apiaiinfra.mapper.AiInfraMapper;
import com.soprasteria.g4it.backend.apiaiinfra.model.AiInfraBO;
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
import com.soprasteria.g4it.backend.server.gen.api.AiInfraInputsApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.AiInfraRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InDatacenterRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InVirtualEquipmentRest;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Optional;

public class AiInfraInputsApiService {

    @Autowired
    private  AiInfraMapper aiInfraMapper;

    @Autowired
    private DigitalServiceRepository digitalServiceRepository;

    @Autowired
    private InDatacenterMapper inDatacenterMapper;

    @Autowired
    private InDatacenterRepository inDatacenterRepository;

    @Autowired
    private InPhysicalEquipmentMapper inPhysicalEquipmentMapper;

    @Autowired
    private InPhysicalEquipmentRepository inPhysicalEquipmentRepository;

    @Autowired
    private InVirtualEquipmentMapper inVirtualEquipmentMapper;

    @Autowired
    private InVirtualEquipmentRepository inVirtualEquipmentRepository;

    public InDatacenterRest postDigitalServiceInputsAiInfra(String digitalServiceUid, AiInfraRest aiInfraRest) {
        Optional<DigitalService> digitalService = digitalServiceRepository.findById(digitalServiceUid);

        if (digitalService.isEmpty()) {
            throw new G4itRestException("404", String.format("the digital service of uid : %s, doesn't exist", digitalServiceUid));
        }

        AiInfraBO aiInfraBO = aiInfraMapper.toBO(aiInfraRest);

        if(aiInfraBO.getInfrastructureType()==AiInfraRest.InfrastructureTypeEnum.SERVER_DC){
            final InDatacenter inDatacenterToCreate = inDatacenterMapper.toEntity(InDatacenterRest.builder().build());
            final LocalDateTime now = LocalDateTime.now();
            inDatacenterToCreate.setDigitalServiceUid(digitalServiceUid);
            inDatacenterToCreate.setLocation(aiInfraBO.getLocation());
            inDatacenterToCreate.setPue(aiInfraBO.getPue());
            inDatacenterToCreate.setName("Data center Gen AI");
            inDatacenterToCreate.setCreationDate(now);
            inDatacenterToCreate.setLastUpdateDate(now);
            inDatacenterRepository.save(inDatacenterToCreate);
            return inDatacenterMapper.toRest(inDatacenterToCreate);
        } else if (aiInfraBO.getInfrastructureType()==AiInfraRest.InfrastructureTypeEnum.PHYSICAL_EQUIPMENT) {
            final InPhysicalEquipment inPhysicalEquipmentToCreate = inPhysicalEquipmentMapper.toEntity(InPhysicalEquipmentRest.builder().build());
            final LocalDateTime now = LocalDateTime.now();
            inPhysicalEquipmentToCreate.setDigitalServiceUid(digitalServiceUid);
            inPhysicalEquipmentToCreate.setLocation(aiInfraBO.getLocation());
            inPhysicalEquipmentToCreate.setName("In Physical Equipement AI");
            inPhysicalEquipmentToCreate.setCreationDate(now);
            inPhysicalEquipmentToCreate.setLastUpdateDate(now);
            inPhysicalEquipmentRepository.save(inPhysicalEquipmentToCreate);
            return null;//inPhysicalEquipmentMapper.toRest(inPhysicalEquipmentToCreate)
        }else if (aiInfraBO.getInfrastructureType()==AiInfraRest.InfrastructureTypeEnum.VIRTUAL_EQUIPMENT) {
            final InVirtualEquipment inVirtualEquipmentToCreate = inVirtualEquipmentMapper.toEntity(InVirtualEquipmentRest.builder().build());
            final LocalDateTime now = LocalDateTime.now();
            inVirtualEquipmentToCreate.setDigitalServiceUid(digitalServiceUid);
            inVirtualEquipmentToCreate.setLocation(aiInfraBO.getLocation());
            inVirtualEquipmentToCreate.setName("In Virtual Equipement AI");
            inVirtualEquipmentToCreate.setCreationDate(now);
            inVirtualEquipmentToCreate.setLastUpdateDate(now);
            inVirtualEquipmentRepository.save(inVirtualEquipmentToCreate);
            return null;//inVirtualEquipmentMapper.toRest(inVirtualEquipmentToCreate)
        }
        return null;
    }
}
