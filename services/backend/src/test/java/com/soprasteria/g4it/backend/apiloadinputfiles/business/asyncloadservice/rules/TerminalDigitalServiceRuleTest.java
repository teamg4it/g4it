/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceType;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.DeviceTypeRef;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkobject.TerminalDigitalServiceRule;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DeviceTypeRefRepository;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TerminalDigitalServiceRuleTest {

    @InjectMocks
    private TerminalDigitalServiceRule terminalDigitalServiceRule;

    @Mock
    private DeviceTypeRefRepository deviceTypeRefRepository;

    @Mock
    private MessageSource messageSource;

    @Mock
    private RulePhysicalEquipmentService rulePhysicalEqpService;

    private final Locale locale = Locale.ENGLISH;
    private final String filename = "testFile.csv";
    private final int line = 1;
    private InPhysicalEquipmentRest physicalEquipment;

    @BeforeEach
    void setUp() {
        physicalEquipment = new InPhysicalEquipmentRest();
        physicalEquipment.setType(DigitalServiceType.TERMINAL.getValue());
        physicalEquipment.setModel("ValidModel");
        physicalEquipment.setNumberOfUsers(10.0);
        physicalEquipment.setDurationHour(8760.0);
    }

    @Test
    void testValidate_ValidData_NoErrors() {
        when(deviceTypeRefRepository.findByReference("ValidModel"))
                .thenReturn(Optional.of(new DeviceTypeRef()));
        when(rulePhysicalEqpService.checkDurationHour(locale, filename, line, 8760.0))
                .thenReturn(Optional.empty());

        List<LineError> errors = terminalDigitalServiceRule.validate(locale, physicalEquipment, filename, line);

        assertNotNull(errors);
        assertTrue(errors.isEmpty());
        assertEquals(10.0 * 8760.0 / 8760, physicalEquipment.getQuantity());
    }

    @Test
    void testValidate_InvalidType_ReturnsError() {
        physicalEquipment.setType("INVALID_TYPE");
        when(deviceTypeRefRepository.findByReference("ValidModel")).thenReturn(Optional.of(new DeviceTypeRef()));
        when(rulePhysicalEqpService.checkDurationHour(locale, filename, line, 8760.0)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("physical.eqp.type.invalid"), any(), eq(locale)))
                .thenReturn("Invalid type");

        List<LineError> errors = terminalDigitalServiceRule.validate(locale, physicalEquipment, filename, line);

        assertEquals(1, errors.size());
        assertEquals(new LineError(filename, line, "Invalid type"), errors.getFirst());
    }

    @Test
    void testValidate_InvalidModel_ReturnsError() {
        physicalEquipment.setModel("InvalidModel");
        when(deviceTypeRefRepository.findByReference("InvalidModel")).thenReturn(Optional.empty());
        when(rulePhysicalEqpService.checkDurationHour(locale, filename, line, 8760.0)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("referential.model.not.exist"), any(), eq(locale)))
                .thenReturn("Model does not exist");

        List<LineError> errors = terminalDigitalServiceRule.validate(locale, physicalEquipment, filename, line);

        assertEquals(1, errors.size());
        assertEquals(new LineError(filename, line, "Model does not exist"), errors.getFirst());
    }

    @Test
    void testValidate_NullNumberOfUsers_ReturnsError() {
        physicalEquipment.setNumberOfUsers(null);
        when(deviceTypeRefRepository.findByReference("ValidModel")).thenReturn(Optional.of(new DeviceTypeRef()));
        when(rulePhysicalEqpService.checkDurationHour(locale, filename, line, 8760.0)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("physical.eqp.numberOfUsers.invalid"), any(), eq(locale)))
                .thenReturn("Number of users invalid");

        List<LineError> errors = terminalDigitalServiceRule.validate(locale, physicalEquipment, filename, line);

        assertEquals(1, errors.size());
        assertEquals(new LineError(filename, line, "Number of users invalid"), errors.getFirst());
    }

    @Test
    void testValidate_ZeroOrNegativeNumberOfUsers_ReturnsError() {
        physicalEquipment.setNumberOfUsers(0.0);
        when(deviceTypeRefRepository.findByReference("ValidModel")).thenReturn(Optional.of(new DeviceTypeRef()));
        when(rulePhysicalEqpService.checkDurationHour(locale, filename, line, 8760.0)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("physical.eqp.numberOfUsers.invalid"), any(), eq(locale)))
                .thenReturn("Number of users invalid");

        List<LineError> errorsZero = terminalDigitalServiceRule.validate(locale, physicalEquipment, filename, line);

        assertEquals(1, errorsZero.size());
        assertEquals(new LineError(filename, line, "Number of users invalid"), errorsZero.getFirst());

        physicalEquipment.setNumberOfUsers(-5.0);
        List<LineError> errorsNeg = terminalDigitalServiceRule.validate(locale, physicalEquipment, filename, line);

        assertEquals(1, errorsNeg.size());
        assertEquals(new LineError(filename, line, "Number of users invalid"), errorsNeg.getFirst());
    }

    @Test
    void testValidate_ValidNumberOfUsersAndDuration_CalculatesQuantity() {
        physicalEquipment.setNumberOfUsers(5.0);
        physicalEquipment.setDurationHour(4380.0);
        when(deviceTypeRefRepository.findByReference("ValidModel")).thenReturn(Optional.of(new DeviceTypeRef()));
        when(rulePhysicalEqpService.checkDurationHour(locale, filename, line, 4380.0)).thenReturn(Optional.empty());

        List<LineError> errors = terminalDigitalServiceRule.validate(locale, physicalEquipment, filename, line);

        assertTrue(errors.isEmpty());
        assertEquals(5.0 * 4380.0 / 8760, physicalEquipment.getQuantity());
    }

    @Test
    void testValidate_DurationHourReturnsError() {
        when(deviceTypeRefRepository.findByReference("ValidModel")).thenReturn(Optional.of(new DeviceTypeRef()));
        when(rulePhysicalEqpService.checkDurationHour(locale, filename, line, 8760.0))
                .thenReturn(Optional.of(new LineError(filename, line, "Invalid duration")));

        List<LineError> errors = terminalDigitalServiceRule.validate(locale, physicalEquipment, filename, line);

        assertEquals(1, errors.size());
        assertEquals(new LineError(filename, line, "Invalid duration"), errors.getFirst());
    }

    @Test
    void testValidate_MultipleErrorsTogether() {
        physicalEquipment.setModel("InvalidModel");
        physicalEquipment.setType("INVALID_TYPE");
        physicalEquipment.setNumberOfUsers(0.0);

        when(deviceTypeRefRepository.findByReference("InvalidModel")).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("referential.model.not.exist"), any(), eq(locale)))
                .thenReturn("Model does not exist");
        when(messageSource.getMessage(eq("physical.eqp.type.invalid"), any(), eq(locale)))
                .thenReturn("Invalid type");
        when(messageSource.getMessage(eq("physical.eqp.numberOfUsers.invalid"), any(), eq(locale)))
                .thenReturn("Number of users invalid");
        when(rulePhysicalEqpService.checkDurationHour(locale, filename, line, 8760.0))
                .thenReturn(Optional.empty());

        List<LineError> errors = terminalDigitalServiceRule.validate(locale, physicalEquipment, filename, line);

        assertEquals(3, errors.size());

    }
}
