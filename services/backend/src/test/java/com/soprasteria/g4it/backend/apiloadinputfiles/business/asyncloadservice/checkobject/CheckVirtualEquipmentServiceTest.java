package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkobject;

import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules.GenericRuleService;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules.RuleVirtualEquipmentService;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.common.utils.InfrastructureType;
import com.soprasteria.g4it.backend.server.gen.api.dto.InVirtualEquipmentRest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckVirtualEquipmentServiceTest {

    @InjectMocks
    private CheckVirtualEquipmentService service;

    @Mock
    private GenericRuleService genericRuleService;

    @Mock
    private RuleVirtualEquipmentService ruleVirtualEquipmentService;

    private static final String FILENAME = "file.csv";
    private static final int LINE = 1;

    private InVirtualEquipmentRest mockEqp() {
        InVirtualEquipmentRest eq = new InVirtualEquipmentRest();
        eq.setName("VM1");
        eq.setInfrastructureType(InfrastructureType.CLOUD_SERVICES.name());
        eq.setDurationHour(100.0);
        eq.setQuantity(1.0);
        eq.setProvider("AWS");
        eq.setInstanceType("m5.large");
        eq.setLocation("France");
        eq.setWorkload(50.0);
        return eq;
    }

    // ---------------------------------------------------------------------------------------------------------
    // 1) Happy path — Cloud service and ALL rules return empty → NO errors
    // ---------------------------------------------------------------------------------------------------------
    @Test
    void testCheckRules_Cloud_NoErrors() {
        InVirtualEquipmentRest eq = mockEqp();
        Context ctx = Context.builder().locale(Locale.getDefault()).digitalServiceVersionUid(null).build();

        when(ruleVirtualEquipmentService.checkInfrastructureType(any(), any(), anyInt(), anyString()))
                .thenReturn(Optional.empty());
        when(ruleVirtualEquipmentService.checkVirtualEquipmentName(any(), any(), anyInt(), anyString(), anySet(), eq(true), eq(false)))
                .thenReturn(Optional.empty());
        when(ruleVirtualEquipmentService.checkUsageDuration(any(), any(), anyInt(), any()))
                .thenReturn(Optional.empty());
        when(ruleVirtualEquipmentService.checkCloudQuantity(any(), any(), anyInt(), any()))
                .thenReturn(Optional.empty());
        when(ruleVirtualEquipmentService.checkCloudProvider(any(), any(), anyInt(), anyString()))
                .thenReturn(Optional.empty());
        when(ruleVirtualEquipmentService.checkCloudInstanceType(any(), any(), anyInt(), anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(ruleVirtualEquipmentService.checkCloudLocation(any(), any(), anyInt(), anyString()))
                .thenReturn(Optional.empty());
        when(ruleVirtualEquipmentService.checkCloudWorkload(any(), any(), anyInt(), any()))
                .thenReturn(Optional.empty());

        List<LineError> result = service.checkRules(ctx, eq, FILENAME, LINE, new HashSet<>());
        assertTrue(result.isEmpty());
    }

    // ---------------------------------------------------------------------------------------------------------
    // 2) Cloud service — ALL validations return error → All errors should be collected
    // ---------------------------------------------------------------------------------------------------------
    @Test
    void testCheckRules_Cloud_AllErrorsCollected() {
        InVirtualEquipmentRest eq = mockEqp();
        Context ctx = Context.builder().locale(Locale.getDefault()).digitalServiceVersionUid(null).build();

        LineError err = new LineError(FILENAME, LINE, "error");
        when(ruleVirtualEquipmentService.checkInfrastructureType(any(), any(), anyInt(), anyString())).thenReturn(Optional.of(err));
        when(ruleVirtualEquipmentService.checkVirtualEquipmentName(any(), any(), anyInt(), anyString(), anySet(), eq(true), eq(false))).thenReturn(Optional.of(err));
        when(ruleVirtualEquipmentService.checkUsageDuration(any(), any(), anyInt(), any())).thenReturn(Optional.of(err));
        when(ruleVirtualEquipmentService.checkCloudQuantity(any(), any(), anyInt(), any())).thenReturn(Optional.of(err));
        when(ruleVirtualEquipmentService.checkCloudProvider(any(), any(), anyInt(), anyString())).thenReturn(Optional.of(err));
        when(ruleVirtualEquipmentService.checkCloudInstanceType(any(), any(), anyInt(), anyString(), anyString())).thenReturn(Optional.of(err));
        when(ruleVirtualEquipmentService.checkCloudLocation(any(), any(), anyInt(), anyString())).thenReturn(Optional.of(err));
        when(ruleVirtualEquipmentService.checkCloudWorkload(any(), any(), anyInt(), any())).thenReturn(Optional.of(err));

        List<LineError> result = service.checkRules(ctx, eq, FILENAME, LINE, new HashSet<>());
        assertEquals(8, result.size());
    }

    // ---------------------------------------------------------------------------------------------------------
    // 3) NON-Cloud equipment — executes physical rule instead of cloud rules
    // ---------------------------------------------------------------------------------------------------------
    @Test
    void testCheckRules_NonCloud() {

        // Arrange
        InVirtualEquipmentRest eq = mockEqp();
        eq.setInfrastructureType(InfrastructureType.NON_CLOUD_SERVERS.name());
        eq.setPhysicalEquipmentName(null);          // Non-cloud → physical equipment expected but NULL
        eq.setType(null);
        eq.setSizeDiskGb(null);
        eq.setVcpuCoreNumber(null);
        Context ctx = Context.builder()
                .locale(Locale.getDefault())
                .digitalServiceVersionUid(null)     // Not a digital service
                .build();

        when(ruleVirtualEquipmentService.checkInfrastructureType(any(), anyString(), anyInt(), anyString()))
                .thenReturn(Optional.empty());

        when(ruleVirtualEquipmentService.checkVirtualEquipmentName(any(), anyString(), anyInt(),
                anyString(), anySet(), eq(false), eq(false)))
                .thenReturn(Optional.empty());

        when(ruleVirtualEquipmentService.checkPhysicalEquipmentLinked(any(), anyString(), anyInt(), anyString(), any()))
                .thenReturn(Optional.empty());      // Accept null correctly

        when(ruleVirtualEquipmentService.checkType(any(), anyString(), anyInt(), any(),
                any(), any(), eq(false)))
                .thenReturn(Optional.empty());

        // Act
        List<LineError> result = service.checkRules(ctx, eq, FILENAME, LINE, new HashSet<>());

        // Assert
        assertTrue(result.isEmpty());

        verify(ruleVirtualEquipmentService).checkInfrastructureType(any(), eq(FILENAME), eq(LINE), eq(InfrastructureType.NON_CLOUD_SERVERS.name()));
        verify(ruleVirtualEquipmentService).checkVirtualEquipmentName(any(), eq(FILENAME), eq(LINE), eq(eq.getName()), anySet(), eq(false), eq(false));
        verify(ruleVirtualEquipmentService).checkPhysicalEquipmentLinked(any(), eq(FILENAME), eq(LINE), eq(InfrastructureType.NON_CLOUD_SERVERS.name()), any());
        verify(ruleVirtualEquipmentService).checkType(any(), eq(FILENAME), eq(LINE), any(), any(), any(), eq(false));
        verify(ruleVirtualEquipmentService, never()).checkCloudProvider(any(), any(), anyInt(), any());
    }


    // ---------------------------------------------------------------------------------------------------------
    // 4) Adds name to Set only when rule returns empty
    // ---------------------------------------------------------------------------------------------------------
    @Test
    void testCheckRules_NameAddedToSet() {
        InVirtualEquipmentRest eq = mockEqp();
        Context ctx = Context.builder().locale(Locale.getDefault()).build();
        Set<String> names = new HashSet<>();

        when(ruleVirtualEquipmentService.checkInfrastructureType(any(), any(), anyInt(), anyString())).thenReturn(Optional.empty());
        when(ruleVirtualEquipmentService.checkVirtualEquipmentName(any(), any(), anyInt(), eq("VM1"), anySet(), eq(true), eq(false)))
                .then(inv -> {
                    names.add("VM1");
                    return Optional.empty();
                });

        when(ruleVirtualEquipmentService.checkUsageDuration(any(), any(), anyInt(), any())).thenReturn(Optional.empty());
        when(ruleVirtualEquipmentService.checkCloudQuantity(any(), any(), anyInt(), any())).thenReturn(Optional.empty());
        when(ruleVirtualEquipmentService.checkCloudProvider(any(), any(), anyInt(), any())).thenReturn(Optional.empty());
        when(ruleVirtualEquipmentService.checkCloudInstanceType(any(), any(), anyInt(), any(), any())).thenReturn(Optional.empty());
        when(ruleVirtualEquipmentService.checkCloudLocation(any(), any(), anyInt(), any())).thenReturn(Optional.empty());
        when(ruleVirtualEquipmentService.checkCloudWorkload(any(), any(), anyInt(), any())).thenReturn(Optional.empty());

        service.checkRules(ctx, eq, FILENAME, LINE, names);

        assertTrue(names.contains("VM1"));
    }
}