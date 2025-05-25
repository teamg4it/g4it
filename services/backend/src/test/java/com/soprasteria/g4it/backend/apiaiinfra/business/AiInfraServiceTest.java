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
import com.soprasteria.g4it.backend.server.gen.api.dto.AiInfraRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InDatacenterRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InVirtualEquipmentRest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AiInfraServiceTest {
    @InjectMocks
    private AiInfraInputsService service;

    @Mock private AiInfraMapper aiInfraMapper;
    @Mock private DigitalServiceRepository digitalServiceRepository;
    @Mock private InDatacenterRepository inDatacenterRepository;
    @Mock private InVirtualEquipmentRepository inVirtualEquipmentRepository;
    @Mock private AiInfraInputsService aiInfraService;


    @Test
    void testPostDigitalServiceInputsAiInfra_digitalServiceNotFound() {
        // GIVEN
        String uid = "nonexistent";
        when(digitalServiceRepository.findById(uid)).thenReturn(Optional.empty());

        // THEN
        G4itRestException ex = assertThrows(G4itRestException.class, () ->
                service.postDigitalServiceInputsAiInfra(uid, new AiInfraRest())
        );
        assertEquals("404", ex.getCode());
        assertTrue(ex.getMessage().contains(uid));
    }
}
