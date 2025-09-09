/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules;

import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceReferentialService;
import com.soprasteria.g4it.backend.apidigitalservice.model.ServerHostBO;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkobject.NonCloudDigitalServiceRule;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NonCloudDigitalServiceRuleTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private DigitalServiceReferentialService digitalServiceRefService;

    @InjectMocks
    private NonCloudDigitalServiceRule nonCloudDigitalServiceRule;

    @Mock
    private RulePhysicalEquipmentService rulePhysicalEqpService;

    private final String filename = "testFile.csv";
    private final int line = 1;

    private InPhysicalEquipmentRest equipment;
    private Locale locale;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        locale = Locale.ENGLISH;

        equipment = new InPhysicalEquipmentRest();
        equipment.setModel("TestModel");
        equipment.setSizeDiskGb(100.0);
        equipment.setCpuCoreNumber(8.0);

        // By default, mock messageSource to return the key itself
        lenient().when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testValidate_ModelNotFound_ShouldReturnError() {
        // Arrange
        when(digitalServiceRefService.getServerHosts()).thenReturn(Collections.emptyList());

        // Act
        List<LineError> errors = nonCloudDigitalServiceRule.validate(locale, equipment, filename, line);

        // Assert
        assertEquals(1, errors.size());
        assertEquals(new LineError(filename, line, "referential.model.not.exist"), errors.get(0));
    }

    @Test
    void testValidate_StorageType_MissingDiskSize_ShouldReturnError() {
        // Arrange
        ServerHostBO host = new ServerHostBO();
        host.setReference("TestModel");
        host.setType("Storage");

        equipment.setSizeDiskGb(null); // Missing disk size

        when(digitalServiceRefService.getServerHosts()).thenReturn(Collections.singletonList(host));

        // Act
        List<LineError> errors = nonCloudDigitalServiceRule.validate(locale, equipment, filename, line);

        // Assert
        assertEquals(1, errors.size());
        assertEquals(new LineError(filename, line, "disk.size.blank"), errors.get(0));
    }

    @Test
    void testValidate_ComputeType_MissingCpuCore_ShouldReturnError() {
        // Arrange
        ServerHostBO host = new ServerHostBO();
        host.setReference("TestModel");
        host.setType("Compute");

        equipment.setCpuCoreNumber(null); // Missing CPU core number

        when(digitalServiceRefService.getServerHosts()).thenReturn(Collections.singletonList(host));

        // Act
        List<LineError> errors = nonCloudDigitalServiceRule.validate(locale, equipment, filename, line);

        // Assert
        assertEquals(1, errors.size());
        assertEquals(new LineError(filename, line, "cpu.number.blank"), errors.get(0));
    }

    @Test
    void testValidate_StorageType_ValidDiskSize_ShouldNotReturnError() {
        // Arrange
        ServerHostBO host = new ServerHostBO();
        host.setReference("TestModel");
        host.setType("Storage");

        when(digitalServiceRefService.getServerHosts()).thenReturn(Collections.singletonList(host));

        // Act
        List<LineError> errors = nonCloudDigitalServiceRule.validate(locale, equipment, filename, line);

        // Assert
        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidate_ComputeType_ValidCpuCore_ShouldNotReturnError() {
        // Arrange
        ServerHostBO host = new ServerHostBO();
        host.setReference("TestModel");
        host.setType("Compute");

        when(digitalServiceRefService.getServerHosts()).thenReturn(Collections.singletonList(host));

        // Act
        List<LineError> errors = nonCloudDigitalServiceRule.validate(locale, equipment, filename, line);

        // Assert
        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidate_MultipleHosts_ModelMatched_ShouldNotReturnError() {
        // Arrange
        ServerHostBO host1 = new ServerHostBO();
        host1.setReference("OtherModel");
        host1.setType("Storage");

        ServerHostBO host2 = new ServerHostBO();
        host2.setReference("TestModel");
        host2.setType("Compute");

        when(digitalServiceRefService.getServerHosts()).thenReturn(Arrays.asList(host1, host2));

        // Act
        List<LineError> errors = nonCloudDigitalServiceRule.validate(locale, equipment, filename, line);

        // Assert
        assertTrue(errors.isEmpty());
    }
}