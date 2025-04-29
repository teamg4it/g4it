/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules;

import com.soprasteria.g4it.backend.common.model.LineError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.time.LocalDate;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RuleApplicationServiceTest {
    @InjectMocks
    RuleApplicationService service;

    @Mock
    MessageSource messageSource;
    private final Locale locale = Locale.getDefault();
    private final String filename = "filename";
    private final int line = 1;

    @Test
    void testVirtualEquipmentLinkedOk() {
        Assertions.assertTrue(service.checkVirtualEquipmentLinked(locale, filename, line, "virtualEquipmentName").isEmpty());
    }
    @Test
    void testNullVirtualEquipmentLinkedError() {
        when(messageSource.getMessage(any(), any(), eq(locale)))
                .thenReturn("Application must have a virtual equipment name");
       var actual = service.checkVirtualEquipmentLinked(locale, filename, line, null);
        Assertions.assertTrue(actual.isPresent());

        Assertions.assertEquals(new LineError(filename,line, "Application must have a virtual equipment name"), actual.get());

    }
}
