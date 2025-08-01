package com.soprasteria.g4it.backend.apiaiinfra.business;

import com.soprasteria.g4it.backend.apiaiinfra.mapper.InAiInfrastructureMapper;
import com.soprasteria.g4it.backend.apiaiinfra.model.InAiInfrastructureBO;
import com.soprasteria.g4it.backend.apiaiinfra.modeldb.InAiInfrastructure;
import com.soprasteria.g4it.backend.apiaiinfra.repository.InAiInfrastructureRepository;
import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceReferentialService;
import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceService;
import com.soprasteria.g4it.backend.apidigitalservice.model.DigitalServiceBO;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.EcomindTypeRef;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apiinout.business.InDatacenterService;
import com.soprasteria.g4it.backend.apiinout.business.InPhysicalEquipmentService;
import com.soprasteria.g4it.backend.apiinout.business.InVirtualEquipmentService;
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
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class InAiInfrastructureService {

    @Autowired
    InAiInfrastructureMapper inAiInfrastructureMapper;

    @Autowired
    DigitalServiceRepository digitalServiceRepository;
    @Autowired
    DigitalServiceService digitalServiceService;

    @Autowired
    DigitalServiceReferentialService digitalServiceReferentialService;


    @Autowired
    InDatacenterMapper inDatacenterMapper;

    @Autowired
    InDatacenterRepository inDatacenterRepository;

    @Autowired
    InDatacenterService inDatacenterService;
    @Autowired
    InPhysicalEquipmentService inPhysicalEquipmentService;
    @Autowired
    InVirtualEquipmentService inVirtualEquipmentService;

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

    /**
     * Saves inAiInfrastructureRest information in tables InDatacenter, InPhysicalEquipment, InVirtualEquipment and InAiInfrastructure.
     *
     * @param digitalServiceUid      - The digital Service uid
     * @param inAiInfrastructureRest - The Rest object of the inAiInfrastructure
     * @return The new physical equipment
     */
    public InPhysicalEquipmentRest postDigitalServiceInputsAiInfra(String digitalServiceUid, InAiInfrastructureRest inAiInfrastructureRest) {
        Optional<DigitalService> digitalService = digitalServiceRepository.findById(digitalServiceUid);

        if (digitalService.isEmpty()) {
            throw new G4itRestException("404", String.format("the digital service of uid : %s, doesn't exist", digitalServiceUid));
        }
        final LocalDateTime now = LocalDateTime.now();

        InAiInfrastructureBO inAiInfrastructureBO = inAiInfrastructureMapper.toBO(inAiInfrastructureRest);

        final InDatacenter inDatacenterToCreate = inDatacenterMapper.toEntity(InDatacenterRest.builder().build());
        inDatacenterToCreate.setDigitalServiceUid(digitalServiceUid);
        inDatacenterToCreate.setLocation(inAiInfrastructureBO.getLocation());
        inDatacenterToCreate.setPue(inAiInfrastructureBO.getPue());
        inDatacenterToCreate.setName("Datacenter");
        inDatacenterToCreate.setCreationDate(now);
        inDatacenterToCreate.setLastUpdateDate(now);
        inDatacenterRepository.save(inDatacenterToCreate);

        final InPhysicalEquipment inPhysicalEquipmentToCreate = inPhysicalEquipmentMapper.toEntity(InPhysicalEquipmentRest.builder().build());
        inPhysicalEquipmentToCreate.setDigitalServiceUid(digitalServiceUid);
        inPhysicalEquipmentToCreate.setName("Physical equipment");
        inPhysicalEquipmentToCreate.setModel(inAiInfrastructureBO.getInfrastructureType().getCode());
        inPhysicalEquipmentToCreate.setType("Dedicated Server");

        // dateWithdrawal
        double lifespan = inAiInfrastructureBO.getInfrastructureType().getLifespan().intValue();
        int numberYear = inAiInfrastructureBO.getInfrastructureType().getLifespan().intValue();
        int month = (int) Math.round((numberYear - lifespan) * 12);
        inPhysicalEquipmentToCreate.setDateWithdrawal(LocalDate.from(now.plusYears(numberYear).plusMonths(month)));

        inPhysicalEquipmentToCreate.setManufacturer("Manufacturer1");
        inPhysicalEquipmentToCreate.setDatacenterName(inDatacenterToCreate.getName());
        inPhysicalEquipmentToCreate.setCpuCoreNumber(Optional.ofNullable(inAiInfrastructureBO.getNbCpuCores()).map(Long::doubleValue).orElse(0.0));
        inPhysicalEquipmentToCreate.setLocation(inDatacenterToCreate.getLocation());
        inPhysicalEquipmentToCreate.setDatePurchase(now.toLocalDate());
        inPhysicalEquipmentToCreate.setSizeMemoryGb(Optional.ofNullable(inAiInfrastructureBO.getRamSize()).map(Long::doubleValue).orElse(0.0));
        inPhysicalEquipmentToCreate.setCreationDate(now);
        inPhysicalEquipmentToCreate.setQuantity(1.0);
        inPhysicalEquipmentToCreate.setLastUpdateDate(now);
        inPhysicalEquipmentRepository.save(inPhysicalEquipmentToCreate);

        final InVirtualEquipment inVirtualEquipmentToCreate = inVirtualEquipmentMapper.toEntity(InVirtualEquipmentRest.builder().build());
        inVirtualEquipmentToCreate.setDigitalServiceUid(digitalServiceUid);
        inVirtualEquipmentToCreate.setName("Virtual equipment");
        inVirtualEquipmentToCreate.setPhysicalEquipmentName(inPhysicalEquipmentToCreate.getName());
        inVirtualEquipmentToCreate.setInfrastructureType(inAiInfrastructureBO.getInfrastructureType().getCode());
        inVirtualEquipmentToCreate.setLocation(inPhysicalEquipmentToCreate.getLocation());
        inVirtualEquipmentToCreate.setSizeMemoryGb(Optional.ofNullable(inAiInfrastructureBO.getRamSize()).map(Long::doubleValue).orElse(0.0));
        inVirtualEquipmentToCreate.setVcpuCoreNumber(Optional.ofNullable(inAiInfrastructureBO.getNbCpuCores()).map(Long::doubleValue).orElse(0.0));
        inVirtualEquipmentToCreate.setCreationDate(now);
        inVirtualEquipmentToCreate.setWorkload(1.0);
        inVirtualEquipmentToCreate.setQuantity(1.0);
        inVirtualEquipmentToCreate.setLastUpdateDate(now);
        inVirtualEquipmentRepository.save(inVirtualEquipmentToCreate);

        final InAiInfrastructure inAiInfrastructure = inAiInfrastructureMapper.toEntity(inAiInfrastructureRest);
        inAiInfrastructure.setDigitalServiceUid(digitalServiceUid);
        inAiInfrastructureRepository.save(inAiInfrastructure);

        return inPhysicalEquipmentMapper.toRest(inPhysicalEquipmentToCreate);
    }

    /**
     * Get the InAiInfrastructureBO that was in InDatacenter, InPhysicalEquipment, InVirtualEquipment and InAiInfrastructure.
     *
     * @param digitalServiceUid - The digital service uid
     * @return The InAiInfrastructureBO with all the information
     */
    public InAiInfrastructureBO getDigitalServiceInputsAiInfraRest(String digitalServiceUid) {
        DigitalServiceBO digitalService = digitalServiceService.getDigitalService(digitalServiceUid);

        if (digitalService == null) {
            throw new G4itRestException("404", String.format("the digital service of uid : %s, doesn't exist", digitalServiceUid));
        }


        InAiInfrastructure inAiInfrastructure = inAiInfrastructureRepository.findByDigitalServiceUid(digitalServiceUid);

        InAiInfrastructureBO inAiInfrastructureBO = inAiInfrastructureMapper.entityToBO(inAiInfrastructure);
        if (inAiInfrastructure != null) {
            //set the ecomind type
            EcomindTypeRef ecomindTypeRef = digitalServiceReferentialService.getEcomindDeviceType(inAiInfrastructureBO.getInfrastructureType().getCode());
            inAiInfrastructureBO.getInfrastructureType().setValue(ecomindTypeRef.getDescription());
            inAiInfrastructureBO.getInfrastructureType().setLifespan(ecomindTypeRef.getLifespan());
            List<InDatacenterRest> InDatacenter = inDatacenterService.getByDigitalService(digitalServiceUid);
            for (InDatacenterRest inDatacenterRest : InDatacenter) {
                inAiInfrastructureBO.setPue(inDatacenterRest.getPue());
                inAiInfrastructureBO.setLocation(inDatacenterRest.getLocation());
            }
            List<InPhysicalEquipmentRest> inPhysicalEquipments = inPhysicalEquipmentService.getByDigitalService(digitalServiceUid);
            for (InPhysicalEquipmentRest inPhysicalEquipmentRest : inPhysicalEquipments) {
                inAiInfrastructureBO.setNbCpuCores(Optional.ofNullable(inPhysicalEquipmentRest.getCpuCoreNumber()).map(Double::longValue).orElse(0L));
                inAiInfrastructureBO.setRamSize(Optional.ofNullable(inPhysicalEquipmentRest.getSizeMemoryGb()).map(Double::longValue).orElse(0L));
            }
        }

        return inAiInfrastructureBO;
    }

    /**
     * Update inAiInfrastructureRest information in tables InDatacenter, InPhysicalEquipment, InVirtualEquipment and InAiInfrastructure
     *
     * @param digitalServiceUid      - The digital service uid
     * @param inAiInfrastructureRest - The Rest object of the inAiInfrastructure
     * @return The new physical equipment
     */
    public InPhysicalEquipmentRest updateDigitalServiceInputsAiInfraRest(String digitalServiceUid, InAiInfrastructureRest inAiInfrastructureRest) {
        Optional<DigitalService> digitalService = digitalServiceRepository.findById(digitalServiceUid);

        if (digitalService.isEmpty()) {
            throw new G4itRestException("404", String.format("the digital service of uid : %s, doesn't exist", digitalServiceUid));
        }
        final LocalDateTime now = LocalDateTime.now();

        InAiInfrastructureBO inAiInfrastructureBO = inAiInfrastructureMapper.toBO(inAiInfrastructureRest);

        Long idInAiInfrastructure = inAiInfrastructureRepository.findByDigitalServiceUid(digitalServiceUid).getId();
        InAiInfrastructure inAiInfrastructure = inAiInfrastructureMapper.toEntity(inAiInfrastructureRest);
        inAiInfrastructure.setDigitalServiceUid(digitalServiceUid);
        inAiInfrastructure.setId(idInAiInfrastructure);
        inAiInfrastructureRepository.save(inAiInfrastructure);

        List<InDatacenterRest> inDatacenter = inDatacenterService.getByDigitalService(digitalServiceUid);
        // because there is only one datacenter in this case
        InDatacenterRest inDatacenterRest = inDatacenter.getFirst();
        inDatacenterRest.setLocation(inAiInfrastructureBO.getLocation());
        inDatacenterRest.setPue(inAiInfrastructureBO.getPue());
        inDatacenterRest.setLastUpdateDate(now);
        inDatacenterService.updateInDatacenter(digitalServiceUid, inDatacenterRest.getId(), inDatacenterRest);

        List<InPhysicalEquipmentRest> inPhysicalEquipments = inPhysicalEquipmentService.getByDigitalService(digitalServiceUid);
        // because there is only one InPhysicalEquipmentRest in this case
        InPhysicalEquipmentRest inPhysicalEquipmentRest = inPhysicalEquipments.getFirst();

        // dateWithdrawal
        double lifespan = inAiInfrastructureBO.getInfrastructureType().getLifespan().intValue();
        int numberYear = inAiInfrastructureBO.getInfrastructureType().getLifespan().intValue();
        int month = (int) Math.round((numberYear - lifespan) * 12);
        inPhysicalEquipmentRest.setDateWithdrawal(LocalDate.from(now.plusYears(numberYear).plusMonths(month)));

        inPhysicalEquipmentRest.setCpuCoreNumber(Optional.ofNullable(inAiInfrastructureBO.getNbCpuCores()).map(Long::doubleValue).orElse(0.0));
        inPhysicalEquipmentRest.setLocation(inDatacenterRest.getLocation());
        inPhysicalEquipmentRest.setSizeMemoryGb(Optional.ofNullable(inAiInfrastructureBO.getRamSize()).map(Long::doubleValue).orElse(0.0));
        inPhysicalEquipmentRest.setLastUpdateDate(now);
        inPhysicalEquipmentService.updateInPhysicalEquipment(digitalServiceUid, inPhysicalEquipmentRest.getId(), inPhysicalEquipmentRest);

        List<InVirtualEquipmentRest> inVirtualEquipments = inVirtualEquipmentService.getByDigitalService(digitalServiceUid);
        // because there is only one InPhysicalEquipmentRest in this case
        InVirtualEquipmentRest inVirtualEquipmentRest = inVirtualEquipments.getFirst();
        inVirtualEquipmentRest.setLocation(inAiInfrastructureBO.getLocation());
        inVirtualEquipmentRest.setPhysicalEquipmentName(inPhysicalEquipmentRest.getName());
        inVirtualEquipmentRest.setInfrastructureType(inAiInfrastructureBO.getInfrastructureType().getValue());
        inVirtualEquipmentRest.setLocation(inPhysicalEquipmentRest.getLocation());
        inVirtualEquipmentRest.setSizeMemoryGb(Optional.ofNullable(inAiInfrastructureBO.getRamSize()).map(Long::doubleValue).orElse(0.0));
        inVirtualEquipmentRest.setVcpuCoreNumber(Optional.ofNullable(inAiInfrastructureBO.getNbCpuCores()).map(Long::doubleValue).orElse(0.0));
        inVirtualEquipmentRest.setLastUpdateDate(now);
        inVirtualEquipmentService.updateInVirtualEquipment(digitalServiceUid, inVirtualEquipmentRest.getId(), inVirtualEquipmentRest);

        return inPhysicalEquipmentRest;
    }
}
