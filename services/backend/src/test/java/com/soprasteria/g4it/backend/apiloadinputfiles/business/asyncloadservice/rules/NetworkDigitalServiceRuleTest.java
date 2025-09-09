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
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.NetworkTypeRef;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkobject.NetworkDigitalServiceRule;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NetworkDigitalServiceRuleTest {

    @InjectMocks
    private NetworkDigitalServiceRule networkDigitalServiceRule;

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


        // Default valid physical equipment object
        physicalEquipment = new InPhysicalEquipmentRest();
        physicalEquipment.setType(DigitalServiceType.NETWORK.getValue());
        physicalEquipment.setModel("ValidModel");
        physicalEquipment.setDatePurchase(LocalDate.parse(Constants.NETWORK_DATE_PURCHASE));
        physicalEquipment.setDateWithdrawal(LocalDate.parse(Constants.NETWORK_DATE_WITHDRAWAL));
    }


    @Test
    void testValidate_ValidData_NoErrors() {
        when(digitalServiceRefService.getNetworkType("ValidModel"))
                .thenReturn(new NetworkTypeRef());

        List<LineError> errors = networkDigitalServiceRule.validate(locale, physicalEquipment, filename, line);

        assertNotNull(errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidate_InvalidType_ReturnsError() {
        physicalEquipment.setType("INVALID_TYPE");

        when(digitalServiceRefService.getNetworkType("ValidModel"))
                .thenReturn(new NetworkTypeRef());
        when(messageSource.getMessage(eq("physical.eqp.type.invalid"), any(), eq(locale)))
                .thenReturn("Invalid equipment type");

        List<LineError> errors = networkDigitalServiceRule.validate(locale, physicalEquipment, filename, line);

        assertEquals(1, errors.size());
        assertEquals(new LineError(filename, line, "Invalid equipment type"), errors.get(0));

    }


    @Test
    void testValidate_InvalidPurchaseDate_ReturnsError() {
        physicalEquipment.setDatePurchase(LocalDate.of(2019, 1, 1));

        when(digitalServiceRefService.getNetworkType("ValidModel"))
                .thenReturn(new NetworkTypeRef());
        when(messageSource.getMessage(eq("date.purchase.invalid"), any(), eq(locale)))
                .thenReturn("Invalid purchase date");

        List<LineError> errors = networkDigitalServiceRule.validate(locale, physicalEquipment, filename, line);

        assertEquals(1, errors.size());
        assertEquals(new LineError(filename, line, "Invalid purchase date"), errors.get(0));

    }

    @Test
    void testValidate_NullPurchaseDate_DefaultsToConstant() {
        physicalEquipment.setDatePurchase(null);

        when(digitalServiceRefService.getNetworkType("ValidModel"))
                .thenReturn(new NetworkTypeRef());

        List<LineError> errors = networkDigitalServiceRule.validate(locale, physicalEquipment, filename, line);

        assertTrue(errors.isEmpty());
        assertEquals(LocalDate.parse(Constants.NETWORK_DATE_PURCHASE), physicalEquipment.getDatePurchase());
    }

    @Test
    void testValidate_InvalidWithdrawalDate_ReturnsError() {
        physicalEquipment.setDateWithdrawal(LocalDate.of(2022, 1, 1));

        when(digitalServiceRefService.getNetworkType("ValidModel"))
                .thenReturn(new NetworkTypeRef());
        when(messageSource.getMessage(eq("date.withdrawal.invalid"), any(), eq(locale)))
                .thenReturn("Invalid withdrawal date");

        List<LineError> errors = networkDigitalServiceRule.validate(locale, physicalEquipment, filename, line);

        assertEquals(1, errors.size());
        assertEquals(new LineError(filename, line, "Invalid withdrawal date"), errors.get(0));
    }


    @Test
    void testValidate_NullWithdrawalDate_DefaultsToConstant() {
        physicalEquipment.setDateWithdrawal(null);

        when(digitalServiceRefService.getNetworkType("ValidModel"))
                .thenReturn(new NetworkTypeRef());

        List<LineError> errors = networkDigitalServiceRule.validate(locale, physicalEquipment, filename, line);

        assertTrue(errors.isEmpty());
        assertEquals(LocalDate.parse(Constants.NETWORK_DATE_WITHDRAWAL), physicalEquipment.getDateWithdrawal());
    }


    @Test
    void testValidate_InvalidModel_ReturnsError() {
        physicalEquipment.setModel("InvalidModel");

        when(digitalServiceRefService.getNetworkType("InvalidModel")).thenReturn(null);
        when(messageSource.getMessage(eq("referential.model.not.exist"), any(), eq(locale)))
                .thenReturn("Model does not exist");

        List<LineError> errors = networkDigitalServiceRule.validate(locale, physicalEquipment, filename, line);

        assertEquals(1, errors.size());
        assertEquals(new LineError(filename, line, "Model does not exist"), errors.get(0));
    }


    @Test
    void testValidate_MultipleErrorsTogether() {
        physicalEquipment.setType("INVALID_TYPE");
        physicalEquipment.setDatePurchase(LocalDate.of(2019, 1, 1));
        physicalEquipment.setDateWithdrawal(LocalDate.of(2022, 1, 1));
        physicalEquipment.setModel("InvalidModel");

        when(digitalServiceRefService.getNetworkType("InvalidModel")).thenReturn(null);

        when(messageSource.getMessage(eq("physical.eqp.type.invalid"), any(), eq(locale)))
                .thenReturn("Invalid type");
        when(messageSource.getMessage(eq("date.purchase.invalid"), any(), eq(locale)))
                .thenReturn("Invalid purchase date");
        when(messageSource.getMessage(eq("date.withdrawal.invalid"), any(), eq(locale)))
                .thenReturn("Invalid withdrawal date");
        when(messageSource.getMessage(eq("referential.model.not.exist"), any(), eq(locale)))
                .thenReturn("Model does not exist");

        List<LineError> errors = networkDigitalServiceRule.validate(locale, physicalEquipment, filename, line);

        assertEquals(4, errors.size());
    }
}