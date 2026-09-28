/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.functionaltest;

import com.soprasteria.g4it.backend.apiaiinfra.repository.InAiInfrastructureRepository;
import com.soprasteria.g4it.backend.apiinout.repository.*;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckApplicationRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckDatacenterRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.repository.CheckVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiparameterai.repository.InAiParameterRepository;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.external.boavizta.business.BoaviztapiService;
import com.soprasteria.g4it.backend.external.boavizta.client.BoaviztapiClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


@SpringBootTest(
        properties = {
                "spring.cloud.azure.storage.blob.enabled=false",
                "spring.liquibase.enabled=false",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.cloud.azure.keyvault.secret.enabled=false"
        }
)
@ActiveProfiles({"local", "test"})
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FunctionalTests {

    @MockitoBean
    private BoaviztapiClient boaviztapiClient;
    @Autowired
    TaskRepository taskRepository;
    @Autowired
    InDatacenterRepository inDatacenterRepository;
    @Autowired
    InPhysicalEquipmentRepository inPhysicalEquipmentRepository;
    @Autowired
    InVirtualEquipmentRepository inVirtualEquipmentRepository;
    @Autowired
    InApplicationRepository inApplicationRepository;
    @Autowired
    InAiServiceRepository inAiServiceRepository;
    @Autowired
    InAiInfrastructureRepository inAiInfrastructureRepository;
    @Autowired
    InAiParameterRepository inAiParameterRepository;
    @Autowired
    CheckDatacenterRepository checkDatacenterRepository;
    @Autowired
    CheckVirtualEquipmentRepository checkVirtualEquipmentRepository;
    @Autowired
    CheckPhysicalEquipmentRepository checkPhysicalEquipmentRepository;
    @Autowired
    CheckApplicationRepository checkApplicationRepository;


    @MockitoBean
    BoaviztapiService boaviztapiService;

    public void cleanDB() {
        checkDatacenterRepository.deleteAll();
        checkVirtualEquipmentRepository.deleteAll();
        checkPhysicalEquipmentRepository.deleteAll();
        checkApplicationRepository.deleteAll();
        inDatacenterRepository.deleteAll();
        inPhysicalEquipmentRepository.deleteAll();
        inVirtualEquipmentRepository.deleteAll();
        inApplicationRepository.deleteAll();
        inAiServiceRepository.deleteAll();
        inAiInfrastructureRepository.deleteAll();
        inAiParameterRepository.deleteAll();

        taskRepository.deleteAll();
    }
}
