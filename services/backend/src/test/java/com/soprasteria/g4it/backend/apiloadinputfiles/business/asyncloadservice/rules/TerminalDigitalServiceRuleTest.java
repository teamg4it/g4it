/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules;

import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceReferentialService;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceType;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.DeviceTypeRef;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkobject.TerminalDigitalServiceRule;
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

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TerminalDigitalServiceRuleTest {

    @InjectMocks
    private TerminalDigitalServiceRule terminalDigitalServiceRule;

    @Mock
    private DigitalServiceReferentialService digitalServiceRefService;

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
        MockitoAnnotations.openMocks(this);

        // Default valid physical equipment object
        physicalEquipment = new InPhysicalEquipmentRest();
        physicalEquipment.setType(DigitalServiceType.TERMINAL.getValue());
        physicalEquipment.setModel("ValidModel");
        physicalEquipment.setNumberOfUsers(10.0);
        physicalEquipment.setDurationHour(8760.0); // default one year duration
    }


    @Test
    void testValidate_ValidData_NoErrors() {
        when(digitalServiceRefService.getTerminalDeviceType("ValidModel"))
                .thenReturn(new DeviceTypeRef());

        List<LineError> errors = terminalDigitalServiceRule.validate(locale, physicalEquipment, filename, line);

        assertNotNull(errors);
        assertTrue(errors.isEmpty());
    }


    @Test
    void testValidate_InvalidType_ReturnsError() {
        physicalEquipment.setType("INVALID_TYPE");

        when(digitalServiceRefService.getTerminalDeviceType("ValidModel"))
                .thenReturn(new DeviceTypeRef());
        when(messageSource.getMessage(eq("physical.eqp.type.invalid"), any(), eq(locale)))
                .thenReturn("Invalid type");

        List<LineError> errors = terminalDigitalServiceRule.validate(locale, physicalEquipment, filename, line);

        assertEquals(1, errors.size());
        assertEquals(new LineError(filename, line, "Invalid type"), errors.get(0));
    }


    @Test
    void testValidate_InvalidModel_ReturnsError() {
        physicalEquipment.setModel("InvalidModel");

        when(digitalServiceRefService.getTerminalDeviceType("InvalidModel"))
                .thenReturn(null);
        when(messageSource.getMessage(eq("referential.model.not.exist"), any(), eq(locale)))
                .thenReturn("Model does not exist");

        List<LineError> errors = terminalDigitalServiceRule.validate(locale, physicalEquipment, filename, line);

        assertEquals(1, errors.size());
        assertEquals(new LineError(filename, line, "Model does not exist"), errors.get(0));
    }


    @Test
    void testValidate_NullNumberOfUsers_ReturnsError() {
        physicalEquipment.setNumberOfUsers(null);

        when(digitalServiceRefService.getTerminalDeviceType("ValidModel"))
                .thenReturn(new DeviceTypeRef());
        when(messageSource.getMessage(eq("physical.eqp.numberOfUsers.invalid"), any(), eq(locale)))
                .thenReturn("Number of users invalid");

        List<LineError> errors = terminalDigitalServiceRule.validate(locale, physicalEquipment, filename, line);

        assertEquals(1, errors.size());
        assertEquals(new LineError(filename, line, "Number of users invalid"), errors.get(0));
    }


    @Test
    void testValidate_ZeroNumberOfUsers_ReturnsError() {
        physicalEquipment.setNumberOfUsers(0.0);

        when(digitalServiceRefService.getTerminalDeviceType("ValidModel"))
                .thenReturn(new DeviceTypeRef());
        when(messageSource.getMessage(eq("physical.eqp.numberOfUsers.invalid"), any(), eq(locale)))
                .thenReturn("Number of users invalid");

        List<LineError> errors = terminalDigitalServiceRule.validate(locale, physicalEquipment, filename, line);

        assertEquals(1, errors.size());
        assertEquals(new LineError(filename, line, "Number of users invalid"), errors.get(0));
    }


    @Test
    void testValidate_NegativeNumberOfUsers_ReturnsError() {
        physicalEquipment.setNumberOfUsers(-5.0);

        when(digitalServiceRefService.getTerminalDeviceType("ValidModel"))
                .thenReturn(new DeviceTypeRef());
        when(messageSource.getMessage(eq("physical.eqp.numberOfUsers.invalid"), any(), eq(locale)))
                .thenReturn("Number of users invalid");

        List<LineError> errors = terminalDigitalServiceRule.validate(locale, physicalEquipment, filename, line);

        assertEquals(1, errors.size());
        assertEquals(new LineError(filename, line, "Number of users invalid"), errors.get(0));
    }


    @Test
    void testValidate_ValidNumberOfUsersAndDuration_CalculatesQuantity() {
        physicalEquipment.setNumberOfUsers(5.0);
        physicalEquipment.setDurationHour(4380.0); // half year duration

        when(digitalServiceRefService.getTerminalDeviceType("ValidModel"))
                .thenReturn(new DeviceTypeRef());

        List<LineError> errors = terminalDigitalServiceRule.validate(locale, physicalEquipment, filename, line);

        assertTrue(errors.isEmpty());
        assertEquals(Math.ceil(5 * 4380.0 / 8760), physicalEquipment.getQuantity());
    }


    @Test
    void testValidate_MultipleErrorsTogether() {
        physicalEquipment.setModel("InvalidModel");
        physicalEquipment.setType("INVALID_TYPE");
        physicalEquipment.setNumberOfUsers(0.0);

        when(digitalServiceRefService.getTerminalDeviceType("InvalidModel"))
                .thenReturn(null);
        when(messageSource.getMessage(eq("referential.model.not.exist"), any(), eq(locale)))
                .thenReturn("Model does not exist");
        when(messageSource.getMessage(eq("physical.eqp.type.invalid"), any(), eq(locale)))
                .thenReturn("Invalid type");
        when(messageSource.getMessage(eq("physical.eqp.numberOfUsers.invalid"), any(), eq(locale)))
                .thenReturn("Number of users invalid");

        List<LineError> errors = terminalDigitalServiceRule.validate(locale, physicalEquipment, filename, line);

        assertEquals(3, errors.size());
    }
}
