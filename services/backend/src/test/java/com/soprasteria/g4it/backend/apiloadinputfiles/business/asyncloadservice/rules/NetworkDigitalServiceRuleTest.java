/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceType;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.NetworkTypeRef;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkobject.NetworkDigitalServiceRule;
import com.soprasteria.g4it.backend.apidigitalservice.repository.NetworkTypeRefRepository;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NetworkDigitalServiceRuleTest {

    @InjectMocks
    private NetworkDigitalServiceRule networkDigitalServiceRule;

    @Mock
    private MessageSource messageSource;

    @Mock
    private NetworkTypeRefRepository networkTypeRefRepository;

    private final Locale locale = Locale.ENGLISH;
    private final String filename = "testFile.csv";
    private final int line = 1;
    private InPhysicalEquipmentRest physicalEquipment;

    @BeforeEach
    void setUp() {
        physicalEquipment = new InPhysicalEquipmentRest();
        physicalEquipment.setType(DigitalServiceType.NETWORK.getValue());
        physicalEquipment.setModel("ValidModel");
        // Dates will always be overwritten by the rule anyway
        physicalEquipment.setDatePurchase(LocalDate.of(2000, 1, 1));
        physicalEquipment.setDateWithdrawal(LocalDate.of(2000, 1, 1));
    }

    @Test
    void testValidate_ValidData_NoErrors() {
        when(networkTypeRefRepository.findByReference("ValidModel"))
                .thenReturn(Optional.of(new NetworkTypeRef()));

        List<LineError> errors = networkDigitalServiceRule.validate(locale, physicalEquipment, filename, line);

        assertNotNull(errors);
        assertTrue(errors.isEmpty());
        // Dates are set to default constants
        assertEquals(LocalDate.parse(Constants.NETWORK_DATE_PURCHASE), physicalEquipment.getDatePurchase());
        assertEquals(LocalDate.parse(Constants.NETWORK_DATE_WITHDRAWAL), physicalEquipment.getDateWithdrawal());
    }

    @Test
    void testValidate_InvalidType_ReturnsError() {
        physicalEquipment.setType("INVALID_TYPE");
        when(networkTypeRefRepository.findByReference("ValidModel"))
                .thenReturn(Optional.of(new NetworkTypeRef()));
        when(messageSource.getMessage(eq("physical.eqp.type.invalid"), any(), eq(locale)))
                .thenReturn("Invalid equipment type");

        List<LineError> errors = networkDigitalServiceRule.validate(locale, physicalEquipment, filename, line);

        assertEquals(1, errors.size());
        assertEquals(new LineError(filename, line, "Invalid equipment type"), errors.getFirst());
    }

    @Test
    void testValidate_InvalidModel_ReturnsError() {
        physicalEquipment.setModel("InvalidModel");
        when(networkTypeRefRepository.findByReference("InvalidModel")).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("referential.model.not.exist"), any(), eq(locale)))
                .thenReturn("Model does not exist");

        List<LineError> errors = networkDigitalServiceRule.validate(locale, physicalEquipment, filename, line);

        assertEquals(1, errors.size());
        assertEquals(new LineError(filename, line, "Model does not exist"), errors.getFirst());
    }

    @Test
    void testValidate_MultipleErrorsTogether() {
        physicalEquipment.setType("INVALID_TYPE");
        physicalEquipment.setModel("InvalidModel");
        when(networkTypeRefRepository.findByReference("InvalidModel")).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("physical.eqp.type.invalid"), any(), eq(locale)))
                .thenReturn("Invalid type");
        when(messageSource.getMessage(eq("referential.model.not.exist"), any(), eq(locale)))
                .thenReturn("Model does not exist");

        List<LineError> errors = networkDigitalServiceRule.validate(locale, physicalEquipment, filename, line);

        assertEquals(2, errors.size());
    }
}
