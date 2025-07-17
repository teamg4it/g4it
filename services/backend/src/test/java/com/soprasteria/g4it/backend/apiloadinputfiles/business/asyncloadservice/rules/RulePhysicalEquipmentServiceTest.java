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
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class RulePhysicalEquipmentServiceTest {

    @InjectMocks
    RulePhysicalEquipmentService ruleService;
    @Mock
    DigitalServiceReferentialService digitalServiceRefService;

    @Mock
    MessageSource messageSource;
    private final Locale locale = Locale.getDefault();
    private final String filename = "filename";
    private final int line = 1;

        @Test
        void testCheckElectricityConsumption_NullValue_ReturnsError() {
            
            Mockito.when(messageSource.getMessage(any(), any(), any())).thenReturn("Field 'consoElecAn' is mandatory.");
            var actual = ruleService.checkElectricityConsumption(locale,filename, line, null);
            Assertions.assertTrue(actual.isPresent());
            Assertions.assertEquals(new LineError(filename,line, "Field 'consoElecAn' is mandatory."), actual.get());

        }

        @Test
        void testCheckElectricityConsumption_ValidValue_ReturnsEmpty() {
            var actual = ruleService.checkElectricityConsumption(locale, "file.csv", line, 100.0);
            assertTrue(actual.isEmpty());
        }

        @Test
        void testCheckDurationHour_Null_ReturnsBlankError() {
            Mockito.when(messageSource.getMessage(any(), any(), any())).thenReturn("Field 'dureeUtilisation' is mandatory.");
            var actual = ruleService.checkDurationHour(locale, filename, line, null);
            assertTrue(actual.isPresent());
            Assertions.assertEquals(new LineError(filename,line, "Field 'dureeUtilisation' is mandatory."), actual.get());
        }

        @Test
        void testCheckDurationHour_Negative_ReturnsBlankError() {
            Mockito.when(messageSource.getMessage(any(), any(), any())).thenReturn("Field 'dureeUtilisation' is mandatory.");
            var actual = ruleService.checkDurationHour(locale, filename, line, -5.0);
            assertTrue(actual.isPresent());
            Assertions.assertEquals(new LineError(filename,line, "Field 'dureeUtilisation' is mandatory."), actual.get());
        }

        @Test
        void testCheckDurationHour_TooHigh_ReturnsInvalidError() {
            Mockito.when(messageSource.getMessage(eq("durationHour.invalid"), any(),any())).thenReturn("dureeUtilisation should not be more than 8760");
            var actual = ruleService.checkDurationHour(locale, filename, line, 9999.0);
            assertTrue(actual.isPresent());
            Assertions.assertEquals(new LineError(filename,line, "dureeUtilisation should not be more than 8760"), actual.get());
        }

        @Test
        void testCheckDurationHour_Valid_ReturnsEmpty() {
            assertTrue(ruleService.checkDurationHour(locale, filename, line, 100.0).isEmpty());
        }

        @Test
        void testCheckDigitalServiceModel_ModelNotFound_ReturnsError() {
            Mockito.when(digitalServiceRefService.getServerHosts()).thenReturn(Collections.emptyList());
            Mockito.when(messageSource.getMessage(eq("referential.model.not.exist"), any(), eq(locale))).thenReturn
                    ("Model MODEL_X does not exist in referential. Check your reference or ask your administrator to update the referential according to your needs.");

            InPhysicalEquipmentRest equipment = InPhysicalEquipmentRest.builder().build();
            var actual = ruleService.checkDigitalServiceModel(locale, filename, line, "MODEL_X", equipment);

            assertTrue(actual.isPresent());
            Assertions.assertEquals(new LineError(filename,line,
                    "Model MODEL_X does not exist in referential. Check your reference or ask your administrator to update the referential according to your needs."), actual.get());
        }

        @Test
        void testCheckDigitalServiceModel_Storage_NoDiskSize_ReturnsError() {
            ServerHostBO host = ServerHostBO.builder().build();
            host.setReference("STORAGE_X");
            host.setType("Storage");
            Mockito.when(digitalServiceRefService.getServerHosts()).thenReturn(List.of(host));
            Mockito.when(messageSource.getMessage(eq("disk.size.blank"), any(),any())).thenReturn("Field 'tailleDuDisque' is mandatory if model is associated to a storage server.");

            InPhysicalEquipmentRest equipment = mock(InPhysicalEquipmentRest.class);
            Mockito.when(equipment.getSizeDiskGb()).thenReturn(null);

            var actual = ruleService.checkDigitalServiceModel(locale, filename, line, "STORAGE_X", equipment);
            assertTrue(actual.isPresent());
            Assertions.assertEquals(new LineError(filename,line, "Field 'tailleDuDisque' is mandatory if model is associated to a storage server."), actual.get());
        }

        @Test
        void testCheckDigitalServiceModel_Compute_NoCpuCoreNumber_ReturnsError() {
            ServerHostBO host = new ServerHostBO();
            host.setReference("COMPUTE_X");
            host.setType("Compute");
            Mockito.when(digitalServiceRefService.getServerHosts()).thenReturn(List.of(host));
            Mockito.when(messageSource.getMessage(eq("cpu.number.blank"), any(), eq(locale))).thenReturn("Field 'nbCoeur' is mandatory if  model is associated to a compute server.");

            InPhysicalEquipmentRest equipment = mock(InPhysicalEquipmentRest.class);
            Mockito.when(equipment.getCpuCoreNumber()).thenReturn(null);

            var actual = ruleService.checkDigitalServiceModel(locale, filename, line, "COMPUTE_X", equipment);
            assertTrue(actual.isPresent());
            Assertions.assertEquals(new LineError(filename,line, "Field 'nbCoeur' is mandatory if  model is associated to a compute server."), actual.get());
        }

        @Test
        void testCheckDigitalServiceModel_MatchingHostAndFieldsValid_ReturnsEmpty() {
            ServerHostBO host = new ServerHostBO();
            host.setReference("STORAGE_Y");
            host.setType("Storage");
            Mockito.when(digitalServiceRefService.getServerHosts()).thenReturn(List.of(host));

            InPhysicalEquipmentRest equipment = mock(InPhysicalEquipmentRest.class);
            Mockito.when(equipment.getSizeDiskGb()).thenReturn(100D);

            var actual = ruleService.checkDigitalServiceModel(locale, filename, line, "STORAGE_Y", equipment);
            assertTrue(actual.isEmpty());
        }
    }
